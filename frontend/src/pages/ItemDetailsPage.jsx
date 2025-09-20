import { PageLayout } from '../components/PageLayout';

export function ItemDetailsPage() {
  return (
    <PageLayout
      title="Item Details"
      description="Showcase full context for a lost or found report, including gallery, metadata, and actions."
      actions={[
        { label: 'Claim Item', to: '/items/:id/claim', emphasis: 'primary' },
        { label: 'Edit Report', to: '/items/:id/edit' }
      ]}
    >
      <section>
        <h2>Content Blocks</h2>
        <ul>
          <li>Status badge with owner/finder identity and time since posted.</li>
          <li>Photo carousel, rich description, and map embed for last known location.</li>
          <li>Suggested matches using similarity scoring.</li>
          <li>Secure messages thread between poster, claimant, and admin.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
