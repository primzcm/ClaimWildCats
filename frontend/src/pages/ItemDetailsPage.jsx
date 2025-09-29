import './ItemDetailsPage.css';
import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ref as storageRef, getDownloadURL } from 'firebase/storage';
import { PageLayout } from '../components/PageLayout';
import { api } from '../api/client';
import { storage } from '../lib/firebase';

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

function isPdf(url) {
  return /\.pdf(\?.*)?$/i.test(url);
}

function looksLikeImage(url) {
  return /\.(jpe?g|png|gif|webp|bmp)(\?.*)?$/i.test(url);
}

export function ItemDetailsPage() {
  const { id } = useParams();
  const [item, setItem] = useState(null);
  const [similar, setSimilar] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [imageUrls, setImageUrls] = useState([]);
  const [documentLinks, setDocumentLinks] = useState([]);

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const detail = await api(`/api/items/${id}`);
        if (!ignore) {
          setItem(detail);
        }
      } catch (err) {
        if (!ignore) {
          setError(err?.message ?? 'Unable to load item.');
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };
    const loadSimilar = async () => {
      try {
        const matches = await api(`/api/items/${id}/similar`);
        setSimilar(matches ?? []);
      } catch (err) {
        // fail silently for similar items
      }
    };

    load();
    loadSimilar();
    return () => {
      ignore = true;
    };
  }, [id]);

  useEffect(() => {
    let cancelled = false;
    const resolveAttachments = async () => {
      if (!item?.docUrls || item.docUrls.length === 0) {
        if (!cancelled) {
          setImageUrls([]);
          setDocumentLinks([]);
        }
        return;
      }
      const resolvedImages = [];
      const fallbackLinks = [];
      await Promise.all(item.docUrls.map(async (docUrl) => {
        if (!docUrl) {
          return;
        }
        if (docUrl.startsWith('gs://')) {
          try {
            const url = await getDownloadURL(storageRef(storage, docUrl));
            resolvedImages.push(url);
          } catch (err) {
            // log for diagnostics but do not crash the page
            console.warn('Failed to resolve Firebase Storage URL', docUrl, err);
          }
          return;
        }
        if (looksLikeImage(docUrl)) {
          resolvedImages.push(docUrl);
        } else {
          fallbackLinks.push(docUrl);
        }
      }));
      if (!cancelled) {
        setImageUrls(resolvedImages);
        setDocumentLinks(fallbackLinks);
      }
    };
    resolveAttachments();
    return () => {
      cancelled = true;
    };
  }, [item?.docUrls]);

  const tags = useMemo(() => item?.tags ?? [], [item]);

  if (loading) {
    return (
      <PageLayout title="Item Details" description="Loading report details...">
        <p>Loading item...</p>
      </PageLayout>
    );
  }

  if (error) {
    return (
      <PageLayout title="Item Details" description="We could not retrieve the item right now.">
        <p>{error}</p>
      </PageLayout>
    );
  }

  if (!item) {
    return (
      <PageLayout title="Item Details" description="The requested item was not found.">
        <p>Check the link or browse the <Link to="/search">search page</Link>.</p>
      </PageLayout>
    );
  }

  return (
    <PageLayout
      title={item.title}
      description={item.description}
      actions={[
        { label: 'Claim Item', to: `/items/${id}/claim`, emphasis: 'primary' },
        { label: 'Edit Report', to: `/items/${id}/edit` },
      ]}
    >
      <section className="item-detail">
        <dl className="item-detail__grid">
          <div>
            <dt>Status</dt>
            <dd className={`item-detail__badge item-detail__badge--${item.status}`}>{item.status}</dd>
          </div>
          <div>
            <dt>Location</dt>
            <dd>
              {item.locationText}
              {item.campusZone ? <span className="item-detail__sub"> ({item.campusZone})</span> : null}
            </dd>
          </div>
          <div>
            <dt>Last seen / found</dt>
            <dd>{formatDate(item.lastSeenAt)}</dd>
          </div>
          <div>
            <dt>Reported</dt>
            <dd>{formatDate(item.createdAt)}</dd>
          </div>
        </dl>

        {tags.length > 0 ? (
          <div className="item-detail__section">
            <h3>Tags</h3>
            <ul className="item-detail__tags">
              {tags.map((tag) => (
                <li key={tag}>{tag}</li>
              ))}
            </ul>
          </div>
        ) : null}

        {imageUrls.length > 0 ? (
          <div className="item-detail__section">
            <h3>Images</h3>
            <ul className="item-detail__images">
              {imageUrls.map((src, index) => (
                <li key={src}>
                  <img src={src} alt={`${item.title} evidence ${index + 1}`} loading="lazy" />
                </li>
              ))}
            </ul>
          </div>
        ) : null}

        {documentLinks.length > 0 ? (
          <div className="item-detail__section">
            <h3>Documents</h3>
            <ul className="item-detail__docs">
              {documentLinks.map((url, index) => (
                <li key={url}>
                  <a
                    href={url}
                    target={isPdf(url) ? '_blank' : undefined}
                    rel={isPdf(url) ? 'noopener noreferrer' : undefined}
                  >
                    Document {index + 1}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        ) : null}
      </section>

      {similar.length > 0 ? (
        <section className="item-detail__section">
          <h3>Similar items</h3>
          <ul className="item-detail__similar">
            {similar.map((match) => (
              <li key={match.id}>
                <Link to={`/items/${match.id}`}>{match.title}</Link>
                <span>{formatDate(match.createdAt)}</span>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
    </PageLayout>
  );
}
