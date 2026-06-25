import { useState, useEffect } from 'react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import type { Campeonato } from '../types/campeonato';
import { CAMPEONATO_LOGO } from '../constants/placeholders';
import { toast } from 'sonner';

interface CampeonatoFormProps {
  campeonato?: Campeonato | null;
  onClose: () => void;
  onSaved: () => void;
}

const Label = ({ children, htmlFor }: { children: React.ReactNode; htmlFor?: string }) => (
  <label
    htmlFor={htmlFor}
    className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
  >
    {children}
  </label>
);

const inputClass =
  'w-full bg-white/5 border border-white/10 text-slate-200 placeholder-slate-600 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-accent/50 focus:border-accent/50 transition-colors';

export default function CampeonatoForm({ campeonato, onClose, onSaved }: CampeonatoFormProps) {
  const [nome, setNome] = useState(campeonato?.nome || '');
  const [pais, setPais] = useState(campeonato?.pais || '');
  const [temporada, setTemporada] = useState(campeonato?.temporada || '');
  const [logoUrl, setLogoUrl] = useState(campeonato?.logoUrl || '');
  const [logoManual, setLogoManual] = useState(!!campeonato?.logoUrl);
  const [tipoFormato, setTipoFormato] = useState(campeonato?.tipoFormato || 'PONTOS_CORRIDOS');
  const [amarelosParaSuspensao, setAmarelosParaSuspensao] = useState(campeonato?.amarelosParaSuspensao || 3);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!logoManual && nome) setLogoUrl(CAMPEONATO_LOGO(nome));
  }, [nome, logoManual]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (logoUrl && !/^https?:\/\/.+/.test(logoUrl)) {
      const msg = 'URL da logo inválida. Insira uma URL válida começando com http:// ou https://';
      setError(msg);
      toast.error(msg);
      return;
    }

    setSaving(true);
    try {
      const payload = { nome, pais, temporada, logoUrl, tipoFormato, amarelosParaSuspensao };
      if (campeonato) {
        await api.put(`/campeonatos/${campeonato.id}`, payload);
        toast.success('Campeonato atualizado');
      } else {
        await api.post('/campeonatos', payload);
        toast.success('Campeonato criado');
      }
      onSaved();
    } catch (err) {
      const msg = getApiError(err, 'Erro ao salvar campeonato. Verifique os dados e tente novamente.');
      setError(msg);
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-[#0d1f3c] border border-white/10 rounded-2xl w-full max-w-md shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.07]">
          <div>
            <h3 className="text-base font-semibold text-slate-100">
              {campeonato ? 'Editar campeonato' : 'Novo campeonato'}
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              {campeonato ? `Editando "${campeonato.nome}"` : 'Preencha os dados do campeonato'}
            </p>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-500 hover:text-slate-300 hover:bg-white/5 transition-colors"
            aria-label="Fechar"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <form onSubmit={handleSubmit}>
          <div className="px-6 py-5 space-y-4 max-h-[60vh] overflow-y-auto">
            {error && (
              <div className="flex items-start gap-2 bg-red-500/10 border border-red-500/20 rounded-lg px-3 py-2.5">
                <svg className="w-4 h-4 text-red-400 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
                </svg>
                <p className="text-xs text-red-400">{error}</p>
              </div>
            )}

            {/* Nome + País */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="nome">Nome *</Label>
                <input
                  id="nome"
                  className={inputClass}
                  placeholder="Ex: Brasileirão"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required
                />
              </div>
              <div>
                <Label htmlFor="pais">País *</Label>
                <input
                  id="pais"
                  className={inputClass}
                  placeholder="Ex: Brasil"
                  value={pais}
                  onChange={(e) => setPais(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* Temporada + Logo */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="temporada">Temporada *</Label>
                <input
                  id="temporada"
                  className={inputClass}
                  placeholder="Ex: 2024"
                  value={temporada}
                  onChange={(e) => setTemporada(e.target.value)}
                  required
                />
              </div>
              <div>
                <Label htmlFor="logoUrl">Logo URL</Label>
                <input
                  id="logoUrl"
                  className={inputClass}
                  placeholder={CAMPEONATO_LOGO(nome || 'Campeonato')}
                  value={logoUrl}
                  onChange={(e) => { setLogoUrl(e.target.value); setLogoManual(true); }}
                />
              </div>
            </div>

            {/* Formato */}
            <div>
              <Label htmlFor="tipoFormato">Formato</Label>
              <select
                id="tipoFormato"
                className={inputClass + ' appearance-none cursor-pointer'}
                value={tipoFormato}
                onChange={(e) => setTipoFormato(e.target.value)}
                required
              >
                <option value="PONTOS_CORRIDOS" className="bg-[#0d1f3c] text-slate-200">Pontos Corridos</option>
                <option value="MATA_MATA" className="bg-[#0d1f3c] text-slate-200">Mata‑Mata</option>
                <option value="GRUPOS_E_MATA_MATA" className="bg-[#0d1f3c] text-slate-200">Grupos e Mata‑Mata</option>
              </select>
            </div>

            {/* Amarelos */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <Label htmlFor="amarelos">Amarelos para suspensão</Label>
                <span className="text-xs font-bold text-accent">{amarelosParaSuspensao}</span>
              </div>
              <input
                id="amarelos"
                type="range"
                min={1}
                max={10}
                step={1}
                value={amarelosParaSuspensao}
                onChange={(e) => setAmarelosParaSuspensao(parseInt(e.target.value, 10))}
                className="w-full accent-yellow-400 h-1.5 rounded-full cursor-pointer"
              />
              <div className="flex justify-between mt-1">
                <span className="text-xs text-slate-600">1</span>
                <span className="text-xs text-slate-500">Número de cartões que suspende automaticamente</span>
                <span className="text-xs text-slate-600">10</span>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="flex items-center justify-end gap-2 px-6 py-4 border-t border-white/[0.07]">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm text-slate-400 hover:text-slate-200 hover:bg-white/5 rounded-lg transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex items-center gap-2 px-5 py-2 bg-accent text-primary-dark text-sm font-semibold rounded-lg hover:bg-accent-hover transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {saving && (
                <svg className="animate-spin w-3.5 h-3.5" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                </svg>
              )}
              {saving ? 'Salvando...' : 'Salvar campeonato'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}