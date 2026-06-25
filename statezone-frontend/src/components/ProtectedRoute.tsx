import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'sonner';

export default function ProtectedRoute() {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) return null;
  if (!isAuthenticated) {
    toast.error('Você precisa estar logado para acessar esta página');
    return <Navigate to="/login" replace />;
  }
  if (!isAdmin) {
    toast.error('Acesso restrito a administradores');
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
