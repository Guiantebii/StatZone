import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trophy, Search, Plus } from 'lucide-react';
import { getLogoUrl } from '../constants/helpers';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { Campeonato } from '../types/campeonato';
import type { Time } from '../types/time';
import CampeonatoForm from '../components/CampeonatoForm';
import ConfirmModal from '../components/ui/ConfirmModal';
import PageHeader from '../components/ui/PageHeader';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import StatCard from '../components/ui/StatCard';
import Modal from '../components/ui/Modal';
import { SkeletonCard, SkeletonTable } from '../components/ui/Skeleton';
import { toast } from 'sonner';

export default function CampeonatosPage() {
  const navigate = useNavigate();
  const [campeonatos, setCampeonatos] = useState<Campeonato[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editData, setEditData] = useState<Campeonato | null>(null);
  const [search, setSearch] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<{ id: number; nome: string } | null>(null);

  const [manageTarget, setManageTarget] = useState<{ id: number; nome: string } | null>(null);
  const [campeonatoTimes, setCampeonatoTimes] = useState<Time[]>([]);
  const [allTimes, setAllTimes] = useState<Time[]>([]);
  const [selectedAddTimeId, setSelectedAddTimeId] = useState(0);
  const [manageLoading, setManageLoading] = useState(false);

  useEffect(() => {
    let isMounted = true;
    api
      .get('/campeonatos')
      .then((res) => {
        if (isMounted) setCampeonatos(res.data);
      })
      .catch(() => {
        toast.error('Erro ao carregar campeonatos');
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });
    return () => {
      isMounted = false;
    };
  }, []);

  const load = () => {
    api
      .get('/campeonatos')
      .then((res) => setCampeonatos(res.data))
      .catch(() => {
        toast.error('Erro ao carregar campeonatos');
      });
  };

  const handleDelete = (id: number, nome: string) => {
    setDeleteTarget({ id, nome });
  };

  const handleFormClose = () => {
    setShowForm(false);
    setEditData(null);
  };
  const handleSaved = () => {
    handleFormClose();
    load();
  };
  const openEdit = (campeonato: Campeonato) => {
    setEditData(campeonato);
    setShowForm(true);
  };

  const openManageTimes = async (campeonato: Campeonato) => {
    setManageTarget({ id: campeonato.id, nome: campeonato.nome });
    setSelectedAddTimeId(0);
    setManageLoading(true);
    try {
      const [timesRes, allRes] = await Promise.all([api.get(`/campeonatos/${campeonato.id}/times`), api.get('/times')]);
      setCampeonatoTimes(timesRes.data);
      setAllTimes(allRes.data);
    } catch {
      toast.error('Erro ao carregar times');
    } finally {
      setManageLoading(false);
    }
  };

  const closeManageTimes = () => {
    setManageTarget(null);
    setCampeonatoTimes([]);
    setAllTimes([]);
  };

  const handleAddTime = async () => {
    if (!manageTarget || !selectedAddTimeId) return;
    try {
      await api.post(`/campeonatos/${manageTarget.id}/times/${selectedAddTimeId}`);
      toast.success('Time adicionado ao campeonato');
      setSelectedAddTimeId(0);
      const res = await api.get(`/campeonatos/${manageTarget.id}/times`);
      setCampeonatoTimes(res.data);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao adicionar time'));
    }
  };

  const formatarTipo = (tipo?: string) => {
    switch (tipo) {
      case 'MATA_MATA':
        return 'Mata-Mata';
      case 'GRUPOS_E_MATA_MATA':
        return 'Grupos e Mata-Mata';
      default:
        return 'Pontos Corridos';
    }
  };

  const badgeClasses = (tipo?: string) => {
    switch (tipo) {
      case 'MATA_MATA':
        return 'bg-danger-bg text-danger border border-danger-border';
      case 'GRUPOS_E_MATA_MATA':
        return 'bg-info-bg text-info border border-info-border';
      default:
        return 'bg-success-bg text-success border border-success-border';
    }
  };

  const initials = (nome: string) =>
    nome
      .split(' ')
      .map((w) => w[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();

  const filtered = campeonatos.filter(
    (c) => c.nome.toLowerCase().includes(search.toLowerCase()) || c.pais?.toLowerCase().includes(search.toLowerCase()),
  );

  const totalFormatos = (tipo: string) =>
    campeonatos.filter((c) => (c.tipoFormato || 'PONTOS_CORRIDOS') === tipo).length;

  const formatoMaisUsado = () => {
    if (campeonatos.length === 0) return { tipo: 'PONTOS_CORRIDOS', count: 0 };
    const counts = {
      PONTOS_CORRIDOS: totalFormatos('PONTOS_CORRIDOS'),
      MATA_MATA: totalFormatos('MATA_MATA'),
      GRUPOS_E_MATA_MATA: totalFormatos('GRUPOS_E_MATA_MATA'),
    };
    const max = Object.entries(counts).sort((a, b) => b[1] - a[1])[0];
    return { tipo: max[0], count: max[1] };
  };

  const mediaAmarelos = campeonatos.length
    ? Math.round(campeonatos.reduce((acc, c) => acc + (c.amarelosParaSuspensao || 3), 0) / campeonatos.length)
    : 3;

  if (loading)
    return (
      <div className="space-y-6">
        <div className="flex items-start justify-between">
          <div className="space-y-1">
            <div className="h-8 w-44 rounded-lg animate-shimmer" />
            <div className="h-4 w-64 rounded-lg animate-shimmer" />
          </div>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <SkeletonCard />
          <SkeletonCard />
          <SkeletonCard />
        </div>
        <SkeletonTable rows={4} />
      </div>
    );

  const { tipo: fmtTipo, count: fmtCount } = formatoMaisUsado();

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader
        title="Campeonatos"
        description="Gerencie todos os campeonatos cadastrados na plataforma"
        actions={
          <Button onClick={() => setShowForm(true)}>
            <Plus size={15} />
            Novo campeonato
          </Button>
        }
      />

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard label="Total" value={campeonatos.length} sublabel="campeonatos" />
        <StatCard
          label="Formato mais usado"
          value={formatarTipo(fmtTipo)}
          sublabel={fmtCount > 0 ? `${fmtCount} campeonato(s)` : undefined}
        />
        <StatCard label="Suspensão média" value={`${mediaAmarelos}`} sublabel="cartões" />
      </div>

      <Card className="overflow-hidden">
        <div className="flex items-center justify-between px-5 py-3.5 border-b border-white/[0.04]">
          <span className="text-sm font-semibold text-slate-200">Todos os campeonatos</span>
          <Input
            placeholder="Buscar..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-40"
            leftIcon={<Search size={13} />}
          />
        </div>

        {filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="w-14 h-14 rounded-2xl bg-accent/5 flex items-center justify-center mb-4">
              <Trophy size={28} className="text-accent/40" />
            </div>
            <p className="text-sm text-slate-400 font-medium">Nenhum campeonato encontrado</p>
            <p className="text-xs text-slate-600 mt-1">
              {search ? 'Tente outro termo de busca' : 'Clique em "Novo campeonato" para começar'}
            </p>
          </div>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="bg-white/[0.02]">
                <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Campeonato
                </th>
                <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Formato
                </th>
                <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Status
                </th>
                <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Suspensão
                </th>
                <th className="text-right px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Ações
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.03]">
              {filtered.map((c) => (
                <tr
                  key={c.id}
                  className="group hover:bg-white/[0.02] transition-colors cursor-pointer"
                  onClick={() => navigate(`/dashboard/campeonatos/${c.id}`)}
                >
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-3">
                      {c.logoUrl ? (
                        <img
                          src={c.logoUrl}
                          alt={c.nome}
                          className="w-9 h-9 rounded-xl object-contain bg-white/5 ring-1 ring-white/[0.06]"
                        />
                      ) : (
                        <div className="w-9 h-9 rounded-xl bg-accent/10 flex items-center justify-center text-xs font-bold text-accent flex-shrink-0 ring-1 ring-accent/20">
                          {initials(c.nome)}
                        </div>
                      )}
                      <div>
                        <p className="text-sm font-medium text-slate-200">{c.nome}</p>
                        {c.pais && (
                          <p className="text-xs text-slate-500 mt-0.5">
                            {c.pais}
                            {c.temporada ? ` · ${c.temporada}` : ''}
                          </p>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3.5">
                    <span
                      className={`inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full ${badgeClasses(c.tipoFormato)}`}
                    >
                      {formatarTipo(c.tipoFormato)}
                    </span>
                  </td>
                  <td className="px-5 py-3.5">
                    <span
                      className={`inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full ${
                        c.status === 'ATIVO'
                          ? 'bg-success-bg text-success border border-success-border'
                          : 'bg-warning-bg text-warning border border-warning-border'
                      }`}
                    >
                      {c.status === 'ATIVO' ? 'Ativo' : 'Rascunho'}
                    </span>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className="text-sm tabular-nums text-slate-300">{c.amarelosParaSuspensao ?? 3} amarelos</span>
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <div className="flex items-center justify-end gap-1.5">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          openManageTimes(c);
                        }}
                      >
                        Times
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          openEdit(c);
                        }}
                      >
                        Editar
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete(c.id, c.nome);
                        }}
                      >
                        Excluir
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      {showForm && <CampeonatoForm campeonato={editData} onClose={handleFormClose} onSaved={handleSaved} />}

      {deleteTarget && (
        <ConfirmModal
          title="Excluir campeonato"
          message={`Tem certeza que deseja excluir "${deleteTarget.nome}"?`}
          onConfirm={async () => {
            try {
              await api.delete(`/campeonatos/${deleteTarget.id}`);
              setCampeonatos((prev) => prev.filter((c) => c.id !== deleteTarget.id));
              toast.success('Campeonato excluído');
              setDeleteTarget(null);
            } catch (err) {
              toast.error(getApiError(err, 'Erro ao excluir campeonato'));
              setDeleteTarget(null);
            }
          }}
          onCancel={() => setDeleteTarget(null)}
        />
      )}

      {manageTarget && (
        <Modal title={`Times — ${manageTarget.nome}`} onClose={closeManageTimes}>
          {manageLoading ? (
            <p className="text-sm text-slate-500 text-center py-4">Carregando...</p>
          ) : (
            <div className="space-y-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                  Times no campeonato
                </p>
                {campeonatoTimes.length === 0 ? (
                  <p className="text-sm text-slate-500 py-2">Nenhum time adicionado</p>
                ) : (
                  <div className="space-y-1 max-h-40 overflow-y-auto">
                    {campeonatoTimes.map((t) => (
                      <div key={t.id} className="flex items-center gap-2 py-1.5 px-2 rounded-lg bg-white/[0.02]">
                        <img
                          src={t.escudoUrl || getLogoUrl(t.nome, 24)}
                          alt={t.nome}
                          className="w-5 h-5 rounded-full bg-white/5"
                        />
                        <span className="text-sm text-slate-300">{t.nome}</span>
                        <span className="text-[10px] text-slate-600">{t.sigla}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="border-t border-white/[0.06] pt-4">
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">Adicionar time</p>
                <div className="flex gap-2">
                  <select
                    value={selectedAddTimeId}
                    onChange={(e) => setSelectedAddTimeId(Number(e.target.value))}
                    className="flex-1 bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                  >
                    <option value={0}>Selecione...</option>
                    {allTimes
                      .filter((t) => !campeonatoTimes.some((ct) => ct.id === t.id))
                      .map((t) => (
                        <option key={t.id} value={t.id}>
                          {t.nome} ({t.sigla})
                        </option>
                      ))}
                  </select>
                  <Button size="sm" onClick={handleAddTime} disabled={!selectedAddTimeId}>
                    Adicionar
                  </Button>
                </div>
              </div>
            </div>
          )}
        </Modal>
      )}
    </div>
  );
}
