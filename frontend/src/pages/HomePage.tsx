import { PageLayout } from '../components/PageLayout';

export function HomePage() {
  return (
    <PageLayout
      title="Home / Feed"
      description="Browse and filter all open lost and found posts, with quick access to reporting actions."
      actions={[
        { label: 'Report Lost Item', to: '/items/new/lost', emphasis: 'primary' },
        { label: 'Report Found Item', to: '/items/new/found' }
      ]}
    >
      <section>
        <h2>Highlights</h2>
        <ul>
          <li>Global search bar across the catalog.</li>
          <li>Filters for category, color, status, date range, and campus location.</li>
          <li>Grid of item cards showing status, location chip, and match score placeholders.</li>
          <li>Saved searches and notifications sidebar placeholder content.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
