import { PageLayout } from '../components/PageLayout';
import { ItemReportForm } from '../components/ItemReportForm';

export function ReportLostPage() {
  return (
    <PageLayout
      title="Report Lost Item"
      description="Share details about what you misplaced so we can help reunite you quickly."
    >
      <ItemReportForm mode="lost" />
    </PageLayout>
  );
}
