import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'sonner';

export default function ProtectedRoute() {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-accent/30 border-t-accent rounded-full animate-spin" />
          <span className="text-xs text-slate-500">Carregando...</span>
        </div>
      </div>
    );
  }
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
