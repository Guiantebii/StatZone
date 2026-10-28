import { useCallback, useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { Plus, Filter, Calendar } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { Partida } from '../types/partida';
import type { Campeonato } from '../types/campeonato';
import PartidaCard from '../components/PartidaCard';
import PageHeader from '../components/ui/PageHeader';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import { SkeletonCard } from '../components/ui/Skeleton';
import PartidaForm from '../components/PartidaForm';
import { toast } from 'sonner';
import { STATUS_LABEL } from '../constants/status';
import { useAuth } from '../context/AuthContext';

const statusList = [
  'TODOS',
  'AO_VIVO',
  'INTERVALO',
  'PENALTIS',
  'AGENDADA',
  'ENCERRADA',
  'ADIADA',
  'CANCELADA',
  'WO_MANDANTE',
  'WO_VISITANTE',
] as const;

const TAMANHO = 20;

export default function PartidasPage() {
  const [aoVivo, setAoVivo] = useState<Partida[]>([]);
  const [partidas, setPartidas] = useState<Partida[]>([]);
  const [campeonatos, setCampeonatos] = useState<Campeonato[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const { isAdmin } = useAuth();
  const location = useLocation();
  const isDashboardContext = location.pathname.startsWith('/dashboard');
  const [filterStatus, setFilterStatus] = useState<string>('TODOS');
  const [filterCampeonato, setFilterCampeonato] = useState<string>('TODOS');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const campeonatosEndpoint = isDashboardContext ? '/campeonatos' : '/campeonatos?publico=true';

  const partidasUrl = useCallback(
    (pageNumber: number) => {
      const params = new URLSearchParams({ pagina: String(pageNumber), tamanho: String(TAMANHO) });
      if (filterStatus !== 'TODOS') params.set('status', filterStatus);
      if (filterCampeonato !== 'TODOS') params.set('campeonatoId', filterCampeonato);
      return `/partidas?${params.toString()}`;
    },
    [filterStatus, filterCampeonato],
  );

  const filtroRef = useRef(0);

  useEffect(() => {
    const geracao = ++filtroRef.current;
    const controller = new AbortController();
    let isMounted = true;
    Promise.all([
      api.get(partidasUrl(0), { signal: controller.signal }),
      api.get(campeonatosEndpoint, { signal: controller.signal }),
      api.get('/partidas/painel', { signal: controller.signal }),
    ])
      .then(([partidasRes, campeonatosRes, painelRes]) => {
        if (!isMounted || geracao !== filtroRef.current) return;
        setPartidas(partidasRes.data);
        setCampeonatos(campeonatosRes.data);
        setAoVivo(painelRes.data.aoVivo);
        setHasMore((partidasRes.data as Partida[]).length === TAMANHO);
        setPage(1);
      })
      .catch((err) => {
        if (controller.signal.aborted) return;
        toast.error(getApiError(err, 'Erro ao carregar partidas'));
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });
    return () => {
      isMounted = false;
      controller.abort();
    };
  }, [partidasUrl, campeonatosEndpoint]);

  const loadMore = () => {
    if (loadingMore || !hasMore) return;
    setLoadingMore(true);
    const geracao = filtroRef.current;
    api
      .get(partidasUrl(page))
      .then((res) => {
        if (geracao !== filtroRef.current) return;
        const next = res.data as Partida[];
        setPartidas((prev) => [...prev, ...next]);
        setHasMore(next.length === TAMANHO);
        setPage((p) => p + 1);
      })
      .catch((err) => {
        toast.error(getApiError(err, 'Erro ao carregar mais partidas'));
      })
      .finally(() => {
        if (geracao === filtroRef.current) setLoadingMore(false);
      });
  };

  const agendadas = partidas.filter((p) => p.status === 'AGENDADA');

  if (loading)
    return (
      <div className="space-y-6">
        <div className="flex items-start justify-between">
          <div className="space-y-1">
            <div className="h-8 w-40 rounded-lg animate-shimmer" />
            <div className="h-4 w-56 rounded-lg animate-shimmer" />
          </div>
        </div>
        <div className="flex gap-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-8 w-24 rounded-lg animate-shimmer" />
          ))}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      </div>
    );

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader
        title="Partidas"
        description="Acompanhe as partidas"
        actions={
          isAdmin && (
            <Button onClick={() => setShowForm(true)}>
              <Plus size={15} />
              Nova partida
            </Button>
          )
        }
      />

      {aoVivo.length > 0 && (
        <div>
          <div className="flex items-center gap-2 mb-3">
            <span className="w-2 h-2 rounded-full bg-success animate-pulse" />
            <h2 className="text-sm font-semibold text-slate-200">Ao Vivo</h2>
            <span className="text-xs text-slate-500">({aoVivo.length})</span>
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-4">
            {aoVivo.map((p) => (
              <PartidaCard key={p.id} partida={p} />
            ))}
          </div>
        </div>
      )}

      <Card className="p-3">
        <div className="flex items-center gap-4 flex-wrap">
          <Filter size={14} className="text-slate-500" />
          <div className="overflow-x-auto">
            <div className="flex gap-1.5">
              {statusList.map((status) => (
                <button
                  key={status}
                  onClick={() => setFilterStatus(status)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                    filterStatus === status
                      ? 'bg-accent/10 text-accent border border-accent/20'
                      : 'text-slate-500 hover:text-slate-300 hover:bg-white/[0.04] border border-transparent'
                  }`}
                >
                  {status === 'TODOS' ? 'Todas' : STATUS_LABEL[status] || status}
                </button>
              ))}
            </div>
          </div>
          <div className="w-px h-6 bg-white/[0.06]" />
          <select
            value={filterCampeonato}
            onChange={(e) => setFilterCampeonato(e.target.value)}
            className="bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-1.5 text-xs text-slate-300 focus:outline-none focus:border-accent/40"
          >
            <option value="TODOS">Todos os campeonatos</option>
            {campeonatos.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </select>
        </div>
      </Card>

      {partidas.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-center glass rounded-2xl">
          <div className="w-14 h-14 rounded-2xl bg-accent/5 flex items-center justify-center mb-4">
            <Calendar size={28} className="text-accent/40" />
          </div>
          <p className="text-sm text-slate-400 font-medium">Nenhuma partida encontrada</p>
          <p className="text-xs text-slate-600 mt-1">
            {filterStatus !== 'TODOS' ? 'Tente outro filtro' : 'Clique em "Nova partida" para começar'}
          </p>
        </div>
      ) : (
        <div>
          {filterStatus === 'TODOS' && (
            <div className="flex items-center gap-2 mb-3">
              <h2 className="text-sm font-semibold text-slate-200">Próximas partidas</h2>
              <span className="text-xs text-slate-500">({agendadas.length})</span>
            </div>
          )}
          <div className="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-4">
            {partidas.map((p) => (
              <PartidaCard key={p.id} partida={p} />
            ))}
          </div>

          {hasMore && partidas.length > 0 && (
            <div className="flex justify-center mt-2">
              <Button onClick={loadMore} disabled={loadingMore} className="text-xs">
                {loadingMore ? 'Carregando...' : 'Carregar mais'}
              </Button>
            </div>
          )}
        </div>
      )}

      {showForm && isAdmin && (
        <PartidaForm
          onClose={() => setShowForm(false)}
          onSaved={() => {
            setShowForm(false);
            setPartidas([]);
            setPage(0);
            setHasMore(true);
            setLoading(true);
            Promise.all([api.get(partidasUrl(0)), api.get(campeonatosEndpoint), api.get('/partidas/painel')])
              .then(([partidasRes, campeonatosRes, painelRes]) => {
                setPartidas(partidasRes.data);
                setCampeonatos(campeonatosRes.data);
                setAoVivo(painelRes.data.aoVivo);
                setHasMore((partidasRes.data as Partida[]).length === TAMANHO);
                setPage(1);
              })
              .catch((err) => {
                toast.error(getApiError(err, 'Erro ao carregar partidas'));
              })
              .finally(() => setLoading(false));
          }}
          campeonatos={campeonatos}
        />
      )}
    </div>
  );
}
