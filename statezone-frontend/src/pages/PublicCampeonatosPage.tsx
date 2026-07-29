import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Trophy, ChevronRight } from 'lucide-react';
import api from '../api/client';
import type { Campeonato } from '../types/campeonato';
import Card from '../components/ui/Card';
import { SkeletonCard } from '../components/ui/Skeleton';
import { toast } from 'sonner';

export default function PublicCampeonatosPage() {
  const [campeonatos, setCampeonatos] = useState<Campeonato[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get('/campeonatos?publico=true')
      .then((r) => setCampeonatos(r.data))
      .catch(() => {
        toast.error('Erro ao carregar campeonatos');
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {Array.from({ length: 6 }).map((_, i) => (
          <SkeletonCard key={i} />
        ))}
      </div>
    );
  }

  if (campeonatos.length === 0) {
    return (
      <div className="text-center py-20">
        <Trophy size={40} className="text-slate-600 mx-auto mb-3" />
        <p className="text-sm text-slate-500">Nenhum campeonato disponível</p>
      </div>
    );
  }

  return (
    <div className="animate-fade-in-up">
      <h1 className="text-2xl font-bold text-slate-100 tracking-tight mb-6">Campeonatos</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {campeonatos.map((c) => (
          <Link key={c.id} to={`/campeonatos/${c.id}`}>
            <Card className="p-5 hover:bg-white/[0.04] transition-colors h-full">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-xl bg-accent/10 flex items-center justify-center text-accent shrink-0">
                  <Trophy size={22} />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-semibold text-slate-200 truncate">{c.nome}</p>
                  <p className="text-xs text-slate-500 mt-0.5">
                    {c.pais} · {c.temporada}
                  </p>
                </div>
                <ChevronRight size={16} className="text-slate-600 shrink-0" />
              </div>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
