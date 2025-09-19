import { PageLayout } from '../components/PageLayout';

export function MyReportsPage() {
  return (
    <PageLayout
      title="My Reports"
      description="Manage all items you have reported, track claims, and perform quick actions."
    >
      <section>
        <h2>Features</h2>
        <ul>
          <li>Table of reports with status, last update, and location.</li>
          <li>Inline controls to edit, close, or mark an item as returned.</li>
          <li>Badge indicators for pending claims that need attention.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
