import { PageLayout } from '../components/PageLayout';
import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <PageLayout
      title="Page Not Found"
      description="We couldn't find the page you were looking for."
      actions={[
        { label: 'Return Home', to: '/' }
      ]}
    >
      <section>
        <p>
          Double-check the URL or navigate using the main menu. If you think this is an error, contact support
          through the admin dashboard.
        </p>
        <p>
          <Link to="/admin">Visit the admin console</Link> or <Link to="/search">search items</Link>.
        </p>
      </section>
    </PageLayout>
  );
}
