import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Shield } from 'lucide-react';
import api from '../api/client';
import type { Time } from '../types/time';
import Card from '../components/ui/Card';
import { SkeletonCard } from '../components/ui/Skeleton';
import { getLogoUrl } from '../constants/helpers';
import { toast } from 'sonner';

export default function PublicTimesPage() {
  const [times, setTimes] = useState<Time[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/times')
      .then((r) => setTimes(r.data))
      .catch(() => { toast.error('Erro ao carregar times'); })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        {Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)}
      </div>
    );
  }

  if (times.length === 0) {
    return (
      <div className="text-center py-20">
        <Shield size={40} className="text-slate-600 mx-auto mb-3" />
        <p className="text-sm text-slate-500">Nenhum time disponível</p>
      </div>
    );
  }

  return (
    <div className="animate-fade-in-up">
      <h1 className="text-2xl font-bold text-slate-100 tracking-tight mb-6">Times</h1>
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        {times.map((t) => (
          <Link key={t.id} to={`/times/${t.id}`}>
            <Card className="p-5 hover:bg-white/[0.04] transition-colors h-full text-center">
              <img
                  src={t.escudoUrl || getLogoUrl(t.nome)}
                  alt={t.nome}
                  className="w-14 h-14 rounded-xl mx-auto mb-3 bg-white/5"
                  onError={(e) => { (e.target as HTMLImageElement).src = getLogoUrl(t.nome); }}
                />
              <p className="text-sm font-semibold text-slate-200 truncate">{t.nome}</p>
              <p className="text-xs text-slate-500 mt-0.5">{t.sigla || t.pais}</p>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
