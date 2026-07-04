export type TipoTime = 'CLUBE' | 'SELECAO';

export interface Time {
  id: number;
  nome: string;
  sigla: string;
  tipo?: TipoTime;
  cidade?: string;
  pais: string;
  escudoUrl: string;
  tecnico?: string;
  estadio?: string;
  fundadoEm?: string;
  criadoEm?: string;
  atualizadoEm?: string;
}
