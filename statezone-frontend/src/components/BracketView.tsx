import type { FaseEliminatoria, FaseEnum } from '../types/fases';
import ConfrontoCard from './ConfrontoCard';

const faseOrder: FaseEnum[] = ['OITAVAS', 'QUARTAS', 'SEMIFINAL', 'TERCEIRO_LUGAR', 'FINAL'];

const faseLabel: Record<FaseEnum, string> = {
  OITAVAS: 'Oitavas de final',
  QUARTAS: 'Quartas de final',
  SEMIFINAL: 'Semifinal',
  TERCEIRO_LUGAR: 'Terceiro lugar',
  FINAL: 'Final',
};

interface BracketViewProps {
  fases: FaseEliminatoria[];
  getLogoUrl: (nome: string) => string;
}

export default function BracketView({ fases, getLogoUrl }: BracketViewProps) {
  const sorted = [...fases].sort((a, b) => faseOrder.indexOf(a.fase) - faseOrder.indexOf(b.fase));
  const maxConfrontos = Math.max(...sorted.map((f) => f.confrontos.length), 0);

  return (
    <div className="overflow-x-auto pb-4">
      <div className="flex gap-6 min-w-max" style={{ padding: `${maxConfrontos * 1.5}rem 0` }}>
        {sorted.map((fase, faseIdx) => (
          <div key={fase.id} className="flex flex-col gap-3 relative">
            <div className="text-center mb-2">
              <span className="text-[10px] font-bold text-accent uppercase tracking-wider">{faseLabel[fase.fase]}</span>
            </div>
            {fase.confrontos.map((conf, confIdx) => (
              <div key={conf.id} className="relative" style={{ marginTop: confIdx > 0 ? `${(2 ** (sorted.length - faseIdx - 1)) * 0.5 - 0.75}rem` : '0' }}>
                {faseIdx > 0 && confIdx % 2 === 0 && confIdx + 1 < sorted[faseIdx - 1]?.confrontos.length && (
                  <div className="absolute -left-6 top-1/2 w-6 h-[200%] pointer-events-none">
                    <div className="absolute right-0 top-0 w-3 h-1/2 border-r border-b border-white/[0.08] rounded-br" />
                    <div className="absolute right-0 top-1/2 w-3 h-1/2 border-r border-t border-white/[0.08] rounded-tr" />
                  </div>
                )}
                <ConfrontoCard confronto={conf} getLogoUrl={getLogoUrl} />
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}
