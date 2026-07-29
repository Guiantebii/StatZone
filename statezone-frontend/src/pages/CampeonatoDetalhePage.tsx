import { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { ArrowLeft, Trophy, Swords, Medal, Star, Calendar, Zap, RefreshCw, GitBranch, Globe } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import { PAGE_SIZE } from '../constants/pagination';
import { getAvatarUrl } from '../constants/helpers';
import ClassificacaoTable from '../components/ui/ClassificacaoTable';
import type { Campeonato } from '../types/campeonato';
import type { ClassificacaoTime, Artilharia, SelecaoCampeonato, CraqueCampeonato } from '../types/estatisticas';
import type { Partida } from '../types/partida';
import type { Grupo, FaseEliminatoria } from '../types/fases';
import Card from '../components/ui/Card';
import PartidaCard from '../components/PartidaCard';
import BracketView from '../components/BracketView';
import { SkeletonCard } from '../components/ui/Skeleton';
import { toast } from 'sonner';

type Tab = 'classificacao' | 'partidas' | 'artilharia' | 'selecao' | 'mvp' | 'chaveamento';

function buildTabs(tipoFormato: string | null): { key: Tab; label: string; icon: React.ReactNode }[] {
  const base = [
    { key: 'partidas' as Tab, label: 'Partidas', icon: <Swords size={14} /> },
    { key: 'artilharia' as Tab, label: 'Artilharia', icon: <Medal size={14} /> },
    { key: 'selecao' as Tab, label: 'Seleção', icon: <Star size={14} /> },
    { key: 'mvp' as Tab, label: 'MVP', icon: <Medal size={14} /> },
  ];
  if (tipoFormato === 'MATA_MATA') {
    return [{ key: 'chaveamento' as Tab, label: 'Chaveamento', icon: <GitBranch size={14} /> }, ...base];
  }
  if (tipoFormato === 'GRUPOS_E_MATA_MATA') {
    return [
      { key: 'classificacao' as Tab, label: 'Classificação', icon: <Trophy size={14} /> },
      { key: 'chaveamento' as Tab, label: 'Chaveamento', icon: <GitBranch size={14} /> },
      ...base,
    ];
  }
  return [{ key: 'classificacao' as Tab, label: 'Classificação', icon: <Trophy size={14} /> }, ...base];
}

export default function CampeonatoDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const isAdminContext = location.pathname.startsWith('/dashboard');
  const [campeonato, setCampeonato] = useState<Campeonato | null>(null);
  const [classificacao, setClassificacao] = useState<ClassificacaoTime[]>([]);
  const [partidas, setPartidas] = useState<Partida[]>([]);
  const [artilharia, setArtilharia] = useState<Artilharia[]>([]);
  const [selecao, setSelecao] = useState<SelecaoCampeonato[]>([]);
  const [mvp, setMvp] = useState<CraqueCampeonato | null>(null);
  const [fases, setFases] = useState<FaseEliminatoria[]>([]);
  const [grupos, setGrupos] = useState<Grupo[]>([]);
  const [grupoClassificacoes, setGrupoClassificacoes] = useState<Record<number, ClassificacaoTime[]>>({});
  const [loading, setLoading] = useState(true);
  const [reprocessing, setReprocessing] = useState(false);
  const [tab, setTab] = useState<Tab>('classificacao');

  useEffect(() => {
    if (!id) return;
    let isMounted = true;
    const load = async () => {
      try {
        const [campRes, classRes, partRes] = await Promise.all([
          api.get(`/campeonatos/${id}`),
          api.get(`/campeonatos/${id}/classificacao`),
          api.get(`/campeonatos/${id}/partidas`),
        ]);
        if (!isMounted) return;
        setCampeonato(campRes.data);
        setClassificacao(classRes.data);
        setPartidas(partRes.data);

        try {
          const [artRes, selRes, mvpRes] = await Promise.all([
            api.get(`/campeonatos/${id}/artilharia?pagina=0&tamanho=${PAGE_SIZE}`),
            api.get(`/campeonatos/${id}/selecao-do-campeonato`),
            api.get(`/campeonatos/${id}/mvp`),
          ]);
          if (isMounted) {
            setArtilharia(artRes.data);
            setSelecao(selRes.data);
            setMvp(mvpRes.data);
          }
        } catch {
          if (isMounted) {
            setArtilharia([]);
            setSelecao([]);
            setMvp(null);
          }
        }

        const camp = campRes.data as Campeonato;
        if (camp.tipoFormato === 'GRUPOS_E_MATA_MATA') {
          try {
            const gruposRes = await api.get(`/campeonatos/${id}/grupos`);
            const gs = gruposRes.data as Grupo[];
            if (isMounted) setGrupos(gs);
            const classMap: Record<number, ClassificacaoTime[]> = {};
            await Promise.all(
              gs.map(async (g) => {
                try {
                  const r = await api.get(`/campeonatos/${id}/grupos/${g.id}/classificacao`);
                  classMap[g.id] = r.data;
                } catch (err) {
                  console.error('Erro ao carregar classificação do grupo', err);
                  classMap[g.id] = [];
                }
              }),
            );
            if (isMounted) setGrupoClassificacoes(classMap);
          } catch (err) {
            console.error('Erro ao carregar grupos do campeonato', err);
          }
        }
        if (camp.tipoFormato === 'MATA_MATA' || camp.tipoFormato === 'GRUPOS_E_MATA_MATA') {
          try {
            const fasesRes = await api.get(`/campeonatos/${id}/fases`);
            if (isMounted) setFases(fasesRes.data);
          } catch (err) {
            console.error('Erro ao carregar fases do campeonato', err);
            if (isMounted) setFases([]);
          }
        }
      } catch (err) {
        if (isMounted) {
          toast.error(getApiError(err, 'Erro ao carregar campeonato'));
          navigate('/campeonatos');
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };
    load();
    return () => {
      isMounted = false;
    };
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleReprocess = async () => {
    if (!id || reprocessing) return;
    setReprocessing(true);
    try {
      await api.post(`/campeonatos/${id}/reprocessar-estatisticas`);
      toast.success('Estatísticas reprocessadas com sucesso');
      const promises = [
        api
          .get(`/campeonatos/${id}/classificacao`)
          .then((r) => setClassificacao(r.data))
          .catch(() => undefined),
        api
          .get(`/campeonatos/${id}/artilharia?pagina=0&tamanho=${PAGE_SIZE}`)
          .then((r) => setArtilharia(r.data))
          .catch(() => undefined),
        api
          .get(`/campeonatos/${id}/selecao-do-campeonato`)
          .then((r) => setSelecao(r.data))
          .catch(() => undefined),
        api
          .get(`/campeonatos/${id}/mvp`)
          .then((r) => setMvp(r.data))
          .catch(() => undefined),
      ];
      await Promise.all(promises);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao reprocessar estatísticas'));
    } finally {
      setReprocessing(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-8 w-48 rounded-lg animate-shimmer" />
        <div className="h-48 rounded-2xl animate-shimmer" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      </div>
    );
  }

  if (!campeonato) return null;

  return (
    <div className="space-y-6 animate-fade-in-up">
      <div className="space-y-3">
        <Link
          to={isAdminContext ? '/dashboard/campeonatos' : '/campeonatos'}
          className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-200 transition-colors"
        >
          <ArrowLeft size={15} />
          Todos os campeonatos
        </Link>

        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-accent/10 flex items-center justify-center text-accent shrink-0">
            <Trophy size={28} />
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold text-slate-100 tracking-tight">{campeonato.nome}</h1>
              <span
                className={`inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full ${
                  campeonato.status === 'ATIVO'
                    ? 'bg-success-bg text-success border border-success-border'
                    : 'bg-warning-bg text-warning border border-warning-border'
                }`}
              >
                {campeonato.status === 'ATIVO' ? 'Ativo' : 'Rascunho'}
              </span>
            </div>
            <p className="text-sm text-slate-500 mt-0.5">
              {campeonato.pais} · Temporada {campeonato.temporada}
            </p>
          </div>
          {isAdminContext && (
            <div className="flex items-center gap-2">
              {campeonato.status === 'RASCUNHO' ? (
                <button
                  onClick={async () => {
                    try {
                      const res = await api.patch(`/campeonatos/${id}/ativar`);
                      setCampeonato(res.data);
                      toast.success('Campeonato publicado com sucesso');
                    } catch (err) {
                      toast.error(getApiError(err, 'Erro ao publicar campeonato'));
                    }
                  }}
                  className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold bg-success-bg text-success hover:bg-success/20 transition-colors"
                >
                  <Globe size={13} />
                  Publicar
                </button>
              ) : (
                <button
                  onClick={async () => {
                    try {
                      const res = await api.patch(`/campeonatos/${id}/reverter`);
                      setCampeonato(res.data);
                      toast.success('Campeonato revertido para rascunho');
                    } catch (err) {
                      toast.error(getApiError(err, 'Erro ao reverter campeonato'));
                    }
                  }}
                  className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold bg-warning-bg text-warning hover:bg-warning/20 transition-colors"
                >
                  <Globe size={13} />
                  Reverter rascunho
                </button>
              )}
              <button
                onClick={handleReprocess}
                disabled={reprocessing}
                className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold bg-accent/10 text-accent hover:bg-accent/20 transition-colors disabled:opacity-50"
              >
                <RefreshCw size={13} className={reprocessing ? 'animate-spin' : ''} />
                {reprocessing ? 'Reprocessando...' : 'Reprocessar estatísticas'}
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="flex gap-1 bg-white/[0.03] rounded-xl p-1">
        {buildTabs(campeonato.tipoFormato || null).map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-xs font-semibold transition-all ${
              tab === t.key ? 'bg-accent/10 text-accent shadow-sm' : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            {t.icon}
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'classificacao' &&
        (campeonato.tipoFormato === 'GRUPOS_E_MATA_MATA' ? (
          <div className="space-y-6">
            {grupos.map((g) => {
              const dados = grupoClassificacoes[g.id];
              return (
                <Card key={g.id} className="overflow-hidden">
                  <div className="px-5 py-3.5 border-b border-white/[0.04]">
                    <span className="text-sm font-semibold text-slate-200">Grupo {g.nome}</span>
                  </div>
                  {!dados || dados.length === 0 ? (
                    <p className="text-sm text-slate-500 text-center py-10">Nenhum dado disponível</p>
                  ) : (
                    <ClassificacaoTable
                      dados={dados}
                      onTimeClick={(timeId) => navigate(`${isAdminContext ? '/dashboard' : ''}/times/${timeId}`)}
                      isGroup
                    />
                  )}
                </Card>
              );
            })}
          </div>
        ) : (
          <Card className="overflow-hidden">
            <div className="px-5 py-3.5 border-b border-white/[0.04]">
              <span className="text-sm font-semibold text-slate-200">Tabela de classificação</span>
            </div>
            {classificacao.length === 0 ? (
              <p className="text-sm text-slate-500 text-center py-10">Nenhum dado disponível</p>
            ) : (
              <ClassificacaoTable
                dados={classificacao}
                onTimeClick={(timeId) => navigate(`${isAdminContext ? '/dashboard' : ''}/times/${timeId}`)}
              />
            )}
          </Card>
        ))}

      {tab === 'partidas' && (
        <div>
          {partidas.length === 0 ? (
            <Card className="p-6 text-center">
              <Calendar size={24} className="text-slate-600 mx-auto mb-2" />
              <p className="text-sm text-slate-500">Nenhuma partida neste campeonato</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {[...new Set(partidas.map((p) => p.rodada))]
                .sort((a, b) => a - b)
                .map((rodada) => {
                  const rodadaPartidas = partidas.filter((p) => p.rodada === rodada);
                  return (
                    <div key={rodada}>
                      <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider mb-2 flex items-center gap-2">
                        <Zap size={13} className="text-accent" />
                        {rodada}ª Rodada
                        <span className="text-xs text-slate-500 font-normal normal-case">
                          ({rodadaPartidas.length} partidas)
                        </span>
                      </h3>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {rodadaPartidas.map((p) => (
                          <PartidaCard key={p.id} partida={p} />
                        ))}
                      </div>
                    </div>
                  );
                })}
            </div>
          )}
        </div>
      )}

      {tab === 'artilharia' && (
        <Card className="overflow-hidden">
          <div className="px-5 py-3.5 border-b border-white/[0.04]">
            <span className="text-sm font-semibold text-slate-200">Artilheiros</span>
          </div>
          {artilharia.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-10">Nenhum gol marcado</p>
          ) : (
            <div className="divide-y divide-white/[0.03]">
              {artilharia.map((a) => (
                <Link
                  key={a.jogadorId}
                  to={`/jogadores/${a.jogadorId}`}
                  className="flex items-center gap-3 px-5 py-3 hover:bg-white/[0.02] transition-colors"
                >
                  <span
                    className={`text-sm font-bold font-mono w-6 ${a.posicao <= 3 ? 'text-accent' : 'text-slate-400'}`}
                  >
                    {a.posicao}
                  </span>
                  <img
                    src={getAvatarUrl(a.nomeJogador, 24)}
                    alt={a.nomeJogador}
                    className="w-8 h-8 rounded-full bg-white/5"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-slate-200 truncate">{a.nomeJogador}</p>
                    <p className="text-xs text-slate-500">{a.nomeTime}</p>
                  </div>
                  <span className="text-lg font-bold text-accent font-mono">{a.gols}</span>
                </Link>
              ))}
            </div>
          )}
        </Card>
      )}

      {tab === 'selecao' && (
        <Card className="p-5">
          <h3 className="text-sm font-semibold text-slate-200 mb-4">Seleção do campeonato</h3>
          {selecao.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-10">Nenhuma seleção disponível</p>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
              {selecao.map((j) => (
                <Link
                  key={`${j.posicao}-${j.nomeJogador}`}
                  to={`/jogadores/${j.jogadorId}`}
                  className="flex items-center gap-3 p-3 rounded-xl bg-white/[0.03] border border-white/[0.06] hover:bg-white/[0.06] hover:border-accent/20 transition-all duration-200"
                >
                  <div className="w-9 h-9 rounded-lg bg-accent/10 flex items-center justify-center text-xs font-bold text-accent flex-shrink-0">
                    {j.posicao.substring(0, 2)}
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-200">{j.nomeJogador}</p>
                    <p className="text-[10px] text-slate-500">{j.nomeTime}</p>
                  </div>
                  <div className="ml-auto text-right">
                    <span className="text-xs font-mono text-accent">{j.score.toFixed(1)}</span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </Card>
      )}

      {tab === 'mvp' && (
        <Card className="p-6">
          {!mvp ? (
            <p className="text-sm text-slate-500 text-center py-10">Nenhum MVP disponível</p>
          ) : (
            <Link to={`/jogadores/${mvp.jogadorId}`} className="flex flex-col items-center text-center group">
              <div className="w-20 h-20 rounded-full bg-gradient-to-br from-accent/20 to-accent/5 flex items-center justify-center mb-4 ring-4 ring-accent/20 group-hover:ring-accent/40 transition-all">
                <Medal size={36} className="text-accent" />
              </div>
              <h3 className="text-xl font-bold text-slate-100 group-hover:text-accent transition-colors">
                {mvp.nomeJogador}
              </h3>
              <p className="text-sm text-slate-400 mt-1">{mvp.nomeTime}</p>

              <div className="flex items-center gap-1 mt-2">
                <Star size={14} className="text-accent fill-accent" />
                <span className="text-sm font-semibold text-accent font-mono">{mvp.score.toFixed(1)}</span>
                <span className="text-xs text-slate-500">de rating</span>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-6 w-full max-w-md">
                <div className="p-3 rounded-xl bg-white/[0.03] border border-white/[0.06]">
                  <div className="text-xl font-extrabold font-mono text-accent">{mvp.gols}</div>
                  <div className="text-[10px] text-slate-500 uppercase tracking-wider mt-0.5">Gols</div>
                </div>
                <div className="p-3 rounded-xl bg-white/[0.03] border border-white/[0.06]">
                  <div className="text-xl font-extrabold font-mono text-blue-400">{mvp.assistencias}</div>
                  <div className="text-[10px] text-slate-500 uppercase tracking-wider mt-0.5">Assists</div>
                </div>
                <div className="p-3 rounded-xl bg-white/[0.03] border border-white/[0.06]">
                  <div className="text-xl font-extrabold font-mono text-emerald-400">{mvp.defesas}</div>
                  <div className="text-[10px] text-slate-500 uppercase tracking-wider mt-0.5">Defesas</div>
                </div>
                <div className="p-3 rounded-xl bg-white/[0.03] border border-white/[0.06]">
                  <div className="text-xl font-extrabold font-mono text-yellow-400">{mvp.penaltisDefendidos}</div>
                  <div className="text-[10px] text-slate-500 uppercase tracking-wider mt-0.5">Pênaltis</div>
                </div>
              </div>
              <div className="flex items-center gap-4 mt-4 text-xs text-slate-500">
                <span>&#x1F7E8; {mvp.cartoesAmarelos} amarelos</span>
                <span>&#x1F7E5; {mvp.cartoesVermelhos} vermelhos</span>
                <span>&#x274C; {mvp.penaltisPerdidos} pênaltis perdidos</span>
              </div>
            </Link>
          )}
        </Card>
      )}

      {tab === 'chaveamento' && (
        <Card className="overflow-hidden p-5">
          <h3 className="text-sm font-semibold text-slate-200 mb-4">Chaveamento</h3>
          {fases.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-10">Nenhuma fase eliminatória disponível</p>
          ) : (
            <BracketView fases={fases} />
          )}
        </Card>
      )}
    </div>
  );
}
