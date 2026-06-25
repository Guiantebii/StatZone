import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { PartidaRequest, StatusPartida } from '../types/partida';
import type { Campeonato } from '../types/campeonato';
import type { Time } from '../types/time';
import Button from './ui/Button';
import { toast } from 'sonner';

interface PartidaFormProps {
  onClose: () => void;
  onSaved: () => void;
  campeonatos: Campeonato[];
}

export default function PartidaForm({ onClose, onSaved, campeonatos }: PartidaFormProps) {
  const [times, setTimes] = useState<Time[]>([]);
  const [estadio, setEstadio] = useState('');
  const [arbitro, setArbitro] = useState('');
  const [rodada, setRodada] = useState(1);
  const [dataPartida, setDataPartida] = useState('');
  const [campeonatoId, setCampeonatoId] = useState<number>(campeonatos[0]?.id || 0);
  const [timeMandanteId, setTimeMandanteId] = useState<number>(0);
  const [timeVisitanteId, setTimeVisitanteId] = useState<number>(0);
  const [saving, setSaving] = useState(false);
  const [loadingTimes, setLoadingTimes] = useState(false);

  useEffect(() => {
    setLoadingTimes(true);
    api.get('/times').then((res) => setTimes(res.data)).catch(() => console.error('Erro ao carregar times')).finally(() => setLoadingTimes(false));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (timeMandanteId === timeVisitanteId) {
      toast.error('O time mandante e visitante devem ser diferentes');
      return;
    }
    setSaving(true);
    try {
      const payload: PartidaRequest = {
        estadio,
        arbitro,
        rodada,
        dataPartida: dataPartida + ':00',
        status: 'AGENDADA' as StatusPartida,
        campeonatoId,
        timeMandanteId,
        timeVisitanteId,
      };
      await api.post('/partidas', payload);
      toast.success('Partida criada com sucesso');
      onSaved();
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao criar partida'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
      <div className="glass-strong rounded-2xl w-full max-w-lg mx-4 border border-white/[0.06] shadow-2xl animate-scale-in">
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.06]">
          <h2 className="text-lg font-bold text-slate-100">Nova partida</h2>
          <button onClick={onClose} className="text-slate-500 hover:text-white p-1" aria-label="Fechar">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="campeonatoId" className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">Campeonato</label>
              <select
                id="campeonatoId"
                value={campeonatoId}
                onChange={(e) => setCampeonatoId(Number(e.target.value))}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                required
              >
                {campeonatos.map((c) => (
                  <option key={c.id} value={c.id}>{c.nome}</option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="rodada" className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">Rodada</label>
              <input
                id="rodada"
                type="number"
                min={1}
                max={99}
                value={rodada}
                onChange={(e) => setRodada(Number(e.target.value))}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                required
              />
            </div>
          </div>

          <div>
            <label htmlFor="dataPartida" className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">Data e horário</label>
            <input
              id="dataPartida"
              type="datetime-local"
              value={dataPartida}
              onChange={(e) => setDataPartida(e.target.value)}
              className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="estadio" className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">Estádio</label>
              <input
                id="estadio"
                type="text"
                value={estadio}
                onChange={(e) => setEstadio(e.target.value)}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40 placeholder-slate-600"
                placeholder="Maracanã"
                required
              />
            </div>
            <div>
              <label htmlFor="arbitro" className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">Árbitro</label>
              <input
                id="arbitro"
                type="text"
                value={arbitro}
                onChange={(e) => setArbitro(e.target.value)}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40 placeholder-slate-600"
                placeholder="Wilton Sampaio"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="timeMandante" className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">Time mandante</label>
              <select
                id="timeMandante"
                value={timeMandanteId}
                onChange={(e) => setTimeMandanteId(Number(e.target.value))}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                required
              >
                <option value={0}>Selecione...</option>
                {times.map((t) => (
                  <option key={t.id} value={t.id}>{t.nome} ({t.sigla})</option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="timeVisitante" className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">Time visitante</label>
              <select
                id="timeVisitante"
                value={timeVisitanteId}
                onChange={(e) => setTimeVisitanteId(Number(e.target.value))}
                className="w-full bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-accent/40"
                required
              >
                <option value={0}>Selecione...</option>
                {times.map((t) => (
                  <option key={t.id} value={t.id}>{t.nome} ({t.sigla})</option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="ghost" onClick={onClose}>Cancelar</Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Salvando...' : 'Criar partida'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
