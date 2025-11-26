import { NavLink, Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { auth } from './lib/firebase';
import './App.css';

const primaryNav = [
  { to: '/home', label: 'Home', end: true },
  { to: '/get-started', label: 'Get Started' },
  { to: '/lost', label: 'Lost' },
  { to: '/found', label: 'Found' },
];

export default function App() {
  const { user, loading } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  // const handleSignOut = async () => {
  //   await auth.signOut();
  // };

  const handleSignOut = async () => {
    await auth.signOut();
    navigate('/', { replace: true });  // ← redirect to landing
  };

  // Otherwise → render the normal app layout
  return (
    <div className="app">
      <header className="app-header">
        <div className="app-header__inner">
          <Link to="/" className="app-logo">
            ClaimWildCats
          </Link>

          <nav className="app-nav">
            {primaryNav.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  isActive ? 'app-nav__link app-nav__link--active' : 'app-nav__link'
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="app-header__actions">
            {!loading && user ? (
              <>
                <span className="app-user-pill">
                  {user.photoURL ? (
                    <img src={user.photoURL} alt={user.displayName ?? user.email ?? 'User'} />
                  ) : null}
                  <span>{user.displayName ?? user.email}</span>
                </span>
                <button type="button" className="btn btn--ghost" onClick={handleSignOut}>
                  Log out
                </button>
              </>
            ) : (
              <NavLink to="/auth/login" className="btn btn--ghost">
                Log in
              </NavLink>
            )}
          </div>
        </div>
      </header>

      <main className="app-main">
        <div className="app-content">
          <Outlet />
        </div>
      </main>

      <footer className="app-footer">
        <div className="app-footer__inner">
          <div className="app-footer__links">
            <Link to="/faq">Help / FAQ</Link>
            <Link to="/admin">Admin Console</Link>
            <Link to="/settings">Privacy</Link>
          </div>
          <p>(c) {new Date().getFullYear()} ClaimWildCats Lost &amp; Found</p>
          <small className="app-footer__meta">
            123 Campus Drive • (000) 000-0000
          </small>
        </div>
      </footer>
    </div>
  );
}
