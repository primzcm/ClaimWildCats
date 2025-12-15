import { useEffect, useMemo, useState } from 'react';
//import { getDownloadURL, ref as storageRef } from 'firebase/storage';
import { PageLayout } from '../components/PageLayout';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
//import { storage } from '../lib/firebase';
import './MyReportsPage.css';

const formatter = new Intl.DateTimeFormat('en-PH', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'Asia/Manila',
});

function formatDate(value) {
  if (!value) return 'Unknown';
  try {
    return formatter.format(new Date(value));
  } catch {
    return 'Unknown';
  }
}

export function MyReportsPage() {
  const { user } = useAuth();
  const [reports, setReports] = useState([]);
  const [reportsLoading, setReportsLoading] = useState(true);
  const [reportsError, setReportsError] = useState('');
  const [selectedItemId, setSelectedItemId] = useState(null);
  const [claims, setClaims] = useState([]);
  const [claimsLoading, setClaimsLoading] = useState(false);
  const [claimsError, setClaimsError] = useState('');
  const [claimNotes, setClaimNotes] = useState({});
  const [decisionBusy, setDecisionBusy] = useState('');
  const [resolvedAttachments, setResolvedAttachments] = useState({});

  useEffect(() => {
    if (!user?.uid) {
      setReports([]);
      return;
    }
    let ignore = false;
    const loadReports = async () => {
      setReportsLoading(true);
      setReportsError('');
      try {
        const list = await api(`/api/users/${user.uid}/reports`);
        if (!ignore) {
          setReports(list ?? []);
        }
      } catch (err) {
        if (!ignore) {
          setReportsError(err?.message ?? 'Unable to load your reports.');
          setReports([]);
        }
      } finally {
        if (!ignore) {
          setReportsLoading(false);
        }
      }
    };
    loadReports();
    return () => {
      ignore = true;
    };
  }, [user?.uid]);

  const foundReports = useMemo(
    () => reports.filter((report) => report.status === 'FOUND'),
    [reports],
  );
  const selectedItem = foundReports.find((report) => report.id === selectedItemId);

  useEffect(() => {
    if (!selectedItemId) {
      setClaims([]);
      return;
    }
    let ignore = false;
    const loadClaims = async () => {
      setClaimsLoading(true);
      setClaimsError('');
      try {
        const list = await api(`/api/items/${selectedItemId}/claims`);
        if (!ignore) {
          setClaims(list ?? []);
        }
      } catch (err) {
        if (!ignore) {
          setClaimsError(err?.message ?? 'Unable to load claims.');
          setClaims([]);
        }
      } finally {
        if (!ignore) {
          setClaimsLoading(false);
        }
      }
    };
    loadClaims();
    return () => {
      ignore = true;
    };
  }, [selectedItemId]);

  useEffect(() => {
    if (claims.length === 0) {
      setResolvedAttachments({});
      return;
    }
    let cancelled = false;
    const resolve = async () => {
      const entries = {};
      await Promise.all(claims.map(async (claim) => {
        if (!claim.attachmentUrls || claim.attachmentUrls.length === 0) {
          entries[claim.id] = [];
          return;
        }
        const resolved = await Promise.all(claim.attachmentUrls.map(async (url) => {
          if (!url) return null;
          if (!url.startsWith('gs://')) {
            return url;
          }
          try {
            return await getDownloadURL(storageRef(storage, url));
          } catch (err) {
            console.warn('Failed to resolve claim attachment', url, err);
            return null;
          }
        }));
        entries[claim.id] = resolved.filter(Boolean);
      }));
      if (!cancelled) {
        setResolvedAttachments(entries);
      }
    };
    resolve();
    return () => {
      cancelled = true;
    };
  }, [claims]);

  const handleSelectItem = (itemId) => {
    setSelectedItemId((prev) => (prev === itemId ? null : itemId));
    setClaimNotes({});
    setResolvedAttachments({});
  };

  const handleNoteChange = (claimId, value) => {
    setClaimNotes((prev) => ({ ...prev, [claimId]: value }));
  };

  const refreshClaims = async (itemId) => {
    setClaimsLoading(true);
    setClaimsError('');
    try {
      const list = await api(`/api/items/${itemId}/claims`);
      setClaims(list ?? []);
    } catch (err) {
      setClaims([]);
      setClaimsError(err?.message ?? 'Unable to refresh claims.');
    } finally {
      setClaimsLoading(false);
    }
  };

  const refreshReports = async () => {
    if (!user?.uid) return;
    try {
      const list = await api(`/api/users/${user.uid}/reports`);
      setReports(list ?? []);
    } catch {
      // keep previous data; error already surfaced earlier
    }
  };

  const handleDecision = async (claim, status) => {
    const note = (claimNotes[claim.id] ?? '').trim();
    setDecisionBusy(claim.id);
    setClaimsError('');
    try {
      await api(`/api/claims/${claim.id}/decision`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, reviewerNote: note }),
      });
      await refreshClaims(claim.itemId);
      if (status === 'APPROVED') {
        await refreshReports();
      }
    } catch (err) {
      setClaimsError(err?.message ?? 'Unable to record your decision.');
    } finally {
      setDecisionBusy('');
    }
  };

  return (
    <PageLayout
      title="My Reports"
      description="Review your found reports, track claims, and decide when to hand items back."
    >
      {reportsLoading ? (
        <p>Loading your reports...</p>
      ) : reportsError ? (
        <p className="reports-status reports-status--error">{reportsError}</p>
      ) : foundReports.length === 0 ? (
        <p>You have no found-item reports yet.</p>
      ) : (
        <div className="reports-table-wrapper">
          <table className="reports-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Location</th>
                <th>Posted</th>
                <th>Status</th>
                <th>Claims</th>
              </tr>
            </thead>
            <tbody>
              {foundReports.map((report) => (
                <tr key={report.id}>
                  <td>{report.title}</td>
                  <td>{report.locationText}</td>
                  <td>{formatDate(report.createdAt)}</td>
                  <td className={`reports-table__status reports-table__status--${report.status}`}>
                    {report.status}
                  </td>
                  <td>
                    <button
                      type="button"
                      className="reports-table__action"
                      onClick={() => handleSelectItem(report.id)}
                    >
                      {selectedItemId === report.id ? 'Hide' : 'Review'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selectedItemId ? (
        <section className="claims-panel">
          <header className="claims-panel__header">
            <div>
              <h2>Claims for {selectedItem?.title ?? 'selected report'}</h2>
              <p>{selectedItem?.locationText}</p>
            </div>
            <button
              type="button"
              className="claims-panel__refresh"
              onClick={() => refreshClaims(selectedItemId)}
              disabled={claimsLoading}
            >
              Refresh
            </button>
          </header>

          {claimsLoading ? (
            <p>Loading claims...</p>
          ) : claimsError ? (
            <p className="reports-status reports-status--error">{claimsError}</p>
          ) : claims.length === 0 ? (
            <p>No one has claimed this item yet.</p>
          ) : (
            <ul className="claims-list">
              {claims.map((claim) => {
                const resolved = resolvedAttachments[claim.id] ?? [];
                const noteValue = claimNotes[claim.id] ?? '';
                const isPending = claim.status === 'PENDING';
                return (
                  <li key={claim.id} className="claims-card">
                    <div className="claims-card__header">
                      <div>
                        <h3>Claim by {claim.claimantId ?? 'Unknown user'}</h3>
                        <p>Submitted {formatDate(claim.submittedAt)}</p>
                      </div>
                      <span className={`claims-card__status claims-card__status--${claim.status}`}>
                        {claim.status}
                      </span>
                    </div>
                    <p className="claims-card__detail">
                      <strong>Secret detail:</strong> {claim.secretDetail ?? 'Not provided'}
                    </p>
                    <p className="claims-card__detail">
                      <strong>Justification:</strong> {claim.justification ?? 'Not provided'}
                    </p>
                    {resolved.length > 0 ? (
                      <div className="claims-card__attachments">
                        <strong>Attachments:</strong>
                        <ul>
                          {resolved.map((url) => (
                            <li key={url}>
                              <a href={url} target="_blank" rel="noreferrer">
                                View evidence
                              </a>
                            </li>
                          ))}
                        </ul>
                      </div>
                    ) : null}
                    {claim.reviewerNote ? (
                      <p className="claims-card__note">
                        <strong>Reviewer note:</strong> {claim.reviewerNote}
                      </p>
                    ) : null}

                    {isPending ? (
                      <div className="claims-card__decision">
                        <label>
                          Reviewer note (optional)
                          <input
                            type="text"
                            value={noteValue}
                            onChange={(event) => handleNoteChange(claim.id, event.target.value)}
                            maxLength={140}
                          />
                        </label>
                        <div className="claims-card__actions">
                          <button
                            type="button"
                            onClick={() => handleDecision(claim, 'DENIED')}
                            disabled={decisionBusy === claim.id}
                          >
                            Deny
                          </button>
                          <button
                            type="button"
                            className="claims-card__approve"
                            onClick={() => handleDecision(claim, 'APPROVED')}
                            disabled={decisionBusy === claim.id}
                          >
                            Approve
                          </button>
                        </div>
                      </div>
                    ) : null}
                  </li>
                );
              })}
            </ul>
          )}
        </section>
      ) : null}
    </PageLayout>
  );
}

