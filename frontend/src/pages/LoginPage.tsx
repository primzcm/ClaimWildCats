import { PageLayout } from '../components/PageLayout';

export function LoginPage() {
  return (
    <PageLayout
      title="Login"
      description="Authenticate with campus credentials or continue as a guest."
      actions={[
        { label: 'Need an account?', to: '/auth/register', emphasis: 'primary' }
      ]}
    >
      <section>
        <h2>Authentication Options</h2>
        <ul>
          <li>Email/password form with validation placeholders.</li>
          <li>Campus Google or SSO button group.</li>
          <li>Guest browsing link back to the home feed.</li>
        </ul>
      </section>
    </PageLayout>
  );
}
