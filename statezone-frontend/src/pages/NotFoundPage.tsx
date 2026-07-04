import { Link } from 'react-router-dom';
import { Home, ArrowLeft } from 'lucide-react';
import Button from '../components/ui/Button';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-primary-dark flex items-center justify-center px-6">
      <div className="text-center max-w-md">
        <div className="text-8xl font-extrabold text-accent/20 mb-4">404</div>
        <h1 className="text-2xl font-bold text-slate-100 mb-2">Página não encontrada</h1>
        <p className="text-sm text-slate-500 mb-8">A página que você está procurando não existe ou foi removida.</p>
        <div className="flex items-center justify-center gap-3">
          <Link to="/">
            <Button variant="secondary" className="flex items-center gap-2">
              <Home size={14} /> Ir para o início
            </Button>
          </Link>
          <button
            onClick={() => window.history.back()}
            className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-200 transition-colors"
          >
            <ArrowLeft size={15} /> Voltar
          </button>
        </div>
      </div>
    </div>
  );
}
