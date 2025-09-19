import { PageLayout } from '../components/PageLayout';

export function ReportLostPage() {
  return (
    <PageLayout
      title="Report Lost Item"
      description="Form for students to submit detailed information about a lost possession."
    >
      <section>
        <h2>Form Blueprint</h2>
        <ul>
          <li>Required fields: title, category, last seen date/time, and location picker.</li>
          <li>Optional enrichments: color, brand, distinguishing marks, reward details.</li>
          <li>Photo uploader with drag-and-drop placeholder thumbnails.</li>
          <li>Call to action buttons for saving a draft or submitting the report.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
