import { Link } from "react-router-dom";
import heroImage from "../icons/getstarted.png";
import "./GetStartedPage.css";

export function GetStartedPage() {
  return (
    <div className="gs-page">
      <section className="gs-hero">
        <img
          src={heroImage}
          alt="Students reconnecting over returned belongings using ClaimWildcats"
          className="gs-hero__image"
        />
        <div className="gs-hero__overlay" />
        <div className="gs-hero__content">
          <p className="gs-hero__eyebrow">Welcome to ClaimWildcats</p>
          <h1 className="gs-hero__headline">
            Reconnecting You with Your Lost Items
          </h1>
          <p className="gs-hero__subtitle">
            Simple, secure, efficient. Report and recover your belongings on
            campus.
          </p>
        </div>
      </section>

      <section className="gs-how">
        <div className="gs-how__card">
          <p className="gs-how__label">How it works</p>
          <div className="gs-how__steps">
            <div className="gs-step">
              <div className="gs-step__icon">1</div>
              <h2 className="gs-step__title">Post a report</h2>
              <p className="gs-step__body">
                Share what was lost or found, where it was last seen, and any
                details that will help others recognize it.
              </p>
            </div>
            <div className="gs-step">
              <div className="gs-step__icon">2</div>
              <h2 className="gs-step__title">Match &amp; message</h2>
              <p className="gs-step__body">
                We surface potential matches so students and staff can safely
                connect and confirm ownership through the platform.
              </p>
            </div>
            <div className="gs-step">
              <div className="gs-step__icon">3</div>
              <h2 className="gs-step__title">Verify &amp; pick up</h2>
              <p className="gs-step__body">
                Once the owner is verified, meet at the agreed pickup spot and
                close the report with a successful match.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="gs-actions" aria-label="Create a lost or found report">
        <Link className="gs-cta gs-cta--lost" to="/items/new/lost">
          Report Lost
        </Link>
        <Link className="gs-cta gs-cta--found" to="/items/new/found">
          Report Found
        </Link>
      </section>

    </div>
  );
}