import { PageLayout } from '../components/PageLayout';

export function EditItemPage() {
  return (
    <PageLayout
      title="Edit Report"
      description="Manage an existing lost or found report, update status, or close it after resolution."
    >
      <section>
        <h2>Capabilities</h2>
        <ul>
          <li>Pre-filled form mirroring the create experience.</li>
          <li>Quick actions to mark as returned, close the report, or duplicate.</li>
          <li>Audit trail placeholder so admins can trace changes.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
