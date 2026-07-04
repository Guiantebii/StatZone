import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationBarProps {
  page: number;
  hasMore: boolean;
  onPrev: () => void;
  onNext: () => void;
}

export default function PaginationBar({ page, hasMore, onPrev, onNext }: PaginationBarProps) {
  const safePrev = () => {
    if (page > 0) onPrev();
  };
  const safeNext = () => {
    if (hasMore) onNext();
  };
  return (
    <div className="flex items-center justify-between px-5 py-3 border-t border-white/[0.04]">
      <button
        onClick={safePrev}
        disabled={page === 0}
        className="flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-400 hover:text-slate-200 hover:bg-white/[0.04] disabled:opacity-30 disabled:cursor-not-allowed transition-all"
      >
        <ChevronLeft size={14} />
        Anterior
      </button>
      <span className="text-xs text-slate-500 font-mono">Página {page + 1}</span>
      <button
        onClick={safeNext}
        disabled={!hasMore}
        className="flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-400 hover:text-slate-200 hover:bg-white/[0.04] disabled:opacity-30 disabled:cursor-not-allowed transition-all"
      >
        Próximo
        <ChevronRight size={14} />
      </button>
    </div>
  );
}
