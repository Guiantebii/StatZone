import { ChevronRight, Trophy } from 'lucide-react';
import { getLogoUrl } from '../constants/helpers';
import type { ConfrontoEliminatorio } from '../types/fases';
import Card from './ui/Card';

interface ConfrontoCardProps {
  confronto: ConfrontoEliminatorio;
}

export default function ConfrontoCard({ confronto }: ConfrontoCardProps) {
  const isFinished = confronto.statusConfronto === 'ENCERRADO';

  const statusBadge = () => {
    switch (confronto.statusConfronto) {
      case 'ENCERRADO':
        return <span className="text-[10px] text-success font-semibold">Encerrado</span>;
      case 'EM_ANDAMENTO':
        return <span className="text-[10px] text-accent font-semibold animate-pulse">Ao vivo</span>;
      case 'PENDENTE':
        return <span className="text-[10px] text-slate-500">Pendente</span>;
      case 'PENALTIS':
        return <span className="text-[10px] text-warning font-semibold animate-pulse">Pênaltis</span>;
      default:
        return null;
    }
  };

  return (
    <Card className="p-3 w-56">
      <div className="flex items-center justify-between mb-2">
        <span className="text-[10px] text-slate-600 uppercase tracking-wider">{confronto.id}</span>
        {statusBadge()}
      </div>

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
        <div className="mt-2 pt-2 border-t border-white/[0.04] text-[10px] text-slate-600">
          {confronto.partidaIdaId && <span>Ida: #{confronto.partidaIdaId}</span>}
          {confronto.partidaIdaId && confronto.partidaVoltaId && <span> · </span>}
          {confronto.partidaVoltaId && <span>Volta: #{confronto.partidaVoltaId}</span>}
        </div>
      )}
    </Card>
  );
}
