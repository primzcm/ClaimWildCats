import { PageLayout } from '../components/PageLayout';

export function AdminAnalyticsPage() {
  return (
    <PageLayout
      title="Analytics & Exports"
      description="Generate reports and visualize trends across categories, locations, and time."
    >
      <section>
        <h2>Insights</h2>
        <ul>
          <li>Charts by category, building, and time-of-day distributions.</li>
          <li>Download options for CSV and PDF exports.</li>
          <li>Integrations placeholder for data warehousing or BI tools.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
