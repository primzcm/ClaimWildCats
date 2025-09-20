import { useRef, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { signInWithEmailAndPassword, signInWithPopup } from 'firebase/auth';
import { auth, googleProvider } from '../lib/firebase';
import './LoginPage.css';

export function LoginPage() {
  const navigate = useNavigate();
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
    if (err.code === 'auth/user-not-found' || err.code === 'auth/wrong-password') {
      return 'Check your email and password and try again.';
    }
    if (err.code === 'auth/invalid-email') {
      return 'Enter a valid email address.';
    }
    if (err.code === 'auth/network-request-failed') {
      return 'Network error. Check your connection and retry.';
    }
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
      navigate('/me');
    } catch (err) {
      setError(surfaceError(err));
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    if (loading) return;
    setLoading(true);
    setError('');
    try {
      if (!googleProvider) {
        throw new Error('Google sign-in is not configured.');
      }
      await signInWithPopup(auth, googleProvider);
      resetForm();
      navigate('/me');
    } catch (err) {
      setError(surfaceError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-shell">
      <div className="auth-card">
        <h1>Log in</h1>
        <p className="auth-subtitle">Access saved reports, manage claims, and receive alerts.</p>

        <form className="auth-form" onSubmit={handleEmailSignIn} ref={formRef}>
          <label className="auth-label">
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              required
            />
          </label>
          <label className="auth-label">
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />
          </label>
          <button type="submit" className="auth-submit" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <div className="auth-divider">
          <span />
          <p>or</p>
          <span />
        </div>

        <button type="button" className="auth-google" onClick={handleGoogleSignIn} disabled={loading}>
          Continue with Google
        </button>

        {error && <p className="auth-error">{error}</p>}

        <p className="auth-footer">
          Need an account? <Link to="/auth/register">Register instead</Link>
        </p>
      </div>
    </section>
  );
}
