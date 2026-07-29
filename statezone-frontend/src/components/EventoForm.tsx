import { useState, useEffect, useCallback } from 'react';
import { X } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { TipoEvento, EscalacaoPartidaList } from '../types/partida';
import Button from './ui/Button';
import { toast } from 'sonner';

interface JogadorItem {
  id: number;
  nome: string;
  nomeTime: string;
  numeroCamisa: number;
}

interface EventoFormProps {
  partidaId: number;
  onClose: () => void;
  onSaved: () => void;
}

const tiposEvento: { value: TipoEvento; label: string; icon: string }[] = [
  { value: 'GOL', label: 'Gol', icon: '⚽' },
  { value: 'GOL_CONTRA', label: 'Gol contra', icon: '⚽🔄' },
  { value: 'PENALTI_GOL', label: 'Pênalti convertido', icon: '⚽' },
  { value: 'PENALTI_DEFENDIDO', label: 'Pênalti defendido', icon: '🧤' },
  { value: 'PENALTI_PERDIDO', label: 'Pênalti perdido', icon: '❌' },
  { value: 'CARTAO_AMARELO', label: 'Cartão amarelo', icon: '🟨' },
  { value: 'CARTAO_VERMELHO', label: 'Cartão vermelho', icon: '🟥' },
  { value: 'SUBSTITUICAO', label: 'Substituição', icon: '🔄' },
  { value: 'FALTA', label: 'Falta', icon: '🔇' },
  { value: 'IMPEDIMENTO', label: 'Impedimento', icon: '🚩' },
  { value: 'DEFESA', label: 'Defesa', icon: '🧤' },
];

export default function EventoForm({ partidaId, onClose, onSaved }: EventoFormProps) {
  const [tipoEvento, setTipoEvento] = useState<TipoEvento>('GOL');
  const [minuto, setMinuto] = useState(1);
  const [minutoExtra, setMinutoExtra] = useState<number | undefined>();
  const [jogadorId, setJogadorId] = useState<number | undefined>();
  const [jogadorSecundarioId, setJogadorSecundarioId] = useState<number | undefined>();
  const [jogadores, setJogadores] = useState<JogadorItem[]>([]);
  const [saving, setSaving] = useState(false);
  const [loadingJogadores, setLoadingJogadores] = useState(true);
  const aceitaJogadorSecundario = ['SUBSTITUICAO', 'GOL', 'PENALTI_GOL', 'PENALTI_DEFENDIDO'].includes(tipoEvento);
  const exigeJogadorSecundario = ['SUBSTITUICAO', 'PENALTI_DEFENDIDO'].includes(tipoEvento);

  const handleTipoChange = useCallback((novoTipo: TipoEvento) => {
    setTipoEvento(novoTipo);
    if (!['SUBSTITUICAO', 'GOL', 'PENALTI_GOL', 'PENALTI_DEFENDIDO'].includes(novoTipo)) {
      setJogadorSecundarioId(undefined);
    }
  }, []);

  useEffect(() => {
    let isMounted = true;
    api
      .get<EscalacaoPartidaList>(`/partidas/${partidaId}/escalacao`)
      .then((res) => {
        if (!isMounted) return;
        const all = [...(res.data.titulares || []), ...(res.data.reservas || [])];
        const seen = new Set<number>();
        const mapped: JogadorItem[] = [];
        for (const j of all) {
          if (!seen.has(j.jogadorId)) {
            seen.add(j.jogadorId);
            mapped.push({ id: j.jogadorId, nome: j.nomeJogador, nomeTime: j.nomeTime, numeroCamisa: j.numeroCamisa });
          }
        }
        setJogadores(mapped);
      })
      .catch((err) => {
        if (isMounted) toast.error(getApiError(err, 'Erro ao carregar jogadores da partida'));
      })
      .finally(() => {
        if (isMounted) setLoadingJogadores(false);
      });
    return () => {
      isMounted = false;
    };
  }, [partidaId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.post(`/partidas/${partidaId}/eventos`, {
        tipoEvento,
        minuto,
        minutoExtra: minutoExtra || null,
        jogadorId: jogadorId || null,
        jogadorSecundarioId: aceitaJogadorSecundario ? jogadorSecundarioId || null : null,
      });
      toast.success('Evento registrado');
      onSaved();
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao registrar evento'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
      <div className="glass-strong rounded-2xl w-full max-w-md mx-4 border border-white/[0.06] shadow-2xl animate-scale-in">
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.06]">
          <h2 className="text-lg font-bold text-slate-100">Registrar evento</h2>
          <button onClick={onClose} className="text-slate-500 hover:text-white p-1" aria-label="Fechar">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label
              htmlFor="tipoEvento"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
            >
              Tipo
            </label>
            <div id="tipoEvento" className="grid grid-cols-2 gap-1.5 max-h-40 overflow-y-auto">
              {tiposEvento.map((t) => (
                <button
                  key={t.value}
                  type="button"
                  onClick={() => handleTipoChange(t.value)}
                  className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                    tipoEvento === t.value
                      ? 'bg-accent/10 text-accent border border-accent/20'
                      : 'bg-white/[0.03] text-slate-400 hover:text-slate-200 border border-transparent'
                  }`}
                >
                  <span>{t.icon}</span>
                  {t.label}
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label
                htmlFor="minuto"
                className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
              >
                Minuto
              </label>
              <input
                id="minuto"
                type="number"
                min={1}
                max={150}
                value={minuto}
                onChange={(e) => setMinuto(Number(e.target.value))}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                required
              />
            </div>
            <div>
              <label
                htmlFor="minutoExtra"
                className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
              >
                Acréscimos
              </label>
              <input
                id="minutoExtra"
                type="number"
                min={1}
                max={30}
                value={minutoExtra ?? ''}
                onChange={(e) => setMinutoExtra(e.target.value ? Number(e.target.value) : undefined)}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                placeholder="-"
              />
            </div>
          </div>

          <div>
            <label
              htmlFor="jogadorId"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
            >
              Jogador
            </label>
            <select
              id="jogadorId"
              value={jogadorId ?? ''}
              onChange={(e) => setJogadorId(e.target.value ? Number(e.target.value) : undefined)}
              className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
              required
            >
              <option value="">Selecione</option>
              {jogadores.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.nome} {j.nomeTime ? `(${j.nomeTime})` : ''}
                </option>
              ))}
            </select>
          </div>

          {aceitaJogadorSecundario && (
            <div>
              <label
                htmlFor="jogadorSecundarioId"
                className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
              >
                {tipoEvento === 'SUBSTITUICAO'
                  ? 'Substituto'
                  : tipoEvento === 'PENALTI_DEFENDIDO'
                    ? 'Goleiro'
                    : 'Assistente'}
              </label>
              <select
                id="jogadorSecundarioId"
                value={jogadorSecundarioId ?? ''}
                onChange={(e) => setJogadorSecundarioId(e.target.value ? Number(e.target.value) : undefined)}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                required={exigeJogadorSecundario}
              >
                <option value="">{exigeJogadorSecundario ? 'Selecione' : 'Selecione (opcional)'}</option>
                {loadingJogadores ? (
                  <option value="" disabled>
                    Carregando jogadores...
                  </option>
                ) : (
                  jogadores.map((j) => (
                    <option key={j.id} value={j.id}>
                      {j.nome} {j.nomeTime ? `(${j.nomeTime})` : ''}
                    </option>
                  ))
                )}
              </select>
            </div>
          )}

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="ghost" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Salvando...' : 'Registrar'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
