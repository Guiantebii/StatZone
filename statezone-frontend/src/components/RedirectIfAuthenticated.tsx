import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RedirectIfAuthenticated({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return null;
  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  return <>{children}</>;
}
