import type { EscalacaoPartida, Posicao } from '../types/partida';

interface FormationViewProps {
  titulares: EscalacaoPartida[];
  timeNome: string;
}

const posicaoRow: Record<Posicao, number> = {
  GOLEIRO: 0,
  ZAGUEIRO: 1,
  LATERAL_DIREITO: 1,
  LATERAL_ESQUERDO: 1,
  VOLANTE: 2,
  MEIO_CAMPO: 2,
  PONTA_DIREITA: 3,
  PONTA_ESQUERDA: 3,
  MEIA_ATACANTE: 3,
  CENTROAVANTE: 4,
};

function getColIndex(posicao: Posicao, totalInRow: number, index: number): number {
  if (totalInRow === 1) return 0.5;
  return (index + 1) / (totalInRow + 1);
}

export default function FormationView({ titulares, timeNome }: FormationViewProps) {
  const rows: { posicao: Posicao; nomeJogador: string; numeroCamisa: number }[][] = [[], [], [], [], []];
  for (const j of titulares) {
    const row = posicaoRow[j.posicao] ?? 2;
    rows[row].push({ posicao: j.posicao, nomeJogador: j.nomeJogador, numeroCamisa: j.numeroCamisa });
  }

  const formacao = rows.slice(1).map((r) => r.length).join('-');

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h4 className="text-sm font-semibold text-slate-200">{timeNome}</h4>
        <span className="text-xs font-mono text-accent font-bold">{formacao}</span>
      </div>

      <div className="relative rounded-xl overflow-hidden aspect-[3/4] max-h-[400px]">

        <div className="absolute inset-0 bg-gradient-to-b from-green-700 via-green-600 to-green-700">

          <div className="absolute inset-x-[10%] top-0 bottom-0 border-x border-white/10" />
          <div className="absolute inset-y-[15%] left-0 right-0 border-y border-white/10" />
          <div className="absolute left-1/2 top-0 bottom-0 border-l border-white/10" />
          <div className="absolute left-1/2 top-[15%] -translate-x-1/2 w-16 aspect-square rounded-full border border-white/10" />
        </div>


        <div className="absolute inset-0 p-3">
          {rows.map((row, rowIdx) => (
            <div key={rowIdx} className="absolute inset-x-3 flex items-center justify-center gap-1" style={{ top: `${10 + rowIdx * 20}%`, height: '18%' }}>
              {row.length === 0 ? null : (
                <div className="flex items-center justify-center gap-1 w-full">
                  {row.map((j, i) => {
                    const col = getColIndex(j.posicao, row.length, i);
                    return (
                      <div
                        key={j.nomeJogador}
                        className="flex flex-col items-center gap-0.5"
                        style={{ width: `${Math.min(80, 100 / row.length)}%` }}
                      >
                        <div className="w-8 h-8 rounded-full bg-primary-dark/80 border-2 border-accent/60 flex items-center justify-center shadow-lg backdrop-blur-sm">
                          <span className="text-[10px] font-bold text-accent font-mono">{j.numeroCamisa}</span>
                        </div>
                        <span className="text-[9px] text-white/80 font-medium text-center leading-tight truncate max-w-full px-0.5 drop-shadow-lg">
                          {j.nomeJogador.split(' ')[0]}
                        </span>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
