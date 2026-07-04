import { getLogoUrl } from '../../constants/helpers';
import type { ClassificacaoTime } from '../../types/estatisticas';

interface ClassificacaoTableProps {
  dados: ClassificacaoTime[];
  onTimeClick: (timeId: number) => void;
  isGroup?: boolean;
}

export default function ClassificacaoTable({ dados, onTimeClick, isGroup = false }: ClassificacaoTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr className="bg-white/[0.02]">
            <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold w-8">#</th>
            <th className="text-left px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">Time</th>
            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">P</th>
            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">J</th>
            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">V</th>
            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">E</th>
            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">D</th>
            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">GP</th>
            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">GC</th>
            <th className="text-center px-3 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">SG</th>
            <th className="text-right px-5 py-3 text-xs uppercase tracking-wider text-slate-500 font-semibold">AP%</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-white/[0.03]">
          {dados.map((c) => (
            <tr
              key={c.timeId}
              className={`hover:bg-white/[0.02] transition-colors cursor-pointer ${
                isGroup
                  ? c.posicao <= 4
                    ? 'bg-success/5'
                    : ''
                  : dados.length >= 4 && c.posicao >= dados.length - 3
                    ? 'bg-danger/5'
                    : c.posicao <= 4
                      ? 'bg-success/5'
                      : ''
              }`}
              onClick={() => onTimeClick(c.timeId)}
            >
              <td className="px-5 py-3">
                <span
                  className={`text-sm font-bold font-mono ${
                    isGroup
                      ? c.posicao <= 4
                        ? 'text-accent'
                        : 'text-slate-400'
                      : dados.length >= 4 && c.posicao >= dados.length - 3
                        ? 'text-danger'
                        : c.posicao <= 4
                          ? 'text-accent'
                          : 'text-slate-400'
                  }`}
                >
                  {c.posicao}
                </span>
              </td>
              <td className="px-5 py-3">
                <div className="flex items-center gap-2">
                  <img
                    src={getLogoUrl(c.nomeTime)}
                    alt={c.nomeTime}
                    className="w-6 h-6 rounded-full bg-white/5"
                  />
                  <span className="text-sm font-medium text-slate-200">{c.nomeTime}</span>
                </div>
              </td>
              <td className="px-3 py-3 text-center text-sm font-bold text-accent font-mono">{c.pontos}</td>
              <td className="px-3 py-3 text-center text-sm text-slate-400 font-mono">{c.partidas}</td>
              <td className="px-3 py-3 text-center text-sm text-success font-mono">{c.vitorias}</td>
              <td className="px-3 py-3 text-center text-sm text-slate-400 font-mono">{c.empates}</td>
              <td className="px-3 py-3 text-center text-sm text-danger font-mono">{c.derrotas}</td>
              <td className="px-3 py-3 text-center text-sm text-slate-300 font-mono">{c.golsFeitos}</td>
              <td className="px-3 py-3 text-center text-sm text-slate-300 font-mono">{c.golsSofridos}</td>
              <td
                className={`px-3 py-3 text-center text-sm font-mono ${c.saldoGols > 0 ? 'text-success' : c.saldoGols < 0 ? 'text-danger' : 'text-slate-400'}`}
              >
                {c.saldoGols > 0 ? '+' : ''}
                {c.saldoGols}
              </td>
              <td className="px-5 py-3 text-right text-sm text-slate-400 font-mono">
                {c.aproveitamento.toFixed(1)}%
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
