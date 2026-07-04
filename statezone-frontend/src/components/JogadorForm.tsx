import { useState, useEffect } from 'react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { Jogador } from '../types/jogador';
import { PLAYER_PHOTO } from '../constants/placeholders';
import { toast } from 'sonner';

interface TimeOption { id: number; nome: string; }

interface JogadorFormProps {
  jogador?: Jogador | null;
  onClose: () => void;
  onSaved: () => void;
}

const inputClass = 'w-full bg-white/5 border border-white/10 text-slate-200 placeholder-slate-600 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-accent/50 focus:border-accent/50 transition-colors';
const Label = ({ children, htmlFor }: { children: React.ReactNode; htmlFor?: string }) => (
  <label htmlFor={htmlFor} className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">{children}</label>
);

export default function JogadorForm({ jogador, onClose, onSaved }: JogadorFormProps) {
  const [nome, setNome] = useState(jogador?.nome ?? '');
  const [dataNascimento, setDataNascimento] = useState(jogador?.dataNascimento ?? '');
  const [nacionalidade, setNacionalidade] = useState(jogador?.nacionalidade ?? '');
  const [posicao, setPosicao] = useState(jogador?.posicao ?? 'MEIO_CAMPO');
  const [numeroCamisa, setNumeroCamisa] = useState(jogador?.numeroCamisa ?? 1);
  const [altura, setAltura] = useState<string | number>(jogador ? (jogador.altura ?? '') : 1.80);
  const [peso, setPeso] = useState<string | number>(jogador ? (jogador.peso ?? '') : 75);
  const [valorMercado, setValorMercado] = useState(jogador?.valorMercado ?? 0);
  const [peForte, setPeForte] = useState(jogador?.peForte ?? 'DIREITO');
  const [fotoUrl, setFotoUrl] = useState(jogador?.fotoUrl ?? '');
  const [fotoManual, setFotoManual] = useState(!!jogador?.fotoUrl);
  const [timeId, setTimeId] = useState<number | ''>(jogador?.timeId ?? '');
  const [times, setTimes] = useState<TimeOption[]>([]);
  const [saving, setSaving] = useState(false);

  const fotoUrlFinal = fotoManual ? fotoUrl : PLAYER_PHOTO(nome || jogador?.nome || '');

  useEffect(() => {
    let isCancelled = false;
    api.get('/times').then(res => {
      if (!isCancelled) setTimes(res.data);
    }).catch(err => toast.error(getApiError(err, 'Erro ao carregar times')));
    return () => { isCancelled = true; };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = { nome, dataNascimento: dataNascimento || null, nacionalidade, posicao, numeroCamisa, altura: altura || null, peso: peso || null, valorMercado, peForte, fotoUrl: fotoUrlFinal, timeId: timeId || null };
      if (jogador) {
        await api.put(`/jogadores/${jogador.id}`, payload);
        toast.success('Jogador atualizado');
      } else {
        await api.post('/jogadores', payload);
        toast.success('Jogador criado');
      }
      onSaved();
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao salvar jogador'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-[#0d1f3c] border border-white/10 rounded-2xl w-full max-w-md shadow-2xl max-h-[85vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.07]">
          <div>
            <h3 className="text-base font-semibold text-slate-100">{jogador ? 'Editar jogador' : 'Novo jogador'}</h3>
            <p className="text-xs text-slate-500 mt-0.5">{jogador ? `Editando "${jogador.nome}"` : 'Preencha os dados do jogador'}</p>
          </div>
          <button onClick={onClose} className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-500 hover:text-slate-300 hover:bg-white/5" aria-label="Fechar">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="px-6 py-5 space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="nome">Nome *</Label>
                <input id="nome" className={inputClass} value={nome} onChange={e => setNome(e.target.value)} required />
              </div>
              <div>
                <Label htmlFor="nacionalidade">Nacionalidade *</Label>
                <input id="nacionalidade" className={inputClass} value={nacionalidade} onChange={e => setNacionalidade(e.target.value)} required />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="dataNascimento">Data Nasc.</Label>
                <input id="dataNascimento" type="date" className={inputClass} value={dataNascimento} onChange={e => setDataNascimento(e.target.value)} />
              </div>
              <div>
                <Label htmlFor="timeId">Time *</Label>
                <select id="timeId" className={inputClass + ' appearance-none'} value={timeId} onChange={e => setTimeId(Number(e.target.value) || '')} required>
                  <option value="">Selecione</option>
                  {times.map(t => <option key={t.id} value={t.id} className="bg-[#0d1f3c]">{t.nome}</option>)}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="posicao">Posição *</Label>
                <select id="posicao" className={inputClass + ' appearance-none'} value={posicao} onChange={e => setPosicao(e.target.value)} required>
                  <option value="GOLEIRO">Goleiro</option>
                  <option value="ZAGUEIRO">Zagueiro</option>
                  <option value="LATERAL_DIREITO">Lateral Direito</option>
                  <option value="LATERAL_ESQUERDO">Lateral Esquerdo</option>
                  <option value="VOLANTE">Volante</option>
                  <option value="MEIO_CAMPO">Meio-Campo</option>
                  <option value="MEIO_ESQUERDO">Meio Esquerdo</option>
                  <option value="MEIO_DIREITO">Meio Direito</option>
                  <option value="PONTA_DIREITA">Ponta Direita</option>
                  <option value="PONTA_ESQUERDA">Ponta Esquerda</option>
                  <option value="MEIA_ATACANTE">Meia-Atacante</option>
                  <option value="CENTROAVANTE">Centroavante</option>
                </select>
              </div>
              <div>
                <Label htmlFor="numeroCamisa">Nº Camisa</Label>
                <input id="numeroCamisa" type="number" className={inputClass} value={numeroCamisa} onChange={e => setNumeroCamisa(Number(e.target.value))} min={1} max={99} />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div>
                <Label htmlFor="altura">Altura (m)</Label>
                <input id="altura" type="number" step="0.01" min={0.5} max={2.5} inputMode="decimal" className={inputClass} value={altura} onChange={e => setAltura(Number(e.target.value))} />
              </div>
              <div>
                <Label htmlFor="peso">Peso (kg)</Label>
                <input id="peso" type="number" step="0.1" min={20} max={200} inputMode="decimal" className={inputClass} value={peso} onChange={e => setPeso(Number(e.target.value))} />
              </div>
              <div>
                <Label htmlFor="valorMercado">Valor (M€)</Label>
                <input id="valorMercado" type="number" step="0.01" min={0} inputMode="decimal" className={inputClass} value={valorMercado} onChange={e => setValorMercado(Number(e.target.value))} />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="peForte">Pé Forte</Label>
                <select id="peForte" className={inputClass + ' appearance-none'} value={peForte} onChange={e => setPeForte(e.target.value)}>
                  <option value="DIREITO">Direito</option>
                  <option value="ESQUERDO">Esquerdo</option>
                  <option value="AMBIDESTRO">Ambidestro</option>
                </select>
              </div>
              <div>
                <Label htmlFor="fotoUrl">Foto URL</Label>
                <input
                  id="fotoUrl"
                  className={inputClass}
                  placeholder={PLAYER_PHOTO(nome || 'Jogador')}
                  value={fotoUrl}
                  onChange={e => { setFotoUrl(e.target.value); setFotoManual(true); }}
                />
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-2 px-6 py-4 border-t border-white/[0.07]">
            <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-400 hover:text-slate-200 hover:bg-white/5 rounded-lg transition-colors">Cancelar</button>
            <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2 bg-accent text-primary-dark text-sm font-semibold rounded-lg hover:bg-accent-hover transition-colors disabled:opacity-60 disabled:cursor-not-allowed">
              {saving && (
                <svg className="animate-spin w-3.5 h-3.5" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                </svg>
              )}
              {saving ? 'Salvando...' : 'Salvar jogador'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}