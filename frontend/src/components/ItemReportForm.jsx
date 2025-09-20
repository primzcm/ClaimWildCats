import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import './ItemReportForm.css';

const INITIAL_FORM = {
  title: '',
  category: '',
  description: '',
  location: '',
  when: '',
  color: '',
  brand: '',
  distinguishingMarks: '',
  rewardOffered: false,
  contactPreference: '',
  photoUrls: '',
  custody: 'WITH_FINDER',
  serialNumber: '',
};

export function ItemReportForm({ mode }) {
  const navigate = useNavigate();
  const isLost = mode === 'lost';
  const [form, setForm] = useState(INITIAL_FORM);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const resetForm = () => {
    setForm(INITIAL_FORM);
    setError('');
    setSuccess('');
  };

  const buildPayload = () => {
    const photos = form.photoUrls
      .split(/\r?\n|,/)
      .map((entry) => entry.trim())
      .filter(Boolean);

    const description = form.distinguishingMarks
      ? `${form.description}\n\nDistinguishing marks: ${form.distinguishingMarks}`.trim()
      : form.description;

    const base = {
      title: form.title,
      category: form.category,
      location: form.location,
      description,
      color: form.color || null,
      brand: form.brand || null,
      contactPreference: form.contactPreference || null,
      photoUrls: photos,
    };

    if (isLost) {
      return {
        ...base,
        lastSeenAt: form.when ? new Date(form.when).toISOString() : null,
        rewardOffered: form.rewardOffered,
      };
    }

    return {
      ...base,
      foundAt: form.when ? new Date(form.when).toISOString() : null,
      custody: form.custody,
      serialNumber: form.serialNumber || null,
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

        <div className="report-form__row report-form__row--split">
          <label>
            Category
            <input
              required
              name="category"
              value={form.category}
              onChange={handleChange}
              placeholder="Bags, Electronics, ID Card"
            />
          </label>
          <label>
            Location
            <input
              required
              name="location"
              value={form.location}
              onChange={handleChange}
              placeholder="Building / area"
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
            Colour
            <input name="color" value={form.color} onChange={handleChange} placeholder="Optional" />
          </label>
          <label>
            Brand
            <input name="brand" value={form.brand} onChange={handleChange} placeholder="Optional" />
          </label>
        </div>

        <div className="report-form__row">
          <label>
            Distinguishing marks
            <textarea
              name="distinguishingMarks"
              value={form.distinguishingMarks}
              onChange={handleChange}
              placeholder="Scratches, stickers, engravings"
            />
          </label>
        </div>

        <div className="report-form__row report-form__row--split">
          <label>
            {isLost ? 'Last seen' : 'Found'} (date & time)
            <input
              type="datetime-local"
              required
              name="when"
              value={form.when}
              onChange={handleChange}
            />
          </label>
          {isLost ? (
            <label className="report-form__checkbox">
              <span>
                <strong>Reward offered?</strong>
                <span className="report-form__hint">Tick if you plan to reward whoever returns the item.</span>
              </span>
              <input
                type="checkbox"
                name="rewardOffered"
                checked={form.rewardOffered}
                onChange={handleChange}
              />
            </label>
          ) : (
            <label>
              Custody status
              <select name="custody" value={form.custody} onChange={handleChange}>
                <option value="WITH_FINDER">With me</option>
                <option value="TURNED_IN_OFFICE">Turned in at office</option>
              </select>
            </label>
          )}
        </div>

        {!isLost && (
          <div className="report-form__row">
            <label>
              Serial / ID number
              <input
                name="serialNumber"
                value={form.serialNumber}
                onChange={handleChange}
                placeholder="Optional"
              />
            </label>
          </div>
        )}

        <div className="report-form__row">
          <label>
            Contact preference
            <input
              name="contactPreference"
              value={form.contactPreference}
              onChange={handleChange}
              placeholder="Email, phone, pickup instructions"
            />
          </label>
        </div>

        <div className="report-form__row">
          <label>
            Photo URLs
            <textarea
              name="photoUrls"
              value={form.photoUrls}
              onChange={handleChange}
              placeholder="Paste image URLs, one per line or separated by commas"
            />
            <span className="report-form__hint">Uploads coming soon - link to shared drives or cloud folders for now.</span>
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
