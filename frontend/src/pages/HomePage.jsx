import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDownloadURL, ref as storageRef } from 'firebase/storage';
import heroIllustration from '../icons/front.png';
import { api } from '../api/client';
import { storage } from '../lib/firebase';

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

function normaliseWhitespace(value) {
  return value ? value.toString().replace(/[\s\u2000-\u200B\uFEFF]+/g, ' ').trim() : '';
}

function selectPrimaryDocUrl(docUrls) {
  if (!Array.isArray(docUrls)) return '';
  for (const url of docUrls) {
    const trimmed = normaliseWhitespace(url);
    if (trimmed) {
      return trimmed;
    }
  }
  return '';
}

async function resolveFirstImage(docUrls = []) {
  const candidate = selectPrimaryDocUrl(docUrls);
  if (!candidate) return null;
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

const STATUS_STYLES = {
  lost: 'bg-[#fce5e2] text-[#b42318]',
  found: 'bg-[#dff7e3] text-[#0f6b2f]',
  claimed: 'bg-[#fef3c7] text-[#92400e]',
};

export function HomePage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [assetMap, setAssetMap] = useState({});
  const imageCacheRef = useRef({});

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

  useEffect(() => {
    let cancelled = false;
    const fetchImages = async () => {
      if (items.length === 0) {
        setAssetMap({});
        return;
      }
      const results = await Promise.all(
        items.map(async (item) => {
          const primaryDoc = selectPrimaryDocUrl(item.docUrls);
          if (primaryDoc && imageCacheRef.current[primaryDoc]) {
            return { id: item.id, url: imageCacheRef.current[primaryDoc] };
          }
          const url = await resolveFirstImage(item.docUrls);
          if (primaryDoc && url) {
            imageCacheRef.current[primaryDoc] = url;
          }
          return { id: item.id, url };
        }),
      );
      if (cancelled) return;
      const next = {};
      for (const entry of results) {
        next[entry.id] = entry.url;
      }
      setAssetMap(next);
    };

    fetchImages();
    return () => {
      cancelled = true;
    };
  }, [items]);

  return (
    <div className="space-y-24">
      <section className="relative left-1/2 w-screen -translate-x-1/2 bg-[#f8f0e5]">
        <div className="mx-auto grid max-w-7xl items-center gap-20 px-6 py-20 sm:px-12 lg:grid-cols-[1.05fr_1fr] lg:py-28">
          <div className="space-y-10">
            <p className="text-sm font-semibold uppercase tracking-[0.32em] text-burgundy">Campus Lost &amp; Found</p>
            <h1 className="text-5xl font-extrabold leading-[1.02] tracking-tight text-brown md:text-6xl lg:text-7xl">
              ClaimWildcats: Campus Lost &amp; Found.
            </h1>
            <p className="max-w-xl text-lg text-brown/80">
              Find &amp; Recover With Ease. Your dedicated campus service.
            </p>
            <div className="flex flex-wrap gap-4">
              <Link
                to="/items/new/lost"
                className="rounded-full bg-burgundy px-6 py-3 text-base font-semibold text-white shadow-soft transition hover:-translate-y-0.5 hover:shadow-card focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-burgundy"
              >
                Report Lost
              </Link>
              <Link
                to="/items/new/found"
                className="rounded-full bg-brown px-6 py-3 text-base font-semibold text-white shadow-soft transition hover:-translate-y-0.5 hover:shadow-card focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brown"
              >
                Report Found
              </Link>
            </div>
          </div>

          <div className="relative flex items-center justify-center overflow-hidden rounded-3xl bg-[#f8f0e5]">
            <div className="relative flex w-full max-w-[760px] items-center justify-center">
              <img
                src={heroIllustration}
                alt="Students exchanging a lost item"
                className="h-auto w-full max-w-[760px] object-contain"
                loading="lazy"
              />
            </div>
          </div>

        </div>
      </section>

      <section className="relative left-1/2 w-screen -translate-x-1/2 bg-[#3E0703] text-white">
        <div className="mx-auto max-w-5xl px-6 py-10 sm:px-8 lg:py-12">
          <header className="flex flex-wrap items-center justify-between gap-4">
            <h2 className="text-2xl font-bold leading-tight text-white">Recent Activity</h2>
            <Link
              to="/search"
              className="rounded-full bg-gold px-4 py-2 text-sm font-semibold text-brown shadow-card transition hover:-translate-y-0.5 hover:shadow-lg focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white/70"
            >
              Browse All Reports
            </Link>
          </header>

          {loading ? (
            <p className="mt-6 text-sm text-white/80">Loading items...</p>
          ) : error ? (
            <p className="mt-6 text-sm font-semibold text-gold">{error}</p>
          ) : items.length === 0 ? (
            <p className="mt-6 text-sm text-white/80">No items posted yet. Be the first to report.</p>
          ) : (
            <div className="mt-6 grid grid-cols-1 gap-6 md:grid-cols-3">
              {items.map((item) => {
                const status = (item.status || '').toLowerCase();
                const badgeClass = STATUS_STYLES[status] ?? 'bg-white text-burgundy';
                const statusLabel = (item.status ?? 'New').toUpperCase();
                const imageSrc = assetMap[item.id] ?? item.imageUrl ?? item.image;
                return (
                  <Link
                    key={item.id}
                    to={`/items/${item.id}`}
                    className="grid h-full min-h-[22rem] grid-rows-[11rem_1fr] overflow-hidden rounded-xl bg-white text-left text-brown shadow-lg transition hover:-translate-y-1 hover:shadow-xl focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gold"
                  >
                    <div className="relative h-48 w-full overflow-hidden bg-white">
                      {imageSrc ? (
                        <img src={imageSrc} alt="" className="h-full w-full object-cover" loading="lazy" />
                      ) : (
                        <div className="flex h-full w-full items-center justify-center bg-gray-200 text-sm font-semibold text-brown/60">
                          No image
                        </div>
                      )}
                      <span
                        className={`absolute left-3 top-3 rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wide ${badgeClass}`}
                      >
                        {statusLabel}
                      </span>
                    </div>
                    <div className="flex flex-col justify-between gap-2 px-4 py-4">
                      <h3 className="text-lg font-semibold leading-tight">{item.title}</h3>
                      <p className="text-sm text-brown/70">
                        {item.locationText}
                        {item.campusZone ? <span className="text-brown/60"> • {item.campusZone}</span> : null}
                      </p>
                      <p className="text-xs font-medium text-brown/60">Last updated: {formatDate(item.createdAt)}</p>
                    </div>
                  </Link>
                );
              })}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
