import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import './HomePage.css';

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

export function HomePage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      try {
        const response = await api('/api/items?page=0&pageSize=6');
        if (!ignore) {
          setItems(response?.items ?? []);
        }
      } catch (err) {
        if (!ignore) {
          setError(err?.message ?? 'Unable to load recent activity.');
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
  }, []);

  return (
    <>
      <section className="home-hero">
        <div className="home-hero__content">
          <span className="home-hero__eyebrow">Campus Lost &amp; Found</span>
          <h1 className="home-hero__title">Find &amp; Recover With Ease</h1>
          <p className="home-hero__subtitle">
            Experience effortless recovery with our dedicated lost and found service.
          </p>
          <div className="home-hero__cta">
            <Link to="/get-started" className="home-hero__cta-link">
              Get Started
            </Link>
          </div>
        </div>

        <div className="home-hero__visuals">
          <div className="home-hero__actions">
            <Link to="/items/new/lost" className="home-hero__action home-hero__action--lost">
              <span className="home-hero__action-text">Lost</span>
              <span className="home-hero__action-icon" aria-hidden="true">?</span>
            </Link>
            <Link to="/items/new/found" className="home-hero__action home-hero__action--found">
              <span className="home-hero__action-text">Found</span>
              <span className="home-hero__action-icon" aria-hidden="true">-&gt;</span>
            </Link>
          </div>
          <div className="home-hero__gallery" aria-hidden="true">
            <div className="home-hero__placeholder home-hero__placeholder--one" />
            <div className="home-hero__placeholder home-hero__placeholder--two" />
            <div className="home-hero__placeholder home-hero__placeholder--three" />
          </div>
        </div>
      </section>

      <section className="home-feed">
        <header className="home-feed__header">
          <h2>Recent activity</h2>
          <Link to="/search" className="home-feed__link">
            Browse all reports
          </Link>
        </header>
        {loading ? (
          <p className="home-feed__status">Loading items...</p>
        ) : error ? (
          <p className="home-feed__status home-feed__status--error">{error}</p>
        ) : items.length === 0 ? (
          <p className="home-feed__status">No items posted yet. Be the first to report.</p>
        ) : (
          <ul className="home-feed__list">
            {items.map((item) => (
              <li key={item.id} className="home-feed__item">
                <div className={`home-feed__badge home-feed__badge--${item.status}`}>
                  {item.status}
                </div>
                <h3>
                  <Link to={`/items/${item.id}`}>{item.title}</Link>
                </h3>
                <p className="home-feed__meta">
                  <span>{item.locationText}</span>
                  {item.campusZone ? <span>&middot; {item.campusZone}</span> : null}
                </p>
                <p className="home-feed__time">Last updated: {formatDate(item.createdAt)}</p>
                {item.tags && item.tags.length > 0 ? (
                  <ul className="home-feed__tags">
                    {item.tags.slice(0, 4).map((tag) => (
                      <li key={`${item.id}-${tag}`}>{tag}</li>
                    ))}
                  </ul>
                ) : null}
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}