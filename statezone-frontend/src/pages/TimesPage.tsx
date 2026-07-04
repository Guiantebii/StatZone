import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Globe, MapPin, Building2, UserRound, Shield, Plus } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { Time } from '../types/time';
import TimeForm from '../components/TimeForm';
import ConfirmModal from '../components/ui/ConfirmModal';
import PageHeader from '../components/ui/PageHeader';
import Button from '../components/ui/Button';
import StatCard from '../components/ui/StatCard';
import { SkeletonCard } from '../components/ui/Skeleton';
import { getAvatarUrl } from '../constants/helpers';
import { toast } from 'sonner';

export default function TimesPage() {
  const navigate = useNavigate();
  const [times, setTimes] = useState<Time[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editData, setEditData] = useState<Time | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<{ id: number; nome: string } | null>(null);

  useEffect(() => {
    let isMounted = true;
    api.get('/times').then((res) => {
      if (isMounted) setTimes(res.data);
    }).catch((err) => {
      toast.error(getApiError(err, 'Erro ao carregar times'));
    }).finally(() => {
      if (isMounted) setLoading(false);
    });
    return () => { isMounted = false; };
  }, []);

  const load = () => {
    api.get('/times').then((res) => setTimes(res.data)).catch((err) => {
      toast.error(getApiError(err, 'Erro ao carregar times'));
    });
  };

  const handleDelete = (id: number, nome: string) => {
    setDeleteTarget({ id, nome });
  };

  const handleFormClose = () => { setShowForm(false); setEditData(null); };
  const handleSaved = () => { handleFormClose(); load(); };
  const openEdit = (time: Time) => { setEditData(time); setShowForm(true); };

  if (loading) return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <div className="h-8 w-32 rounded-lg animate-shimmer" />
          <div className="h-4 w-52 rounded-lg animate-shimmer" />
        </div>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-6">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="glass rounded-2xl p-5 space-y-4">
            <div className="flex flex-col items-center gap-3">
              <div className="w-16 h-16 rounded-full animate-shimmer" />
              <div className="h-4 w-32 rounded-lg animate-shimmer" />
              <div className="h-3 w-16 rounded-lg animate-shimmer" />
            </div>
            <div className="space-y-2">
              {Array.from({ length: 4 }).map((_, j) => (
                <div key={j} className="flex justify-between">
                  <div className="h-3 w-16 rounded animate-shimmer" />
                  <div className="h-3 w-24 rounded animate-shimmer" />
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader
        title="Times"
        description="Gerencie clubes e seleções cadastrados"
        actions={<Button onClick={() => { setEditData(null); setShowForm(true); }}><Plus size={15} /> Novo time</Button>}
      />

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard label="Total de times" value={times.length} sublabel="times" />
        <StatCard label="Países" value={new Set(times.map(t => t.pais)).size} sublabel="nacionalidades" />
        <StatCard label="Estádios" value={new Set(times.filter(t => t.estadio).map(t => t.estadio)).size} sublabel="diferentes" />
      </div>

      {times.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-center glass rounded-2xl">
          <div className="w-14 h-14 rounded-2xl bg-accent/5 flex items-center justify-center mb-4">
            <Shield size={28} className="text-accent/40" />
          </div>
          <p className="text-sm text-slate-400 font-medium">Nenhum time cadastrado</p>
          <p className="text-xs text-slate-600 mt-1">Clique em "Novo time" para começar</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-6">
          {times.map((t, idx) => (
            <div
              key={t.id}
              className="group glass rounded-2xl p-5 border border-white/[0.06] hover:border-accent/20 transition-all duration-300 hover:shadow-[0_8px_30px_rgba(255,215,0,0.06)] hover:-translate-y-1"
              style={{ animationDelay: `${idx * 50}ms` }}
            >
              <div className="flex flex-col items-center mb-4">
                <div className="relative">
                  <div className="absolute inset-0 bg-gradient-to-br from-accent/10 to-transparent rounded-full blur-md opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                  <img
                    src={t.escudoUrl}
                    alt={t.nome}
                    className="w-16 h-16 object-contain mb-3 rounded-full bg-white/5 ring-2 ring-white/[0.06] relative"
                    onError={(e) => { (e.target as HTMLImageElement).src = getAvatarUrl(t.sigla || '?', 64, 'DC052D', 'fff'); }}
                  />
                </div>
                <h3 className="text-base font-bold text-slate-100 text-center">{t.nome}</h3>
                <span className="text-xs text-accent font-semibold mt-0.5">{t.sigla}</span>
                {t.tipo && (
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full mt-1 ${t.tipo === 'SELECAO' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-blue-500/10 text-blue-400'}`}>
                    {t.tipo === 'SELECAO' ? 'SELEÇÃO' : 'CLUBE'}
                  </span>
                )}
              </div>

              <div className="space-y-2.5 text-sm">
                <div className="flex justify-between items-center py-1.5 px-3 rounded-lg bg-white/[0.02]">
                  <span className="text-slate-500 flex items-center gap-1.5"><Globe size={14} /> País</span>
                  <span className="text-slate-200 font-medium">{t.pais}</span>
                </div>
                {t.cidade && (
                  <div className="flex justify-between items-center py-1.5 px-3 rounded-lg bg-white/[0.02]">
                    <span className="text-slate-500 flex items-center gap-1.5"><MapPin size={14} /> Cidade</span>
                    <span className="text-slate-200 font-medium">{t.cidade}</span>
                  </div>
                )}
                {t.estadio && (
                  <div className="flex justify-between items-center py-1.5 px-3 rounded-lg bg-white/[0.02]">
                    <span className="text-slate-500 flex items-center gap-1.5"><Building2 size={14} /> Estádio</span>
                    <span className="text-slate-200 font-medium truncate ml-2 max-w-[130px]">{t.estadio}</span>
                  </div>
                )}
                {t.tecnico && (
                  <div className="flex justify-between items-center py-1.5 px-3 rounded-lg bg-white/[0.02]">
                    <span className="text-slate-500 flex items-center gap-1.5"><UserRound size={14} /> Técnico</span>
                    <span className="text-slate-200 font-medium truncate ml-2 max-w-[130px]">{t.tecnico}</span>
                  </div>
                )}
              </div>

              <div className="flex gap-2 mt-5">
                <Button
                  variant="primary"
                  size="sm"
                  className="flex-1"
                  onClick={() => navigate(`/times/${t.id}`)}
                >
                  Ver time
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  className="flex-1"
                  onClick={() => openEdit(t)}
                >
                  Editar
                </Button>
                <Button
                  variant="danger"
                  size="sm"
                  className="flex-1"
                  onClick={() => handleDelete(t.id, t.nome)}
                >
                  Excluir
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showForm && (
        <TimeForm time={editData} onClose={handleFormClose} onSaved={handleSaved} />
      )}

      {deleteTarget && (
        <ConfirmModal
          title="Excluir time"
          message={`Tem certeza que deseja excluir "${deleteTarget.nome}"?`}
          onConfirm={async () => {
            try {
              await api.delete(`/times/${deleteTarget.id}`);
              setTimes(prev => prev.filter(t => t.id !== deleteTarget.id));
              toast.success('Time excluído');
              setDeleteTarget(null);
            } catch (err) {
              toast.error(getApiError(err, 'Erro ao excluir time'));
              setDeleteTarget(null);
            }
          }}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  );
}
