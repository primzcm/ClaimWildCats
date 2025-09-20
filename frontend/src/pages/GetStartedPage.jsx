import { PageLayout } from '../components/PageLayout';

export function GetStartedPage() {
  return (
    <PageLayout
      title="How ClaimWildCats Works"
      description="Follow these steps to reunite items with their owners quickly."
    >
      <section>
        <h2>Three Simple Steps</h2>
        <ol>
          <li>Report a lost or found item with as much detail as possible.</li>
          <li>Track matches and manage claims from your dashboard.</li>
          <li>Coordinate pickup using secure in-app messages.</li>
        </ol>
      </section>
      <section>
        <h2>Need Support?</h2>
        <p>
          Visit the Help / FAQ link in the footer or email support@claimwildcats.com for assistance. We’ll be adding tutorials and
          campus drop-off instructions soon.
        </p>
      </section>
    </PageLayout>
  );
}
