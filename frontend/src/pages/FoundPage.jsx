import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { ItemGallery } from '../components/ItemGallery';
import './FoundPage.css';

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest first' },
  { value: 'oldest', label: 'Oldest first' },
  { value: 'name', label: 'Name A-Z' }
];

export function FoundPage() {
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
        params.set('status', 'found');
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
          setError(err?.message ?? 'Unable to load found items right now.');
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
    <div className="found-page">
      <header className="found-page__header">
        <h1>Found Items</h1>
        <Link to="/items/new/found" className="found-page__report">
          <span className="found-page__report-icon" aria-hidden="true">+</span>
          <span>Report</span>
        </Link>
      </header>

      <section className="found-page__controls" aria-label="Search found items">
        <form className="found-search" onSubmit={handleSubmit}>
          <label htmlFor="found-search-input" className="found-search__label">
            <span aria-hidden="true">#</span>
            <span className="sr-only">Item name</span>
          </label>
          <input
            id="found-search-input"
            type="search"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Item name"
          />
          <button type="submit" className="found-search__submit" aria-label="Search">
            Search
          </button>
        </form>

        <div className="found-sort">
          <label htmlFor="found-sort-select">Sort</label>
          <select
            id="found-sort-select"
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
        <p className="found-status">Loading found reports...</p>
      ) : error ? (
        <p className="found-status found-status--error">{error}</p>
      ) : sortedItems.length === 0 ? (
        <p className="found-status">No found items reported yet. Try another search or file a report.</p>
      ) : (
        <ItemGallery items={sortedItems} />
      )}
    </div>
  );
}
