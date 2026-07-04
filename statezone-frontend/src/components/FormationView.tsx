import type { EscalacaoPartida, Posicao, Formacao } from '../types/partida';
import { FORMACOES } from '../config/formations';

interface FormationViewProps {
  titulares: EscalacaoPartida[];
  formacao?: Formacao | null;
}

const posicaoRow: Record<Posicao, number> = {
  GOLEIRO: 0,
  ZAGUEIRO: 1,
  LATERAL_DIREITO: 1,
  LATERAL_ESQUERDO: 1,
  VOLANTE: 2,
  MEIO_CAMPO: 2,
  MEIO_ESQUERDO: 2,
  MEIO_DIREITO: 2,
  PONTA_DIREITA: 3,
  PONTA_ESQUERDA: 3,
  MEIA_ATACANTE: 3,
  CENTROAVANTE: 3,
};

function buildDefaultRows(titulares: EscalacaoPartida[]) {
  const rows: { nomeJogador: string; numeroCamisa: number }[][] = [[], [], [], []];
  for (const j of titulares) {
    const row = posicaoRow[j.posicao] ?? 2;
    rows[row].push({ nomeJogador: j.nomeJogador, numeroCamisa: j.numeroCamisa });
  }
  return rows;
}

function buildFormacaoRows(titulares: EscalacaoPartida[], formacao: Formacao) {
  const config = FORMACOES[formacao];
  if (!config) return buildDefaultRows(titulares);

  const posCount: Record<string, number> = {};
  const slotOccupation: (EscalacaoPartida | null)[] = config.lines.flatMap((line) =>
    line.slots.map((slot) => {
      const idx = posCount[slot.posicao] || 0;
      posCount[slot.posicao] = idx + 1;
      return titulares.filter((t) => t.posicao === slot.posicao)[idx] || null;
    })
  );

  const rows: { nomeJogador: string; numeroCamisa: number }[][] = [];
  let slotIdx = 0;
  for (const line of config.lines) {
    const row: { nomeJogador: string; numeroCamisa: number }[] = [];
    for (let i = 0; i < line.slots.length; i++) {
      const ocupado = slotOccupation[slotIdx++];
      if (ocupado) row.push({ nomeJogador: ocupado.nomeJogador, numeroCamisa: ocupado.numeroCamisa });
    }
    rows.push(row);
  }
  return rows;
}

export default function FormationView({ titulares, formacao }: FormationViewProps) {
  const rawRows = formacao ? buildFormacaoRows(titulares, formacao) : buildDefaultRows(titulares);
  const formacaoLabel = formacao ? FORMACOES[formacao]?.label : null;
  const formacaoCalculada = rawRows.slice(1).map((r) => r.length).join('-');

  const displayRows = [...rawRows].reverse();

  const rowCount = displayRows.length;

  return (
    <div className="space-y-3 flex flex-col items-center">
      <span className="text-xs font-mono text-accent font-bold px-3 py-1 rounded-full bg-accent/10">
        {formacaoLabel || formacaoCalculada}
      </span>

      <div className="relative rounded-xl overflow-hidden aspect-[3/4] max-h-[400px] w-full max-w-[260px] mx-auto">

        <div className="absolute inset-0 bg-gradient-to-b from-green-700 via-green-600 to-green-700">

          <div className="absolute inset-x-[10%] top-0 bottom-0 border-x border-white/10" />
          <div className="absolute inset-y-[15%] left-0 right-0 border-y border-white/10" />
          <div className="absolute left-1/2 top-0 bottom-0 border-l border-white/10" />
          <div className="absolute left-1/2 top-[15%] -translate-x-1/2 w-16 aspect-square rounded-full border border-white/10" />
        </div>


        <div className="absolute inset-0 p-3">
          {displayRows.map((row, idx) => {
            const totalHeight = 90;
            const top = 5 + (idx / Math.max(rowCount, 1)) * totalHeight;
            return (
              <div
                key={idx}
                className="absolute inset-x-3 flex items-center justify-center gap-1"
                style={{ top: `${top}%`, height: `${totalHeight / rowCount}%` }}
              >
                {row.length === 0 ? null : (
                  <div className="flex items-center justify-center gap-1 w-full">
                    {row.map((j) => (
                      <div
                        key={`${j.nomeJogador}-${j.numeroCamisa}`}
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
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
