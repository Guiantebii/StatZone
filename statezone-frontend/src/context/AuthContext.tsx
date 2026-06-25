import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import api from '../api/client';
import { setToken as setGlobalToken } from '../api/tokenManager';

interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  userEmail: string | null;
  loading: boolean;
  login: (email: string, senha: string) => Promise<{ token: string; isAdmin: boolean }>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function parseJwt(token: string) {
  try {
    const base64url = token.split('.')[1];
    const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [userEmail, setUserEmail] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/auth/me')
      .then((res) => {
        const data = res.data as { email: string; role: string };
        setUserEmail(data.email);
        setIsAdmin(data.role === 'ADMIN');
        setIsAuthenticated(true);
        setGlobalToken(null);
      })
      .catch(() => {
        setIsAuthenticated(false);
        setIsAdmin(false);
        setUserEmail(null);
        setGlobalToken(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (email: string, senha: string) => {
    const response = await api.post('/api/auth/login', { email, senha });
    const newToken = response.data.token;
    setToken(newToken);
    setGlobalToken(newToken);
    const payload = parseJwt(newToken);
    let admin = false;
    if (payload) {
      setUserEmail(payload.sub || null);
      const roles: string = payload.roles || '';
      admin = roles.includes('ROLE_ADMIN');
      setIsAdmin(admin);
    }
    setIsAuthenticated(true);
    return { token: newToken, isAdmin: admin };
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.post('/api/auth/logout');
    } catch {}
    setToken(null);
    setGlobalToken(null);
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
