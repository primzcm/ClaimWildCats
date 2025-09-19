import { NavLink, Outlet, Link } from 'react-router-dom';
import './App.css';

const primaryNav = [
  { to: '/', label: 'Home', end: true },
  { to: '/search', label: 'Search' },
  { to: '/me', label: 'Profile' },
  { to: '/me/reports', label: 'My Reports' },
];

export default function App() {
  return (
    <div className="app-shell">
      <header className="app-header">
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
          <NavLink to="/items/new/lost" className="btn btn--ghost">
            Report Lost
          </NavLink>
          <NavLink to="/items/new/found" className="btn btn--primary">
            Report Found
          </NavLink>
          <NavLink to="/auth/login" className="btn btn--outline">
            Login
          </NavLink>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
      <footer className="app-footer">
        <div className="app-footer__links">
          <Link to="/search">Help / FAQ</Link>
          <Link to="/admin">Admin Console</Link>
          <Link to="/settings">Privacy</Link>
        </div>
        <p>© {new Date().getFullYear()} ClaimWildCats Lost &amp; Found</p>
      </footer>
    </div>
  );
}
