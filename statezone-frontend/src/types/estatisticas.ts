export interface ClassificacaoTime {
  timeId: number;
  nomeTime: string;
  pontos: number;
  partidas: number;
  vitorias: number;
  empates: number;
  derrotas: number;
  golsFeitos: number;
  golsSofridos: number;
  saldoGols: number;
  posicao: number;
  aproveitamento: number;
}

export interface Artilharia {
  posicao: number;
  jogadorId: number;
  nomeJogador: string;
  nomeTime: string;
  escudoTime?: string;
  gols: number;
}

export interface AssistenciaRanking {
  posicao: number;
  jogadorId: number;
  nomeJogador: string;
  nomeTime: string;
  escudoTime?: string;
  assistencias: number;
}

export interface RankingCartao {
  posicao: number;
  jogadorId: number;
  nomeJogador: string;
  nomeTime: string;
  escudoTime?: string;
  cartoesAmarelos?: number;
  cartoesVermelhos?: number;
}

export interface RankingGoleiro {
  posicao: number;
  jogadorId: number;
  nomeJogador: string;
  fotoUrl?: string;
  nomeTime: string;
  escudoTime?: string;
  cleanSheets: number;
  defesas: number;
  penaltisDefendidos: number;
  partidasJogadas: number;
}

export interface SelecaoCampeonato {
  posicao: string;
  nomeJogador: string;
  nomeTime: string;
  score: number;
}

export interface TimeEstatisticas {
  timeId: number;
  partidas: number;
  vitorias: number;
  empates: number;
  derrotas: number;
  golsMarcados: number;
  golsSofridos: number;
}

export interface CraqueCampeonato {
  jogadorId: number;
  nomeJogador: string;
  nomeTime: string;
  escudoTime?: string;
  gols: number;
  assistencias: number;
  defesas: number;
  penaltisDefendidos: number;
  penaltisPerdidos: number;
  cartoesAmarelos: number;
  cartoesVermelhos: number;
  score: number;
}
