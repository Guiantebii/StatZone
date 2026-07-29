import { useEffect, useRef } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'sonner';

export default function ProtectedRoute() {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  const redirectReason = useRef<string | null>(null);

  useEffect(() => {
    if (!loading && !isAuthenticated && !redirectReason.current) {
      redirectReason.current = 'not-authenticated';
      toast.error('Você precisa estar logado para acessar esta página');
    } else if (!loading && isAuthenticated && !isAdmin && !redirectReason.current) {
      redirectReason.current = 'not-admin';
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
