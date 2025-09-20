import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './ProtectedRoute.css';

export function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="route-guard">
        <div className="route-guard__card">Checking your session…</div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/auth/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}

export function PublicOnlyRoute({ children, redirectTo = '/me' }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="route-guard">
        <div className="route-guard__card">Preparing page…</div>
      </div>
    );
  }

  if (user) {
    const target = location.state?.from ?? redirectTo;
    return <Navigate to={target} replace />;
  }

  return children;
}
