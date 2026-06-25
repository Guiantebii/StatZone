import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Trophy, Clock, ArrowRight, Users, ChevronRight, Medal, Calendar } from 'lucide-react';
import api from '../api/client';
import type { Partida } from '../types/partida';
import type { Campeonato } from '../types/campeonato';
import type { Artilharia } from '../types/estatisticas';
import Card from '../components/ui/Card';
import { SkeletonCard } from '../components/ui/Skeleton';
import { ARTILHARIA_TOP } from '../constants/pagination';
import { STATUS_PARTIDA, STATUS_AO_VIVO, STATUS_ENCERRADA } from '../constants/status';

export default function PublicHomePage() {
  const navigate = useNavigate();
  const [aoVivo, setAoVivo] = useState<Partida[]>([]);
  const [proximas, setProximas] = useState<Partida[]>([]);
  const [recentes, setRecentes] = useState<Partida[]>([]);
  const [campeonatos, setCampeonatos] = useState<Campeonato[]>([]);
  const [artilharia, setArtilharia] = useState<Artilharia[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [partidasRes, campRes] = await Promise.all([
          api.get('/partidas'),
          api.get('/campeonatos'),
        ]);

        const todas: Partida[] = partidasRes.data;

        const live = todas.filter(
          (p) => STATUS_AO_VIVO.includes(p.status as typeof STATUS_AO_VIVO[number])
        );
        const scheduled = todas
          .filter((p) => p.status === STATUS_PARTIDA.AGENDADA)
          .sort((a, b) => new Date(a.dataPartida).getTime() - new Date(b.dataPartida).getTime())
          .slice(0, 8);

        const finished = todas
          .filter((p) => STATUS_ENCERRADA.includes(p.status as typeof STATUS_ENCERRADA[number]))
          .sort((a, b) => new Date(b.dataPartida).getTime() - new Date(a.dataPartida).getTime())
          .slice(0, 5);

        setAoVivo(live);
        setProximas(scheduled);
        setRecentes(finished);
        setCampeonatos(campRes.data);

        if (campRes.data.length > 0) {
          const artRes = await api.get(`/campeonatos/${campRes.data[0].id}/artilharia?pagina=0&tamanho=${ARTILHARIA_TOP}`);
          setArtilharia(artRes.data);
        }
      } catch {
        console.error('Erro ao carregar dados da página inicial');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  useEffect(() => {
    if (aoVivo.length === 0) return;
    const interval = setInterval(async () => {
      try {
        const res = await api.get('/partidas');
        const todas: Partida[] = res.data;
        const live = todas.filter(
          (p) => STATUS_AO_VIVO.includes(p.status as typeof STATUS_AO_VIVO[number])
        );
        setAoVivo(live);
      } catch {
        console.error('Erro ao atualizar partidas ao vivo');
      }
    }, 15000);
    return () => clearInterval(interval);
  }, [aoVivo.length]);

  const getLogoUrl = (nome: string) =>
    `https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=1a3460&color=FFD700&size=64&bold=true`;

  if (loading) {
    return (
      <div className="space-y-8">
        <div className="h-64 rounded-2xl animate-shimmer" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fade-in-up">
      {/* Hero - Ao Vivo */}
      {aoVivo.length > 0 && (
        <section>
          <div className="flex items-center gap-2 mb-4">
            <span className="w-2.5 h-2.5 rounded-full bg-success animate-pulse" />
            <h1 className="text-lg font-bold text-slate-100 uppercase tracking-wider">Ao Vivo</h1>
            <span className="text-xs text-slate-500">({aoVivo.length})</span>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {aoVivo.map((p) => (
              <button
                key={p.id}
                onClick={() => navigate(`/partidas/${p.id}`)}
                className="group text-left w-full"
              >
                <Card hover className="p-5 relative overflow-hidden">
                  <div className="absolute top-0 left-0 w-full h-0.5 bg-gradient-to-r from-success via-success to-transparent animate-pulse" />
                  <div className="flex items-center gap-2 mb-3 text-xs text-slate-500">
                    <Trophy size={12} />
                    <span>{p.campeonatoNome}</span>
                    <span className="text-slate-700">·</span>
                    <span>{p.rodada}ª rod.</span>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="flex-1 text-right">
                      <p className="text-sm font-bold text-slate-200 truncate">{p.timeMandanteNome}</p>
                    </div>
                    <img
                      src={getLogoUrl(p.timeMandanteNome)}
                      alt={p.timeMandanteNome}
                      className="w-10 h-10 rounded-full bg-white/5"
                    />
                    <div className="text-center min-w-[60px]">
                      <span className="text-2xl font-extrabold text-accent font-mono tabular-nums">
                        {p.golsMandante}:{p.golsVisitante}
                      </span>
                    </div>
                    <img
                      src={getLogoUrl(p.timeVisitanteNome)}
                      alt={p.timeVisitanteNome}
                      className="w-10 h-10 rounded-full bg-white/5"
                    />
                    <div className="flex-1">
                      <p className="text-sm font-bold text-slate-200 truncate">{p.timeVisitanteNome}</p>
                    </div>
                  </div>
                </Card>
              </button>
            ))}
          </div>
        </section>
      )}

      {/* Campeonatos grid */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-base font-bold text-slate-100 uppercase tracking-wider">Campeonatos</h2>
          <Link to="/campeonatos" className="text-xs text-accent hover:text-accent-hover flex items-center gap-1">
            Ver todos <ArrowRight size={12} />
          </Link>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
          {campeonatos.map((c) => (
              <Link key={c.id} to={`/campeonatos/${c.id}`}>
              <Card hover className="p-4 flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-white/[0.04] flex items-center justify-center text-accent font-bold text-lg">
                  {c.nome.charAt(0)}
                </div>
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-slate-200 truncate">{c.nome}</p>
                  <p className="text-[10px] text-slate-500">{c.pais} · {c.temporada}</p>
                </div>
              </Card>
            </Link>
          ))}
          {campeonatos.length === 0 && (
            <div className="col-span-full text-center py-12 text-sm text-slate-500">
              Nenhum campeonato cadastrado
            </div>
          )}
        </div>
      </section>

      {/* Próximas partidas + Artilharia */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Próximas */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-slate-100 uppercase tracking-wider">Próximas partidas</h2>
            <Link to="/partidas" className="text-xs text-accent hover:text-accent-hover flex items-center gap-1">
              Ver todas <ArrowRight size={12} />
            </Link>
          </div>

          {proximas.length === 0 ? (
            <Card className="p-6 text-center">
              <Calendar size={24} className="text-slate-600 mx-auto mb-2" />
              <p className="text-sm text-slate-500">Nenhuma partida agendada</p>
            </Card>
          ) : (
            <div className="space-y-2">
              {proximas.map((p) => (
                <button
                  key={p.id}
                  onClick={() => navigate(`/partidas/${p.id}`)}
                  className="w-full text-left"
                >
                  <Card hover className="p-3">
                    <div className="flex items-center gap-3">
                      <div className="flex items-center gap-2 w-10 text-[10px] text-slate-500">
                        <Clock size={10} />
                        <span>{new Date(p.dataPartida).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}</span>
                      </div>
                      <div className="flex-1 flex items-center gap-2 justify-end">
                        <span className="text-xs font-medium text-slate-300">{p.timeMandanteNome}</span>
                        <img src={getLogoUrl(p.timeMandanteNome)} alt={p.timeMandanteNome} className="w-6 h-6 rounded-full bg-white/5" />
                      </div>
                      <span className="text-xs font-mono text-slate-600 w-8 text-center">vs</span>
                      <div className="flex-1 flex items-center gap-2">
                        <img src={getLogoUrl(p.timeVisitanteNome)} alt={p.timeVisitanteNome} className="w-6 h-6 rounded-full bg-white/5" />
                        <span className="text-xs font-medium text-slate-300">{p.timeVisitanteNome}</span>
                      </div>
                      <span className="text-[10px] text-slate-600 w-16 text-right">{p.rodada}ª rod.</span>
                    </div>
                  </Card>
                </button>
              ))}
            </div>
          )}

          {/* Últimos resultados */}
          {recentes.length > 0 && (
            <div>
              <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider mb-3">Últimos resultados</h3>
              <div className="space-y-1.5">
                {recentes.map((p) => (
                  <button key={p.id} onClick={() => navigate(`/partidas/${p.id}`)} className="w-full text-left">
                    <Card className="p-2.5">
                      <div className="flex items-center gap-2">
                        <div className="flex-1 flex items-center gap-2 justify-end">
                          <span className={`text-xs ${p.golsMandante > p.golsVisitante ? 'text-accent font-bold' : 'text-slate-400'}`}>
                            {p.timeMandanteNome}
                          </span>
                          <img src={getLogoUrl(p.timeMandanteNome)} alt={p.timeMandanteNome} className="w-5 h-5 rounded-full bg-white/5" />
                        </div>
                        <span className="text-sm font-bold font-mono text-slate-100">
                          {p.golsMandante} - {p.golsVisitante}
                        </span>
                        <div className="flex-1 flex items-center gap-2">
                          <img src={getLogoUrl(p.timeVisitanteNome)} alt={p.timeVisitanteNome} className="w-5 h-5 rounded-full bg-white/5" />
                          <span className={`text-xs ${p.golsVisitante > p.golsMandante ? 'text-accent font-bold' : 'text-slate-400'}`}>
                            {p.timeVisitanteNome}
                          </span>
                        </div>
                      </div>
                    </Card>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-4">
          {/* Artilharia */}
          {artilharia.length > 0 && (
            <Card className="overflow-hidden">
              <div className="flex items-center gap-2 px-5 py-3.5 border-b border-white/[0.04]">
                <Medal size={14} className="text-accent" />
                <span className="text-sm font-semibold text-slate-200">Artilheiros</span>
              </div>
              <div className="divide-y divide-white/[0.03]">
                {artilharia.map((a) => (
                  <Link key={a.jogadorId} to={`/jogadores/${a.jogadorId}`} className="flex items-center gap-3 px-5 py-3 hover:bg-white/[0.02] transition-colors">
                    <span className={`text-xs font-bold font-mono w-5 ${a.posicao <= 3 ? 'text-accent' : 'text-slate-600'}`}>
                      {a.posicao}
                    </span>
                    <img
                      src={`https://ui-avatars.com/api/?name=${encodeURIComponent(a.nomeJogador)}&background=1a3460&color=fff&size=24`}
                      alt={a.nomeJogador}
                      className="w-7 h-7 rounded-full bg-white/5"
                    />
                    <div className="flex-1 min-w-0">
                      <p className="text-xs font-medium text-slate-200 truncate">{a.nomeJogador}</p>
                      <p className="text-[10px] text-slate-500 truncate">{a.nomeTime}</p>
                    </div>
                    <span className="text-sm font-bold text-accent font-mono">{a.gols}</span>
                  </Link>
                ))}
              </div>
            </Card>
          )}

          {/* Links rápidos */}
          <Card className="p-4">
            <h3 className="text-sm font-semibold text-slate-200 mb-3">Navegar</h3>
            <div className="space-y-1">
              <Link to="/partidas" className="flex items-center gap-3 px-3 py-2 rounded-lg text-xs text-slate-400 hover:text-slate-200 hover:bg-white/[0.04] transition-colors">
                <Calendar size={14} /> Todas as partidas <ChevronRight size={12} className="ml-auto text-slate-600" />
              </Link>
              <Link to="/campeonatos" className="flex items-center gap-3 px-3 py-2 rounded-lg text-xs text-slate-400 hover:text-slate-200 hover:bg-white/[0.04] transition-colors">
                <Trophy size={14} /> Campeonatos <ChevronRight size={12} className="ml-auto text-slate-600" />
              </Link>
              <Link to="/times" className="flex items-center gap-3 px-3 py-2 rounded-lg text-xs text-slate-400 hover:text-slate-200 hover:bg-white/[0.04] transition-colors">
                <Users size={14} /> Times <ChevronRight size={12} className="ml-auto text-slate-600" />
              </Link>
            </div>
          </Card>

          {/* Stats minimal */}
          <Card className="p-4">
            <div className="grid grid-cols-2 gap-3 text-center">
              <div>
                <p className="text-lg font-extrabold text-accent font-mono">{campeonatos.length}</p>
                <p className="text-[10px] text-slate-500 uppercase tracking-wider">Campeonatos</p>
              </div>
              <div>
                <p className="text-lg font-extrabold text-accent font-mono">
                  {proximas.length + recentes.length + aoVivo.length}
                </p>
                <p className="text-[10px] text-slate-500 uppercase tracking-wider">Partidas</p>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
