/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import api from '../api/client';
import { clearToken } from '../api/tokenManager';

interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  userEmail: string | null;
  loading: boolean;
  login: (email: string, senha: string) => Promise<{ token: string | null; isAdmin: boolean }>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  // Client no longer stores token locally; server provides httpOnly cookie.
  const [token, setToken] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [userEmail, setUserEmail] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get('/api/auth/me')
      .then((res) => {
        const data = res.data as { email: string; role: string };
        setUserEmail(data.email);
        setIsAdmin(data.role === 'ADMIN');
        setIsAuthenticated(true);
      })
      .catch(() => {
        setIsAuthenticated(false);
        setIsAdmin(false);
        setUserEmail(null);
        clearToken();
      })
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (email: string, senha: string) => {
    // Server will set httpOnly cookie; call /me to retrieve user info
    await api.post('/api/auth/login', { email, senha });
    const res = await api.get('/api/auth/me');
    const data = res.data as { email: string; role: string };
    setUserEmail(data.email);
    const admin = data.role === 'ADMIN';
    setIsAdmin(admin);
    setIsAuthenticated(true);
    return { token: null, isAdmin: admin };
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.post('/api/auth/logout');
    } catch {}
    // Clear client-side references
    setToken(null);
    clearToken();
    setIsAuthenticated(false);
    setIsAdmin(false);
    setUserEmail(null);
  }, []);

  return (
    <AuthContext.Provider value={{ token, isAuthenticated, isAdmin, userEmail, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
