import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { ArrowLeft, Clock, MapPin, User, Trophy, Play, Square, Pause, Ban, Users, Trash2 } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { Partida, EventoTimeline, EscalacaoPartidaList, EstatisticasPartida } from '../types/partida';
import ConfirmModal from '../components/ui/ConfirmModal';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import { toast } from 'sonner';
import EventoForm from '../components/EventoForm';
import EscalacaoForm from '../components/EscalacaoForm';
import FormationView from '../components/FormationView';
import { usePartidaWebSocket } from '../hooks/useWebSocket';
import { useAuth } from '../context/AuthContext';
import { STATUS_PARTIDA, STATUS_AO_VIVO, STATUS_ENCERRADA } from '../constants/status';

type Tab = 'timeline' | 'escalacao' | 'estatisticas';

const statusLabel: Record<string, string> = {
  AO_VIVO: 'Ao Vivo',
  INTERVALO: 'Intervalo',
  PENALTIS: 'Pênaltis',
  ENCERRADA: 'Encerrada',
  AGENDADA: 'Agendada',
  ADIADA: 'Adiada',
  CANCELADA: 'Cancelada',
  WO_MANDANTE: 'W.O. Mandante',
  WO_VISITANTE: 'W.O. Visitante',
};

export default function PartidaDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const isAdminContext = location.pathname.startsWith('/dashboard');
  const [partida, setPartida] = useState<Partida | null>(null);
  const [timeline, setTimeline] = useState<EventoTimeline[]>([]);
  const [escalacao, setEscalacao] = useState<EscalacaoPartidaList | null>(null);
  const [estatisticas, setEstatisticas] = useState<EstatisticasPartida | null>(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<Tab>('timeline');
  const [showEventForm, setShowEventForm] = useState(false);
  const [showEscalacaoForm, setShowEscalacaoForm] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const { isAdmin } = useAuth();
  const [deleteTarget, setDeleteTarget] = useState<boolean>(false);

  const load = async () => {
    try {
      const pRes = await api.get(`/partidas/${id}`);
      setPartida(pRes.data);
      await Promise.all([
        loadTimeline(),
        loadEscalacao(),
        loadEstatisticas(),
      ]);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao carregar partida'));
      navigate(isAdminContext ? '/dashboard/partidas' : '/partidas');
    } finally {
      setLoading(false);
    }
  };

  const loadTimeline = async () => {
    if (!id) return;
    try {
      const res = await api.get(`/partidas/${id}/timeline`);
      setTimeline(res.data);
    } catch { /* timeline may not exist yet */ }
  };

  const loadEscalacao = async () => {
    if (!id) return;
    try {
      const res = await api.get(`/partidas/${id}/escalacao`);
      setEscalacao(res.data);
    } catch { /* escalacao may not exist yet */ }
  };

  const loadEstatisticas = async () => {
    if (!id) return;
    try {
      const res = await api.get(`/estatisticas/${id}`);
      setEstatisticas(res.data);
    } catch { /* estatisticas may not exist yet */ }
  };

  useEffect(() => { if (id) load(); }, [id]);

  const handleWsUpdate = useCallback((data: unknown) => {
    setPartida(data as Partida);
  }, []);

  const handleWsEvent = useCallback((data: unknown) => {
    const event = data as EventoTimeline;
    setTimeline((prev) => {
      if (prev.some((e) => e.id === event.id)) return prev;
      return [...prev, event].sort((a, b) => a.minuto - b.minuto || (a.minutoExtra ?? 0) - (b.minutoExtra ?? 0));
    });
  }, []);

  usePartidaWebSocket(id ? Number(id) : undefined, handleWsUpdate, handleWsEvent);

  useEffect(() => {
    if (!id) return;
    const p = partida;
    if (!p || !(STATUS_AO_VIVO as readonly string[]).includes(p.status)) return;
    const interval = setInterval(() => {
      loadTimeline();
      loadEstatisticas();
    }, 10000);
    return () => clearInterval(interval);
  }, [id, partida?.status]);

  const handleAction = async (action: string, body?: unknown) => {
    setActionLoading(true);
    try {
      const res = await api.post(`/partidas/${id}/${action}`, body || {});
      setPartida(res.data);
      toast.success(`Partida ${statusLabel[res.data.status] || action}`);
      loadTimeline();
      loadEstatisticas();
    } catch (err) {
      toast.error(getApiError(err, `Erro ao ${action} partida`));
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = () => {
    setDeleteTarget(true);
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('pt-BR', {
      day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit',
    });
  };

  const getLogoUrl = (nome: string) =>
    `https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=1a3460&color=FFD700&size=64&bold=true`;

  const isLive = partida ? (STATUS_AO_VIVO as readonly string[]).includes(partida.status) : false;
  const isScheduled = partida?.status === STATUS_PARTIDA.AGENDADA;
  const isFinished = partida ? (STATUS_ENCERRADA as readonly string[]).includes(partida.status) : false;

  const eventIcon = (tipo: string) => {
    switch (tipo) {
      case 'GOL': case 'PENALTI_GOL': return '⚽';
      case 'GOL_CONTRA': return '⚽🔄';
      case 'CARTAO_AMARELO': return '🟨';
      case 'CARTAO_VERMELHO': return '🟥';
      case 'SUBSTITUICAO': return '🔄';
      case 'PENALTI_PERDIDO': return '❌';
      case 'PENALTI_DEFENDIDO': return '🧤';
      case 'VAR_GOL_ANULADO': return '📺❌';
      case 'VAR_GOL_CONFIRMADO': return '📺✅';
      case 'FIM_PRIMEIRO_TEMPO': return '⏸️';
      case 'INICIO_SEGUNDO_TEMPO': return '▶️';
      case 'FIM_PARTIDA': return '🏁';
      default: return '•';
    }
  };

  if (loading) return (
    <div className="space-y-6">
      <div className="h-8 w-32 rounded-lg animate-shimmer" />
      <div className="glass rounded-2xl p-8">
        <div className="flex justify-center items-center gap-8 mb-6">
          <div className="flex flex-col items-center gap-2">
            <div className="w-16 h-16 rounded-full animate-shimmer" />
            <div className="h-4 w-24 rounded animate-shimmer" />
          </div>
          <div className="h-12 w-20 rounded-lg animate-shimmer" />
          <div className="flex flex-col items-center gap-2">
            <div className="w-16 h-16 rounded-full animate-shimmer" />
            <div className="h-4 w-24 rounded animate-shimmer" />
          </div>
        </div>
      </div>
    </div>
  );

  if (!partida) return null;

  return (
    <div className="space-y-6 animate-fade-in-up">
      {/* Back button */}
      <button onClick={() => navigate(isAdminContext ? '/dashboard/partidas' : '/partidas')} className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-200 transition-colors">
        <ArrowLeft size={15} />
        Voltar para partidas
      </button>

      {/* Header - Teams and Score */}
      <Card className="p-6 md:p-8">
        <div className="flex items-center justify-center gap-4 md:gap-10">
          {/* Home team */}
          <Link to={`/times/${partida.timeMandanteId}`} className="flex flex-col items-center gap-2 flex-1 text-right hover:opacity-80 transition-opacity">
            <img src={getLogoUrl(partida.timeMandanteNome)} alt={partida.timeMandanteNome} className="w-12 h-12 md:w-16 md:h-16 rounded-full bg-white/5 ring-2 ring-white/[0.06]" />
            <span className={`text-sm md:text-base font-bold ${isLive && partida.golsMandante > partida.golsVisitante ? 'text-accent' : 'text-slate-100'}`}>
              {partida.timeMandanteNome}
            </span>
          </Link>

          {/* Score / Status */}
          <div className="flex flex-col items-center gap-2">
            {(isLive || isFinished) ? (
              <div className="flex items-center gap-3">
                <span className={`text-3xl md:text-5xl font-extrabold font-mono ${isLive && partida.golsMandante > partida.golsVisitante ? 'text-accent' : 'text-slate-100'}`}>
                  {partida.golsMandante}
                </span>
                <span className="text-2xl md:text-4xl text-slate-600 font-extralight">:</span>
                <span className={`text-3xl md:text-5xl font-extrabold font-mono ${isLive && partida.golsVisitante > partida.golsMandante ? 'text-accent' : 'text-slate-100'}`}>
                  {partida.golsVisitante}
                </span>
              </div>
            ) : (
              <div className="text-center">
                <CalendarIcon className="text-slate-500 mx-auto mb-1" size={20} />
                <span className="text-xs text-slate-500">{formatDate(partida.dataPartida)}</span>
              </div>
            )}
            <span className={`text-xs font-bold uppercase tracking-wider px-3 py-1 rounded-full ${
              isLive ? 'bg-success/20 text-success border border-success/30 animate-pulse' :
              isFinished ? 'bg-slate-500/20 text-slate-400 border border-slate-500/30' :
              'bg-info/20 text-info border border-info/30'
            }`}>
              {statusLabel[partida.status] || partida.status}
            </span>
          </div>

          {/* Away team */}
          <Link to={`/times/${partida.timeVisitanteId}`} className="flex flex-col items-center gap-2 flex-1 hover:opacity-80 transition-opacity">
            <img src={getLogoUrl(partida.timeVisitanteNome)} alt={partida.timeVisitanteNome} className="w-12 h-12 md:w-16 md:h-16 rounded-full bg-white/5 ring-2 ring-white/[0.06]" />
            <span className={`text-sm md:text-base font-bold ${isLive && partida.golsVisitante > partida.golsMandante ? 'text-accent' : 'text-slate-100'}`}>
              {partida.timeVisitanteNome}
            </span>
          </Link>
        </div>

        {/* Match info */}
        <div className="flex items-center justify-center gap-4 mt-6 text-xs text-slate-500">
          <span className="flex items-center gap-1"><Trophy size={12} /> {partida.campeonatoNome}</span>
          <span className="text-slate-700">·</span>
          <span className="flex items-center gap-1"><MapPin size={12} /> {partida.estadio}</span>
          <span className="text-slate-700">·</span>
          <span className="flex items-center gap-1"><User size={12} /> {partida.arbitro}</span>
          <span className="text-slate-700">·</span>
          <span className="flex items-center gap-1"><Clock size={12} /> {partida.rodada}ª rodada</span>
        </div>

        {/* Action buttons (admin only) */}
        {isAdmin && <div className="flex justify-center gap-2 mt-6 flex-wrap">
          <Button size="sm" variant="secondary" onClick={() => setShowEscalacaoForm(true)}>
            <Users size={14} /> Escalação
          </Button>
          {isScheduled && (
            <Button size="sm" onClick={() => handleAction('iniciar')} disabled={actionLoading}>
              <Play size={14} /> Iniciar
            </Button>
          )}
          {partida.status === STATUS_PARTIDA.AO_VIVO && (
            <>
              <Button size="sm" variant="secondary" onClick={() => handleAction('intervalo')} disabled={actionLoading}>
                <Pause size={14} /> Intervalo
              </Button>
              <Button size="sm" variant="danger" onClick={() => handleAction('encerrar')} disabled={actionLoading}>
                <Square size={14} /> Encerrar
              </Button>
              <Button size="sm" variant="secondary" onClick={() => setShowEventForm(true)} disabled={actionLoading}>
                + Evento
              </Button>
            </>
          )}
          {partida.status === STATUS_PARTIDA.INTERVALO && (
            <Button size="sm" onClick={() => handleAction('segundo-tempo')} disabled={actionLoading}>
              <Play size={14} /> Segundo tempo
            </Button>
          )}
          {isScheduled && (
            <>
              <Button size="sm" variant="secondary" onClick={() => handleAction('adiar')} disabled={actionLoading}>
                <Clock size={14} /> Adiar
              </Button>
              <Button size="sm" variant="danger" onClick={() => handleAction('cancelar')} disabled={actionLoading}>
                <Ban size={14} /> Cancelar
              </Button>
            </>
          )}
          <div className="w-px h-6 bg-white/[0.06] self-center" />
          <Button size="sm" variant="danger" onClick={handleDelete}>
            <Trash2 size={14} /> Excluir
          </Button>
        </div>}
      </Card>

      {/* Tabs */}
      <div className="flex gap-1 bg-white/[0.03] rounded-xl p-1">
        {[
          { key: 'timeline' as Tab, label: 'Timeline' },
          { key: 'escalacao' as Tab, label: 'Escalação' },
          { key: 'estatisticas' as Tab, label: 'Estatísticas' },
        ].map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`flex-1 py-2 rounded-lg text-xs font-semibold transition-all ${
              tab === t.key ? 'bg-accent/10 text-accent shadow-sm' : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {tab === 'timeline' && (
        <Card className="p-5">
          <h3 className="text-sm font-semibold text-slate-200 mb-4">Timeline da partida</h3>
          {timeline.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-8">Nenhum evento registrado</p>
          ) : (
            <div className="relative space-y-0">
              {timeline
                .filter((e) => !['INICIO_PRIMEIRO_TEMPO', 'FIM_PARTIDA'].includes(e.tipo))
                .map((event, idx) => (
                  <div key={event.id} className="flex gap-3 py-2 group">
                    <div className="flex flex-col items-center">
                      <div className={`w-2 h-2 rounded-full mt-1.5 ${
                        ['GOL', 'PENALTI_GOL'].includes(event.tipo) ? 'bg-accent' :
                        ['CARTAO_VERMELHO'].includes(event.tipo) ? 'bg-danger' :
                        ['CARTAO_AMARELO'].includes(event.tipo) ? 'bg-warning' :
                        'bg-slate-600'
                      }`} />
                      {idx < timeline.length - 1 && <div className="w-px flex-1 bg-white/[0.04]" />}
                    </div>
                    <div className="flex-1 pb-3">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-mono font-bold text-slate-500 w-8">{event.minuto}{event.minutoExtra ? `+${event.minutoExtra}` : ''}'</span>
                        <span className="text-sm">{eventIcon(event.tipo)}</span>
                        <span className="text-sm text-slate-200 font-medium">{event.jogador}</span>
                        {event.jogadorSecundario && (
                          <span className="text-xs text-slate-500">({event.jogadorSecundario})</span>
                        )}
                        {event.tipo === 'GOL_CONTRA' && <span className="text-xs text-danger font-semibold">(gol contra)</span>}
                      </div>
                      {event.nomeTime && (
                        <span className="text-[10px] text-slate-600 ml-[4.5rem]">{event.nomeTime}</span>
                      )}
                    </div>
                  </div>
                ))}
            </div>
          )}
        </Card>
      )}

      {tab === 'escalacao' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {[partida.timeMandanteId, partida.timeVisitanteId].map((timeId, idx) => {
            const timeNome = idx === 0 ? partida.timeMandanteNome : partida.timeVisitanteNome;
            const titulares = escalacao?.titulares?.filter((e) => e.nomeTime === timeNome) || [];
            const reservas = escalacao?.reservas?.filter((e) => e.nomeTime === timeNome) || [];
            return (
              <Card key={timeId} className="p-5">
                <Link to={`/times/${timeId}`} className="flex items-center gap-2 mb-4 hover:opacity-80 transition-opacity">
                  <img src={getLogoUrl(timeNome)} alt={timeNome} className="w-8 h-8 rounded-full bg-white/5" />
                  <h3 className="text-sm font-bold text-slate-200">{timeNome}</h3>
                </Link>
                {titulares.length === 0 && reservas.length === 0 ? (
                  <p className="text-sm text-slate-500 text-center py-6">Nenhum jogador escalado</p>
                ) : (
                  <>
                    {titulares.length > 0 && (
                      <FormationView titulares={titulares} timeNome={timeNome} />
                    )}
                    {reservas.length > 0 && (
                      <div className="mt-3 pt-3 border-t border-white/[0.06]">
                        <p className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-2">Reservas</p>
                        <div className="flex flex-wrap gap-1.5">
                          {reservas.map((j) => (
                            <div key={j.id} className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-white/[0.04]">
                              <span className="text-[10px] font-mono text-slate-500">{j.numeroCamisa}</span>
                              <img
                                src={j.fotoUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(j.nomeJogador)}&background=333&color=fff&size=16`}
                                alt={j.nomeJogador}
                                className="w-4 h-4 rounded-full bg-white/5"
                              />
                              <span className="text-xs text-slate-300">{j.nomeJogador}</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </>
                )}
              </Card>
            );
          })}
        </div>
      )}

      {tab === 'estatisticas' && (
        <Card className="p-5">
          <h3 className="text-sm font-semibold text-slate-200 mb-4">Estatísticas da partida</h3>
          {!estatisticas ? (
            <p className="text-sm text-slate-500 text-center py-8">Nenhuma estatística disponível</p>
          ) : (
            <div className="space-y-4">
              <StatBar label="Posse de bola" home={estatisticas.posseBolaMandante} away={estatisticas.posseBolaVisitante} suffix="%" />
              <StatBar label="Finalizações" home={estatisticas.finalizacoesMandante} away={estatisticas.finalizacoesVisitante} />
              <StatBar label="Finalizações no gol" home={estatisticas.finalizacoesGolMandante} away={estatisticas.finalizacoesGolVisitante} />
              <StatBar label="Faltas" home={estatisticas.faltasMandante} away={estatisticas.faltasVisitante} />
              <StatBar label="Escanteios" home={estatisticas.escanteiosMandante} away={estatisticas.escanteiosVisitante} />
              <StatBar label="Cartões amarelos" home={estatisticas.cartoesAmarelosMandante} away={estatisticas.cartoesAmarelosVisitante} />
              <StatBar label="Cartões vermelhos" home={estatisticas.cartoesVermelhosMandante} away={estatisticas.cartoesVermelhosVisitante} />
              <StatBar label="Defesas" home={estatisticas.defesasMandante} away={estatisticas.defesasVisitante} />
            </div>
          )}
        </Card>
      )}

      {showEventForm && (
        <EventoForm
          partidaId={partida.id}
          onClose={() => setShowEventForm(false)}
          onSaved={() => { setShowEventForm(false); loadTimeline(); }}
        />
      )}

      {showEscalacaoForm && (
        <EscalacaoForm
          partidaId={partida.id}
          timeMandanteId={partida.timeMandanteId}
          timeVisitanteId={partida.timeVisitanteId}
          timeMandanteNome={partida.timeMandanteNome}
          timeVisitanteNome={partida.timeVisitanteNome}
          onClose={() => setShowEscalacaoForm(false)}
          onSaved={() => { setShowEscalacaoForm(false); loadEscalacao(); }}
        />
      )}

      {deleteTarget && (
        <ConfirmModal
          title="Excluir partida"
          message={`Tem certeza que deseja excluir "${partida!.timeMandanteNome} x ${partida!.timeVisitanteNome}"?`}
          onConfirm={async () => {
            try {
              await api.delete(`/partidas/${id}`);
              toast.success('Partida excluída');
              navigate(isAdminContext ? '/dashboard/partidas' : '/partidas');
            } catch (err) {
              toast.error(getApiError(err, 'Erro ao excluir partida'));
              setDeleteTarget(false);
            }
          }}
          onCancel={() => setDeleteTarget(false)}
        />
      )}
    </div>
  );
}

function StatBar({ label, home, away, suffix = '' }: { label: string; home: number; away: number; suffix?: string }) {
  const total = home + away || 1;
  const homePct = (home / total) * 100;
  const awayPct = (away / total) * 100;

  return (
    <div>
      <div className="flex justify-between text-xs text-slate-500 mb-1.5">
        <span className="font-semibold text-slate-200">{home}{suffix}</span>
        <span className="text-slate-400 text-[10px] uppercase tracking-wider">{label}</span>
        <span className="font-semibold text-slate-200">{away}{suffix}</span>
      </div>
      <div className="flex gap-0.5 h-1.5">
        <div className="rounded-l-full bg-accent/70 transition-all" style={{ width: `${homePct}%` }} />
        <div className="rounded-r-full bg-info/70 transition-all" style={{ width: `${awayPct}%` }} />
      </div>
    </div>
  );
}

function CalendarIcon({ className, size }: { className?: string; size?: number }) {
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
      <line x1="16" y1="2" x2="16" y2="6" />
      <line x1="8" y1="2" x2="8" y2="6" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </svg>
  );
}
