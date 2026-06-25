import { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { ArrowLeft, Trophy, Swords, Medal, Calendar, Zap } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import { PAGE_SIZE } from '../constants/pagination';
import type { Campeonato } from '../types/campeonato';
import type { ClassificacaoTime, Artilharia } from '../types/estatisticas';
import type { Partida } from '../types/partida';
import type { Grupo } from '../types/fases';
import Card from '../components/ui/Card';
import PartidaCard from '../components/PartidaCard';
import { SkeletonCard } from '../components/ui/Skeleton';
import { toast } from 'sonner';

type Tab = 'classificacao' | 'partidas' | 'artilharia';

const tabs: { key: Tab; label: string; icon: React.ReactNode }[] = [
  { key: 'classificacao', label: 'Classificação', icon: <Trophy size={14} /> },
  { key: 'partidas', label: 'Partidas', icon: <Swords size={14} /> },
  { key: 'artilharia', label: 'Artilharia', icon: <Medal size={14} /> },
];

export default function CampeonatoDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const isAdminContext = location.pathname.startsWith('/dashboard');
  const [campeonato, setCampeonato] = useState<Campeonato | null>(null);
  const [classificacao, setClassificacao] = useState<ClassificacaoTime[]>([]);
  const [partidas, setPartidas] = useState<Partida[]>([]);
  const [artilharia, setArtilharia] = useState<Artilharia[]>([]);
  const [grupos, setGrupos] = useState<Grupo[]>([]);
  const [grupoClassificacoes, setGrupoClassificacoes] = useState<Record<number, ClassificacaoTime[]>>({});
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<Tab>('classificacao');

  useEffect(() => {
    if (!id) return;
    let isMounted = true;
    const load = async () => {
      try {
        const [campRes, classRes, partRes, artRes] = await Promise.all([
          api.get(`/campeonatos/${id}`),
          api.get(`/campeonatos/${id}/classificacao`),
          api.get(`/campeonatos/${id}/partidas`),
          api.get(`/campeonatos/${id}/artilharia?pagina=0&tamanho=${PAGE_SIZE}`),
        ]);
        if (!isMounted) return;
        setCampeonato(campRes.data);
        setClassificacao(classRes.data);
        setPartidas(partRes.data);
        setArtilharia(artRes.data);

        const camp = campRes.data as Campeonato;
        if (camp.tipoFormato === 'GRUPOS_E_MATA_MATA') {
          try {
            const gruposRes = await api.get(`/campeonatos/${id}/grupos`);
            const gs = gruposRes.data as Grupo[];
            if (isMounted) setGrupos(gs);
            const classMap: Record<number, ClassificacaoTime[]> = {};
            await Promise.all(gs.map(async (g) => {
              try {
                const r = await api.get(`/campeonatos/${id}/grupos/${g.id}/classificacao`);
                classMap[g.id] = r.data;
              } catch { /* sem partidas */ }
            }));
            if (isMounted) setGrupoClassificacoes(classMap);
          } catch {
            /* sem grupos */
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
    return () => { isMounted = false; };
  }, [id]);

  const getLogoUrl = (nome: string) =>
    `https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=1a3460&color=FFD700&size=32&bold=true`;

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-8 w-48 rounded-lg animate-shimmer" />
        <div className="h-48 rounded-2xl animate-shimmer" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      </div>
    );
  }

  if (!campeonato) return null;

  return (
    <div className="space-y-6 animate-fade-in-up">
      {/* Back + Header */}
      <div className="space-y-3">
        <Link to={isAdminContext ? '/dashboard/campeonatos' : '/campeonatos'} className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-200 transition-colors">
          <ArrowLeft size={15} />
          Todos os campeonatos
        </Link>

        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-accent/10 flex items-center justify-center text-accent shrink-0">
            <Trophy size={28} />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-100 tracking-tight">{campeonato.nome}</h1>
            <p className="text-sm text-slate-500 mt-0.5">{campeonato.pais} · Temporada {campeonato.temporada}</p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-white/[0.03] rounded-xl p-1">
        {tabs.map((t) => (
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

      {/* Classificação */}
      {tab === 'classificacao' && (
        campeonato.tipoFormato === 'GRUPOS_E_MATA_MATA' ? (
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
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead>
                          <tr className="bg-white/[0.02]">
                            <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold w-8">#</th>
                            <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">Time</th>
                            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">P</th>
                            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">J</th>
                            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">V</th>
                            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">E</th>
                            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">D</th>
                            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">GP</th>
                            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">GC</th>
                            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">SG</th>
                            <th className="text-right px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">AP%</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-white/[0.03]">
                          {dados.map((c) => (
                            <tr
                              key={c.timeId}
                              className={`hover:bg-white/[0.02] transition-colors cursor-pointer ${c.posicao <= 4 ? 'bg-success/5' : ''}`}
                              onClick={() => navigate(`${isAdminContext ? '/dashboard' : ''}/times/${c.timeId}`)}
                            >
                              <td className="px-5 py-3">
                                <span className={`text-sm font-bold font-mono ${c.posicao <= 4 ? 'text-accent' : 'text-slate-400'}`}>
                                  {c.posicao}
                                </span>
                              </td>
                              <td className="px-5 py-3">
                                <div className="flex items-center gap-2">
                                  <img src={getLogoUrl(c.nomeTime)} alt={c.nomeTime} className="w-6 h-6 rounded-full bg-white/5" />
                                  <span className="text-sm font-medium text-slate-200">{c.nomeTime}</span>
                                </div>
                              </td>
                              <td className="px-3 py-3 text-center text-sm font-bold text-accent font-mono">{c.pontos}</td>
                              <td className="px-3 py-3 text-center text-sm text-slate-400 font-mono">{c.partidas}</td>
                              <td className="px-3 py-3 text-center text-sm text-success font-mono">{c.vitorias}</td>
                              <td className="px-3 py-3 text-center text-sm text-slate-400 font-mono">{c.empates}</td>
                              <td className="px-3 py-3 text-center text-sm text-danger font-mono">{c.derrotas}</td>
                              <td className="px-3 py-3 text-center text-sm text-slate-300 font-mono">{c.golsFeitos}</td>
                              <td className="px-3 py-3 text-center text-sm text-slate-300 font-mono">{c.golsSofridos}</td>
                              <td className="px-3 py-3 text-center text-sm font-mono">
                                <span className={c.saldoGols > 0 ? 'text-success' : c.saldoGols < 0 ? 'text-danger' : 'text-slate-400'}>
                                  {c.saldoGols > 0 ? '+' : ''}{c.saldoGols}
                                </span>
                              </td>
                              <td className="px-5 py-3 text-right text-sm text-slate-400 font-mono">{c.aproveitamento.toFixed(1)}%</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
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
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="bg-white/[0.02]">
                      <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold w-8">#</th>
                      <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">Time</th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">P</th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">J</th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">V</th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">E</th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">D</th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">GP</th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">GC</th>
                      <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">SG</th>
                      <th className="text-right px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">AP%</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/[0.03]">
                    {classificacao.map((c) => (
                      <tr
                        key={c.timeId}
                        className={`hover:bg-white/[0.02] transition-colors cursor-pointer ${c.posicao <= 4 ? 'bg-success/5' : ''}`}
                        onClick={() => navigate(`${isAdminContext ? '/dashboard' : ''}/times/${c.timeId}`)}
                      >
                        <td className="px-5 py-3">
                          <span className={`text-sm font-bold font-mono ${c.posicao <= 4 ? 'text-accent' : 'text-slate-400'}`}>
                            {c.posicao}
                          </span>
                        </td>
                        <td className="px-5 py-3">
                          <div className="flex items-center gap-2">
                            <img src={getLogoUrl(c.nomeTime)} alt={c.nomeTime} className="w-6 h-6 rounded-full bg-white/5" />
                            <span className="text-sm font-medium text-slate-200">{c.nomeTime}</span>
                          </div>
                        </td>
                        <td className="px-3 py-3 text-center text-sm font-bold text-accent font-mono">{c.pontos}</td>
                        <td className="px-3 py-3 text-center text-sm text-slate-400 font-mono">{c.partidas}</td>
                        <td className="px-3 py-3 text-center text-sm text-success font-mono">{c.vitorias}</td>
                        <td className="px-3 py-3 text-center text-sm text-slate-400 font-mono">{c.empates}</td>
                        <td className="px-3 py-3 text-center text-sm text-danger font-mono">{c.derrotas}</td>
                        <td className="px-3 py-3 text-center text-sm text-slate-300 font-mono">{c.golsFeitos}</td>
                        <td className="px-3 py-3 text-center text-sm text-slate-300 font-mono">{c.golsSofridos}</td>
                        <td className="px-3 py-3 text-center text-sm font-mono">
                          <span className={c.saldoGols > 0 ? 'text-success' : c.saldoGols < 0 ? 'text-danger' : 'text-slate-400'}>
                            {c.saldoGols > 0 ? '+' : ''}{c.saldoGols}
                          </span>
                        </td>
                        <td className="px-5 py-3 text-right text-sm text-slate-400 font-mono">{c.aproveitamento.toFixed(1)}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        )
      )}

      {/* Partidas */}
      {tab === 'partidas' && (
        <div>
          {partidas.length === 0 ? (
            <Card className="p-6 text-center">
              <Calendar size={24} className="text-slate-600 mx-auto mb-2" />
              <p className="text-sm text-slate-500">Nenhuma partida neste campeonato</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {/* Agrupamento por rodada */}
              {[...new Set(partidas.map((p) => p.rodada))].sort((a, b) => a - b).map((rodada) => {
                const rodadaPartidas = partidas.filter((p) => p.rodada === rodada);
                return (
                  <div key={rodada}>
                    <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider mb-2 flex items-center gap-2">
                      <Zap size={13} className="text-accent" />
                      {rodada}ª Rodada
                      <span className="text-xs text-slate-500 font-normal normal-case">({rodadaPartidas.length} partidas)</span>
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

      {/* Artilharia */}
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
                <Link key={a.jogadorId} to={`/jogadores/${a.jogadorId}`} className="flex items-center gap-3 px-5 py-3 hover:bg-white/[0.02] transition-colors">
                  <span className={`text-sm font-bold font-mono w-6 ${a.posicao <= 3 ? 'text-accent' : 'text-slate-400'}`}>
                    {a.posicao}
                  </span>
                  <img
                    src={`https://ui-avatars.com/api/?name=${encodeURIComponent(a.nomeJogador)}&background=1a3460&color=fff&size=24`}
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
    </div>
  );
}
