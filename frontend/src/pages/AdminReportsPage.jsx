import { PageLayout } from '../components/PageLayout';

export function AdminReportsPage() {
  return (
    <PageLayout
      title="Manage Reports"
      description="Admin view over every lost and found report across the system."
    >
      <section>
        <h2>Moderation Tools</h2>
        <ul>
          <li>Bulk actions for hiding, merging duplicates, or deleting with reasons.</li>
          <li>Advanced filters for category, status, custodian, and date ranges.</li>
          <li>Inline flags showing dispute or abuse reports.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
