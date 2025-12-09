import { useRef, useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../lib/firebase';
import CatLogo from '../icons/CatLogo.png';
import './LoginPage.css';

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from ?? '/home';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const formRef = useRef(null);

  const resetForm = () => {
    formRef.current?.reset();
    setEmail('');
    setPassword('');
  };

  const surfaceError = (err) => {
    if (!err) return 'Something went wrong. Please try again.';
    if (err.code === 'auth/user-not-found' || err.code === 'auth/wrong-password')
      return 'Check your email and password and try again.';
    if (err.code === 'auth/invalid-email')
      return 'Enter a valid email address.';
    if (err.code === 'auth/network-request-failed')
      return 'Network error. Check your connection and retry.';
    return err.message ?? 'Unable to sign in right now.';
  };

  const handleEmailSignIn = async (event) => {
    event.preventDefault();
    if (loading) return;

    setLoading(true);
    setError('');

    try {
      await signInWithEmailAndPassword(auth, email, password);
      resetForm();
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setError(surfaceError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="login-shell">

      {/* LEFT SIDE CAT IMAGE */}
      <div className="login-cat">
        <img src={CatLogo} alt="Cat Logo" />
      </div>

      {/* RIGHT SIDE FORM */}
      <div className="login-card">
        <h1 className="login-title">LOG IN</h1>

        <form className="login-form" onSubmit={handleEmailSignIn} ref={formRef}>
          <label className="login-label">
            Email / Username
            <input
              type="text"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="username"
              required
            />
          </label>

          <label className="login-label">
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </label>

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        {error && <p className="login-error">{error}</p>}

        <p className="login-footer">
          Still don’t have an account? <Link to="/auth/register">Register here</Link>
        </p>
      </div>
    </section>
  );
}
