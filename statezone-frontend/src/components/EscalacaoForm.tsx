import { useState, useEffect, useCallback } from 'react';
import { X, Plus, User, ShieldCheck } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { EscalacaoPartidaList, EscalacaoPartida, FuncaoEscalacao, Posicao, Formacao } from '../types/partida';
import type { Jogador } from '../types/jogador';
import { FORMACOES_LIST, FORMACOES, POSICAO_ABBR } from '../config/formations';
import Button from './ui/Button';
import { toast } from 'sonner';

interface EscalacaoFormProps {
  partidaId: number;
  timeMandanteId: number;
  timeVisitanteId: number;
  timeMandanteNome: string;
  timeVisitanteNome: string;
  formacaoMandante?: Formacao | null;
  formacaoVisitante?: Formacao | null;
  onClose: () => void;
  onSaved: () => void;
}

interface TeamSectionProps {
  timeNome: string;
  timeId: number;
  jogadores: Jogador[];
  escalados: EscalacaoPartida[];
  formacao: Formacao | null;
  partidaId: number;
  onAdd: (jogadorId: number, funcao: FuncaoEscalacao, posicao?: Posicao) => void;
  onFormacaoChange: (timeId: number, formacao: Formacao) => void;
}

function TeamSection({ timeNome, timeId, jogadores, escalados, formacao, partidaId, onAdd, onFormacaoChange }: TeamSectionProps) {
  const [selectedJogador, setSelectedJogador] = useState('');
  const [selectedPosicao, setSelectedPosicao] = useState<Posicao | ''>('');
  const [funcao, setFuncao] = useState<FuncaoEscalacao>('TITULAR');
  const [savingFormacao, setSavingFormacao] = useState(false);

  const escaladosIds = new Set(escalados.map((e) => e.jogadorId));
  const disponiveis = jogadores.filter((j) => !escaladosIds.has(j.id));

  const formacaoConfig = formacao ? FORMACOES[formacao] : null;
  const allSlots = formacaoConfig ? formacaoConfig.lines.flatMap((l) => l.slots) : [];
  const totalSlots = allSlots.length;

  const titulares = escalados.filter((e) => e.funcao === 'TITULAR');
  const reservas = escalados.filter((e) => e.funcao === 'RESERVA');

  // Mapeia cada slot ao seu jogador específico (1º slot ZAG → 1º ZAG titular, etc.)
  const posCount: Record<string, number> = {};
  const slotOccupation: (EscalacaoPartida | null)[] = allSlots.map((slot) => {
    const idx = posCount[slot.posicao] || 0;
    posCount[slot.posicao] = idx + 1;
    return titulares.filter((t) => t.posicao === slot.posicao)[idx] || null;
  });
  const filledCount = slotOccupation.filter(Boolean).length;

  // Contagem de slots vs titulares por posição
  const slotCountPerPos: Record<string, number> = {};
  for (const s of allSlots) slotCountPerPos[s.posicao] = (slotCountPerPos[s.posicao] || 0) + 1;
  const titularCountPerPos: Record<string, number> = {};
  for (const t of titulares) titularCountPerPos[t.posicao] = (titularCountPerPos[t.posicao] || 0) + 1;

  const handleAdd = () => {
    if (!selectedJogador) return;
    if (funcao === 'TITULAR' && !selectedPosicao) {
      toast.error('Selecione a posição para o titular');
      return;
    }
    onAdd(Number(selectedJogador), funcao, funcao === 'TITULAR' ? (selectedPosicao as Posicao) : undefined);
    setSelectedJogador('');
    setSelectedPosicao('');
  };

  const handleFormacaoSelect = async (value: string) => {
    onFormacaoChange(timeId, value as Formacao);
    setSavingFormacao(true);
    try {
      await api.patch(`/partidas/${partidaId}/formacao`, { timeId, formacao: value });
      toast.success(`Formação ${FORMACOES[value as Formacao].label} definida`);
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao salvar formação'));
    } finally {
      setSavingFormacao(false);
    }
  };

  const positions = ['GOLEIRO', 'ZAGUEIRO', 'LATERAL_DIREITO', 'LATERAL_ESQUERDO', 'VOLANTE', 'MEIO_CAMPO', 'MEIO_ESQUERDO', 'MEIO_DIREITO', 'PONTA_DIREITA', 'PONTA_ESQUERDA', 'MEIA_ATACANTE', 'CENTROAVANTE'] as const;
  const posicoesDisponiveis = positions.filter((p) => {
    const needed = slotCountPerPos[p] || 0;
    const have = titularCountPerPos[p] || 0;
    return have < needed;
  });

  return (
    <div className="space-y-4">
      <h3 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
        <User size={14} className="text-accent" />
        {timeNome}
      </h3>

      <div className="space-y-1">
        <label htmlFor={`formacao-${timeId}`} className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-0.5 block">
          <ShieldCheck size={12} className="inline mr-1" />
          Formação tática
        </label>
        <select
          id={`formacao-${timeId}`}
          value={formacao || ''}
          onChange={(e) => handleFormacaoSelect(e.target.value)}
          disabled={savingFormacao}
          className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-accent/40"
        >
          <option value="">Selecionar formação</option>
          {FORMACOES_LIST.map((f) => (
            <option key={f.value} value={f.value}>{f.label}</option>
          ))}
        </select>
      </div>

      {formacaoConfig && (
        <div className="bg-white/[0.02] rounded-lg p-3 space-y-1">
          <div className="flex justify-between text-[10px] text-slate-500 mb-1">
            <span className="font-semibold uppercase tracking-wider">Estrutura</span>
            <span>{filledCount}/{totalSlots} preenchidos</span>
          </div>
          {formacaoConfig.lines.map((line, li) => (
            <div key={li} className="flex items-center justify-center gap-2 text-xs">
              <span className="text-slate-600 w-16 shrink-0 text-right">{line.label}:</span>
              <div className="flex gap-1 flex-wrap justify-center">
                {line.slots.map((slot, si) => {
                  const globalIdx = formacaoConfig.lines.slice(0, li).reduce((sum, l) => sum + l.slots.length, 0) + si;
                  const ocupado = slotOccupation[globalIdx];
                  return (
                    <span
                      key={`${li}-${si}`}
                      className={`px-1.5 py-0.5 rounded text-[10px] font-mono font-semibold ${
                        ocupado
                          ? 'bg-accent/15 text-accent border border-accent/20'
                          : 'bg-white/[0.04] text-slate-600 border border-white/[0.06]'
                      }`}
                      title={slot.label}
                    >
                      {ocupado ? ocupado.nomeJogador.split(' ')[0] : slot.label}
                    </span>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="space-y-1">
        <div className="flex gap-2 items-end">
          <div className="flex-1">
            <label htmlFor={`jogador-${timeId}`} className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-0.5 block">Jogador</label>
            <select
              id={`jogador-${timeId}`}
              value={selectedJogador}
              onChange={(e) => setSelectedJogador(e.target.value)}
              className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-accent/40"
            >
              <option value="">Selecionar jogador</option>
              {disponiveis.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.nome} #{j.numeroCamisa} — {POSICAO_ABBR[j.posicao] || j.posicao}
                </option>
              ))}
            </select>
          </div>
          {funcao === 'TITULAR' && (
            <div>
              <label htmlFor={`posicao-${timeId}`} className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-0.5 block">Posição</label>
              <select
                id={`posicao-${timeId}`}
                value={selectedPosicao}
                onChange={(e) => setSelectedPosicao(e.target.value as Posicao)}
                className="bg-white/[0.04] border border-white/[0.08] rounded-lg px-2 py-2 text-xs text-slate-200 focus:outline-none focus:border-accent/40 max-w-[120px]"
              >
                <option value="">Posição</option>
                {posicoesDisponiveis.map((p) => (
                  <option key={p} value={p}>{POSICAO_ABBR[p] || p.replace(/_/g, ' ')} — {p.replace(/_/g, ' ')}</option>
                ))}
              </select>
            </div>
          )}
          <div>
            <label htmlFor={`funcao-${timeId}`} className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-0.5 block">Função</label>
            <select
              id={`funcao-${timeId}`}
              value={funcao}
              onChange={(e) => { setFuncao(e.target.value as FuncaoEscalacao); setSelectedPosicao(''); }}
              className="bg-white/[0.04] border border-white/[0.08] rounded-lg px-2 py-2 text-xs text-slate-200 focus:outline-none focus:border-accent/40"
            >
              <option value="TITULAR">Titular</option>
              <option value="RESERVA">Reserva</option>
            </select>
          </div>
          <Button size="sm" onClick={handleAdd} disabled={!selectedJogador || savingFormacao}>
            <Plus size={14} />
          </Button>
        </div>
      </div>

      {titulares.length > 0 && (
        <div>
          <p className="text-[10px] uppercase tracking-wider text-slate-500 font-semibold mb-1">Titulares</p>
          <div className="space-y-1">
            {titulares.map((e) => (
              <div key={e.id || e.jogadorId} className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white/[0.02] text-xs text-slate-300">
                <span className="font-mono text-slate-500">#{e.numeroCamisa}</span>
                <span className="flex-1">{e.nomeJogador}</span>
                <span className="text-accent/70 text-[10px]">{e.posicao.replace(/_/g, ' ')}</span>
              </div>
            ))}
          </div>
        </div>
      )}

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
  partidaId, timeMandanteId, timeVisitanteId, timeMandanteNome, timeVisitanteNome,
  formacaoMandante, formacaoVisitante, onClose, onSaved,
}: EscalacaoFormProps) {
  const [escalacao, setEscalacao] = useState<EscalacaoPartidaList | null>(null);
  const [jogadoresMandante, setJogadoresMandante] = useState<Jogador[]>([]);
  const [jogadoresVisitante, setJogadoresVisitante] = useState<Jogador[]>([]);
  const [localFormacaoMandante, setLocalFormacaoMandante] = useState<Formacao | null>(formacaoMandante || null);
  const [localFormacaoVisitante, setLocalFormacaoVisitante] = useState<Formacao | null>(formacaoVisitante || null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
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
    }
  }, [partidaId, timeMandanteId, timeVisitanteId]);

  useEffect(() => {
    let isMounted = true;
    (async () => {
      await load();
      if (isMounted) setLoading(false);
    })();
    return () => { isMounted = false; };
  }, [load]);

  const handleAdd = async (jogadorId: number, funcao: FuncaoEscalacao, posicao?: Posicao) => {
    setSaving(true);
    try {
      await api.post(`/partidas/${partidaId}/escalacao`, { jogadorId, funcao, ...(posicao && { posicao }) });
      toast.success('Jogador adicionado à escalação');
      await load();
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao adicionar jogador'));
    } finally {
      setSaving(false);
    }
  };

  const handleFormacaoChange = (timeId: number, formacao: Formacao) => {
    if (timeId === timeMandanteId) {
      setLocalFormacaoMandante(formacao);
    } else {
      setLocalFormacaoVisitante(formacao);
    }
  };

  const allEscalados = [...(escalacao?.titulares || []), ...(escalacao?.reservas || [])];
  const escMandante = allEscalados.filter((e) => e.nomeTime === timeMandanteNome);
  const escVisitante = allEscalados.filter((e) => e.nomeTime === timeVisitanteNome);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
      <div className="glass-strong rounded-2xl w-full max-w-3xl mx-4 border border-white/[0.06] shadow-2xl animate-scale-in max-h-[90vh] flex flex-col">
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
                formacao={localFormacaoMandante}
                partidaId={partidaId}
                onAdd={handleAdd}
                onFormacaoChange={handleFormacaoChange}
              />
              <TeamSection
                timeNome={timeVisitanteNome}
                timeId={timeVisitanteId}
                jogadores={jogadoresVisitante}
                escalados={escVisitante}
                formacao={localFormacaoVisitante}
                partidaId={partidaId}
                onAdd={handleAdd}
                onFormacaoChange={handleFormacaoChange}
              />
            </div>
          )}
        </div>

        <div className="flex justify-end gap-3 px-6 py-4 border-t border-white/[0.06] shrink-0">
          <Button variant="ghost" onClick={onClose}>Fechar</Button>
          <Button onClick={onSaved} disabled={saving || loading}>Concluído</Button>
        </div>
      </div>
    </div>
  );
}
