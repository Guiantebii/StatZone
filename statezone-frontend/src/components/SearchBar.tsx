import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search } from 'lucide-react';
import api from '../api/client';
import type { Time } from '../types/time';
import type { Jogador } from '../types/jogador';

type SearchResult = {
  times: Time[];
  jogadores: Jogador[];
};

interface SearchBarProps {
  placeholder?: string;
  className?: string;
  navigatePrefix?: string;
}

export default function SearchBar({
  placeholder = 'Buscar...',
  className = '',
  navigatePrefix = '',
}: SearchBarProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult>({ times: [], jogadores: [] });
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const wrapperRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const timerRef = useRef<ReturnType<typeof setTimeout>>(undefined);

  useEffect(() => {
    if (!query.trim()) {
      return;
    }

    let isCancelled = false;
    clearTimeout(timerRef.current);

    timerRef.current = setTimeout(async () => {
      setLoading(true);
      setOpen(true);
      try {
        const [timesRes, jogadoresRes] = await Promise.all([
          api.get<Time[]>('/times', { params: { nome: query } }),
          api.get<Jogador[]>('/jogadores', { params: { nome: query } }),
        ]);
        const data = { times: timesRes.data, jogadores: jogadoresRes.data };
        if (isCancelled) return;
        setResults(data);
      } catch {
        if (!isCancelled) setResults({ times: [], jogadores: [] });
      } finally {
        if (!isCancelled) setLoading(false);
      }
    }, 300);

    return () => {
      isCancelled = true;
      clearTimeout(timerRef.current);
    };
  }, [query]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function handleSelect(path: string) {
    setQuery('');
    setOpen(false);
    navigate(navigatePrefix + path);
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'Escape') {
      setOpen(false);
      inputRef.current?.blur();
    }
  }

  return (
    <div ref={wrapperRef} className="relative">
      <div className="relative">
        <input
          ref={inputRef}
          type="text"
          placeholder={placeholder}
          aria-label={placeholder}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => { if (results.times.length > 0 || results.jogadores.length > 0) setOpen(true); }}
          onKeyDown={handleKeyDown}
          className={`bg-white/[0.04] border border-white/[0.06] rounded-lg pl-8 pr-3 py-1.5 text-xs text-slate-300 placeholder-slate-600 focus:outline-none focus:border-accent/30 transition-all ${className}`}
        />
        <Search size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-600" />
      </div>

      {open && (
        <div className="absolute top-full mt-1 left-0 right-0 z-50 bg-primary border border-white/[0.08] rounded-lg shadow-xl max-h-80 overflow-y-auto">
          {loading && (
            <div className="px-3 py-2 text-xs text-slate-500 text-center">Buscando...</div>
          )}

          {!loading && results.times.length > 0 && (
            <div>
              <div className="px-3 py-1.5 text-[10px] font-semibold text-slate-600 uppercase tracking-wider bg-white/[0.02]">
                Times
              </div>
              {results.times.map((t) => (
                <button
                  key={`time-${t.id}`}
                  onClick={() => handleSelect(`/times/${t.id}`)}
                  className="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-slate-300 hover:bg-white/[0.04] hover:text-white transition-colors text-left"
                >
                  {t.escudoUrl ? (
                    <img src={t.escudoUrl} alt="" className="w-5 h-5 rounded-full object-cover" />
                  ) : (
                    <div className="w-5 h-5 rounded-full bg-accent/20 flex items-center justify-center text-[9px] font-bold text-accent">
                      {t.sigla?.[0] || t.nome[0]}
                    </div>
                  )}
                  <span>{t.nome}</span>
                  <span className="ml-auto text-[10px] text-slate-600">{t.sigla}</span>
                </button>
              ))}
            </div>
          )}

          {!loading && results.jogadores.length > 0 && (
            <div>
              <div className="px-3 py-1.5 text-[10px] font-semibold text-slate-600 uppercase tracking-wider bg-white/[0.02] border-t border-white/[0.04]">
                Jogadores
              </div>
              {results.jogadores.map((j) => (
                <button
                  key={`jogador-${j.id}`}
                  onClick={() => handleSelect(`/jogadores/${j.id}`)}
                  className="w-full flex items-center gap-2.5 px-3 py-2 text-xs text-slate-300 hover:bg-white/[0.04] hover:text-white transition-colors text-left"
                >
                  {j.fotoUrl ? (
                    <img src={j.fotoUrl} alt="" className="w-5 h-5 rounded-full object-cover" />
                  ) : (
                    <div className="w-5 h-5 rounded-full bg-accent/20 flex items-center justify-center text-[9px] font-bold text-accent">
                      {j.nome[0]}
                    </div>
                  )}
                  <span>{j.nome}</span>
                  {j.nomeTime && (
                    <span className="ml-auto text-[10px] text-slate-600">{j.nomeTime}</span>
                  )}
                </button>
              ))}
            </div>
          )}

          {!loading && results.times.length === 0 && results.jogadores.length === 0 && query.trim() && (
            <div className="px-3 py-3 text-xs text-slate-500 text-center">
              Nenhum resultado encontrado para "{query}"
            </div>
          )}
        </div>
      )}
    </div>
  );
}
