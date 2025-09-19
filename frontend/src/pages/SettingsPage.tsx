import { PageLayout } from '../components/PageLayout';

export function SettingsPage() {
  return (
    <PageLayout
      title="Settings"
      description="Control notification preferences, privacy options, and account security."
    >
      <section>
        <h2>Preferences</h2>
        <ul>
          <li>Email, SMS, and push toggle placeholders.</li>
          <li>Privacy controls for anonymous postings and contact visibility.</li>
          <li>Two-factor authentication and delete account workflows.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
