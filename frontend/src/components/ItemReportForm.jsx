import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { CAMPUS_ZONES } from '../constants/campusZones';
import './ItemReportForm.css';

const INITIAL_FORM = {
  title: '',
  description: '',
  locationText: '',
  campusZone: '',
  lastSeenAt: '',
  tags: '',
  docUrls: '',
};

const PH_TIME_SUFFIX = '+08:00';

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

export function ItemReportForm({ mode }) {
  const navigate = useNavigate();
  const isLost = mode === 'lost';
  const [form, setForm] = useState(INITIAL_FORM);
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

  const resetForm = () => {
    setForm(INITIAL_FORM);
    setError('');
    setSuccess('');
  };

  const buildPayload = () => {
    const tags = splitList(form.tags).map((tag) => tag.toLowerCase());
    const docUrls = splitList(form.docUrls);
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

    try {
      const payload = buildPayload();
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
      setError(err?.message ?? 'Unable to submit the report right now.');
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
          <label>
            Document URLs
            <textarea
              name="docUrls"
              value={form.docUrls}
              onChange={handleChange}
              placeholder="Paste image links, one per line"
            />
            <span className="report-form__hint">
              Use Firebase Storage image URLs (e.g. gs://&lt;bucket&gt;/items/&lt;itemId&gt;/photo.jpg). The API rejects other locations.
            </span>
          </label>
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