import { useState, useEffect } from 'react';
import { X, Plus, User } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { EscalacaoPartidaList, EscalacaoPartida, FuncaoEscalacao } from '../types/partida';
import type { Jogador } from '../types/jogador';
import Button from './ui/Button';
import { toast } from 'sonner';

interface EscalacaoFormProps {
  partidaId: number;
  timeMandanteId: number;
  timeVisitanteId: number;
  timeMandanteNome: string;
  timeVisitanteNome: string;
  onClose: () => void;
  onSaved: () => void;
}

const posicoes: { value: Posicao; label: string }[] = [
  { value: 'GOLEIRO', label: 'Goleiro' },
  { value: 'ZAGUEIRO', label: 'Zagueiro' },
  { value: 'LATERAL_DIREITO', label: 'Lateral Direito' },
  { value: 'LATERAL_ESQUERDO', label: 'Lateral Esquerdo' },
  { value: 'VOLANTE', label: 'Volante' },
  { value: 'MEIO_CAMPO', label: 'Meio-Campo' },
  { value: 'PONTA_DIREITA', label: 'Ponta Direita' },
  { value: 'PONTA_ESQUERDA', label: 'Ponta Esquerda' },
  { value: 'MEIA_ATACANTE', label: 'Meia-Atacante' },
  { value: 'CENTROAVANTE', label: 'Centroavante' },
];

interface TeamSectionProps {
  timeNome: string;
  timeId: number;
  jogadores: Jogador[];
  escalados: EscalacaoPartida[];
  onAdd: (jogadorId: number, funcao: FuncaoEscalacao) => void;
}

function TeamSection({ timeNome, timeId, jogadores, escalados, onAdd }: TeamSectionProps) {
  const [selectedJogador, setSelectedJogador] = useState('');
  const [funcao, setFuncao] = useState<FuncaoEscalacao>('TITULAR');

  const escaladosIds = new Set(escalados.map((e) => e.jogadorId));
  const disponiveis = jogadores.filter((j) => !escaladosIds.has(j.id));

  const handleAdd = () => {
    if (!selectedJogador) return;
    onAdd(Number(selectedJogador), funcao);
    setSelectedJogador('');
  };

  const titulares = escalados.filter((e) => e.funcao === 'TITULAR');
  const reservas = escalados.filter((e) => e.funcao === 'RESERVA');

  return (
    <div className="space-y-4">
      <h3 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
        <User size={14} className="text-accent" />
        {timeNome}
      </h3>

      {/* Add player */}
      <div className="space-y-1">
        <div className="flex gap-2 items-end">
          <div className="flex-1">
            <label htmlFor="selectedJogador" className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-0.5 block">Jogador</label>
            <select
              id="selectedJogador"
              value={selectedJogador}
              onChange={(e) => setSelectedJogador(e.target.value)}
              className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-accent/40"
            >
              <option value="">Selecionar jogador</option>
              {disponiveis.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.nome} #{j.numeroCamisa}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="funcao" className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-0.5 block">Função</label>
            <select
              id="funcao"
              value={funcao}
              onChange={(e) => setFuncao(e.target.value as FuncaoEscalacao)}
              className="bg-white/[0.04] border border-white/[0.08] rounded-lg px-2 py-2 text-xs text-slate-200 focus:outline-none focus:border-accent/40"
            >
              <option value="TITULAR">Titular</option>
              <option value="RESERVA">Reserva</option>
            </select>
          </div>
          <Button size="sm" onClick={handleAdd} disabled={!selectedJogador}>
            <Plus size={14} />
          </Button>
        </div>
      </div>

      {/* Titulares */}
      {titulares.length > 0 && (
        <div>
          <p className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-1">Titulares</p>
          <div className="space-y-1">
            {titulares.map((e) => (
              <div key={e.id || e.jogadorId} className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white/[0.02] text-xs text-slate-300">
                <span className="font-mono text-slate-500">#{e.numeroCamisa}</span>
                <span className="flex-1">{e.nomeJogador}</span>
                <span className="text-slate-600">{e.posicao}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Reservas */}
      {reservas.length > 0 && (
        <div>
          <p className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-1">Reservas</p>
          <div className="space-y-1">
            {reservas.map((e) => (
              <div key={e.id || e.jogadorId} className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white/[0.02] text-xs text-slate-300">
                <span className="font-mono text-slate-500">#{e.numeroCamisa}</span>
                <span className="flex-1">{e.nomeJogador}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default function EscalacaoForm({
  partidaId, timeMandanteId, timeVisitanteId, timeMandanteNome, timeVisitanteNome, onClose, onSaved,
}: EscalacaoFormProps) {
  const [escalacao, setEscalacao] = useState<EscalacaoPartidaList | null>(null);
  const [jogadoresMandante, setJogadoresMandante] = useState<Jogador[]>([]);
  const [jogadoresVisitante, setJogadoresVisitante] = useState<Jogador[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const [escRes, jogMandRes, jogVisRes] = await Promise.all([
        api.get<EscalacaoPartidaList>(`/partidas/${partidaId}/escalacao`).catch(() => ({ data: { partidaId, titulares: [], reservas: [] } })),
        api.get<Jogador[]>(`/times/${timeMandanteId}/jogadores`),
        api.get<Jogador[]>(`/times/${timeVisitanteId}/jogadores`),
      ]);
      setEscalacao(escRes.data);
      setJogadoresMandante(jogMandRes.data);
      setJogadoresVisitante(jogVisRes.data);
    } catch {
      toast.error('Erro ao carregar dados');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [partidaId]);

  const handleAdd = async (jogadorId: number, funcao: FuncaoEscalacao) => {
    setSaving(true);
    try {
      await api.post(`/partidas/${partidaId}/escalacao`, { jogadorId, funcao });
      toast.success('Jogador adicionado à escalação');
      await load();
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao adicionar jogador'));
    } finally {
      setSaving(false);
    }
  };

  const allEscalados = [...(escalacao?.titulares || []), ...(escalacao?.reservas || [])];
  const escMandante = allEscalados.filter((e) => e.nomeTime === timeMandanteNome);
  const escVisitante = allEscalados.filter((e) => e.nomeTime === timeVisitanteNome);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
      <div className="glass-strong rounded-2xl w-full max-w-2xl mx-4 border border-white/[0.06] shadow-2xl animate-scale-in max-h-[90vh] flex flex-col">
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.06] shrink-0">
          <h2 className="text-lg font-bold text-slate-100">Gerenciar escalação</h2>
          <button onClick={onClose} className="text-slate-500 hover:text-white p-1" aria-label="Fechar">
            <X size={18} />
          </button>
        </div>

        <div className="p-6 overflow-y-auto space-y-6">
          {loading ? (
            <div className="text-center py-8 text-sm text-slate-500">Carregando...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <TeamSection
                timeNome={timeMandanteNome}
                timeId={timeMandanteId}
                jogadores={jogadoresMandante}
                escalados={escMandante}
                onAdd={handleAdd}
              />
              <TeamSection
                timeNome={timeVisitanteNome}
                timeId={timeVisitanteId}
                jogadores={jogadoresVisitante}
                escalados={escVisitante}
                onAdd={handleAdd}
              />
            </div>
          )}
        </div>

        <div className="flex justify-end gap-3 px-6 py-4 border-t border-white/[0.06] shrink-0">
          <Button variant="ghost" onClick={onClose}>Fechar</Button>
          <Button onClick={onSaved} disabled={saving}>Concluído</Button>
        </div>
      </div>
    </div>
  );
}
