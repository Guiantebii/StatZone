import { useEffect, useState, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Trophy, Shield, Users, Calendar, Plus, ArrowRight, Medal } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import { getLogoUrl, getAvatarUrl } from '../constants/helpers';
import type { Campeonato } from '../types/campeonato';
import type { Time } from '../types/time';
import type { Jogador } from '../types/jogador';
import type { Partida } from '../types/partida';
import type { Artilharia } from '../types/estatisticas';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import { ARTILHARIA_TOP } from '../constants/pagination';
import { SkeletonCard } from '../components/ui/Skeleton';
import { toast } from 'sonner';
import { isLiveStatus, isFinishedStatus } from '../constants/status';
import { usePolling } from '../hooks/usePolling';
import logger from '../utils/logger';

export default function DashboardPage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState({ campeonatos: 0, times: 0, jogadores: 0, partidas: 0 });
  const [aoVivo, setAoVivo] = useState<Partida[]>([]);
  const [recentes, setRecentes] = useState<Partida[]>([]);
  const [artilharia, setArtilharia] = useState<Artilharia[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [campeonatosRes, timesRes, jogadoresRes, partidasRes] = await Promise.all([
          api.get<Campeonato[]>('/campeonatos'),
          api.get<Time[]>('/times'),
          api.get<Jogador[]>('/jogadores'),
          api.get<Partida[]>('/partidas'),
        ]);

        setStats({
          campeonatos: campeonatosRes.data.length,
          times: timesRes.data.length,
          jogadores: jogadoresRes.data.length,
          partidas: partidasRes.data.length,
        });

        const todas = partidasRes.data;
        setAoVivo(todas.filter((p) => isLiveStatus(p.status)));
        setRecentes(
          todas
            .filter((p) => isFinishedStatus(p.status))
            .sort((a, b) => new Date(b.dataPartida).getTime() - new Date(a.dataPartida).getTime())
            .slice(0, 5),
        );

        if (campeonatosRes.data.length > 0) {
          const artRes = await api.get(
            `/campeonatos/${campeonatosRes.data[0].id}/artilharia?pagina=0&tamanho=${ARTILHARIA_TOP}`,
          );
          setArtilharia(Array.isArray(artRes.data) ? artRes.data : []);
        }
      } catch (err) {
        toast.error(getApiError(err, 'Erro ao carregar dados do dashboard'));
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const pollPartidas = useCallback(() => {
    api
      .get<Partida[]>('/partidas')
      .then((res) => {
        const todas = res.data;
        setAoVivo(todas.filter((p) => isLiveStatus(p.status)));
        setRecentes(
          todas
            .filter((p) => isFinishedStatus(p.status))
            .sort((a, b) => new Date(b.dataPartida).getTime() - new Date(a.dataPartida).getTime())
            .slice(0, 5),
        );
      })
      .catch((err) => {
        logger.error('Erro ao atualizar partidas ao vivo', err);
      });
  }, []);

  usePolling(pollPartidas, 15000);

  if (loading)
    return (
      <div className="space-y-6">
        <div className="h-8 w-56 rounded-lg animate-shimmer" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
        <div className="h-48 rounded-2xl animate-shimmer" />
      </div>
    );

  return (
    <div className="space-y-6 animate-fade-in-up">
      <div>
        <h1 className="text-2xl font-bold text-slate-100 tracking-tight">Dashboard</h1>
        <p className="text-sm text-slate-500 mt-1">Visão geral da plataforma</p>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard
          icon={<Trophy size={18} />}
          label="Campeonatos"
          value={stats.campeonatos}
          color="bg-accent/10 text-accent"
          onClick={() => navigate('/dashboard/campeonatos')}
        />
        <StatCard
          icon={<Shield size={18} />}
          label="Times"
          value={stats.times}
          color="bg-info-bg text-info"
          onClick={() => navigate('/dashboard/times')}
        />
        <StatCard
          icon={<Users size={18} />}
          label="Jogadores"
          value={stats.jogadores}
          color="bg-success-bg text-success"
          onClick={() => navigate('/dashboard/jogadores')}
        />
        <StatCard
          icon={<Calendar size={18} />}
          label="Partidas"
          value={stats.partidas}
          color="bg-warning-bg text-warning"
          onClick={() => navigate('/dashboard/partidas')}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          {aoVivo.length > 0 && (
            <div>
              <div className="flex items-center gap-2 mb-3">
                <span className="w-2 h-2 rounded-full bg-success animate-pulse" />
                <h2 className="text-sm font-semibold text-slate-200 uppercase tracking-wider">Ao Vivo</h2>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {aoVivo.map((p) => (
                  <LiveMatchCard key={p.id} partida={p} onClick={() => navigate(`/dashboard/partidas/${p.id}`)} />
                ))}
              </div>
            </div>
          )}

          <div>
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-sm font-semibold text-slate-200 uppercase tracking-wider">Últimos resultados</h2>
              <button
                onClick={() => navigate('/dashboard/partidas')}
                className="text-xs text-accent hover:text-accent-hover flex items-center gap-1"
              >
                Ver todas <ArrowRight size={12} />
              </button>
            </div>
            {recentes.length === 0 ? (
              <Card className="p-6 text-center">
                <p className="text-sm text-slate-500">Nenhuma partida encerrada ainda</p>
              </Card>
            ) : (
              <div className="space-y-2">
                {recentes.map((p) => (
                  <RecentMatchRow key={p.id} partida={p} onClick={() => navigate(`/dashboard/partidas/${p.id}`)} />
                ))}
              </div>
            )}
          </div>

          <Card className="p-4">
            <h2 className="text-sm font-semibold text-slate-200 uppercase tracking-wider mb-3">Ações rápidas</h2>
            <div className="flex flex-wrap gap-2">
              <Button size="sm" onClick={() => navigate('/dashboard/campeonatos')}>
                <Plus size={13} /> Novo campeonato
              </Button>
              <Button size="sm" onClick={() => navigate('/dashboard/times')}>
                <Plus size={13} /> Novo time
              </Button>
              <Button size="sm" onClick={() => navigate('/dashboard/jogadores')}>
                <Plus size={13} /> Novo jogador
              </Button>
              <Button size="sm" onClick={() => navigate('/dashboard/partidas')}>
                <Plus size={13} /> Nova partida
              </Button>
            </div>
          </Card>
        </div>

        <div className="space-y-4">
          {artilharia.length > 0 && (
            <Card className="overflow-hidden">
              <div className="flex items-center gap-2 px-5 py-3.5 border-b border-white/[0.04]">
                <Medal size={14} className="text-accent" />
                <span className="text-sm font-semibold text-slate-200">Artilheiros</span>
              </div>
              <div className="divide-y divide-white/[0.03]">
                {artilharia.map((a) => (
                  <Link
                    key={a.jogadorId}
                    to={`/jogadores/${a.jogadorId}`}
                    className="flex items-center gap-3 px-5 py-3 hover:bg-white/[0.02] transition-colors"
                  >
                    <span
                      className={`text-xs font-bold font-mono w-5 ${a.posicao <= 3 ? 'text-accent' : 'text-slate-600'}`}
                    >
                      {a.posicao}
                    </span>
                    <img
                      src={getAvatarUrl(a.nomeJogador, 24)}
                      alt={a.nomeJogador}
                      className="w-7 h-7 rounded-full bg-white/5"
                    />
                    <div className="flex-1 min-w-0">
                      <p className="text-xs font-medium text-slate-200 truncate">{a.nomeJogador}</p>
                      <p className="text-[10px] text-slate-500 truncate">{a.nomeTime}</p>
                    </div>
                    <span className="text-sm font-bold text-accent font-mono">{a.gols}</span>
                  </Link>
                ))}
              </div>
            </Card>
          )}

          <Card className="p-4">
            <h2 className="text-sm font-semibold text-slate-200 uppercase tracking-wider mb-3">Plataforma</h2>
            <div className="space-y-3">
              <ProgressItem label="Campeonatos" current={stats.campeonatos} total={Math.max(stats.campeonatos, 10)} />
              <ProgressItem label="Times" current={stats.times} total={Math.max(stats.times, 20)} />
              <ProgressItem label="Jogadores" current={stats.jogadores} total={Math.max(stats.jogadores, 50)} />
              <ProgressItem label="Partidas realizadas" current={stats.partidas} total={Math.max(stats.partidas, 30)} />
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}

function StatCard({
  icon,
  label,
  value,
  color,
  onClick,
}: {
  icon: React.ReactNode;
  label: string;
  value: number;
  color: string;
  onClick: () => void;
}) {
  return (
    <button onClick={onClick} className="text-left w-full">
      <Card hover className="p-4 flex items-center gap-4">
        <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${color}`}>{icon}</div>
        <div>
          <p className="text-xs text-slate-500">{label}</p>
          <p className="text-xl font-extrabold text-slate-100 font-mono">{value}</p>
        </div>
      </Card>
    </button>
  );
}

function LiveMatchCard({ partida, onClick }: { partida: Partida; onClick: () => void }) {
  return (
    <button onClick={onClick} className="w-full text-left">
      <Card hover className="p-4">
        <div className="flex items-center gap-2 mb-2">
          <span className="w-1.5 h-1.5 rounded-full bg-success animate-pulse" />
          <span className="text-[10px] font-bold text-success uppercase tracking-wider">AO VIVO</span>
          <span className="text-[10px] text-slate-600 ml-auto">{partida.campeonatoNome}</span>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex-1 flex items-center gap-2 justify-end">
            <span className="text-xs font-medium text-slate-200">{partida.timeMandanteNome}</span>
            <img
              src={getLogoUrl(partida.timeMandanteNome)}
              alt={partida.timeMandanteNome}
              className="w-7 h-7 rounded-full bg-white/5"
            />
          </div>
          <span className="text-lg font-extrabold text-accent font-mono">
            {partida.golsMandante} - {partida.golsVisitante}
          </span>
          <div className="flex-1 flex items-center gap-2">
            <img
              src={getLogoUrl(partida.timeVisitanteNome)}
              alt={partida.timeVisitanteNome}
              className="w-7 h-7 rounded-full bg-white/5"
            />
            <span className="text-xs font-medium text-slate-200">{partida.timeVisitanteNome}</span>
          </div>
        </div>
      </Card>
    </button>
  );
}

function RecentMatchRow({ partida, onClick }: { partida: Partida; onClick: () => void }) {
  const winner =
    partida.golsMandante > partida.golsVisitante
      ? 'mandante'
      : partida.golsVisitante > partida.golsMandante
        ? 'visitante'
        : null;

  return (
    <button onClick={onClick} className="w-full text-left">
      <Card className="p-3 hover:border-accent/20 transition-colors">
        <div className="flex items-center gap-3">
          <div className="flex-1 flex items-center gap-2 justify-end">
            <span
              className={`text-xs font-medium ${winner === 'mandante' ? 'text-accent font-bold' : 'text-slate-400'}`}
            >
              {partida.timeMandanteNome}
            </span>
            <img
              src={getLogoUrl(partida.timeMandanteNome)}
              alt={partida.timeMandanteNome}
              className="w-6 h-6 rounded-full bg-white/5"
            />
          </div>
          <span className="text-sm font-bold text-slate-100 font-mono">
            {partida.golsMandante} - {partida.golsVisitante}
          </span>
          <div className="flex-1 flex items-center gap-2">
            <img
              src={getLogoUrl(partida.timeVisitanteNome)}
              alt={partida.timeVisitanteNome}
              className="w-6 h-6 rounded-full bg-white/5"
            />
            <span
              className={`text-xs font-medium ${winner === 'visitante' ? 'text-accent font-bold' : 'text-slate-400'}`}
            >
              {partida.timeVisitanteNome}
            </span>
          </div>
          <span className="text-[10px] text-slate-600 w-16 text-right">{partida.rodada}ª rod.</span>
        </div>
      </Card>
    </button>
  );
}

function ProgressItem({ label, current, total }: { label: string; current: number; total: number }) {
  const pct = Math.min((current / total) * 100, 100);
  return (
    <div>
      <div className="flex justify-between text-xs mb-1">
        <span className="text-slate-400">{label}</span>
        <span className="text-slate-500 font-mono">{current}</span>
      </div>
      <div className="h-1.5 rounded-full bg-white/[0.06] overflow-hidden">
        <div
          className="h-full rounded-full bg-gradient-to-r from-accent to-accent-hover transition-all"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}
