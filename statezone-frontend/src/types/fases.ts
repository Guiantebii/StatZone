export interface TimeResumo {
  id: number;
  nome: string;
  sigla: string;
  escudoUrl?: string;
}

export type FaseEnum = 'OITAVAS' | 'QUARTAS' | 'SEMIFINAL' | 'TERCEIRO_LUGAR' | 'FINAL';

export type StatusConfronto = 'PENDENTE' | 'EM_ANDAMENTO' | 'AGUARDANDO_PENALTIS' | 'PENALTIS' | 'ENCERRADO';

export interface ConfrontoEliminatorio {
  id: number;
  timeA?: TimeResumo;
  timeB?: TimeResumo;
  partidaIdaId?: number;
  partidaVoltaId?: number;
  timeClassificado?: TimeResumo;
  statusConfronto: StatusConfronto;
}

export interface FaseEliminatoria {
  id: number;
  campeonatoId: number;
  fase: FaseEnum;
  jogoUnico: boolean;
  confrontos: ConfrontoEliminatorio[];
}

export interface Grupo {
  id: number;
  nome: string;
  campeonatoId: number;
  campeonatoNome: string;
  times: TimeResumo[];
}
