import { useEffect } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'sonner';

export default function ProtectedRoute() {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      toast.error('Você precisa estar logado para acessar esta página');
    }
  }, [loading, isAuthenticated]);

  useEffect(() => {
    if (!loading && isAuthenticated && !isAdmin) {
      toast.error('Acesso restrito a administradores');
    }
  }, [loading, isAuthenticated, isAdmin]);

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
    return <Navigate to="/login" replace />;
  }
  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
