import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createUserWithEmailAndPassword, updateProfile } from 'firebase/auth';
import { auth } from '../lib/firebase';
import { api } from '../api/client';
import CatLogo from '../icons/CatLogo.png';
import './LoginPage.css';

export function RegisterPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState([]);
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const validateUsername = (username) => /^[a-zA-Z0-9 ]+$/.test(username);
  const validatePassword = (password) =>
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9])([^\s]{8,})$/.test(password);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (loading) return;

    const newErrors = [];
    const trimmedUsername = username.trim();

    if (!trimmedUsername)
      newErrors.push("Choose a username to continue.");
    else if (!validateUsername(trimmedUsername))
      newErrors.push("Username can only contain letters, numbers, and spaces.");

    if (!email.toLowerCase().endsWith("@cit.edu"))
      newErrors.push("Enter a valid institutional email.");

    if (!validatePassword(password))
      newErrors.push(
        "Password must be at least 8 characters long, include uppercase and lowercase letters, a number, a special character, and no spaces."
      );

    if (password !== confirmPassword)
      newErrors.push("Passwords do not match.");

    if (newErrors.length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    setErrors([]);
    setSuccess('');

    try {
      const cred = await createUserWithEmailAndPassword(auth, email, password);
      await updateProfile(cred.user, { displayName: trimmedUsername });

      try {
        await api("/api/users/me/profile", {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ username: trimmedUsername }),
        });
      } catch (_) {}

      setSuccess("Account created! You may now log in.");

      setTimeout(() => navigate("/auth/login"), 1500);
    } catch (err) {
      setErrors([err?.message ?? "Unable to create account."]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="login-shell">
      
      {/* LEFT CAT IMAGE */}
      <div className="login-cat">
        <img src={CatLogo} alt="Cat Logo" />
      </div>

      {/* RIGHT FORM CARD */}
      <div className="login-card">
        <h1 className="reg-title">CREATE ACCOUNT</h1>

        <form className="login-form" onSubmit={handleSubmit}>
          
          <label className="login-label">
            Username
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Your username"
              required
            />
          </label>

          <label className="login-label">
            Institutional Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ex: juandelacruz@cit.edu"
              required
            />
          </label>

          <label className="login-label">
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>

          <label className="login-label">
            Confirm Password
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </label>

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? "Creating…" : "Create Account"}
          </button>
        </form>

        {errors.length > 0 && (
          <div className="login-error">
            <ul>
              {errors.map((err, i) => (
                <li key={i}>{err}</li>
              ))}
            </ul>
          </div>
        )}

        {success && <p className="login-success">{success}</p>}

        <p className="login-footer">
          Already have an account? <Link to="/auth/login">Log in</Link>
        </p>
      </div>
    </section>
  );
}
