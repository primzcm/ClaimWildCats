import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { PageLayout } from '../components/PageLayout';
import { api } from '../api/client';
import { CAMPUS_ZONES, ITEM_STATUSES } from '../constants/campusZones';

const PAGE_SIZE = 10;

const formatter = new Intl.DateTimeFormat('en-PH', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'Asia/Manila',
});

function formatDate(value) {
  if (!value) return 'Unknown';
  try {
    return formatter.format(new Date(value));
  } catch (error) {
    return 'Unknown';
  }
}

export function SearchPage() {
  const [formState, setFormState] = useState({ q: '', status: '', campusZone: '' });
  const [appliedFilters, setAppliedFilters] = useState({ q: '', status: '', campusZone: '' });
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ items: [], totalItems: 0, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const params = new URLSearchParams();
        if (appliedFilters.q) params.set('q', appliedFilters.q);
        if (appliedFilters.status) params.set('status', appliedFilters.status);
        if (appliedFilters.campusZone) params.set('campusZone', appliedFilters.campusZone);
        params.set('page', String(page));
        params.set('pageSize', String(PAGE_SIZE));
        const response = await api(`/api/items?${params.toString()}`);
        if (!ignore) {
          setData(response ?? { items: [], totalItems: 0, pageSize: PAGE_SIZE });
        }
      } catch (err) {
        if (!ignore) {
          setError(err?.message ?? 'Unable to search items right now.');
          setData({ items: [], totalItems: 0, pageSize: PAGE_SIZE });
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
  }, [appliedFilters, page]);

  const totalPages = useMemo(() => {
    if (!data.totalItems) return 0;
    return Math.ceil(data.totalItems / (data.pageSize ?? PAGE_SIZE));
  }, [data.totalItems, data.pageSize]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormState((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    setAppliedFilters(formState);
    setPage(0);
  };

  const handleReset = () => {
    const defaults = { q: '', status: '', campusZone: '' };
    setFormState(defaults);
    setAppliedFilters(defaults);
    setPage(0);
  };

  const handleNext = () => {
    if (page + 1 < totalPages) {
      setPage((prev) => prev + 1);
    }
  };

  const handlePrev = () => {
    if (page > 0) {
      setPage((prev) => prev - 1);
    }
  };

  return (
    <PageLayout
      title="Search Items"
      description="Filter lost and found reports by status, campus zone, and keywords."
      actions={[{ label: 'Report Lost Item', to: '/items/new/lost', emphasis: 'primary' }]}
    >
      <form className="search-form" onSubmit={handleSubmit}>
        <div className="search-form__grid">
          <label>
            Keywords
            <input
              name="q"
              value={formState.q}
              onChange={handleChange}
              placeholder="Title, description, or tag"
            />
          </label>
          <label>
            Status
            <select name="status" value={formState.status} onChange={handleChange}>
              <option value="">Any status</option>
              {ITEM_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </label>
          <label>
            Campus zone
            <select name="campusZone" value={formState.campusZone} onChange={handleChange}>
              <option value="">All zones</option>
              {CAMPUS_ZONES.map((zone) => (
                <option key={zone} value={zone}>
                  {zone}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="search-form__actions">
          <button type="submit" className="search-form__submit">
            Search
          </button>
          <button type="button" className="search-form__reset" onClick={handleReset}>
            Reset
          </button>
        </div>
      </form>

      {loading ? (
        <p className="search-results__status">Searching...</p>
      ) : error ? (
        <p className="search-results__status search-results__status--error">{error}</p>
      ) : (
        <div className="search-results">
          <header className="search-results__header">
            <h2>Results</h2>
            <span>
              {data.totalItems} {data.totalItems === 1 ? 'item' : 'items'}
            </span>
          </header>
          {data.items?.length > 0 ? (
            <ul className="search-results__list">
              {data.items.map((item) => (
                <li key={item.id} className="search-results__item">
                  <div className={`search-results__badge search-results__badge--${item.status}`}>
                    {item.status}
                  </div>
                  <div className="search-results__body">
                    <h3>
                      <Link to={`/items/${item.id}`}>{item.title}</Link>
                    </h3>
                    <p className="search-results__meta">
                      <span>{item.locationText}</span>
                      {item.campusZone ? <span>&middot; {item.campusZone}</span> : null}
                      <span>&middot; {formatDate(item.createdAt)}</span>
                    </p>
                    {item.tags && item.tags.length > 0 ? (
                      <ul className="search-results__tags">
                        {item.tags.map((tag) => (
                          <li key={`${item.id}-${tag}`}>{tag}</li>
                        ))}
                      </ul>
                    ) : null}
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p className="search-results__status">No items matched your filters.</p>
          )}
          <div className="search-results__pagination">
            <button type="button" onClick={handlePrev} disabled={page === 0}>
              Previous
            </button>
            <span>
              Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
            </span>
            <button type="button" onClick={handleNext} disabled={totalPages === 0 || page + 1 >= totalPages}>
              Next
            </button>
          </div>
        </div>
      )}
    </PageLayout>
  );
}