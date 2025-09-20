import { PageLayout } from '../components/PageLayout';

export function ProfilePage() {
  return (
    <PageLayout
      title="Profile"
      description="Personal hub showing verified identity, role, and quick access to posts and claims."
    >
      <section>
        <h2>Sections</h2>
        <ul>
          <li>Profile overview with avatar, contact info, and verification status.</li>
          <li>Tabs for My Posts, My Claims, Saved Searches, and Notifications.</li>
          <li>Placeholder metrics like successful reunites and open cases.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
