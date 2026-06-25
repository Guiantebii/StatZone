export interface EstatisticasJogador {
  jogadorId: number;
  nomeJogador: string;
  nomeTime: string;
  gols: number;
  assistencias: number;
  finalizacoes: number;
  cartoesAmarelos: number;
  cartoesVermelhos: number;
  faltasCometidas: number;
  partidasJogadas: number;
  defesas: number;
  penaltisDefendidos: number;
  penaltisPerdidos: number;
  cleanSheets: number;
  mediaGolsPorPartida: number;
  mediaAssistenciasPorPartida: number;
  mediaDefesasPorPartida: number;
}
