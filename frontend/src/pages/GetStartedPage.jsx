import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import './GetStartedPage.css';

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

export function GetStartedPage() {
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
          setError(err?.message ?? 'Unable to load recent updates.');
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
    <div className="gs-page">
      <section className="gs-hero">
        <div className="gs-hero__content">
          <h1>Lost &amp; found</h1>
          <p className="gs-hero__subtitle">Here is what to expect when you use ClaimWildCats.</p>
          <h2 className="gs-hero__eyebrow">How it works</h2>
          <ol className="gs-hero__steps">
            <li>Post a detailed lost or found report so campus staff and students can spot it quickly.</li>
            <li>Monitor matches and manage claims in your dashboard with secure messaging.</li>
            <li>Once the owner is verified, arrange a hand-off at the designated pickup point.</li>
          </ol>
        </div>
      </section>

      <section className="gs-actions" aria-label="Report navigation">
        <Link className="gs-cta gs-cta--lost" to="/items/new/lost">
          <span className="gs-cta__icon" aria-hidden="true">?</span>
          <span className="gs-cta__label">Report Lost</span>
        </Link>
        <Link className="gs-cta gs-cta--found" to="/items/new/found">
          <span className="gs-cta__icon" aria-hidden="true">!</span>
          <span className="gs-cta__label">Report Found</span>
        </Link>
      </section>

      <section className="gs-updates">
        <div className="gs-updates__header">
          <h2>Recent Updates</h2>
          <Link to="/search" className="gs-updates__link">
            View all reports
          </Link>
        </div>
        {loading ? (
          <p className="gs-updates__status">Loading recent activity...</p>
        ) : error ? (
          <p className="gs-updates__status gs-updates__status--error">{error}</p>
        ) : items.length === 0 ? (
          <p className="gs-updates__status">No reports yet. Be the first to share an update.</p>
        ) : (
          <ul className="gs-updates__grid">
            {items.map((item) => (
              <li key={item.id} className="gs-card">
                <div className={`gs-card__badge gs-card__badge--${item.status}`}>
                  {item.status}
                </div>
                <h3>
                  <Link to={`/items/${item.id}`}>{item.title}</Link>
                </h3>
                <p className="gs-card__meta">
                  <span>{item.locationText}</span>
                  {item.campusZone ? <span>• {item.campusZone}</span> : null}
                </p>
                <p className="gs-card__time">Updated {formatDate(item.createdAt)}</p>
                {item.tags && item.tags.length > 0 ? (
                  <ul className="gs-card__tags">
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

      <footer className="gs-footer" aria-label="Get started support">
        <p>Need help getting started? Email <a href="mailto:support@claimwildcats.com">support@claimwildcats.com</a> or visit the Help / FAQ link in the footer.</p>
      </footer>
    </div>
  );
}
