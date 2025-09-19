import { PageLayout } from '../components/PageLayout';

export function ReportFoundPage() {
  return (
    <PageLayout
      title="Report Found Item"
      description="Capture details about an item that has been found and is available to claim."
    >
      <section>
        <h2>Form Blueprint</h2>
        <ul>
          <li>Mirrors the lost item form with custody status and optional serial number.</li>
          <li>Allows multiple photos and condition notes to help owners identify items.</li>
          <li>Contact preferences for how the finder wants to be reached.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
