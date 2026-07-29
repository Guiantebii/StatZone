import { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Calendar,
  Clock,
  MapPin,
  User,
  Trophy,
  Play as PlayIcon,
  Square,
  Pause,
  Ban as BanIcon,
  Users,
  Trash2,
  CircleDot,
  ArrowLeftRight,
  Shield,
  ShieldAlert,
  Eye,
  CheckCircle,
  Circle,
  Flag,
} from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import { getLogoUrl, getAvatarUrl } from '../constants/helpers';
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
import { STATUS_PARTIDA, STATUS_LABEL, isLiveStatus, isFinishedStatus } from '../constants/status';

type Tab = 'timeline' | 'escalacao' | 'estatisticas';

const statusLabel = STATUS_LABEL;

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

  const loadTimeline = async () => {
    if (!id) return;
    try {
      const res = await api.get(`/partidas/${id}/timeline`);
      setTimeline(res.data);
    } catch (err) {
      console.error('Erro ao carregar timeline', err);
    }
  };

  const loadEscalacao = async () => {
    if (!id) return;
    try {
      const res = await api.get(`/partidas/${id}/escalacao`);
      setEscalacao(res.data);
    } catch (err) {
      console.error('Erro ao carregar escalação', err);
    }
  };

  const loadEstatisticas = async () => {
    if (!id) return;
    try {
      const res = await api.get(`/estatisticas/${id}`);
      setEstatisticas(res.data);
    } catch (err) {
      console.error('Erro ao carregar estatísticas', err);
    }
  };

  useEffect(() => {
    if (!id) return;
    let isMounted = true;
    api
      .get(`/partidas/${id}`)
      .then((pRes) => {
        if (!isMounted) return;
        setPartida(pRes.data);
        Promise.all([
          api
            .get(`/partidas/${id}/timeline`)
            .then((r) => {
              if (isMounted) setTimeline(r.data);
            })
            .catch(() => undefined),
          api
            .get(`/partidas/${id}/escalacao`)
            .then((r) => {
              if (isMounted) setEscalacao(r.data);
            })
            .catch(() => undefined),
          api
            .get(`/estatisticas/${id}`)
            .then((r) => {
              if (isMounted) setEstatisticas(r.data);
            })
            .catch(() => undefined),
        ]);
      })
      .catch((err) => {
        toast.error(getApiError(err, 'Erro ao carregar partida'));
        navigate(isAdminContext ? '/dashboard/partidas' : '/partidas');
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });
    return () => {
      isMounted = false;
    };
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleWsUpdate = useCallback((data: unknown) => {
    setPartida(data as Partida);
  }, []);

  const handleWsEvent = useCallback((data: unknown) => {
    const event = data as EventoTimeline;
    setTimeline((prev) => {
      if (prev.some((e) => e.id === event.id)) return prev;
      return [...prev, event].sort((a, b) => {
        const tempoOrder =
          a.tempo === b.tempo ? 0 : a.tempo === 'PRIMEIRO_TEMPO' ? -1 : a.tempo === 'SEGUNDO_TEMPO' ? 1 : 0;
        if (tempoOrder !== 0) return tempoOrder;
        return a.minuto - b.minuto || (a.minutoExtra ?? 0) - (b.minutoExtra ?? 0);
      });
    });
  }, []);

  usePartidaWebSocket(id ? Number(id) : undefined, handleWsUpdate, handleWsEvent);

  const pollingRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!id) return;
    if (!partida || !isLiveStatus(partida.status)) return;

    const poll = async () => {
      await Promise.all([loadTimeline(), loadEstatisticas()]);
      pollingRef.current = setTimeout(poll, 10000);
    };
    pollingRef.current = setTimeout(poll, 10000);

    return () => {
      if (pollingRef.current) {
        clearTimeout(pollingRef.current);
        pollingRef.current = null;
      }
    };
  }, [id, partida?.status]); // eslint-disable-line react-hooks/exhaustive-deps

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
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const isLive = partida ? isLiveStatus(partida.status) : false;
  const isScheduled = partida?.status === STATUS_PARTIDA.AGENDADA;
  const isFinished = partida ? isFinishedStatus(partida.status) : false;

  const EVENT_CONFIG: Record<string, { icon: React.ReactNode; label: string; badgeClass: string }> = {
    GOL: { icon: <CircleDot size={16} />, label: 'Gol', badgeClass: 'bg-accent/15 text-accent border-accent/30' },
    PENALTI_GOL: {
      icon: <CircleDot size={16} />,
      label: 'Pênalti',
      badgeClass: 'bg-accent/15 text-accent border-accent/30',
    },
    GOL_CONTRA: {
      icon: <ArrowLeftRight size={16} />,
      label: 'Gol contra',
      badgeClass: 'bg-danger/15 text-danger border-danger/30',
    },
    CARTAO_AMARELO: {
      icon: <Shield size={16} />,
      label: 'Cartão amarelo',
      badgeClass: 'bg-yellow-500/15 text-yellow-400 border-yellow-500/30',
    },
    CARTAO_VERMELHO: {
      icon: <ShieldAlert size={16} />,
      label: 'Cartão vermelho',
      badgeClass: 'bg-danger/15 text-danger border-danger/30',
    },
    SUBSTITUICAO: {
      icon: <ArrowLeftRight size={16} />,
      label: 'Substituição',
      badgeClass: 'bg-slate-500/15 text-slate-400 border-slate-500/30',
    },
    PENALTI_PERDIDO: {
      icon: <BanIcon size={16} />,
      label: 'Pênalti perdido',
      badgeClass: 'bg-danger/15 text-danger border-danger/30',
    },
    PENALTI_DEFENDIDO: {
      icon: <Eye size={16} />,
      label: 'Pênalti defendido',
      badgeClass: 'bg-info/15 text-info border-info/30',
    },
    VAR_GOL_ANULADO: {
      icon: <CheckCircle size={16} />,
      label: 'Gol anulado',
      badgeClass: 'bg-danger/15 text-danger border-danger/30',
    },
    VAR_GOL_CONFIRMADO: {
      icon: <CheckCircle size={16} />,
      label: 'Gol confirmado',
      badgeClass: 'bg-success/15 text-success border-success/30',
    },
    FIM_PRIMEIRO_TEMPO: {
      icon: <Pause size={16} />,
      label: 'Intervalo',
      badgeClass: 'bg-warning/15 text-warning border-warning/30',
    },
    INICIO_SEGUNDO_TEMPO: {
      icon: <PlayIcon size={16} />,
      label: '2º tempo',
      badgeClass: 'bg-info/15 text-info border-info/30',
    },
    FIM_PARTIDA: {
      icon: <Flag size={16} />,
      label: 'Fim de jogo',
      badgeClass: 'bg-slate-500/15 text-slate-400 border-slate-500/30',
    },
  };

  const eventConfig = (tipo: string) =>
    EVENT_CONFIG[tipo] || {
      icon: <Circle size={16} />,
      label: tipo,
      badgeClass: 'bg-slate-500/15 text-slate-400 border-slate-500/30',
    };

  if (loading)
    return (
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
      <button
        onClick={() => navigate(isAdminContext ? '/dashboard/partidas' : '/partidas')}
        className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-200 transition-colors"
      >
        <ArrowLeft size={15} />
        Voltar para partidas
      </button>

      <Card className="p-6 md:p-8">
        <div className="flex items-center justify-center gap-4 md:gap-10">
          <Link
            to={`${isAdminContext ? '/dashboard' : ''}/times/${partida.timeMandanteId}`}
            className="flex flex-col items-center gap-2 flex-1 text-right hover:opacity-80 transition-opacity"
          >
            <img
              src={getLogoUrl(partida.timeMandanteNome)}
              alt={partida.timeMandanteNome}
              className="w-12 h-12 md:w-16 md:h-16 rounded-full bg-white/5 ring-2 ring-white/[0.06]"
            />
            <span
              className={`text-sm md:text-base font-bold ${isLive && partida.golsMandante > partida.golsVisitante ? 'text-accent' : 'text-slate-100'}`}
            >
              {partida.timeMandanteNome}
            </span>
          </Link>

          <div className="flex flex-col items-center gap-2">
            {isLive || isFinished ? (
              <div className="flex items-center gap-3">
                <span
                  className={`text-3xl md:text-5xl font-extrabold font-mono ${isLive && partida.golsMandante > partida.golsVisitante ? 'text-accent' : 'text-slate-100'}`}
                >
                  {partida.golsMandante}
                </span>
                <span className="text-2xl md:text-4xl text-slate-600 font-extralight">:</span>
                <span
                  className={`text-3xl md:text-5xl font-extrabold font-mono ${isLive && partida.golsVisitante > partida.golsMandante ? 'text-accent' : 'text-slate-100'}`}
                >
                  {partida.golsVisitante}
                </span>
              </div>
            ) : (
              <div className="text-center">
                <Calendar className="text-slate-500 mx-auto mb-1" size={20} />
                <span className="text-xs text-slate-500">{formatDate(partida.dataPartida)}</span>
              </div>
            )}
            <span
              className={`text-xs font-bold uppercase tracking-wider px-3 py-1 rounded-full ${
                isLive
                  ? 'bg-success/20 text-success border border-success/30 animate-pulse'
                  : isFinished
                    ? 'bg-slate-500/20 text-slate-400 border border-slate-500/30'
                    : 'bg-info/20 text-info border border-info/30'
              }`}
            >
              {statusLabel[partida.status] || partida.status}
            </span>
          </div>

          <Link
            to={`${isAdminContext ? '/dashboard' : ''}/times/${partida.timeVisitanteId}`}
            className="flex flex-col items-center gap-2 flex-1 hover:opacity-80 transition-opacity"
          >
            <img
              src={getLogoUrl(partida.timeVisitanteNome)}
              alt={partida.timeVisitanteNome}
              className="w-12 h-12 md:w-16 md:h-16 rounded-full bg-white/5 ring-2 ring-white/[0.06]"
            />
            <span
              className={`text-sm md:text-base font-bold ${isLive && partida.golsVisitante > partida.golsMandante ? 'text-accent' : 'text-slate-100'}`}
            >
              {partida.timeVisitanteNome}
            </span>
          </Link>
        </div>

        <div className="flex items-center justify-center gap-4 mt-6 text-xs text-slate-500">
          <span className="flex items-center gap-1">
            <Trophy size={12} /> {partida.campeonatoNome}
          </span>
          <span className="text-slate-700">·</span>
          <span className="flex items-center gap-1">
            <MapPin size={12} /> {partida.estadio}
          </span>
          <span className="text-slate-700">·</span>
          <span className="flex items-center gap-1">
            <User size={12} /> {partida.arbitro}
          </span>
          <span className="text-slate-700">·</span>
          <span className="flex items-center gap-1">
            <Clock size={12} /> {partida.rodada}ª rodada
          </span>
        </div>

        {isAdmin && (
          <div className="flex justify-center gap-2 mt-6 flex-wrap">
            <Button size="sm" variant="secondary" onClick={() => setShowEscalacaoForm(true)}>
              <Users size={14} /> Escalação
            </Button>
            {isScheduled && (
              <Button size="sm" onClick={() => handleAction('iniciar')} disabled={actionLoading}>
                <PlayIcon size={14} /> Iniciar
              </Button>
            )}
            {partida.status === STATUS_PARTIDA.AO_VIVO && (
              <>
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => handleAction('intervalo')}
                  disabled={actionLoading}
                >
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
                <PlayIcon size={14} /> Segundo tempo
              </Button>
            )}
            {isScheduled && (
              <>
                <Button size="sm" variant="secondary" onClick={() => handleAction('adiar')} disabled={actionLoading}>
                  <Clock size={14} /> Adiar
                </Button>
                <Button size="sm" variant="danger" onClick={() => handleAction('cancelar')} disabled={actionLoading}>
                  <BanIcon size={14} /> Cancelar
                </Button>
              </>
            )}
            <div className="w-px h-6 bg-white/[0.06] self-center" />
            <Button size="sm" variant="danger" onClick={handleDelete}>
              <Trash2 size={14} /> Excluir
            </Button>
          </div>
        )}
      </Card>

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

      {tab === 'timeline' && (
        <Card className="p-5">
          <h3 className="text-sm font-semibold text-slate-200 mb-4">Timeline da partida</h3>
          {timeline.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-8">Nenhum evento registrado</p>
          ) : (
            <div className="relative space-y-0">
              {timeline
                .filter((e) => !['INICIO_PRIMEIRO_TEMPO', 'FIM_PARTIDA'].includes(e.tipo))
                .map((event, idx) => {
                  const cfg = eventConfig(event.tipo);
                  const isGoal = ['GOL', 'PENALTI_GOL'].includes(event.tipo);
                  return (
                    <div
                      key={event.id}
                      className={`flex gap-3 py-2 group ${isGoal ? 'bg-accent/[0.03] -mx-5 px-5 rounded-lg' : ''}`}
                    >
                      <div className="flex flex-col items-center">
                        <div
                          className={`w-2 h-2 rounded-full mt-1.5 ${
                            ['GOL', 'PENALTI_GOL'].includes(event.tipo)
                              ? 'bg-accent'
                              : ['CARTAO_VERMELHO'].includes(event.tipo)
                                ? 'bg-danger'
                                : ['CARTAO_AMARELO'].includes(event.tipo)
                                  ? 'bg-warning'
                                  : 'bg-slate-600'
                          }`}
                        />
                        {idx < timeline.length - 1 && <div className="w-px flex-1 bg-white/[0.04]" />}
                      </div>
                      <div className="flex-1 pb-3">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-xs font-mono font-bold text-slate-500 w-8 shrink-0">
                            {event.minuto}
                            {event.minutoExtra ? `+${event.minutoExtra}` : ''}'
                          </span>
                          <span
                            className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${cfg.badgeClass}`}
                          >
                            {cfg.icon}
                            {cfg.label}
                          </span>
                          <span className="text-sm text-slate-200 font-medium">{event.jogador}</span>
                          {event.jogadorSecundario && (
                            <span className="text-xs text-slate-500">({event.jogadorSecundario})</span>
                          )}
                        </div>
                        {event.nomeTime && (
                          <span className="text-[10px] text-slate-600 ml-[4.5rem]">{event.nomeTime}</span>
                        )}
                      </div>
                    </div>
                  );
                })}
            </div>
          )}
        </Card>
      )}

      {tab === 'escalacao' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {[partida.timeMandanteId, partida.timeVisitanteId].map((timeId, idx) => {
            const timeNome = idx === 0 ? partida.timeMandanteNome : partida.timeVisitanteNome;
            const formacao = idx === 0 ? partida.formacaoMandante : partida.formacaoVisitante;
            const titulares = escalacao?.titulares?.filter((e) => e.nomeTime === timeNome) || [];
            const reservas = escalacao?.reservas?.filter((e) => e.nomeTime === timeNome) || [];
            return (
              <Card key={timeId} className="p-5">
                <Link
                  to={`${isAdminContext ? '/dashboard' : ''}/times/${timeId}`}
                  className="flex items-center justify-center gap-2 mb-4 hover:opacity-80 transition-opacity"
                >
                  <img src={getLogoUrl(timeNome)} alt={timeNome} className="w-8 h-8 rounded-full bg-white/5" />
                  <h3 className="text-sm font-bold text-slate-200">{timeNome}</h3>
                </Link>
                {titulares.length === 0 && reservas.length === 0 ? (
                  <p className="text-sm text-slate-500 text-center py-6">Nenhum jogador escalado</p>
                ) : (
                  <>
                    {titulares.length > 0 && <FormationView titulares={titulares} formacao={formacao} />}
                    {reservas.length > 0 && (
                      <div className="mt-3 pt-3 border-t border-white/[0.06]">
                        <p className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-2">
                          Reservas
                        </p>
                        <div className="flex flex-wrap gap-1.5">
                          {reservas.map((j) => (
                            <div
                              key={j.id}
                              className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-white/[0.04]"
                            >
                              <span className="text-[10px] font-mono text-slate-500">{j.numeroCamisa}</span>
                              <img
                                src={j.fotoUrl || getAvatarUrl(j.nomeJogador, 16, '333', 'fff')}
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
              <StatBar
                label="Posse de bola"
                home={estatisticas.posseBolaMandante}
                away={estatisticas.posseBolaVisitante}
                suffix="%"
              />
              <StatBar
                label="Finalizações"
                home={estatisticas.finalizacoesMandante}
                away={estatisticas.finalizacoesVisitante}
              />
              <StatBar
                label="Finalizações no gol"
                home={estatisticas.finalizacoesGolMandante}
                away={estatisticas.finalizacoesGolVisitante}
              />
              <StatBar label="Faltas" home={estatisticas.faltasMandante} away={estatisticas.faltasVisitante} />
              <StatBar
                label="Escanteios"
                home={estatisticas.escanteiosMandante}
                away={estatisticas.escanteiosVisitante}
              />
              <StatBar
                label="Cartões amarelos"
                home={estatisticas.cartoesAmarelosMandante}
                away={estatisticas.cartoesAmarelosVisitante}
              />
              <StatBar
                label="Cartões vermelhos"
                home={estatisticas.cartoesVermelhosMandante}
                away={estatisticas.cartoesVermelhosVisitante}
              />
              <StatBar label="Defesas" home={estatisticas.defesasMandante} away={estatisticas.defesasVisitante} />
            </div>
          )}
        </Card>
      )}

      {showEventForm && (
        <EventoForm
          partidaId={partida.id}
          onClose={() => setShowEventForm(false)}
          onSaved={() => {
            setShowEventForm(false);
            loadTimeline();
          }}
        />
      )}

      {showEscalacaoForm && (
        <EscalacaoForm
          partidaId={partida.id}
          timeMandanteId={partida.timeMandanteId}
          timeVisitanteId={partida.timeVisitanteId}
          timeMandanteNome={partida.timeMandanteNome}
          timeVisitanteNome={partida.timeVisitanteNome}
          formacaoMandante={partida.formacaoMandante}
          formacaoVisitante={partida.formacaoVisitante}
          onClose={() => setShowEscalacaoForm(false)}
          onSaved={() => {
            setShowEscalacaoForm(false);
            loadEscalacao();
            api
              .get(`/partidas/${partida.id}`)
              .then((r) => setPartida(r.data))
              .catch(() => undefined);
          }}
        />
      )}

      {deleteTarget && (
        <ConfirmModal
          title="Excluir partida"
          message={`Tem certeza que deseja excluir "${partida?.timeMandanteNome ?? '...'} x ${partida?.timeVisitanteNome ?? '...'}"?`}
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
        <span className="font-semibold text-slate-200">
          {home}
          {suffix}
        </span>
        <span className="text-slate-400 text-[10px] uppercase tracking-wider">{label}</span>
        <span className="font-semibold text-slate-200">
          {away}
          {suffix}
        </span>
      </div>
      <div className="flex gap-0.5 h-1.5">
        <div className="rounded-l-full bg-accent/70 transition-all" style={{ width: `${homePct}%` }} />
        <div className="rounded-r-full bg-info/70 transition-all" style={{ width: `${awayPct}%` }} />
      </div>
    </div>
  );
}
