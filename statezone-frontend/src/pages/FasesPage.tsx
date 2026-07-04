import { useEffect, useState } from 'react';
import { Plus, Swords, LayoutGrid, Trophy } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { Campeonato } from '../types/campeonato';
import type { FaseEliminatoria, Grupo, FaseEnum } from '../types/fases';
import type { Time } from '../types/time';
import type { ClassificacaoTime } from '../types/estatisticas';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import { VAGAS_PADRAO } from '../constants/pagination';
import Modal from '../components/ui/Modal';
import BracketView from '../components/BracketView';
import { SkeletonCard } from '../components/ui/Skeleton';
import { getLogoUrl } from '../constants/helpers';
import { toast } from 'sonner';

type SubTab = 'grupos' | 'matamata';

const faseLabel: Record<FaseEnum, string> = {
  OITAVAS: 'Oitavas de final',
  QUARTAS: 'Quartas de final',
  SEMIFINAL: 'Semifinal',
  TERCEIRO_LUGAR: 'Terceiro lugar',
  FINAL: 'Final',
};

const faseOrder: FaseEnum[] = ['OITAVAS', 'QUARTAS', 'SEMIFINAL', 'TERCEIRO_LUGAR', 'FINAL'];

export default function FasesPage() {
  const [campeonatos, setCampeonatos] = useState<Campeonato[]>([]);
  const [campeonatoId, setCampeonatoId] = useState<number | null>(null);
  const [subTab, setSubTab] = useState<SubTab>('matamata');
  const [loading, setLoading] = useState(true);

  const [fases, setFases] = useState<FaseEliminatoria[]>([]);
  const [grupos, setGrupos] = useState<Grupo[]>([]);
  const [times, setTimes] = useState<Time[]>([]);
  const [grupoClassificacoes, setGrupoClassificacoes] = useState<Record<number, ClassificacaoTime[]>>({});
  const [dataLoading, setDataLoading] = useState(true);

  const [showCreatePhase, setShowCreatePhase] = useState(false);
  const [selectedFase, setSelectedFase] = useState<FaseEnum>('QUARTAS');
  const [jogoUnico, setJogoUnico] = useState(false);
  const [createPhaseLoading, setCreatePhaseLoading] = useState(false);

  const [showCreateGroup, setShowCreateGroup] = useState(false);
  const [grupoNome, setGrupoNome] = useState('');
  const [createGroupLoading, setCreateGroupLoading] = useState(false);

  const [showAddTeam, setShowAddTeam] = useState<number | null>(null);
  const [selectedTimeId, setSelectedTimeId] = useState<number>(0);
  const [addingTeam, setAddingTeam] = useState(false);

  const [generatingFixtures, setGeneratingFixtures] = useState<number | null>(null);

  useEffect(() => {
    api
      .get('/campeonatos')
      .then((res) => {
        setCampeonatos(res.data);
        if (res.data.length > 0) setCampeonatoId(res.data[0].id);
      })
      .catch((err) => toast.error(getApiError(err, 'Erro ao carregar campeonatos')))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!campeonatoId) return;
    let isMounted = true;
    Promise.all([
      api
        .get(`/campeonatos/${campeonatoId}/fases`)
        .then((r) => {
          if (isMounted) setFases(r.data);
        })
        .catch(() => {
          if (isMounted) setFases([]);
        }),
      api
        .get(`/campeonatos/${campeonatoId}/grupos`)
        .then(async (r) => {
          const grupos = r.data as Grupo[];
          if (isMounted) setGrupos(grupos);
          const classMap: Record<number, ClassificacaoTime[]> = {};
          await Promise.all(
            grupos.map(async (g) => {
              try {
                const cr = await api.get(`/campeonatos/${campeonatoId}/grupos/${g.id}/classificacao`);
                classMap[g.id] = cr.data;
              } catch {
                // grupo sem classificação ainda
              }
            }),
          );
          if (isMounted) setGrupoClassificacoes(classMap);
        })
        .catch(() => {
          if (isMounted) {
            setGrupos([]);
          }
        }),
      api
        .get(`/campeonatos/${campeonatoId}/times`)
        .then((r) => {
          if (isMounted) setTimes(r.data);
        })
        .catch(() => {
          if (isMounted) setTimes([]);
        }),
    ]).finally(() => {
      if (isMounted) setDataLoading(false);
    });
    return () => {
      isMounted = false;
    };
  }, [campeonatoId]);

  const handleCreatePhase = async () => {
    setCreatePhaseLoading(true);
    try {
      await api.post(`/campeonatos/${campeonatoId}/fases`, { fase: selectedFase, jogoUnico });
      toast.success('Fase criada');
      setShowCreatePhase(false);
      const res = await api.get(`/campeonatos/${campeonatoId}/fases`);
      setFases(res.data);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao criar fase'));
    } finally {
      setCreatePhaseLoading(false);
    }
  };

  const handleGenerate = async (faseId: number) => {
    try {
      await api.post(`/campeonatos/${campeonatoId}/fases/${faseId}/gerar?vagasPorGrupo=${VAGAS_PADRAO}`);
      toast.success('Confrontos gerados');
      const res = await api.get(`/campeonatos/${campeonatoId}/fases`);
      setFases(res.data);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao gerar confrontos'));
    }
  };

  const handleCreateGroup = async () => {
    setCreateGroupLoading(true);
    try {
      await api.post(`/campeonatos/${campeonatoId}/grupos`, { nome: grupoNome });
      toast.success('Grupo criado');
      setShowCreateGroup(false);
      setGrupoNome('');
      const res = await api.get(`/campeonatos/${campeonatoId}/grupos`);
      setGrupos(res.data);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao criar grupo'));
    } finally {
      setCreateGroupLoading(false);
    }
  };

  const handleAddTeam = async (grupoId: number) => {
    if (!selectedTimeId) return;
    setAddingTeam(true);
    try {
      await api.post(`/campeonatos/${campeonatoId}/grupos/${grupoId}/times/${selectedTimeId}`);
      toast.success('Time adicionado');
      setShowAddTeam(null);
      setSelectedTimeId(0);
      const res = await api.get(`/campeonatos/${campeonatoId}/grupos`);
      setGrupos(res.data);
      await loadClassificacao(grupoId);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao adicionar time'));
    } finally {
      setAddingTeam(false);
    }
  };

  const loadClassificacao = async (grupoId: number) => {
    try {
      const r = await api.get(`/campeonatos/${campeonatoId}/grupos/${grupoId}/classificacao`);
      setGrupoClassificacoes((prev) => ({ ...prev, [grupoId]: r.data }));
    } catch {
      // classificacao not available yet
    }
  };

  const handleGenerateFixtures = async (grupoId: number) => {
    setGeneratingFixtures(grupoId);
    try {
      await api.post(`/campeonatos/${campeonatoId}/grupos/${grupoId}/fixtures`);
      toast.success('Partidas geradas');
      await loadClassificacao(grupoId);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao gerar partidas'));
    } finally {
      setGeneratingFixtures(null);
    }
  };

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
          <h1 className="text-2xl font-bold text-slate-100 tracking-tight">Fases Eliminatórias</h1>
          <p className="text-sm text-slate-500 mt-1">Gerencie grupos e chaveamento</p>
        </div>
        {campeonatos.length > 0 && (
          <select
            value={campeonatoId ?? ''}
            onChange={(e) => setCampeonatoId(Number(e.target.value))}
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

      {/* Sub-tabs */}
      <div className="flex gap-1 bg-white/[0.03] rounded-xl p-1">
        {[
          { key: 'grupos' as SubTab, label: 'Grupos', icon: <LayoutGrid size={13} /> },
          { key: 'matamata' as SubTab, label: 'Mata-Mata', icon: <Swords size={13} /> },
        ].map((t) => (
          <button
            key={t.key}
            onClick={() => setSubTab(t.key)}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-xs font-semibold transition-all ${
              subTab === t.key ? 'bg-accent/10 text-accent shadow-sm' : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            {t.icon}
            {t.label}
          </button>
        ))}
      </div>

      {dataLoading ? (
        <div className="grid grid-cols-3 gap-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      ) : (
        <>
          {subTab === 'grupos' && (
            <div className="space-y-4">
              <div className="flex justify-end">
                <Button size="sm" onClick={() => setShowCreateGroup(true)}>
                  <Plus size={13} /> Novo grupo
                </Button>
              </div>

              {grupos.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 text-center glass rounded-2xl">
                  <LayoutGrid size={28} className="text-slate-600 mb-3" />
                  <p className="text-sm text-slate-400 font-medium">Nenhum grupo criado</p>
                  <p className="text-xs text-slate-600 mt-1">Crie grupos para organizar os times</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {grupos.map((g) => (
                    <Card key={g.id} className="p-4">
                      <div className="flex items-center justify-between mb-3">
                        <h3 className="text-sm font-bold text-slate-200">Grupo {g.nome}</h3>
                        <button
                          onClick={() => setShowAddTeam(g.id)}
                          className="p-1.5 rounded-lg bg-white/[0.04] text-slate-400 hover:text-accent hover:bg-accent/10 transition-colors"
                          title="Adicionar time"
                        >
                          <Plus size={13} />
                        </button>
                      </div>

                      {g.times.length === 0 ? (
                        <p className="text-xs text-slate-500 py-4 text-center">Nenhum time no grupo</p>
                      ) : (
                        <div className="overflow-x-auto">
                          <table className="w-full text-xs">
                            <thead>
                              <tr className="text-left text-[10px] uppercase tracking-wider text-slate-500 font-semibold">
                                <th className="pr-1 py-1 w-5">#</th>
                                <th className="px-1 py-1">Time</th>
                                <th className="px-1.5 py-1 text-center">P</th>
                                <th className="px-1.5 py-1 text-center">J</th>
                                <th className="px-1.5 py-1 text-center">V</th>
                                <th className="px-1.5 py-1 text-center">E</th>
                                <th className="px-1.5 py-1 text-center">D</th>
                                <th className="px-1.5 py-1 text-center">GP</th>
                                <th className="px-1.5 py-1 text-center">GC</th>
                                <th className="pl-1.5 py-1 text-center">SG</th>
                              </tr>
                            </thead>
                            <tbody>
                              {(grupoClassificacoes[g.id]?.length
                                ? grupoClassificacoes[g.id]
                                : g.times.map((t, idx) => ({
                                    timeId: t.id,
                                    nomeTime: t.nome,
                                    pontos: 0,
                                    partidas: 0,
                                    vitorias: 0,
                                    empates: 0,
                                    derrotas: 0,
                                    golsFeitos: 0,
                                    golsSofridos: 0,
                                    saldoGols: 0,
                                    posicao: idx + 1,
                                    aproveitamento: 0,
                                  }))
                              ).map((c) => (
                                <tr key={c.timeId} className="border-t border-white/[0.03]">
                                  <td
                                    className={`pr-1 py-1.5 font-mono font-bold ${c.posicao <= 4 ? 'text-accent' : 'text-slate-500'}`}
                                  >
                                    {c.posicao}
                                  </td>
                                  <td className="px-1 py-1.5">
                                    <div className="flex items-center gap-1.5">
                                      <img
                                        src={getLogoUrl(c.nomeTime)}
                                        alt={c.nomeTime}
                                        className="w-4 h-4 rounded-full bg-white/5"
                                      />
                                      <span className="text-slate-200 font-medium truncate max-w-[80px]">
                                        {c.nomeTime}
                                      </span>
                                    </div>
                                  </td>
                                  <td className="px-1.5 py-1.5 text-center font-bold text-accent font-mono">
                                    {c.pontos}
                                  </td>
                                  <td className="px-1.5 py-1.5 text-center text-slate-400 font-mono">{c.partidas}</td>
                                  <td className="px-1.5 py-1.5 text-center text-green-400 font-mono">{c.vitorias}</td>
                                  <td className="px-1.5 py-1.5 text-center text-slate-400 font-mono">{c.empates}</td>
                                  <td className="px-1.5 py-1.5 text-center text-red-400 font-mono">{c.derrotas}</td>
                                  <td className="px-1.5 py-1.5 text-center text-slate-300 font-mono">{c.golsFeitos}</td>
                                  <td className="px-1.5 py-1.5 text-center text-slate-300 font-mono">
                                    {c.golsSofridos}
                                  </td>
                                  <td
                                    className={`pl-1.5 py-1.5 text-center font-mono ${c.saldoGols > 0 ? 'text-green-400' : c.saldoGols < 0 ? 'text-red-400' : 'text-slate-400'}`}
                                  >
                                    {c.saldoGols > 0 ? '+' : ''}
                                    {c.saldoGols}
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      )}

                      {g.times.length >= 2 && (
                        <Button
                          size="sm"
                          variant="secondary"
                          className="w-full mt-3"
                          onClick={() => handleGenerateFixtures(g.id)}
                          disabled={generatingFixtures === g.id}
                        >
                          {generatingFixtures === g.id ? 'Gerando...' : 'Gerar partidas'}
                        </Button>
                      )}
                    </Card>
                  ))}
                </div>
              )}

              {showCreateGroup && (
                <Modal title="Novo grupo" onClose={() => setShowCreateGroup(false)}>
                  <div className="space-y-4">
                    <div>
                      <label
                        htmlFor="grupoNome"
                        className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
                      >
                        Nome do grupo
                      </label>
                      <input
                        id="grupoNome"
                        value={grupoNome}
                        onChange={(e) => setGrupoNome(e.target.value)}
                        className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                        placeholder="Ex: A, B, 1, 2"
                      />
                    </div>
                    <div className="flex justify-end gap-3">
                      <Button variant="ghost" onClick={() => setShowCreateGroup(false)}>
                        Cancelar
                      </Button>
                      <Button onClick={handleCreateGroup} disabled={!grupoNome || createGroupLoading}>
                        {createGroupLoading ? 'Criando...' : 'Criar'}
                      </Button>
                    </div>
                  </div>
                </Modal>
              )}

              {showAddTeam && (
                <Modal title="Adicionar time" onClose={() => setShowAddTeam(null)}>
                  <div className="space-y-4">
                    <div>
                      <label
                        htmlFor="selectedTimeId"
                        className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
                      >
                        Time
                      </label>
                      <select
                        id="selectedTimeId"
                        value={selectedTimeId}
                        onChange={(e) => setSelectedTimeId(Number(e.target.value))}
                        className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                      >
                        <option value={0}>Selecione...</option>
                        {times
                          .filter((t) => !grupos.find((g) => g.id === showAddTeam)?.times.some((gt) => gt.id === t.id))
                          .map((t) => (
                            <option key={t.id} value={t.id}>
                              {t.nome} ({t.sigla})
                            </option>
                          ))}
                      </select>
                    </div>
                    <div className="flex justify-end gap-3">
                      <Button variant="ghost" onClick={() => setShowAddTeam(null)}>
                        Cancelar
                      </Button>
                      <Button onClick={() => handleAddTeam(showAddTeam)} disabled={!selectedTimeId || addingTeam}>
                        {addingTeam ? 'Adicionando...' : 'Adicionar'}
                      </Button>
                    </div>
                  </div>
                </Modal>
              )}
            </div>
          )}

          {subTab === 'matamata' && (
            <div className="space-y-6">
              <div className="flex justify-end">
                <Button size="sm" onClick={() => setShowCreatePhase(true)}>
                  <Plus size={13} /> Nova fase
                </Button>
              </div>

              {fases.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 text-center glass rounded-2xl">
                  <Swords size={28} className="text-slate-600 mb-3" />
                  <p className="text-sm text-slate-400 font-medium">Nenhuma fase criada</p>
                  <p className="text-xs text-slate-600 mt-1">Crie fases para montar o chaveamento</p>
                </div>
              ) : (
                fases
                  .sort((a, b) => faseOrder.indexOf(a.fase) - faseOrder.indexOf(b.fase))
                  .map((fase) => (
                    <div key={fase.id}>
                      <div className="flex items-center justify-between mb-3">
                        <div className="flex items-center gap-2">
                          <Trophy size={15} className="text-accent" />
                          <h3 className="text-sm font-bold text-slate-200">{faseLabel[fase.fase]}</h3>
                          {fase.jogoUnico && (
                            <span className="text-[10px] text-slate-500 bg-white/[0.04] px-2 py-0.5 rounded">
                              Jogo único
                            </span>
                          )}
                        </div>
                        {fase.confrontos.length === 0 && (
                          <Button size="sm" variant="secondary" onClick={() => handleGenerate(fase.id)}>
                            Gerar confrontos
                          </Button>
                        )}
                      </div>

                      {fase.confrontos.length === 0 ? (
                        <Card className="p-4">
                          <p className="text-xs text-slate-500 text-center py-3">
                            {fase.fase === 'QUARTAS' || fase.fase === 'SEMIFINAL' || fase.fase === 'FINAL'
                              ? 'Os confrontos serão gerados automaticamente quando a fase anterior for concluída'
                              : 'Clique em "Gerar confrontos" para distribuir os times'}
                          </p>
                        </Card>
                      ) : (
                        <BracketView fases={fases} getLogoUrl={getLogoUrl} />
                      )}
                    </div>
                  ))
              )}

              {showCreatePhase && (
                <Modal title="Nova fase" onClose={() => setShowCreatePhase(false)}>
                  <div className="space-y-4">
                    <div>
                      <label
                        htmlFor="selectedFase"
                        className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
                      >
                        Fase
                      </label>
                      <select
                        id="selectedFase"
                        value={selectedFase}
                        onChange={(e) => setSelectedFase(e.target.value as FaseEnum)}
                        className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                      >
                        {faseOrder.map((f) => (
                          <option key={f} value={f}>
                            {faseLabel[f]}
                          </option>
                        ))}
                      </select>
                    </div>
                    <label className="flex items-center gap-2 text-sm text-slate-300">
                      <input
                        type="checkbox"
                        checked={jogoUnico}
                        onChange={(e) => setJogoUnico(e.target.checked)}
                        className="rounded border-white/20 bg-white/5"
                      />
                      Jogo único (sem ida e volta)
                    </label>
                    <div className="flex justify-end gap-3">
                      <Button variant="ghost" onClick={() => setShowCreatePhase(false)}>
                        Cancelar
                      </Button>
                      <Button onClick={handleCreatePhase} disabled={createPhaseLoading}>
                        {createPhaseLoading ? 'Criando...' : 'Criar'}
                      </Button>
                    </div>
                  </div>
                </Modal>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}
