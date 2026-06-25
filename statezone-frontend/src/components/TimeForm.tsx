import { useState, useEffect } from 'react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { Time, TipoTime } from '../types/time';
import { TEAM_LOGO } from '../constants/placeholders';
import { toast } from 'sonner';

interface TimeFormProps {
  time?: Time | null;
  onClose: () => void;
  onSaved: () => void;
}

const inputClass = 'w-full bg-white/5 border border-white/10 text-slate-200 placeholder-slate-600 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-accent/50 focus:border-accent/50 transition-colors';
const Label = ({ children, htmlFor }: { children: React.ReactNode; htmlFor?: string }) => (
  <label htmlFor={htmlFor} className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">{children}</label>
);

export default function TimeForm({ time, onClose, onSaved }: TimeFormProps) {
  const [nome, setNome] = useState(time?.nome || '');
  const [sigla, setSigla] = useState(time?.sigla || '');
  const [tipo, setTipo] = useState<TipoTime>(time?.tipo || 'CLUBE');
  const [cidade, setCidade] = useState(time?.cidade || '');
  const [pais, setPais] = useState(time?.pais || '');
  const [escudoUrl, setEscudoUrl] = useState(time?.escudoUrl || '');
  const [escudoManual, setEscudoManual] = useState(!!time?.escudoUrl);
  const [tecnico, setTecnico] = useState(time?.tecnico || '');
  const [estadio, setEstadio] = useState(time?.estadio || '');
  const [fundadoEm, setFundadoEm] = useState(time?.fundadoEm || '');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => { document.body.style.overflow = ''; };
  }, []);

  useEffect(() => {
    if (!escudoManual && nome) setEscudoUrl(TEAM_LOGO(nome));
  }, [nome, escudoManual]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload: Record<string, unknown> = { nome, sigla, tipo, pais, escudoUrl, fundadoEm: fundadoEm || null };
      if (tipo === 'CLUBE') {
        payload.cidade = cidade;
        payload.tecnico = tecnico;
        payload.estadio = estadio;
      }
      if (time) {
        await api.put(`/times/${time.id}`, payload);
        toast.success('Time atualizado');
      } else {
        await api.post('/times', payload);
        toast.success('Time criado');
      }
      onSaved();
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao salvar time'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-start justify-center z-50 pt-10">
      <div className="bg-[#0d1f3c] border border-white/10 rounded-2xl w-full max-w-md shadow-2xl max-h-[85vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.07]">
          <div>
            <h3 className="text-base font-semibold text-slate-100">{time ? 'Editar time' : 'Novo time'}</h3>
            <p className="text-xs text-slate-500 mt-0.5">{time ? `Editando "${time.nome}"` : 'Preencha os dados do time'}</p>
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
                <Label htmlFor="sigla">Sigla *</Label>
                <input id="sigla" className={inputClass} value={sigla} onChange={e => setSigla(e.target.value)} required maxLength={5} />
              </div>
            </div>

            <div>
              <Label>Tipo</Label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setTipo('CLUBE')}
                  className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all ${
                    tipo === 'CLUBE' ? 'bg-accent text-primary-dark' : 'bg-white/[0.04] text-slate-400 hover:text-slate-300'
                  }`}
                >
                  Clube
                </button>
                <button
                  type="button"
                  onClick={() => setTipo('SELECAO')}
                  className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all ${
                    tipo === 'SELECAO' ? 'bg-accent text-primary-dark' : 'bg-white/[0.04] text-slate-400 hover:text-slate-300'
                  }`}
                >
                  Seleção
                </button>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              {tipo === 'CLUBE' && (
                <div>
                  <Label htmlFor="cidade">Cidade</Label>
                  <input id="cidade" className={inputClass} value={cidade} onChange={e => setCidade(e.target.value)} />
                </div>
              )}
              <div>
                <Label htmlFor="pais">País *</Label>
                <input id="pais" className={inputClass} value={pais} onChange={e => setPais(e.target.value)} required />
              </div>
            </div>

            <div>
              <Label htmlFor="escudoUrl">Escudo URL</Label>
              <input
                id="escudoUrl"
                className={inputClass}
                placeholder={TEAM_LOGO(nome || 'Time')}
                value={escudoUrl}
                onChange={e => { setEscudoUrl(e.target.value); setEscudoManual(true); }}
              />
            </div>

            {tipo === 'CLUBE' && (
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <Label htmlFor="tecnico">Técnico</Label>
                  <input id="tecnico" className={inputClass} value={tecnico} onChange={e => setTecnico(e.target.value)} />
                </div>
                <div>
                  <Label htmlFor="estadio">Estádio</Label>
                  <input id="estadio" className={inputClass} value={estadio} onChange={e => setEstadio(e.target.value)} />
                </div>
              </div>
            )}

            <div>
              <Label htmlFor="fundadoEm">Fundação</Label>
              <input id="fundadoEm" type="date" className={inputClass} value={fundadoEm} onChange={e => setFundadoEm(e.target.value)} />
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
              {saving ? 'Salvando...' : 'Salvar time'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}