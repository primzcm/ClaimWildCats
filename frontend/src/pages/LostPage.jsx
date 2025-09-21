import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import './LostPage.css';

const formatter = new Intl.DateTimeFormat('en-PH', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'Asia/Manila',
});

function formatDate(value) {
  if (!value) return 'Unknown time';
  try {
    return formatter.format(new Date(value));
  } catch (error) {
    return 'Unknown time';
  }
}

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest first' },
  { value: 'oldest', label: 'Oldest first' },
  { value: 'name', label: 'Name A-Z' }
];

export function LostPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [appliedQuery, setAppliedQuery] = useState('');
  const [sort, setSort] = useState('newest');

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const params = new URLSearchParams();
        params.set('status', 'lost');
        params.set('page', '0');
        params.set('pageSize', '60');
        if (appliedQuery) {
          params.set('q', appliedQuery);
        }
        const response = await api(`/api/items?${params.toString()}`);
        if (!ignore) {
          setItems(response?.items ?? []);
        }
      } catch (err) {
        if (!ignore) {
          setError(err?.message ?? 'Unable to load lost items right now.');
          setItems([]);
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };
    load();
    return () => {
      ignore = true;
    };
  }, [appliedQuery]);

  const sortedItems = useMemo(() => {
    const copy = [...items];
    if (sort === 'oldest') {
      copy.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
    } else if (sort === 'name') {
      copy.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
    } else {
      copy.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }
    return copy;
  }, [items, sort]);

  const handleSubmit = (event) => {
    event.preventDefault();
    setAppliedQuery(searchTerm.trim());
  };

  return (
    <div className="lost-page">
      <header className="lost-page__header">
        <h1>Lost Items</h1>
        <Link to="/items/new/lost" className="lost-page__report">
          <span className="lost-page__report-icon" aria-hidden="true">+</span>
          <span>Report</span>
        </Link>
      </header>

      <section className="lost-page__controls" aria-label="Search lost items">
        <form className="lost-search" onSubmit={handleSubmit}>
          <label htmlFor="lost-search-input" className="lost-search__label">
            <span aria-hidden="true">#</span>
            <span className="sr-only">Item name</span>
          </label>
          <input
            id="lost-search-input"
            type="search"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Item name"
          />
          <button type="submit" className="lost-search__submit" aria-label="Search">
            Search
          </button>
        </form>

        <div className="lost-sort">
          <label htmlFor="lost-sort-select">Sort</label>
          <select
            id="lost-sort-select"
            value={sort}
            onChange={(event) => setSort(event.target.value)}
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </section>

      {loading ? (
        <p className="lost-status">Loading lost reports...</p>
      ) : error ? (
        <p className="lost-status lost-status--error">{error}</p>
      ) : sortedItems.length === 0 ? (
        <p className="lost-status">No lost items found. Try a different search or file a report.</p>
      ) : (
        <ul className="lost-grid">
          {sortedItems.map((item) => (
            <li key={item.id} className="lost-card">
              <div className={`lost-card__badge lost-card__badge--${item.status}`}>
                {item.status}
              </div>
              <h2>
                <Link to={`/items/${item.id}`}>{item.title}</Link>
              </h2>
              <p className="lost-card__meta">
                <span>{item.locationText}</span>
                {item.campusZone ? <span>- {item.campusZone}</span> : null}
              </p>
              <p className="lost-card__time">Updated {formatDate(item.createdAt)}</p>
              {item.tags && item.tags.length > 0 ? (
                <ul className="lost-card__tags">
                  {item.tags.slice(0, 4).map((tag) => (
                    <li key={`${item.id}-${tag}`}>{tag}</li>
                  ))}
                </ul>
              ) : null}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
