import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { ref as storageRef, getDownloadURL } from 'firebase/storage';
import { storage } from '../lib/firebase';
import './ItemGallery.css';

function normaliseWhitespace(value) {
  return value ? value.toString().replace(/[\s\u2000-\u200B\uFEFF]+/g, ' ').trim() : '';
}

function buildSnippet(description, maxLength = 140) {
  const cleaned = normaliseWhitespace(description);
  if (!cleaned) {
    return '';
  }
  if (cleaned.length <= maxLength) {
    return cleaned;
  }
  const truncated = cleaned.slice(0, maxLength);
  const lastSpace = truncated.lastIndexOf(' ');
  return `${lastSpace > 0 ? truncated.slice(0, lastSpace) : truncated}...`;
}

function selectPrimaryDocUrl(docUrls) {
  if (!Array.isArray(docUrls)) {
    return '';
  }
  for (const candidate of docUrls) {
    const trimmed = normaliseWhitespace(candidate);
    if (trimmed) {
      return trimmed;
    }
  }
  return '';
}

async function resolveFirstImage(docUrls = []) {
  const candidate = selectPrimaryDocUrl(docUrls);
  if (!candidate) {
    return null;
  }
  if (candidate.startsWith('gs://')) {
    try {
      return await getDownloadURL(storageRef(storage, candidate));
    } catch (error) {
      console.warn('Failed to resolve Firebase Storage URL', candidate, error);
      return null;
    }
  }
  return candidate;
}

const DATE_FORMAT_OPTIONS = {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'Asia/Manila',
};

export function ItemGallery({ items }) {
  const cacheRef = useRef({});
  const [assetMap, setAssetMap] = useState({});

  useEffect(() => {
    let cancelled = false;

    const run = async () => {
      if (items.length === 0) {
        cacheRef.current = {};
        setAssetMap({});
        return;
      }

      const allowedIds = new Set(items.map((item) => item.id));
      const current = { ...cacheRef.current };
      for (const key of Object.keys(current)) {
        if (!allowedIds.has(key)) {
          delete current[key];
        }
      }

      const pending = items.filter((item) => {
        const docUrl = selectPrimaryDocUrl(item.docUrls);
        const cached = current[item.id];
        return docUrl && (!cached || cached.key !== docUrl);
      });

      if (pending.length === 0) {
        cacheRef.current = current;
        setAssetMap(current);
        return;
      }

      const results = await Promise.all(
        pending.map(async (item) => {
          const docUrl = selectPrimaryDocUrl(item.docUrls);
          try {
            const url = await resolveFirstImage(item.docUrls);
            return { id: item.id, url, key: docUrl };
          } catch (error) {
            console.warn('Failed to resolve first image for item', item.id, error);
            return { id: item.id, url: null, key: docUrl };
          }
        }),
      );

      if (cancelled) {
        return;
      }

      const next = { ...current };
      for (const entry of results) {
        next[entry.id] = { url: entry.url, key: entry.key };
      }
      cacheRef.current = next;
      setAssetMap(next);
    };

    run();
    return () => {
      cancelled = true;
    };
  }, [items]);

  return (
    <ul className="item-gallery" data-count={items.length}>
      {items.map((item) => {
        const assetEntry = cacheRef.current[item.id] ?? assetMap[item.id];
        const asset = assetEntry?.url;
        const hasImage = typeof asset === 'string' && asset.length > 0;
        const snippet = buildSnippet(item.description);
        return (
          <li key={item.id} className="item-gallery__item">
            <article className="item-card">
              <Link
                to={`/items/${item.id}`}
                className="item-card__media"
                aria-label={`View ${item.title}`}
              >
                {hasImage ? (
                  <img src={asset} alt="" loading="lazy" />
                ) : (
                  <div className="item-card__placeholder" aria-hidden="true">
                    No image
                  </div>
                )}
              </Link>
              <div className="item-card__content">
                <div className={`item-card__status item-card__status--${item.status}`}>
                  {item.status}
                </div>
                <h2 className="item-card__title">
                  <Link to={`/items/${item.id}`}>{item.title}</Link>
                </h2>
                <p className="item-card__location">
                  <span>{item.locationText}</span>
                  {item.campusZone ? (
                    <span className="item-card__location-zone"> - {item.campusZone}</span>
                  ) : null}
                </p>
                {snippet ? <p className="item-card__snippet">{snippet}</p> : null}
                <p className="item-card__updated">
                  Updated {new Date(item.createdAt).toLocaleString('en-PH', DATE_FORMAT_OPTIONS)}
                </p>
                {item.tags && item.tags.length > 0 ? (
                  <ul className="item-card__tags">
                    {item.tags.slice(0, 5).map((tag) => (
                      <li key={`${item.id}-${tag}`}>{tag}</li>
                    ))}
                  </ul>
                ) : null}
              </div>
            </article>
          </li>
        );
      })}
    </ul>
  );
}
