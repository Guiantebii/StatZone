import { useEffect, useState } from 'react';
import { Search, UserPlus, Users } from 'lucide-react';
import { Link } from 'react-router-dom';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import { posicaoLabel, getJogadorAvatarUrl } from '../constants/helpers';
import type { Jogador } from '../types/jogador';
import JogadorForm from '../components/JogadorForm';
import ConfirmModal from '../components/ui/ConfirmModal';
import PageHeader from '../components/ui/PageHeader';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import StatCard from '../components/ui/StatCard';
import { SkeletonCard, SkeletonTable } from '../components/ui/Skeleton';
import { toast } from 'sonner';

export default function JogadoresPage() {
  const [jogadores, setJogadores] = useState<Jogador[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editData, setEditData] = useState<Jogador | null>(null);
  const [search, setSearch] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<{ id: number; nome: string } | null>(null);

  useEffect(() => {
    let isMounted = true;
    api
      .get('/jogadores')
      .then((res) => {
        if (isMounted) setJogadores(res.data);
      })
      .catch((err) => {
        toast.error(getApiError(err, 'Erro ao carregar jogadores'));
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
      .get('/jogadores')
      .then((res) => setJogadores(res.data))
      .catch((err) => {
        toast.error(getApiError(err, 'Erro ao carregar jogadores'));
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
  const openEdit = (jogador: Jogador) => {
    setEditData(jogador);
    setShowForm(true);
  };

  const posicaoBadgeClass = (posicao: string) => {
    const defesas = ['GOLEIRO', 'ZAGUEIRO', 'LATERAL_DIREITO', 'LATERAL_ESQUERDO'];
    const meias = ['VOLANTE', 'MEIO_CAMPO', 'MEIO_ESQUERDO', 'MEIO_DIREITO', 'MEIA_ATACANTE'];
    if (defesas.includes(posicao)) return 'bg-info-bg text-info border border-info-border';
    if (meias.includes(posicao)) return 'bg-success-bg text-success border border-success-border';
    return 'bg-warning-bg text-warning border border-warning-border';
  };

  const filtered = jogadores.filter(
    (j) =>
      j.nome.toLowerCase().includes(search.toLowerCase()) ||
      j.nomeTime?.toLowerCase().includes(search.toLowerCase()) ||
      posicaoLabel(j.posicao).toLowerCase().includes(search.toLowerCase()),
  );

  if (loading)
    return (
      <div className="space-y-6">
        <div className="flex items-start justify-between">
          <div className="space-y-1">
            <div className="h-8 w-36 rounded-lg animate-shimmer" />
            <div className="h-4 w-56 rounded-lg animate-shimmer" />
          </div>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <SkeletonCard />
          <SkeletonCard />
          <SkeletonCard />
        </div>
        <SkeletonTable rows={5} />
      </div>
    );

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader
        title="Jogadores"
        description="Gerencie todos os jogadores cadastrados na plataforma"
        actions={
          <Button onClick={() => setShowForm(true)}>
            <UserPlus size={15} /> Novo jogador
          </Button>
        }
      />

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard label="Total" value={jogadores.length} sublabel="jogadores" />
        <StatCard label="Em busca" value={filtered.length} sublabel="filtrados" />
        <StatCard label="Times representados" value={new Set(jogadores.map((j) => j.timeId)).size} sublabel="times" />
      </div>

      <Card className="overflow-hidden">
        <div className="flex items-center justify-between px-5 py-3.5 border-b border-white/[0.04]">
          <span className="text-sm font-semibold text-slate-200">Todos os jogadores</span>
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
              <Users size={28} className="text-accent/40" />
            </div>
            <p className="text-sm text-slate-400 font-medium">Nenhum jogador encontrado</p>
            <p className="text-xs text-slate-600 mt-1">
              {search ? 'Tente outro termo de busca' : 'Clique em "Novo jogador" para começar'}
            </p>
          </div>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="bg-white/[0.02]">
                <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Jogador
                </th>
                <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Posição
                </th>
                <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Time
                </th>
                <th className="text-right px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">
                  Ações
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.03]">
              {filtered.map((j) => (
                <tr key={j.id} className="group hover:bg-white/[0.02] transition-colors">
                  <td className="px-5 py-3.5">
                    <Link
                      to={`/jogadores/${j.id}`}
                      className="flex items-center gap-3 hover:opacity-80 transition-opacity"
                    >
                      <img
                        src={j.fotoUrl || getJogadorAvatarUrl(j.nome, 32)}
                        alt={j.nome}
                        className="w-9 h-9 rounded-full object-cover bg-white/5 ring-2 ring-white/[0.06]"
                        onError={(e) => {
                          (e.target as HTMLImageElement).src = getJogadorAvatarUrl('??', 32);
                        }}
                      />
                      <div>
                        <p className="text-sm font-medium text-slate-200">{j.nome}</p>
                        <p className="text-xs text-slate-500">
                          {j.nacionalidade ?? 'N/D'} · {j.altura ?? '?'}m · {j.peso ?? '?'}kg
                        </p>
                      </div>
                    </Link>
                  </td>
                  <td className="px-5 py-3.5">
                    <span
                      className={`inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full ${posicaoBadgeClass(j.posicao)}`}
                    >
                      {posicaoLabel(j.posicao)}
                    </span>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className="text-sm text-slate-300 font-medium">{j.nomeTime}</span>
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <div className="flex items-center justify-end gap-1.5">
                      <Button variant="outline" size="sm" onClick={() => openEdit(j)}>
                        Editar
                      </Button>
                      <Button variant="danger" size="sm" onClick={() => handleDelete(j.id, j.nome)}>
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

      {showForm && <JogadorForm jogador={editData} onClose={handleFormClose} onSaved={handleSaved} />}

      {deleteTarget && (
        <ConfirmModal
          title="Excluir jogador"
          message={`Tem certeza que deseja excluir "${deleteTarget.nome}"?`}
          onConfirm={async () => {
            try {
              await api.delete(`/jogadores/${deleteTarget.id}`);
              setJogadores((prev) => prev.filter((j) => j.id !== deleteTarget.id));
              toast.success('Jogador excluído');
              setDeleteTarget(null);
            } catch (err) {
              toast.error(getApiError(err, 'Erro ao excluir jogador'));
              setDeleteTarget(null);
            }
          }}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  );
}
