import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Cake,
  Shirt,
  Ruler,
  Weight,
  DollarSign,
  Flag,
  Dumbbell,
  Swords,
  Trophy,
  Medal,
  AlertTriangle,
  Shield,
} from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import { posicaoLabel, getJogadorAvatarUrl } from '../constants/helpers';
import type { Jogador } from '../types/jogador';
import type { EstatisticasJogador } from '../types/estatisticasJogador';
import Card from '../components/ui/Card';
import { SkeletonCard } from '../components/ui/Skeleton';
import { toast } from 'sonner';

const peLabel: Record<string, string> = {
  DIREITO: 'Destro',
  ESQUERDO: 'Canhoto',
  AMBIDESTRO: 'Ambidestro',
};

export default function JogadorDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [jogador, setJogador] = useState<Jogador | null>(null);
  const [stats, setStats] = useState<EstatisticasJogador | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    let isMounted = true;
    const load = async () => {
      try {
        const [jRes, sRes] = await Promise.all([
          api.get(`/jogadores/${id}`),
          api.get(`/jogadores/${id}/estatisticas`).catch(() => ({ data: null })),
        ]);
        if (!isMounted) return;
        setJogador(jRes.data);
        setStats(sRes.data);
      } catch (err) {
        if (isMounted) {
          toast.error(getApiError(err, 'Erro ao carregar jogador'));
          navigate('/');
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

  const idade = (dataNasc: string) => {
    if (!dataNasc) return 0;
    const nasc = new Date(dataNasc);
    if (isNaN(nasc.getTime())) return 0;
    const hoje = new Date();
    let idade = hoje.getFullYear() - nasc.getFullYear();
    const mes = hoje.getMonth() - nasc.getMonth();
    if (mes < 0 || (mes === 0 && hoje.getDate() < nasc.getDate())) idade--;
    return idade;
  };

  const formatValor = (v: number | null | undefined) => {
    if (v == null) return 'N/D';
    if (v >= 1_000_000) return `€${(v / 1_000_000).toFixed(1)}M`;
    if (v >= 1_000) return `€${(v / 1_000).toFixed(0)}K`;
    return `€${v}`;
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

  if (!jogador) return null;

  const infoItems = [
    { icon: Cake, label: 'Idade', value: jogador.dataNascimento ? `${idade(jogador.dataNascimento)} anos` : '—' },
    { icon: Shirt, label: 'Camisa', value: jogador.numeroCamisa ? `#${jogador.numeroCamisa}` : '—' },
    { icon: Ruler, label: 'Altura', value: jogador.altura ? `${jogador.altura}m` : '—' },
    { icon: Weight, label: 'Peso', value: jogador.peso ? `${jogador.peso}kg` : '—' },
    { icon: Flag, label: 'Nacionalidade', value: jogador.nacionalidade || '—' },
    { icon: Dumbbell, label: 'Pé Forte', value: jogador.peForte ? peLabel[jogador.peForte] || jogador.peForte : '—' },
  ];

  const statCards = stats
    ? [
        { icon: Swords, label: 'Partidas', value: stats.partidasJogadas, color: 'text-blue-400' },
        { icon: Trophy, label: 'Gols', value: stats.gols, color: 'text-accent' },
        { icon: Medal, label: 'Assistências', value: stats.assistencias, color: 'text-emerald-400' },
        { icon: AlertTriangle, label: 'Amarelos', value: stats.cartoesAmarelos, color: 'text-yellow-400' },
        { icon: Shield, label: 'Vermelhos', value: stats.cartoesVermelhos, color: 'text-red-400' },
      ]
    : [];

  return (
    <div className="space-y-6 animate-fade-in-up">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-200 transition-colors"
      >
        <ArrowLeft size={15} />
        Voltar
      </button>

      <Card className="overflow-hidden">
        <div className="flex items-center gap-5 p-6">
          <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-accent/20 to-accent/5 flex items-center justify-center shrink-0">
            <img
              src={jogador.fotoUrl || getJogadorAvatarUrl(jogador.nome, 80)}
              alt={jogador.nome}
              className="w-16 h-16 rounded-xl object-cover"
            />
          </div>
          <div className="min-w-0">
            <h1 className="text-2xl font-bold text-slate-100 tracking-tight">{jogador.nome}</h1>
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-1">
              <span className="text-sm font-medium text-accent">{posicaoLabel(jogador.posicao)}</span>
              <Link
                to={`/times/${jogador.timeId}`}
                className="text-sm text-slate-400 hover:text-slate-200 transition-colors"
              >
                {jogador.nomeTime}
              </Link>
            </div>
          </div>
          <div className="ml-auto text-right hidden sm:block">
            <p className="text-xs text-slate-500">Valor de Mercado</p>
            <p className="text-lg font-bold text-emerald-400 font-mono">{formatValor(jogador.valorMercado)}</p>
          </div>
        </div>
      </Card>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
        {infoItems.map((item) => (
          <Card key={item.label} className="p-4 text-center">
            <item.icon size={16} className="text-accent/60 mx-auto mb-1.5" />
            <p className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold">{item.label}</p>
            <p className="text-sm font-semibold text-slate-200 mt-0.5">{item.value}</p>
          </Card>
        ))}

        <Card className="p-4 text-center sm:hidden">
          <DollarSign size={16} className="text-emerald-400/60 mx-auto mb-1.5" />
          <p className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold">Valor</p>
          <p className="text-sm font-semibold text-emerald-400 mt-0.5">{formatValor(jogador.valorMercado)}</p>
        </Card>
      </div>

      {stats && (
        <Card className="overflow-hidden">
          <div className="px-5 py-3.5 border-b border-white/[0.04]">
            <span className="text-sm font-semibold text-slate-200">Estatísticas da carreira</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-0 divide-x divide-white/[0.03]">
            {statCards.map((s) => (
              <div key={s.label} className="p-5 text-center">
                <s.icon size={18} className={`${s.color} mx-auto mb-2`} />
                <p className="text-2xl font-bold text-slate-100 font-mono">{s.value}</p>
                <p className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mt-1">{s.label}</p>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}
