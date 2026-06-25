import { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { ArrowLeft, Globe, MapPin, Building2, UserRound, Cake, Shirt, Users, Swords, Calendar } from 'lucide-react';
import api from '../api/client';
import type { Time } from '../types/time';
import type { Jogador } from '../types/jogador';
import type { TimeEstatisticas } from '../types/estatisticas';
import type { TimePartidas, Partida } from '../types/partida';
import Card from '../components/ui/Card';
import { SkeletonCard } from '../components/ui/Skeleton';
import { toast } from 'sonner';
import { STATUS_ENCERRADA } from '../constants/status';

interface TimeForma {
  timeId: number;
  forma: string[];
}

export default function TimeDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const isAdminContext = location.pathname.startsWith('/dashboard');
  const [time, setTime] = useState<Time | null>(null);
  const [jogadores, setJogadores] = useState<Jogador[]>([]);
  const [forma, setForma] = useState<TimeForma | null>(null);
  const [estatisticas, setEstatisticas] = useState<TimeEstatisticas | null>(null);
  const [partidas, setPartidas] = useState<TimePartidas | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      api.get(`/times/${id}`).then((r) => setTime(r.data)),
      api.get(`/times/${id}/jogadores`).then((r) => setJogadores(r.data)),
      api.get(`/times/${id}/forma`).then((r) => setForma(r.data)),
      api.get(`/times/${id}/estatisticas`).then((r) => setEstatisticas(r.data)),
      api.get(`/times/${id}/partidas`).then((r) => setPartidas(r.data)),
    ]).catch(() => toast.error('Erro ao carregar time'))
      .finally(() => setLoading(false));
  }, [id]);

  const posicaoLabel = (p: string) => {
    const map: Record<string, string> = {
      GOLEIRO: 'Goleiro', ZAGUEIRO: 'Zagueiro', LATERAL_DIREITO: 'Lateral Direito',
      LATERAL_ESQUERDO: 'Lateral Esquerdo', VOLANTE: 'Volante', MEIO_CAMPO: 'Meio-Campo',
      PONTA_DIREITA: 'Ponta Direita', PONTA_ESQUERDA: 'Ponta Esquerda',
      MEIA_ATACANTE: 'Meia-Atacante', CENTROAVANTE: 'Centroavante',
    };
    return map[p] || p;
  };

  const getLogoUrl = (nome: string, size = 80) =>
    `https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=1a3460&color=FFD700&size=${size}&bold=true`;

  if (loading) return (
    <div className="space-y-6">
      <div className="h-6 w-32 rounded-lg animate-shimmer" />
      <div className="glass rounded-2xl p-6">
        <div className="flex items-center gap-4">
          <div className="w-20 h-20 rounded-full animate-shimmer" />
          <div className="space-y-2">
            <div className="h-6 w-40 rounded animate-shimmer" />
            <div className="h-4 w-24 rounded animate-shimmer" />
          </div>
        </div>
      </div>
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)}
      </div>
    </div>
  );

  if (!time) return null;

  return (
    <div className="space-y-6 animate-fade-in-up">
      <button onClick={() => navigate(isAdminContext ? '/dashboard/times' : '/times')} className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-200 transition-colors">
        <ArrowLeft size={15} />
        Voltar para times
      </button>

      {/* Team Header */}
      <Card className="p-6">
        <div className="flex flex-col sm:flex-row items-center sm:items-start gap-5">
          <img
            src={time.escudoUrl || getLogoUrl(time.nome)}
            alt={time.nome}
            className="w-20 h-20 rounded-2xl bg-white/5 ring-2 ring-white/[0.06] object-contain"
          />
          <div className="flex-1 text-center sm:text-left">
            <div className="flex items-center gap-2 justify-center sm:justify-start">
              <h1 className="text-2xl font-bold text-slate-100">{time.nome}</h1>
              <span className="text-xs text-accent font-bold bg-accent/10 px-2 py-0.5 rounded">{time.sigla}</span>
              {time.tipo && (
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${time.tipo === 'SELECAO' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-blue-500/10 text-blue-400'}`}>
                  {time.tipo === 'SELECAO' ? 'SELEÇÃO' : 'CLUBE'}
                </span>
              )}
            </div>
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-x-4 gap-y-1 mt-3 text-xs text-slate-500">
              <span className="flex items-center gap-1"><Globe size={12} /> {time.pais}</span>
              {time.cidade && <span className="flex items-center gap-1"><MapPin size={12} /> {time.cidade}</span>}
              {time.estadio && <span className="flex items-center gap-1"><Building2 size={12} /> {time.estadio}</span>}
              {time.tecnico && <span className="flex items-center gap-1"><UserRound size={12} /> {time.tecnico}</span>}
              {time.fundadoEm && (
                <span className="flex items-center gap-1"><Cake size={12} /> Fundado em {new Date(time.fundadoEm).getFullYear()}</span>
              )}
            </div>
          </div>
        </div>
      </Card>

      {/* Form */}
      {forma && forma.forma.length > 0 && (
        <Card className="p-4">
          <div className="flex items-center gap-2 mb-3">
            <Swords size={14} className="text-slate-500" />
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Últimas partidas</span>
          </div>
          <div className="flex gap-2">
            {forma.forma.map((r, i) => (
              <div
                key={i}
                className={`w-8 h-8 rounded-lg flex items-center justify-center text-xs font-extrabold font-mono ${
                  r === 'V' ? 'bg-success-bg text-success' :
                  r === 'E' ? 'bg-warning-bg text-warning' :
                  'bg-danger-bg text-danger'
                }`}
              >
                {r}
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatBox label="Jogadores" value={jogadores.length} />
        <StatBox label="Partidas" value={estatisticas?.partidas ?? '-'} />
        <StatBox label="Vitórias" value={estatisticas?.vitorias ?? '-'} />
        <StatBox label="Gols marcados" value={estatisticas?.golsMarcados ?? '-'} />
      </div>

      {/* Partidas */}
      {partidas && (partidas.ultimasPartidas.length > 0 || partidas.proximasPartidas.length > 0) && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {partidas.ultimasPartidas.length > 0 && (
            <Card className="p-4">
              <div className="flex items-center gap-2 mb-3">
                <Swords size={14} className="text-slate-500" />
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Últimas partidas</span>
              </div>
              <div className="space-y-2">
                {partidas.ultimasPartidas.map((p) => (
                  <PartidaRow key={p.id} partida={p} timeId={Number(id)} />
                ))}
              </div>
            </Card>
          )}
          {partidas.proximasPartidas.length > 0 && (
            <Card className="p-4">
              <div className="flex items-center gap-2 mb-3">
                <Calendar size={14} className="text-slate-500" />
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Próximas partidas</span>
              </div>
              <div className="space-y-2">
                {partidas.proximasPartidas.map((p) => (
                  <PartidaRow key={p.id} partida={p} timeId={Number(id)} />
                ))}
              </div>
            </Card>
          )}
        </div>
      )}

      {/* Players */}
      <Card className="overflow-hidden">
        <div className="flex items-center gap-2 px-5 py-3.5 border-b border-white/[0.04]">
          <Users size={14} className="text-slate-500" />
          <span className="text-sm font-semibold text-slate-200">Elenco ({jogadores.length})</span>
        </div>
        {jogadores.length === 0 ? (
          <p className="text-sm text-slate-500 text-center py-10">Nenhum jogador no elenco</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3 p-4">
            {jogadores.map((j) => (
              <Link key={j.id} to={`/jogadores/${j.id}`} className="flex items-center gap-3 p-3 rounded-xl bg-white/[0.02] border border-white/[0.04] hover:bg-white/[0.04] transition-colors">
                <img
                  src={j.fotoUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(j.nome)}&background=1B5E20&color=fff&size=40`}
                  alt={j.nome}
                  className="w-10 h-10 rounded-full bg-white/5 ring-2 ring-white/[0.06] object-cover"
                />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-slate-200 truncate">{j.nome}</p>
                  <div className="flex items-center gap-2 text-[10px] text-slate-500">
                    <span className="flex items-center gap-1"><Shirt size={10} /> {j.numeroCamisa || '-'}</span>
                    <span>·</span>
                    <span>{posicaoLabel(j.posicao)}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}

function PartidaRow({ partida, timeId }: { partida: Partida; timeId: number }) {
  const navigate = useNavigate();
  const mandante = partida.timeMandanteId === timeId;
  const isFinished = (STATUS_ENCERRADA as readonly string[]).includes(partida.status);

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });

  const formatTime = (dateStr: string) =>
    new Date(dateStr).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

  const getLogoUrl = (nome: string) =>
    `https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=1a3460&color=FFD700&size=24&bold=true`;

  return (
    <button
      onClick={() => navigate(`/partidas/${partida.id}`)}
      className="w-full flex items-center gap-2 p-2 rounded-xl hover:bg-white/[0.04] transition-colors text-left"
    >
      <span className="text-[10px] text-slate-600 w-5 font-mono text-right">{partida.campeonatoNome?.charAt(0)}</span>
      <img src={getLogoUrl(mandante ? partida.timeVisitanteNome : partida.timeMandanteNome)} alt={mandante ? partida.timeVisitanteNome : partida.timeMandanteNome} className="w-5 h-5 rounded-full bg-white/5 shrink-0" />
      <span className="text-xs text-slate-400 flex-1 truncate">
        {mandante ? partida.timeVisitanteNome : partida.timeMandanteNome}
      </span>
      {isFinished ? (
        <span className={`text-xs font-bold font-mono ${mandante ? (partida.golsMandante > partida.golsVisitante ? 'text-success' : partida.golsMandante < partida.golsVisitante ? 'text-danger' : 'text-slate-400') : (partida.golsVisitante > partida.golsMandante ? 'text-success' : partida.golsVisitante < partida.golsMandante ? 'text-danger' : 'text-slate-400')}`}>
          {mandante ? `${partida.golsMandante}-${partida.golsVisitante}` : `${partida.golsVisitante}-${partida.golsMandante}`}
        </span>
      ) : (
        <span className="text-[10px] text-slate-600 whitespace-nowrap">{formatDate(partida.dataPartida)}</span>
      )}
    </button>
  );
}

function StatBox({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <Card className="p-4 text-center">
      <div className="text-lg font-extrabold text-slate-100 font-mono">{value}</div>
      <div className="text-[10px] text-slate-500 uppercase tracking-wider mt-1">{label}</div>
    </Card>
  );
}
