export const STATUS_PARTIDA = {
  AGENDADA: 'AGENDADA' as const,
  AO_VIVO: 'AO_VIVO' as const,
  INTERVALO: 'INTERVALO' as const,
  PENALTIS: 'PENALTIS' as const,
  ENCERRADA: 'ENCERRADA' as const,
  ADIADA: 'ADIADA' as const,
  CANCELADA: 'CANCELADA' as const,
  WO_MANDANTE: 'WO_MANDANTE' as const,
  WO_VISITANTE: 'WO_VISITANTE' as const,
} as const;

export const STATUS_AO_VIVO = [STATUS_PARTIDA.AO_VIVO, STATUS_PARTIDA.PENALTIS, STATUS_PARTIDA.INTERVALO] as const;
export const STATUS_ENCERRADA = [STATUS_PARTIDA.ENCERRADA, STATUS_PARTIDA.WO_MANDANTE, STATUS_PARTIDA.WO_VISITANTE] as const;

export function isLiveStatus(s: string): boolean {
  return STATUS_AO_VIVO.includes(s as typeof STATUS_AO_VIVO[number]);
}

export function isFinishedStatus(s: string): boolean {
  return STATUS_ENCERRADA.includes(s as typeof STATUS_ENCERRADA[number]);
}

export const STATUS_LABEL: Record<string, string> = {
  AO_VIVO: 'Ao Vivo',
  INTERVALO: 'Intervalo',
  PENALTIS: 'Pênaltis',
  ENCERRADA: 'Encerrada',
  AGENDADA: 'Agendada',
  ADIADA: 'Adiada',
  CANCELADA: 'Cancelada',
  WO_MANDANTE: 'W.O. Mandante',
  WO_VISITANTE: 'W.O. Visitante',
};
