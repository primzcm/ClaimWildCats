import { PageLayout } from '../components/PageLayout';

export function ClaimItemPage() {
  return (
    <PageLayout
      title="Claim Item"
      description="Structured flow for owners to verify items and for finders/admins to review claims."
    >
      <section>
        <h2>Workflow</h2>
        <ul>
          <li>Step 1: Owner provides proof of ownership and secret details.</li>
          <li>Step 2: Submission confirmation with pending status updates.</li>
          <li>Step 3: Finder/admin review panel with approve/deny controls and audit trail.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
