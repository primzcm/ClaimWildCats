import { useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; // Import the new AuthContext
import './LoginPage.css';

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth(); // Get the login function from context
  
  // Default to '/me' (Dashboard) if no previous page was requested
  const redirectTo = location.state?.from ?? '/me'; 
  
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

  const handleEmailSignIn = async (event) => {
    event.preventDefault();
    if (loading) return;
    
    setLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Invalid email or password.');
      }
      console.log("Login Successful:", data);
            login(data);

      resetForm();
      
      navigate(redirectTo, { replace: true });

    } catch (err) {
      console.error(err);
      setError('Invalid email or password. Please try again.');
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
              autoComplete="current-password"
              required
            />
          </label>
          <button type="submit" className="auth-submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        {error && <p className="auth-error">{error}</p>}

        <p className="auth-footer">
          Need an account? <Link to="/auth/register">Register instead</Link>
        </p>
      </div>
    </section>
  );
}