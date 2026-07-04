import { useNavigate, Link } from 'react-router-dom';
import { Clock, Calendar, Trophy } from 'lucide-react';
import { getLogoUrl } from '../constants/helpers';
import type { Partida } from '../types/partida';
import Card from './ui/Card';
import { isLiveStatus, isFinishedStatus } from '../constants/status';

interface PartidaCardProps {
  partida: Partida;
}

const statusConfig: Record<string, { label: string; className: string }> = {
  AO_VIVO: { label: 'AO VIVO', className: 'bg-success/20 text-success border border-success/30 animate-pulse' },
  INTERVALO: { label: 'INTERVALO', className: 'bg-warning/20 text-warning border border-warning/30' },
  PENALTIS: { label: 'PÊNALTIS', className: 'bg-warning/20 text-warning border border-warning/30 animate-pulse' },
  ENCERRADA: { label: 'ENCERRADA', className: 'bg-slate-500/20 text-slate-400 border border-slate-500/30' },
  AGENDADA: { label: 'AGENDADA', className: 'bg-info/20 text-info border border-info/30' },
  ADIADA: { label: 'ADIADA', className: 'bg-danger/20 text-danger border border-danger/30' },
  CANCELADA: { label: 'CANCELADA', className: 'bg-danger/20 text-danger border border-danger/30' },
  WO_MANDANTE: { label: 'W.O. MANDANTE', className: 'bg-danger/20 text-danger border border-danger/30' },
  WO_VISITANTE: { label: 'W.O. VISITANTE', className: 'bg-danger/20 text-danger border border-danger/30' },
};

export default function PartidaCard({ partida }: PartidaCardProps) {
  const navigate = useNavigate();
  const isLive = isLiveStatus(partida.status);
  const isFinished = isFinishedStatus(partida.status);
  const showScore = isLive || isFinished;

  const statusInfo = statusConfig[partida.status] || statusConfig.AGENDADA;

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
  };

  const formatTime = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <Card hover className="p-4 cursor-pointer" onClick={() => navigate(`/partidas/${partida.id}`)}>
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-1.5 text-xs text-slate-500">
          <Trophy size={12} />
          <span>{partida.campeonatoNome}</span>
          <span className="text-slate-700">·</span>
          <span>{partida.rodada}ª rodada</span>
        </div>
        <span
          className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${statusInfo.className}`}
        >
          {statusInfo.label}
        </span>
      </div>

      <div className="flex items-center gap-3 py-2">
        <div className="flex-1 flex items-center justify-end gap-2 text-right">
          <Link
            to={`/times/${partida.timeMandanteId}`}
            onClick={(e) => e.stopPropagation()}
            className="flex items-center gap-2 hover:opacity-80 transition-opacity"
          >
            <span
              className={`text-sm font-semibold ${showScore && partida.golsMandante > partida.golsVisitante ? 'text-accent' : 'text-slate-200'}`}
            >
              {partida.timeMandanteNome}
            </span>
            <img
              src={getLogoUrl(partida.timeMandanteNome)}
              alt={partida.timeMandanteNome}
              className="w-8 h-8 rounded-full bg-white/5"
            />
          </Link>
        </div>

        <div className="flex-shrink-0 w-16 text-center">
          {showScore ? (
            <span className={`text-xl font-extrabold font-mono ${isLive ? 'text-accent' : 'text-slate-100'}`}>
              {partida.golsMandante} - {partida.golsVisitante}
            </span>
          ) : (
            <div className="flex flex-col items-center gap-0.5">
              <Calendar size={14} className="text-slate-500" />
              <span className="text-[10px] text-slate-500 font-medium leading-tight">
                {formatDate(partida.dataPartida)}
              </span>
              <span className="text-[10px] text-slate-600 leading-tight">{formatTime(partida.dataPartida)}</span>
            </div>
          )}
        </div>

        <div className="flex-1 flex items-center gap-2">
          <Link
            to={`/times/${partida.timeVisitanteId}`}
            onClick={(e) => e.stopPropagation()}
            className="flex items-center gap-2 hover:opacity-80 transition-opacity"
          >
            <img
              src={getLogoUrl(partida.timeVisitanteNome)}
              alt={partida.timeVisitanteNome}
              className="w-8 h-8 rounded-full bg-white/5"
            />
            <span
              className={`text-sm font-semibold ${showScore && partida.golsVisitante > partida.golsMandante ? 'text-accent' : 'text-slate-200'}`}
            >
              {partida.timeVisitanteNome}
            </span>
          </Link>
        </div>
      </div>

      {partida.status === 'AGENDADA' && (
        <div className="flex items-center justify-center gap-1.5 mt-2 text-[10px] text-slate-600">
          <Clock size={10} />
          <span>{partida.estadio}</span>
        </div>
      )}
    </Card>
  );
}
