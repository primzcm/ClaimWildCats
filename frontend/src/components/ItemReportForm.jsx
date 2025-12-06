import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { CAMPUS_ZONES } from '../constants/campusZones';
import { cleanupUploads, formatFileSize, generateAttachmentId, uploadFiles } from '../lib/uploads';
import './ItemReportForm.css';

const INITIAL_FORM = {
  title: '',
  description: '',
  locationText: '',
  campusZone: '',
  lastSeenAt: '',
  tags: '',
};

const PH_TIME_SUFFIX = '+08:00';
const ATTACHMENT_LIMIT = 5;

function toPhilippinesIso(value) {
  if (!value) return null;
  const normalised = value.includes('T') ? `${value}:00${PH_TIME_SUFFIX}` : value;
  const date = new Date(normalised);
  if (Number.isNaN(date.getTime())) {
    return null;
  }
  return date.toISOString();
}

function splitList(value) {
  return value
    .split(/\r?\n|,/)
    .map((entry) => entry.trim())
    .filter(Boolean);
}

function toLocalDateTimeInput(value) {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function buildFormFromItem(item) {
  if (!item) {
    return INITIAL_FORM;
  }
  return {
    title: item.title ?? '',
    description: item.description ?? '',
    locationText: item.locationText ?? '',
    campusZone: item.campusZone ?? '',
    lastSeenAt: toLocalDateTimeInput(item.lastSeenAt),
    tags: Array.isArray(item.tags) && item.tags.length > 0 ? item.tags.join(', ') : '',
  };
}

export function ItemReportForm({
  mode,
  variant = 'create',
  itemId,
  initialItem,
  onSaved,
  onCancel,
}) {
  const navigate = useNavigate();
  const isLost = mode === 'lost';
  const isEdit = variant === 'edit';
  const [form, setForm] = useState(() => (isEdit && initialItem ? buildFormFromItem(initialItem) : INITIAL_FORM));
  const [draftItemId, setDraftItemId] = useState(() => generateAttachmentId());
  const [attachments, setAttachments] = useState([]);
  const [existingDocUrls, setExistingDocUrls] = useState(
    () => (isEdit && initialItem && Array.isArray(initialItem.docUrls) ? initialItem.docUrls : []),
  );
  const [attachmentMessage, setAttachmentMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!isEdit || !initialItem) {
      return;
    }
    setForm(buildFormFromItem(initialItem));
    if (Array.isArray(initialItem.docUrls)) {
      setExistingDocUrls(initialItem.docUrls);
    } else {
      setExistingDocUrls([]);
    }
  }, [isEdit, initialItem]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleFileChange = (event) => {
    const selected = Array.from(event.target.files ?? []);
    if (selected.length === 0) {
      return;
    }

    let message = '';
    const alreadyAttachedCount = isEdit ? existingDocUrls.length + attachments.length : attachments.length;
    const remainingSlots = ATTACHMENT_LIMIT - alreadyAttachedCount;
    if (remainingSlots <= 0) {
      setAttachmentMessage(`You can upload up to ${ATTACHMENT_LIMIT} images per report.`);
      event.target.value = '';
      return;
    }

    const imagesOnly = selected.filter((file) => file.type && file.type.startsWith('image/'));
    if (imagesOnly.length !== selected.length) {
      message = 'Only image files are supported. Non-image files were ignored.';
    }

    const usable = imagesOnly.slice(0, remainingSlots).map((file) => ({
      id: generateAttachmentId(),
      file,
    }));

    if (usable.length === 0) {
      setAttachmentMessage(message || 'Select image files to attach.');
      event.target.value = '';
      return;
    }

    if (imagesOnly.length > usable.length || selected.length > imagesOnly.length) {
      message = 'Some files were skipped due to format or upload limit.';
    }

    setAttachments((prev) => [...prev, ...usable]);
    setAttachmentMessage(message);
    event.target.value = '';
  };

  const handleRemoveAttachment = (id) => {
    setAttachments((prev) => prev.filter((item) => item.id !== id));
  };

  const handleRemoveExistingDocUrl = (url) => {
    setExistingDocUrls((prev) => prev.filter((entry) => entry !== url));
  };

  const resetForm = () => {
    setForm(INITIAL_FORM);
    setAttachments([]);
    setAttachmentMessage('');
    setDraftItemId(generateAttachmentId());
    setError('');
    setSuccess('');
  };

  const buildPayload = (docUrls) => {
    const tags = splitList(form.tags).map((tag) => tag.toLowerCase());
    return {
      title: form.title.trim(),
      description: form.description.trim(),
      locationText: form.locationText.trim(),
      campusZone: form.campusZone || null,
      lastSeenAt: toPhilippinesIso(form.lastSeenAt),
      tags,
      docUrls,
    };
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (loading) return;
    setLoading(true);
    setError('');
    setSuccess('');

    if (isEdit) {
      let uploadedEntries = [];
      try {
        if (attachments.length > 0) {
          uploadedEntries = await uploadFiles(`items/${itemId}`, attachments);
        }
        const newDocUrls = uploadedEntries.map((entry) => entry.storageUri);
        const finalDocUrls = [...existingDocUrls, ...newDocUrls];
        const payload = buildPayload(finalDocUrls);
        const updated = await api(`/api/items/${itemId}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });
        setSuccess('Changes saved.');
        setAttachments([]);
        setAttachmentMessage('');
        if (typeof onSaved === 'function') {
          onSaved(updated);
        }
      } catch (err) {
        if (uploadedEntries.length > 0) {
          await cleanupUploads(uploadedEntries);
        }
        setError(err?.message ?? 'Unable to update the report right now.');
      } finally {
        setLoading(false);
      }
      return;
    }

    let uploadedEntries = [];
    try {
      uploadedEntries = await uploadFiles(`items/${draftItemId}`, attachments);
      const payload = buildPayload(uploadedEntries.map((entry) => entry.storageUri));
      const endpoint = isLost ? '/api/items/lost' : '/api/items/found';
      const created = await api(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      setSuccess('Report submitted! Redirecting to the item page...');
      setTimeout(() => {
        resetForm();
        if (created?.id) {
          navigate(`/items/${created.id}`);
        } else {
          navigate('/me/reports');
        }
      }, 800);
    } catch (err) {
      await cleanupUploads(uploadedEntries);
      if (err && err.code === 'storage/unauthorized') {
        setError('We could not upload your images because storage access was denied. Try signing in again.');
      } else {
        setError(err?.message ?? 'Unable to submit the report right now.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="report-form" onSubmit={handleSubmit}>
      <div className="report-form__grid">
        <div className="report-form__row">
          <label>
            Title
            <input
              required
              name="title"
              value={form.title}
              onChange={handleChange}
              placeholder="E.g. Blue North Face backpack"
            />
          </label>
        </div>

        <div className="report-form__row">
          <label>
            Description
            <textarea
              required
              name="description"
              value={form.description}
              onChange={handleChange}
              placeholder="Add details that help others recognise the item."
            />
          </label>
        </div>

        <div className="report-form__row report-form__row--split">
          <label>
            Location details
            <input
              required
              name="locationText"
              value={form.locationText}
              onChange={handleChange}
              placeholder="CIT-U Main Lobby, near guard"
            />
          </label>
          <label>
            Campus zone
            <select name="campusZone" value={form.campusZone} onChange={handleChange}>
              <option value="">Select zone (optional)</option>
              {CAMPUS_ZONES.map((zone) => (
                <option key={zone} value={zone}>
                  {zone}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="report-form__row">
          <label>
            {isLost ? 'Last seen' : 'Last seen / found'} (Philippines time)
            <input
              type="datetime-local"
              required
              name="lastSeenAt"
              value={form.lastSeenAt}
              onChange={handleChange}
            />
          </label>
        </div>

      <div className="report-form__row report-form__row--split">
          <label>
            Tags
            <input
              name="tags"
              value={form.tags}
              onChange={handleChange}
              placeholder="Comma-separated keywords (optional)"
            />
          </label>
          <div className="report-form__files">
            {isEdit && existingDocUrls.length > 0 ? (
              <ul className="report-form__file-list">
                {existingDocUrls.map((url) => (
                  <li key={url} className="report-form__file-item report-form__file-item--existing">
                    <span>
                      {url.split('/').slice(-1)[0].split('?')[0] || 'Existing image'}
                      <span className="report-form__file-size">Existing attachment</span>
                    </span>
                    <button
                      type="button"
                      className="report-form__remove-file"
                      onClick={() => handleRemoveExistingDocUrl(url)}
                      disabled={loading}
                    >
                      Remove
                    </button>
                  </li>
                ))}
              </ul>
            ) : null}
            <label htmlFor="report-files">Upload images</label>
            <input
              id="report-files"
              type="file"
              accept="image/*"
              multiple
              onChange={handleFileChange}
              disabled={
                loading
                || (isEdit ? existingDocUrls.length + attachments.length >= ATTACHMENT_LIMIT : attachments.length >= ATTACHMENT_LIMIT)
              }
            />
            <span className="report-form__hint">
              You can attach up to {ATTACHMENT_LIMIT} images per report.
            </span>
            {attachmentMessage ? (
              <span className="report-form__note">{attachmentMessage}</span>
            ) : null}
            {attachments.length > 0 ? (
              <ul className="report-form__file-list">
                {attachments.map((item) => (
                  <li key={item.id} className="report-form__file-item">
                    <span>
                      {item.file.name}
                      <span className="report-form__file-size">{formatFileSize(item.file.size)}</span>
                    </span>
                    <button
                      type="button"
                      className="report-form__remove-file"
                      onClick={() => handleRemoveAttachment(item.id)}
                      disabled={loading}
                    >
                      Remove
                    </button>
                  </li>
                ))}
              </ul>
            ) : null}
          </div>
        </div>
      </div>

      {error && <div className="report-form__status report-form__status--error">{error}</div>}
      {success && <div className="report-form__status report-form__status--success">{success}</div>}

      <div className="report-form__actions">
        <button type="submit" className="report-form__submit" disabled={loading}>
          {loading
            ? isEdit
              ? 'Saving...'
              : 'Submitting...'
            : isEdit
              ? 'Save changes'
              : 'Submit report'}
        </button>
        {isEdit ? (
          <button
            type="button"
            className="report-form__secondary"
            onClick={() => {
              if (typeof onCancel === 'function') {
                onCancel();
              }
            }}
            disabled={loading}
          >
            Cancel
          </button>
        ) : (
          <button
            type="button"
            className="report-form__secondary"
            onClick={resetForm}
            disabled={loading}
          >
            Clear form
          </button>
        )}
      </div>
    </form>
  );
}
