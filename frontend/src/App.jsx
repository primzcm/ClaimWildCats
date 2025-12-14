import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import './App.css';
import { useAuth } from './context/AuthContext';
import logo from './icons/logo.png';

const primaryNav = [
  { to: '/home', label: 'Home', end: true },
  { to: '/get-started', label: 'Get Started' },
  { to: '/lost', label: 'Lost' },
  { to: '/found', label: 'Found' },
];

export default function App() {
  const { user, loading, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleSignOut = () => {
    logout(); 
    navigate('/', { replace: true });
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#f8f0e5] text-brown">
      
      {/* HEADER */}
      <header className="bg-burgundyDeep text-cream shadow-soft">
        <div className="mx-auto flex max-w-6xl items-center gap-6 px-6 py-3">
          
          {/* LOGO */}
          <Link
            to="/"
            className="group flex items-center gap-2 rounded-full pr-2 text-lg font-extrabold uppercase tracking-[0.16em] text-white transition hover:text-gold"
          >
            <img
              src={logo}
              alt="ClaimWildCats"
              className="h-14 w-auto"
            />
            <span className="sr-only">ClaimWildCats</span>
          </Link>

          {/* NAVIGATION */}
          <nav className="flex flex-1 items-center justify-center gap-6">
            {primaryNav.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  [
                    'rounded-full px-4 py-2 text-xs font-semibold uppercase tracking-[0.18em] transition',
                    isActive
                      ? 'bg-white/10 text-gold shadow-sm'
                      : 'text-white/80 hover:bg-white/10 hover:text-white',
                  ].join(' ')
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          {/* USER ACTIONS */}
          <div className="flex items-center gap-3">
            {!loading && user ? (
              <>
               
                <Link 
                  to="/me" 
                  className="inline-flex items-center gap-3 rounded-full bg-white/15 px-3 py-2 text-sm font-semibold text-white backdrop-blur hover:bg-white/25 transition"
                >
                  {user.photoURL ? (
                    <img
                      src={user.photoURL}
                      alt="User"
                      className="h-8 w-8 rounded-full object-cover ring-2 ring-white/30"
                    />
                  ) : null}
                  
                  <span className="max-w-[12ch] truncate">
                    {user.fullName || user.full_name || user.displayName || user.email}
                  </span>
                </Link>

                <button
                  type="button"
                  className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-burgundy shadow-sm transition hover:-translate-y-0.5 hover:shadow-card focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white/70"
                  onClick={handleSignOut}
                >
                  Log out
                </button>
              </>
            ) : (
              <NavLink
                to="/auth/login"
                state={{ from: location }}
                className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-burgundy shadow-sm transition hover:-translate-y-0.5 hover:shadow-card focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white/70"
              >
                Log in
              </NavLink>
            )}
          </div>
        </div>
      </header>

      {/* MAIN CONTENT */}
      <main className="flex-1">
        <div className="mx-auto max-w-6xl px-4 pt-12 pb-0 sm:px-6 lg:px-8 lg:pt-16">
          <Outlet />
        </div>
      </main>

      {/* FOOTER */}
      <footer className="bg-burgundyDeep text-cream">
        <div className="mx-auto flex max-w-6xl flex-col items-center gap-3 px-6 py-6 text-center text-sm">
          <div className="flex flex-wrap items-center justify-center gap-4 font-semibold">
            <Link to="/faq" className="hover:text-gold">
              Help / FAQ
            </Link>
            <Link to="/admin" className="hover:text-gold">Admin Console</Link>
            <Link to="/settings" className="hover:text-gold">Privacy</Link>
          </div>
          <p className="text-white/80">(c) {new Date().getFullYear()} ClaimWildCats Lost &amp; Found</p>
          <small className="text-white/60">7VVJ+QFR, Natalio B. Bacalso Ave, Cebu City, 6000 Cebu</small>
        </div>
      </footer>
    </div>
  );
}