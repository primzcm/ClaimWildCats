import { Link } from 'react-router-dom';
import './HomePage.css';

export function HomePage() {
  return (
    <section className="home-hero">
      <div className="home-hero__content">
        <span className="home-hero__eyebrow">Campus Lost &amp; Found</span>
        <h1 className="home-hero__title">Find &amp; Recover With Ease</h1>
        <p className="home-hero__subtitle">
          Experience effortless recovery with our dedicated lost and found service.
        </p>
        <div className="home-hero__cta">
          <Link to="/get-started" className="home-hero__cta-link">
            Get Started
          </Link>
        </div>
      </div>

      <div className="home-hero__visuals">
        <div className="home-hero__actions">
          <Link to="/items/new/lost" className="home-hero__action home-hero__action--lost">
            <span className="home-hero__action-text">Lost</span>
            <span className="home-hero__action-icon" aria-hidden="true">?</span>
          </Link>
          <Link to="/items/new/found" className="home-hero__action home-hero__action--found">
            <span className="home-hero__action-text">Found</span>
            <span className="home-hero__action-icon" aria-hidden="true">-&gt;</span>
          </Link>
        </div>
        <div className="home-hero__gallery" aria-hidden="true">
          <div className="home-hero__placeholder home-hero__placeholder--one" />
          <div className="home-hero__placeholder home-hero__placeholder--two" />
          <div className="home-hero__placeholder home-hero__placeholder--three" />
        </div>
      </div>
    </section>
  );
}
