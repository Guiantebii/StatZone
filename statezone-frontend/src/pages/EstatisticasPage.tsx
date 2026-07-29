import { useEffect, useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { Trophy, Medal, Shield, Eye, User, Star, Swords, GitBranch } from 'lucide-react';
import api from '../api/client';
import { PAGE_SIZE } from '../constants/pagination';
import type { Campeonato } from '../types/campeonato';
import type { Grupo, FaseEliminatoria } from '../types/fases';
import type {
  ClassificacaoTime,
  Artilharia,
  AssistenciaRanking,
  RankingCartao,
  RankingGoleiro,
  SelecaoCampeonato,
  CraqueCampeonato,
} from '../types/estatisticas';
import Card from '../components/ui/Card';
import { SkeletonCard, SkeletonTable } from '../components/ui/Skeleton';
import PaginationBar from '../components/ui/PaginationBar';
import BracketView from '../components/BracketView';
import { getAvatarUrl } from '../constants/helpers';
import ClassificacaoTable from '../components/ui/ClassificacaoTable';
import { toast } from 'sonner';

type Tab = 'classificacao' | 'artilharia' | 'assistencias' | 'cartoes' | 'goleiros' | 'selecao' | 'mvp' | 'chaveamento';

function buildTabs(tipoFormato: string | null): { key: Tab; label: string; icon: React.ReactNode }[] {
  const base = [
    { key: 'artilharia' as Tab, label: 'Artilharia', icon: <Medal size={13} /> },
    { key: 'assistencias' as Tab, label: 'Assistências', icon: <Eye size={13} /> },
    { key: 'cartoes' as Tab, label: 'Cartões', icon: <Shield size={13} /> },
    { key: 'goleiros' as Tab, label: 'Goleiros', icon: <User size={13} /> },
    { key: 'selecao' as Tab, label: 'Seleção', icon: <Star size={13} /> },
    { key: 'mvp' as Tab, label: 'MVP', icon: <Swords size={13} /> },
  ];
  if (tipoFormato === 'MATA_MATA') {
    return [{ key: 'chaveamento' as Tab, label: 'Chaveamento', icon: <GitBranch size={13} /> }, ...base];
  }
  if (tipoFormato === 'GRUPOS_E_MATA_MATA') {
    return [
      { key: 'classificacao' as Tab, label: 'Classificação', icon: <Trophy size={13} /> },
      { key: 'chaveamento' as Tab, label: 'Chaveamento', icon: <GitBranch size={13} /> },
      ...base,
    ];
  }
  return [{ key: 'classificacao' as Tab, label: 'Classificação', icon: <Trophy size={13} /> }, ...base];
}

export default function EstatisticasPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const isAdminContext = location.pathname.startsWith('/dashboard');
  const [campeonatos, setCampeonatos] = useState<Campeonato[]>([]);
  const [campeonatoId, setCampeonatoId] = useState<number | null>(null);
  const [tipoFormato, setTipoFormato] = useState<string | null>(null);
  const [tab, setTab] = useState<Tab>('classificacao');
  const [loading, setLoading] = useState(true);

  const [classificacao, setClassificacao] = useState<ClassificacaoTime[]>([]);
  const [grupos, setGrupos] = useState<Grupo[]>([]);
  const [grupoClassificacoes, setGrupoClassificacoes] = useState<Record<number, ClassificacaoTime[]>>({});
  const [artilharia, setArtilharia] = useState<Artilharia[]>([]);
  const [assistencias, setAssistencias] = useState<AssistenciaRanking[]>([]);
  const [cartoesAmarelos, setCartoesAmarelos] = useState<RankingCartao[]>([]);
  const [cartoesVermelhos, setCartoesVermelhos] = useState<RankingCartao[]>([]);
  const [goleiros, setGoleiros] = useState<RankingGoleiro[]>([]);
  const [selecao, setSelecao] = useState<SelecaoCampeonato[]>([]);
  const [mvp, setMvp] = useState<CraqueCampeonato | null>(null);
  const [fases, setFases] = useState<FaseEliminatoria[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [dataLoading, setDataLoading] = useState(true);
  const campeonatosEndpoint = isAdminContext ? '/campeonatos' : '/campeonatos?publico=true';

  useEffect(() => {
    api
      .get(campeonatosEndpoint)
      .then((res) => {
        const data = res.data as Campeonato[];
        setCampeonatos(data);
        if (data.length > 0) {
          setCampeonatoId(data[0].id);
          setTipoFormato(data[0].tipoFormato || null);
        }
      })
      .catch(() => toast.error('Erro ao carregar campeonatos'))
      .finally(() => setLoading(false));
  }, [campeonatosEndpoint]);

  useEffect(() => {
    if (!campeonatoId) return;
    let isCancelled = false;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setDataLoading(true);
    const promises: Promise<void>[] = [];

    if (tab === 'classificacao') {
      if (tipoFormato === 'GRUPOS_E_MATA_MATA') {
        promises.push(
          api
            .get(`/campeonatos/${campeonatoId}/grupos`)
            .then(async (r) => {
              if (isCancelled) return;
              const gs = r.data as Grupo[];
              setGrupos(gs);
              const classMap: Record<number, ClassificacaoTime[]> = {};
              await Promise.all(
                gs.map(async (g) => {
                  try {
                    const cr = await api.get(`/campeonatos/${campeonatoId}/grupos/${g.id}/classificacao`);
                    if (!isCancelled) classMap[g.id] = cr.data;
                  } catch {
                    if (!isCancelled) classMap[g.id] = [];
                  }
                }),
              );
              if (!isCancelled) setGrupoClassificacoes(classMap);
            })
            .catch(() => {
              if (!isCancelled) {
                setGrupos([]);
                setGrupoClassificacoes({});
              }
            }),
        );
      } else {
        promises.push(
          api
            .get(`/campeonatos/${campeonatoId}/classificacao`)
            .then((r) => {
              if (!isCancelled) setClassificacao(r.data);
            })
            .catch(() => {
              if (!isCancelled) setClassificacao([]);
            }),
        );
      }
    }
    if (tab === 'artilharia') {
      promises.push(
        api
          .get(`/campeonatos/${campeonatoId}/artilharia?pagina=${page}&tamanho=${PAGE_SIZE}`)
          .then((r) => {
            if (!isCancelled) {
              setArtilharia(r.data);
              setHasMore(r.data.length >= PAGE_SIZE);
            }
          })
          .catch(() => {
            if (!isCancelled) setArtilharia([]);
          }),
      );
    }
    if (tab === 'assistencias') {
      promises.push(
        api
          .get(`/campeonatos/${campeonatoId}/assistencias?pagina=${page}&tamanho=${PAGE_SIZE}`)
          .then((r) => {
            if (!isCancelled) {
              setAssistencias(r.data);
              setHasMore(r.data.length >= PAGE_SIZE);
            }
          })
          .catch(() => {
            if (!isCancelled) setAssistencias([]);
          }),
      );
    }
    if (tab === 'cartoes') {
      promises.push(
        api
          .get(`/campeonatos/${campeonatoId}/ranking/cartoes-amarelos?pagina=${page}&tamanho=${PAGE_SIZE}`)
          .then((r) => {
            if (!isCancelled) {
              setCartoesAmarelos(r.data);
              setHasMore(r.data.length >= PAGE_SIZE);
            }
          })
          .catch(() => {
            if (!isCancelled) setCartoesAmarelos([]);
          }),
        api
          .get(`/campeonatos/${campeonatoId}/ranking/cartoes-vermelhos?pagina=${page}&tamanho=${PAGE_SIZE}`)
          .then((r) => {
            if (!isCancelled) setCartoesVermelhos(r.data);
          })
          .catch(() => {
            if (!isCancelled) setCartoesVermelhos([]);
          }),
      );
    }
    if (tab === 'goleiros') {
      promises.push(
        api
          .get(`/campeonatos/${campeonatoId}/ranking/goleiros?pagina=${page}&tamanho=${PAGE_SIZE}`)
          .then((r) => {
            if (!isCancelled) {
              setGoleiros(r.data);
              setHasMore(r.data.length >= PAGE_SIZE);
            }
          })
          .catch(() => {
            if (!isCancelled) setGoleiros([]);
          }),
      );
    }
    if (tab === 'selecao') {
      promises.push(
        api
          .get(`/campeonatos/${campeonatoId}/selecao-do-campeonato`)
          .then((r) => {
            if (!isCancelled) setSelecao(r.data);
          })
          .catch(() => {
            if (!isCancelled) setSelecao([]);
          }),
      );
    }
    if (tab === 'mvp') {
      promises.push(
        api
          .get(`/campeonatos/${campeonatoId}/mvp`)
          .then((r) => {
            if (!isCancelled) setMvp(r.data);
          })
          .catch(() => {
            if (!isCancelled) setMvp(null);
          }),
      );
    }
    if (tab === 'chaveamento') {
      promises.push(
        api
          .get(`/campeonatos/${campeonatoId}/fases`)
          .then((r) => {
            if (!isCancelled) setFases(r.data);
          })
          .catch(() => {
            if (!isCancelled) setFases([]);
          }),
      );
    }

    Promise.all(promises).finally(() => {
      if (!isCancelled) setDataLoading(false);
    });
    return () => {
      isCancelled = true;
    };
  }, [campeonatoId, tab, page, tipoFormato]);

  if (loading)
    return (
      <div className="space-y-6">
        <div className="h-8 w-48 rounded-lg animate-shimmer" />
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      </div>
    );

  return (
    <div className="space-y-6 animate-fade-in-up">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 tracking-tight">Estatísticas</h1>
          <p className="text-sm text-slate-500 mt-1">Rankings e números dos campeonatos</p>
        </div>
        {campeonatos.length > 0 && (
          <select
            value={campeonatoId ?? ''}
            onChange={(e) => {
              const id = Number(e.target.value);
              setCampeonatoId(id);
              const camp = campeonatos.find((c) => c.id === id);
              setTipoFormato(camp?.tipoFormato || null);
            }}
            className="bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
          >
            {campeonatos.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </select>
        )}
      </div>

      <div className="flex gap-1 bg-white/[0.03] rounded-xl p-1 overflow-x-auto">
        {buildTabs(tipoFormato).map((t) => (
          <button
            key={t.key}
            onClick={() => {
              setTab(t.key);
              setPage(0);
            }}
            className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold transition-all whitespace-nowrap ${
              tab === t.key ? 'bg-accent/10 text-accent shadow-sm' : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            {t.icon}
            {t.label}
          </button>
        ))}
      </div>

      {dataLoading ? (
        <SkeletonTable rows={8} />
      ) : (
        <>
          {tab === 'classificacao' &&
            (tipoFormato === 'GRUPOS_E_MATA_MATA' ? (
              <div className="space-y-6">
                {grupos.length === 0 ? (
                  <Card className="overflow-hidden">
                    <p className="text-sm text-slate-500 text-center py-10">Nenhum dado disponível</p>
                  </Card>
                ) : (
                  grupos.map((g) => {
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
                  })
                )}
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

          {tab === 'artilharia' && (
            <RankingTable
              title="Artilheiros"
              headers={['Gols']}
              data={artilharia}
              renderValue={(a) => (
                <span className="text-sm font-bold text-accent font-mono">{(a as Artilharia).gols}</span>
              )}
              getJogador={(a) => ({ nome: (a as Artilharia).nomeJogador, time: (a as Artilharia).nomeTime })}
              getJogadorId={(a) => (a as Artilharia).jogadorId}
              emptyText="Nenhum gol registrado"
              pagination={{ page, hasMore, onPrev: () => setPage((p) => p - 1), onNext: () => setPage((p) => p + 1) }}
            />
          )}

          {tab === 'assistencias' && (
            <RankingTable
              title="Assistências"
              headers={['Assists']}
              data={assistencias}
              renderValue={(a) => (
                <span className="text-sm font-bold text-accent font-mono">
                  {(a as AssistenciaRanking).assistencias}
                </span>
              )}
              getJogador={(a) => ({
                nome: (a as AssistenciaRanking).nomeJogador,
                time: (a as AssistenciaRanking).nomeTime,
              })}
              getJogadorId={(a) => (a as AssistenciaRanking).jogadorId}
              emptyText="Nenhuma assistência registrada"
              pagination={{ page, hasMore, onPrev: () => setPage((p) => p - 1), onNext: () => setPage((p) => p + 1) }}
            />
          )}

          {tab === 'cartoes' && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <RankingTable
                title="Cartões amarelos"
                headers={['Amarelos']}
                data={cartoesAmarelos}
                renderValue={(a) => (
                  <span className="text-sm font-bold text-warning font-mono">
                    {(a as RankingCartao).cartoesAmarelos}
                  </span>
                )}
                getJogador={(a) => ({ nome: (a as RankingCartao).nomeJogador, time: (a as RankingCartao).nomeTime })}
                getJogadorId={(a) => (a as RankingCartao).jogadorId}
                emptyText="Nenhum cartão amarelo"
              />
              <RankingTable
                title="Cartões vermelhos"
                headers={['Vermelhos']}
                data={cartoesVermelhos}
                renderValue={(a) => (
                  <span className="text-sm font-bold text-danger font-mono">
                    {(a as RankingCartao).cartoesVermelhos}
                  </span>
                )}
                getJogador={(a) => ({ nome: (a as RankingCartao).nomeJogador, time: (a as RankingCartao).nomeTime })}
                getJogadorId={(a) => (a as RankingCartao).jogadorId}
                emptyText="Nenhum cartão vermelho"
              />
            </div>
          )}

          {tab === 'goleiros' && (
            <Card className="overflow-hidden">
              <div className="px-5 py-3.5 border-b border-white/[0.04]">
                <span className="text-sm font-semibold text-slate-200">Ranking de goleiros</span>
              </div>
              {goleiros.length === 0 ? (
                <p className="text-sm text-slate-500 text-center py-10">Nenhum goleiro registrado</p>
              ) : (
                <table className="w-full">
                  <thead>
                    <tr className="bg-white/[0.02]">
                      <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold w-8">
                        #
                      </th>
                      <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                        Goleiro
                      </th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                        Jogos
                      </th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                        Defesas
                      </th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                        Clean Sheets
                      </th>
                      <th className="text-right px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                        Pênaltis
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/[0.03]">
                    {goleiros.map((g) => (
                      <tr
                        key={g.jogadorId}
                        className="hover:bg-white/[0.02] transition-colors cursor-pointer"
                        onClick={() => navigate(`/jogadores/${g.jogadorId}`)}
                      >
                        <td className="px-5 py-3">
                          <span className="text-sm font-bold font-mono text-slate-400">{g.posicao}</span>
                        </td>
                        <td className="px-5 py-3">
                          <div className="flex items-center gap-2">
                            <img
                              src={g.fotoUrl || getAvatarUrl(g.nomeJogador, 24)}
                              alt={g.nomeJogador}
                              className="w-7 h-7 rounded-full bg-white/5"
                            />
                            <div>
                              <p className="text-sm font-medium text-slate-200 hover:text-accent transition-colors">
                                {g.nomeJogador}
                              </p>
                              <p className="text-[10px] text-slate-500">{g.nomeTime}</p>
                            </div>
                          </div>
                        </td>
                        <td className="px-3 py-3 text-center text-sm text-slate-300 font-mono">{g.partidasJogadas}</td>
                        <td className="px-3 py-3 text-center text-sm text-slate-300 font-mono">{g.defesas}</td>
                        <td className="px-3 py-3 text-center text-sm font-mono">
                          <span className="text-success font-bold">{g.cleanSheets}</span>
                        </td>
                        <td className="px-5 py-3 text-right text-sm text-slate-300 font-mono">
                          {g.penaltisDefendidos}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
              {goleiros.length > 0 && (
                <PaginationBar
                  page={page}
                  hasMore={hasMore}
                  onPrev={() => setPage((p) => p - 1)}
                  onNext={() => setPage((p) => p + 1)}
                />
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
                    <Swords size={36} className="text-accent" />
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
                    <StatItem label="Gols" value={mvp.gols} color="text-accent" />
                    <StatItem label="Assistências" value={mvp.assistencias} color="text-info" />
                    <StatItem label="Defesas" value={mvp.defesas} color="text-success" />
                    <StatItem label="Pênaltis defendidos" value={mvp.penaltisDefendidos} color="text-warning" />
                  </div>
                  <div className="flex items-center gap-4 mt-4 text-xs text-slate-500">
                    <span>🟨 {mvp.cartoesAmarelos} amarelos</span>
                    <span>🟥 {mvp.cartoesVermelhos} vermelhos</span>
                    <span>❌ {mvp.penaltisPerdidos} pênaltis perdidos</span>
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
        </>
      )}
    </div>
  );
}

function StatItem({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div className="p-3 rounded-xl bg-white/[0.03] border border-white/[0.06]">
      <div className={`text-xl font-extrabold font-mono ${color}`}>{value}</div>
      <div className="text-[10px] text-slate-500 uppercase tracking-wider mt-0.5">{label}</div>
    </div>
  );
}

interface RankingTableProps<T extends { posicao: number }> {
  title: string;
  headers: string[];
  data: T[];
  renderValue: (item: T) => React.ReactNode;
  getJogador: (item: T) => { nome: string; time?: string };
  getJogadorId?: (item: T) => number;
  emptyText: string;
  pagination?: {
    page: number;
    hasMore: boolean;
    onPrev: () => void;
    onNext: () => void;
  };
}

function RankingTable<T extends { posicao: number }>({
  title,
  headers,
  data,
  renderValue,
  getJogador,
  getJogadorId,
  emptyText,
  pagination,
}: RankingTableProps<T>) {
  return (
    <Card className="overflow-hidden">
      <div className="px-5 py-3.5 border-b border-white/[0.04]">
        <span className="text-sm font-semibold text-slate-200">{title}</span>
      </div>
      {data.length === 0 ? (
        <p className="text-sm text-slate-500 text-center py-10">{emptyText}</p>
      ) : (
        <table className="w-full">
          <thead>
            <tr className="bg-white/[0.02]">
              <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold w-8">
                #
              </th>
              <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                Jogador
              </th>
              {headers.map((h) => (
                <th
                  key={h}
                  className="text-right px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold"
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-white/[0.03]">
            {data.map((item) => {
              const jog = getJogador(item);
              return (
                <tr key={`${title}-${item.posicao}-${jog.nome}`} className="hover:bg-white/[0.02] transition-colors">
                  <td className="px-5 py-3">
                    <span
                      className={`text-sm font-bold font-mono ${item.posicao <= 3 ? 'text-accent' : 'text-slate-400'}`}
                    >
                      {item.posicao}
                    </span>
                  </td>
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2">
                      <img
                        src={getAvatarUrl(jog.nome, 24)}
                        alt={jog.nome}
                        className="w-7 h-7 rounded-full bg-white/5"
                      />
                      <div>
                        {getJogadorId ? (
                          <Link
                            to={`/jogadores/${getJogadorId(item)}`}
                            className="text-sm font-medium text-slate-200 hover:text-accent transition-colors"
                          >
                            {jog.nome}
                          </Link>
                        ) : (
                          <p className="text-sm font-medium text-slate-200">{jog.nome}</p>
                        )}
                        {jog.time && <p className="text-[10px] text-slate-500">{jog.time}</p>}
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3 text-right">{renderValue(item)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
      {pagination && data.length > 0 && (
        <PaginationBar
          page={pagination.page}
          hasMore={pagination.hasMore}
          onPrev={pagination.onPrev}
          onNext={pagination.onNext}
        />
      )}
    </Card>
  );
}
