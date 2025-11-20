import "./LandingPage.css";
import CatLogo from "../icons/CatLogo.png";
import WelcomeTeknoys from "../icons/WelcomeTeknoys.png";
import NextIcon from "../icons/next_maroon.png";
import { useNavigate } from "react-router-dom";

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="landing">
      {/* Header */}
      <header className="landing-header">
        <div className="landing-header__inner">
          <h1 className="landing-logo">ClaimWildcats</h1>
        </div>
      </header>

      {/* Main Content */}
      <main className="landing-main">
        <div className="landing-left">
          <img src={CatLogo} alt="Cat mascot" className="landing-cat" />
        </div>

        <div className="landing-right">
          <img
            src={WelcomeTeknoys}
            alt="Welcome Teknoys"
            className="landing-title"
          />

          <button
            className="landing-login-btn"
            onClick={() => navigate("/auth/login")}
          >
            Log in
            <img src={NextIcon} alt="Next" className="landing-next-icon" />
          </button>
        </div>
      </main>
    </div>
  );
}
