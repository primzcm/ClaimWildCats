import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createUserWithEmailAndPassword, sendEmailVerification, updateProfile } from 'firebase/auth';
import { auth } from '../lib/firebase';
import './LoginPage.css';

export function RegisterPage() {
  const navigate = useNavigate();
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [roleCode, setRoleCode] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (loading) return;
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const credential = await createUserWithEmailAndPassword(auth, email, password);
      if (displayName) {
        await updateProfile(credential.user, { displayName });
      }
      try {
        await sendEmailVerification(credential.user);
      } catch (verificationError) {
        console.warn('Could not send verification email', verificationError);
      }

      // TODO: persist roleCode in Firestore once roles are implemented.
      setSuccess('Account created! Check your email for a verification link, then log in.');
      setDisplayName('');
      setEmail('');
      setPassword('');
      setConfirmPassword('');
      setRoleCode('');
      setTimeout(() => navigate('/auth/login'), 1500);
    } catch (err) {
      setError(err?.message ?? 'Unable to create account.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-shell">
      <div className="auth-card">
        <h1>Register</h1>
        <p className="auth-subtitle">Create an account to manage reports, claims, and notifications.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label className="auth-label">
            Full name
            <input
              type="text"
              value={displayName}
              onChange={(event) => setDisplayName(event.target.value)}
              autoComplete="name"
              placeholder="Jordan Wildcat"
              required
            />
          </label>
          <label className="auth-label">
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              placeholder="wildcat@campus.edu"
              required
            />
          </label>
          <label className="auth-label">
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="new-password"
              required
            />
          </label>
          <label className="auth-label">
            Confirm password
            <input
              type="password"
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              autoComplete="new-password"
              required
            />
          </label>
          <label className="auth-label">
            Role code <span className="auth-label__hint">(optional)</span>
            <input
              type="text"
              value={roleCode}
              onChange={(event) => setRoleCode(event.target.value.trim())}
              placeholder="e.g. ADMIN-2025"
            />
          </label>
          <button type="submit" className="auth-submit" disabled={loading}>
            {loading ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        {error && <p className="auth-error">{error}</p>}
        {success && <p className="auth-success">{success}</p>}

        <p className="auth-footer">
          Have an account? <Link to="/auth/login">Log in</Link>
        </p>
      </div>
    </section>
  );
}
