import { PageLayout } from '../components/PageLayout';

export function RegisterPage() {
  return (
    <PageLayout
      title="Register"
      description="Create an account to manage reports, claims, and notifications."
      actions={[
        { label: 'Have an account? Login', to: '/auth/login' }
      ]}
    >
      <section>
        <h2>Sign-up Flow</h2>
        <ul>
          <li>Collect name, wildcat email, and password.</li>
          <li>Prompt to verify email through Firebase Authentication.</li>
          <li>Optional role codes for admin/moderator invitation.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
