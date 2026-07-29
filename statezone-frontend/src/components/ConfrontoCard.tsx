import { ChevronRight, Trophy, XCircle } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { getLogoUrl } from '../constants/helpers';
import type { ConfrontoEliminatorio } from '../types/fases';
import { useAuth } from '../context/AuthContext';
import Card from './ui/Card';
import Button from './ui/Button';

interface ConfrontoCardProps {
  confronto: ConfrontoEliminatorio;
  onEncerrar?: (confrontoId: number) => void;
}

export default function ConfrontoCard({ confronto, onEncerrar }: ConfrontoCardProps) {
  const { isAdmin, isAuthenticated } = useAuth();
  const location = useLocation();
  const isDashboardContext = location.pathname.startsWith('/dashboard');
  const partidaBasePath = isDashboardContext ? '/dashboard/partidas' : '/partidas';
  const isFinished = confronto.statusConfronto === 'ENCERRADO';

  const statusBadge = () => {
    switch (confronto.statusConfronto) {
      case 'ENCERRADO':
        return <span className="text-[10px] text-success font-semibold">Encerrado</span>;
      case 'EM_ANDAMENTO':
        return <span className="text-[10px] text-accent font-semibold animate-pulse">Ao vivo</span>;
      case 'PENDENTE':
        return <span className="text-[10px] text-slate-500">Pendente</span>;
      case 'AGUARDANDO_PENALTIS':
        return <span className="text-[10px] text-warning font-semibold animate-pulse">Aguardando pênaltis</span>;
      case 'PENALTIS':
        return <span className="text-[10px] text-warning font-semibold animate-pulse">Pênaltis</span>;
      default:
        return null;
    }
  };

  return (
    <Card className="p-3 w-56">
      <div className="flex items-center justify-between mb-2">{statusBadge()}</div>

      <div className="space-y-1">
        <div
          className={`flex items-center gap-2 py-1.5 px-2 rounded-lg ${isFinished && confronto.timeClassificado?.id === confronto.timeA?.id ? 'bg-success/5 ring-1 ring-success/20' : 'bg-white/[0.02]'}`}
        >
          {confronto.timeA ? (
            <>
              <img
                src={getLogoUrl(confronto.timeA.nome)}
                alt={confronto.timeA.nome}
                className="w-5 h-5 rounded-full bg-white/5"
              />
              <span
                className={`text-xs flex-1 truncate ${isFinished && confronto.timeClassificado?.id === confronto.timeA?.id ? 'text-accent font-bold' : 'text-slate-300'}`}
              >
                {confronto.timeA.nome}
              </span>
            </>
          ) : (
            <span className="text-xs text-slate-600 flex-1 italic">A definir</span>
          )}
          {isFinished && confronto.timeClassificado?.id === confronto.timeA?.id && (
            <ChevronRight size={12} className="text-accent" />
          )}
        </div>

        <div
          className={`flex items-center gap-2 py-1.5 px-2 rounded-lg ${isFinished && confronto.timeClassificado?.id === confronto.timeB?.id ? 'bg-success/5 ring-1 ring-success/20' : 'bg-white/[0.02]'}`}
        >
          {confronto.timeB ? (
            <>
              <img
                src={getLogoUrl(confronto.timeB.nome)}
                alt={confronto.timeB.nome}
                className="w-5 h-5 rounded-full bg-white/5"
              />
              <span
                className={`text-xs flex-1 truncate ${isFinished && confronto.timeClassificado?.id === confronto.timeB?.id ? 'text-accent font-bold' : 'text-slate-300'}`}
              >
                {confronto.timeB.nome}
              </span>
            </>
          ) : (
            <span className="text-xs text-slate-600 flex-1 italic">A definir</span>
          )}
          {isFinished && confronto.timeClassificado?.id === confronto.timeB?.id && (
            <ChevronRight size={12} className="text-accent" />
          )}
        </div>
      </div>

      {isFinished && confronto.timeClassificado && (
        <div className="mt-2 pt-2 border-t border-white/[0.04] text-[10px] text-success font-semibold flex items-center gap-1">
          <Trophy size={10} />
          {confronto.timeClassificado.nome} classificado
        </div>
      )}

      {(confronto.partidaIdaId || confronto.partidaVoltaId) && (
        <div className="mt-2 pt-2 border-t border-white/[0.04] text-[10px] text-slate-600 space-y-0.5">
          {confronto.partidaIdaId && (
            <Link
              to={`${partidaBasePath}/${confronto.partidaIdaId}`}
              className="block hover:text-accent transition-colors"
            >
              Ida: #{confronto.partidaIdaId}
            </Link>
          )}
          {confronto.partidaVoltaId && (
            <Link
              to={`${partidaBasePath}/${confronto.partidaVoltaId}`}
              className="block hover:text-accent transition-colors"
            >
              Volta: #{confronto.partidaVoltaId}
            </Link>
          )}
        </div>
      )}

      {isAuthenticated && isAdmin && !isFinished && onEncerrar && (
        <div className="mt-2 pt-2 border-t border-white/[0.04]">
          <Button size="sm" variant="secondary" onClick={() => onEncerrar(confronto.id)} className="w-full text-[10px]">
            <XCircle size={10} />
            Encerrar confronto
          </Button>
        </div>
      )}
    </Card>
  );
}
