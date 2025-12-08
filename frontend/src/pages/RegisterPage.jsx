import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createUserWithEmailAndPassword, updateProfile } from 'firebase/auth';
import { auth } from '../lib/firebase';
import { api } from '../api/client';
import './LoginPage.css';

export function RegisterPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState([]); // array for multiple errors
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const validateUsername = (username) => /^[a-zA-Z0-9 ]+$/.test(username);

  const validatePassword = (password) =>
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9])([^\s]{8,})$/.test(password);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (loading) return;

    const newErrors = [];

    // Username validation
    const trimmedUsername = username.trim();
    if (!trimmedUsername) newErrors.push('Choose a username to continue.');
    else if (!validateUsername(trimmedUsername))
      newErrors.push('Username can only contain letters, numbers, and spaces.');

    // Email validation
    if (!email.toLowerCase().endsWith('@cit.edu'))
      newErrors.push('Enter a valid institutional email.');

    // Password validation
    if (!validatePassword(password))
      newErrors.push(
        'Password must be at least 8 characters long, include uppercase and lowercase letters, a number, a special character, and no spaces.'
      );

    // Password match
    if (password !== confirmPassword) newErrors.push('Passwords do not match.');

    if (newErrors.length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    setErrors([]);
    setSuccess('');

    try {
      const credential = await createUserWithEmailAndPassword(auth, email, password);
      await updateProfile(credential.user, { displayName: trimmedUsername });

      // Persist username via API (best-effort)
      try {
        await api('/api/users/me/profile', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: trimmedUsername }),
        });
      } catch (profileError) {
        console.warn('Could not persist user profile', profileError);
      }

      setSuccess('Account created! You can now log in.');
      setUsername('');
      setEmail('');
      setPassword('');
      setConfirmPassword('');

      setTimeout(() => navigate('/auth/login'), 1500);
    } catch (err) {
      setErrors([err?.message ?? 'Unable to create account.']);
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
            Username
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              placeholder="wildcat123"
              required
            />
          </label>

          <label className="auth-label">
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              placeholder="wildcat@cit.edu"
              required
            />
          </label>

          <label className="auth-label">
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
              placeholder="At least 8 chars, mix letters, number & symbol"
              required
            />
          </label>

          <label className="auth-label">
            Confirm password
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
              required
            />
          </label>

          <button type="submit" className="auth-submit" disabled={loading}>
            {loading ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        {/* Display all errors with numbering */}
        {errors.length > 0 && (
          <div className="auth-error">
            {errors.map((err, idx) => (
              <p key={idx} style={{ marginBottom: '0.5em' }}>
                {idx + 1}. {err}
              </p>
            ))}
          </div>
        )}

        {success && <p className="auth-success">{success}</p>}

        <p className="auth-footer">
          Have an account? <Link to="/auth/login">Log in</Link>
        </p>
      </div>
    </section>
  );
}
