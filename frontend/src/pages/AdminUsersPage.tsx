import { PageLayout } from '../components/PageLayout';

export function AdminUsersPage() {
  return (
    <PageLayout
      title="Manage Users"
      description="Moderation panel to oversee user roles, status, and activity."
    >
      <section>
        <h2>User Table</h2>
        <ul>
          <li>Columns for name, email, role, status, and post counts.</li>
          <li>Actions to promote, demote, or disable accounts.</li>
          <li>Filters for role, status, and recent activity.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
