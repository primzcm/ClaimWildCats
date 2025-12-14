import { useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import CatLogo from '../icons/CatLogo.png'; 
import './LoginPage.css';

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth(); 
  
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