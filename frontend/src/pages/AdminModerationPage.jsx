import { PageLayout } from '../components/PageLayout';

export function AdminModerationPage() {
  return (
    <PageLayout
      title="Moderation Queue"
      description="Handle flagged content, suspected duplicates, and removal workflows."
    >
      <section>
        <h2>Queue Priorities</h2>
        <ul>
          <li>Flag review with reason codes like spam or inappropriate content.</li>
          <li>Duplicate detection list with merge suggestions.</li>
          <li>Soft delete controls with audit logging and reversal options.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
