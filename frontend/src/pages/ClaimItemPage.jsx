import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { PageLayout } from '../components/PageLayout';
import { api } from '../api/client';
import { cleanupUploads, formatFileSize, generateAttachmentId, uploadFiles } from '../lib/uploads';
import { useAuth } from '../context/AuthContext';
import './ClaimItemPage.css';

const ATTACHMENT_LIMIT = 4;

export function ClaimItemPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [existingClaim, setExistingClaim] = useState(null);
  const [form, setForm] = useState({ secretDetail: '', justification: '' });
  const [attachments, setAttachments] = useState([]);
  const [attachmentMessage, setAttachmentMessage] = useState('');
  const [draftFolder, setDraftFolder] = useState(() => generateAttachmentId());
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState('');

  useEffect(() => {
    let ignore = false;
    const loadItem = async () => {
      setLoading(true);
      setError('');
      try {
        const detail = await api(`/api/items/${id}`);
        if (!ignore) {
          setItem(detail);
        }
      } catch (err) {
        if (!ignore) {
          setError(err?.message ?? 'Unable to load the item right now.');
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };
    loadItem();
    return () => {
      ignore = true;
    };
  }, [id]);

  useEffect(() => {
    if (!user?.uid) {
      setExistingClaim(null);
      return;
    }
    let cancelled = false;
    const loadClaims = async () => {
      try {
        const claims = await api(`/api/users/${user.uid}/claims`);
        if (cancelled) return;
        const match = claims.find((claim) => claim.itemId === id && claim.status === 'PENDING');
        setExistingClaim(match ?? null);
      } catch {
        if (!cancelled) {
          setExistingClaim(null);
        }
      }
    };
    loadClaims();
    return () => {
      cancelled = true;
    };
  }, [user?.uid, id]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (event) => {
    const selected = Array.from(event.target.files ?? []);
    if (selected.length === 0) {
      return;
    }

    let message = '';
    const remainingSlots = ATTACHMENT_LIMIT - attachments.length;
    if (remainingSlots <= 0) {
      setAttachmentMessage(`You can upload up to ${ATTACHMENT_LIMIT} images.`);
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

  const handleRemoveAttachment = (attachmentId) => {
    setAttachments((prev) => prev.filter((entry) => entry.id !== attachmentId));
  };

  const resetForm = () => {
    setForm({ secretDetail: '', justification: '' });
    setAttachments([]);
    setAttachmentMessage('');
    setDraftFolder(generateAttachmentId());
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (submitting) return;
    if (!user?.uid) {
      setError('Sign in to submit a claim.');
      return;
    }
    if (!item) {
      setError('Item not found.');
      return;
    }
    if (item.status?.toLowerCase() !== 'found') {
      setError('Only found reports can be claimed.');
      return;
    }
    if (item.reporterId && item.reporterId === user.uid) {
      setError('You reported this item. Manage it from your reports dashboard.');
      return;
    }

    const secretDetail = form.secretDetail.trim();
    const justification = form.justification.trim();
    if (!secretDetail || !justification) {
      setError('Provide the secret detail and a short justification.');
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');
    let uploadedEntries = [];
    try {
      uploadedEntries = await uploadFiles(`claims/${id}/${draftFolder}`, attachments);
      const payload = {
        secretDetail,
        justification,
        attachmentUrls: uploadedEntries.map((entry) => entry.storageUri),
      };
      await api(`/api/items/${id}/claims`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      setSuccess('Claim submitted! The finder will contact you if it matches.');
      setTimeout(() => {
        resetForm();
        navigate(`/items/${id}`);
      }, 1200);
    } catch (err) {
      await cleanupUploads(uploadedEntries);
      setError(err?.message ?? 'Unable to submit your claim right now.');
    } finally {
      setSubmitting(false);
    }
  };

  const itemStatus = item?.status?.toLowerCase();

  const claimDisabledReason = (() => {
    if (!user) return 'Sign in to claim this item.';
    if (existingClaim) return 'You already have a pending claim for this item.';
    if (item && item.reporterId && item.reporterId === user.uid) return 'You reported this item.';
    if (item && itemStatus !== 'found') return 'Only found items can be claimed.';
    return '';
  })();

  if (loading) {
    return (
      <PageLayout title="Claim Item" description="Loading item details...">
        <p>Loading...</p>
      </PageLayout>
    );
  }

  if (error && !item) {
    return (
      <PageLayout title="Claim Item" description="We could not load the requested item.">
        <p className="claim-status claim-status--error">{error}</p>
      </PageLayout>
    );
  }

  const disabled = submitting || Boolean(claimDisabledReason);

  return (
    <PageLayout
      title="Claim Found Item"
      description="Share proof that this found item belongs to you. Details go directly to the finder."
    >
      {item ? (
        <section className="claim-item__summary">
          <h2>{item.title}</h2>
          <p>{item.description}</p>
          <dl>
            <div>
              <dt>Status</dt>
              <dd>{item.status}</dd>
            </div>
            <div>
              <dt>Location</dt>
              <dd>{item.locationText}</dd>
            </div>
            <div>
              <dt>Reporter</dt>
              <dd>{item.reporterUsername ? item.reporterUsername : 'Unknown'}</dd>
            </div>
          </dl>
        </section>
      ) : null}

      {claimDisabledReason ? (
        <p className="claim-status claim-status--warning">{claimDisabledReason}</p>
      ) : null}
      {existingClaim ? (
        <p className="claim-status claim-status--info">
          Pending claim submitted on {new Date(existingClaim.submittedAt).toLocaleString('en-PH')}.
        </p>
      ) : null}

      <form className="claim-form" onSubmit={handleSubmit}>
        <label>
          Secret detail only you would know
          <textarea
            name="secretDetail"
            value={form.secretDetail}
            onChange={handleChange}
            placeholder="Describe a unique engraving, scratch, or attachment."
            required
            disabled={disabled}
          />
        </label>

        <label>
          Justification / Context
          <textarea
            name="justification"
            value={form.justification}
            onChange={handleChange}
            placeholder="Explain when and where you lost it so the finder can verify."
            required
            disabled={disabled}
          />
        </label>

        <div className="claim-form__files">
          <label htmlFor="claim-files">Evidence photos (optional)</label>
          <input
            id="claim-files"
            type="file"
            accept="image/*"
            multiple
            onChange={handleFileChange}
            disabled={disabled || attachments.length >= ATTACHMENT_LIMIT}
          />
          <span className="claim-form__hint">
            Attach up to {ATTACHMENT_LIMIT} recent photos to help the finder confirm ownership.
          </span>
          {attachmentMessage ? (
            <span className="claim-form__note">{attachmentMessage}</span>
          ) : null}
          {attachments.length > 0 ? (
            <ul className="claim-form__file-list">
              {attachments.map((entry) => (
                <li key={entry.id} className="claim-form__file-item">
                  <span>
                    {entry.file.name}
                    <span className="claim-form__file-size">{formatFileSize(entry.file.size)}</span>
                  </span>
                  <button
                    type="button"
                    className="claim-form__remove"
                    onClick={() => handleRemoveAttachment(entry.id)}
                    disabled={disabled}
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          ) : null}
        </div>

        {error && <p className="claim-status claim-status--error">{error}</p>}
        {success && <p className="claim-status claim-status--success">{success}</p>}

        <div className="claim-form__actions">
          <button type="submit" className="claim-form__submit" disabled={disabled}>
            {submitting ? 'Submitting...' : 'Submit claim'}
          </button>
          <button type="button" className="claim-form__secondary" onClick={resetForm} disabled={submitting}>
            Clear form
          </button>
        </div>
      </form>
    </PageLayout>
  );
}
