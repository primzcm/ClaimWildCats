import { useState } from 'react';
import { ref, uploadBytes, deleteObject } from 'firebase/storage';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { storage } from '../lib/firebase';
import { CAMPUS_ZONES } from '../constants/campusZones';
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

function formatFileSize(bytes) {
  if (!Number.isFinite(bytes)) return '';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 102.4) / 10} KB`;
  return `${Math.round(bytes / 1024 / 102.4) / 10} MB`;
}

function buildObjectName(file, index) {
  const extensionMatch = file.name.match(/\.[^./]+$/);
  const extension = extensionMatch ? extensionMatch[0].toLowerCase() : '';
  const base = file.name.replace(/\.[^./]+$/, '')
    .toLowerCase()
    .replace(/[^a-z0-9-]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '') || 'attachment';
  return `${base}-${Date.now()}-${index}${extension}`;
}

function makeAttachmentId() {
  return crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

async function uploadAttachments(itemId, attachments) {
  if (!itemId) {
    throw new Error('Unable to determine where to store the uploads.');
  }
  if (attachments.length === 0) {
    return [];
  }

  const uploads = [];
  try {
    for (let index = 0; index < attachments.length; index += 1) {
      const { file } = attachments[index];
      const objectName = buildObjectName(file, index);
      const objectPath = `items/${itemId}/${objectName}`;
      const storageRef = ref(storage, objectPath);
      await uploadBytes(storageRef, file, { contentType: file.type });
      uploads.push({ ref: storageRef, storageUri: `gs://${storageRef.bucket}/${objectPath}` });
    }
    return uploads;
  } catch (error) {
    await cleanupUploads(uploads);
    throw error;
  }
}

async function cleanupUploads(entries) {
  if (!entries || entries.length === 0) {
    return;
  }
  await Promise.all(entries.map(({ ref: storageRef }) => deleteObject(storageRef).catch(() => {})));
}

export function ItemReportForm({ mode }) {
  const navigate = useNavigate();
  const isLost = mode === 'lost';
  const [form, setForm] = useState(INITIAL_FORM);
  const [draftItemId, setDraftItemId] = useState(() => makeAttachmentId());
  const [attachments, setAttachments] = useState([]);
  const [attachmentMessage, setAttachmentMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

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
    const remainingSlots = ATTACHMENT_LIMIT - attachments.length;
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
      id: makeAttachmentId(),
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

  const resetForm = () => {
    setForm(INITIAL_FORM);
    setAttachments([]);
    setAttachmentMessage('');
    setDraftItemId(makeAttachmentId());
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

    let uploadedEntries = [];
    try {
      uploadedEntries = await uploadAttachments(draftItemId, attachments);
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
            <label htmlFor="report-files">Upload images</label>
            <input
              id="report-files"
              type="file"
              accept="image/*"
              multiple
              onChange={handleFileChange}
              disabled={loading || attachments.length >= ATTACHMENT_LIMIT}
            />
            <span className="report-form__hint">
              Attach up to {ATTACHMENT_LIMIT} images. Files upload securely to Firebase Storage when you submit the form.
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
          {loading ? 'Submitting...' : 'Submit report'}
        </button>
        <button
          type="button"
          className="report-form__secondary"
          onClick={resetForm}
          disabled={loading}
        >
          Clear form
        </button>
      </div>
    </form>
  );
}
