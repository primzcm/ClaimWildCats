import { PageLayout } from '../components/PageLayout';
import { ItemReportForm } from '../components/ItemReportForm';

export function ReportFoundPage() {
  return (
    <PageLayout
      title="Report Found Item"
      description="Describe the item you discovered so the right owner can claim it."
    >
      <ItemReportForm mode="found" />
    </PageLayout>
  );
}
