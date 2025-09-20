import { PageLayout } from '../components/PageLayout';

export function AdminDashboardPage() {
  return (
    <PageLayout
      title="Admin Dashboard"
      description="High-level overview of lost and found activity, queues, and hot spots."
      actions={[
        { label: 'Manage Users', to: '/admin/users' },
        { label: 'Moderation Queue', to: '/admin/moderation', emphasis: 'primary' }
      ]}
    >
      <section>
        <h2>Dashboard Widgets</h2>
        <ul>
          <li>KPIs like claim rate, average match time, and weekly volume.</li>
          <li>Heatmap widget highlighting campus loss hotspots.</li>
          <li>Queues for pending claims, flagged posts, and suspected duplicates.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
