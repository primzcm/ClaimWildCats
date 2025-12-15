import { createContext, useContext, useEffect, useState } from 'react';

export const AuthContext = createContext({ 
  user: null, 
  loading: true,
  login: (userData) => {},
  logout: () => {}
});

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
   
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        console.error("Failed to parse user data", e);
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

 
  const login = (userData) => {
  // Map backend field names to frontend
  const mappedUser = {
    userId: userData.userId, // map userId → id
    fullName: userData.name, // map name → fullName
    email: userData.email,
    role: userData.role,
  };
  localStorage.setItem('user', JSON.stringify(mappedUser));
  setUser(mappedUser);
};

  const logout = () => {
    localStorage.removeItem('user');
    setUser(null);
    window.location.href = '/auth/login';
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}