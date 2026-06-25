export type StatusPartida =
  | 'AGENDADA'
  | 'AO_VIVO'
  | 'INTERVALO'
  | 'PENALTIS'
  | 'ENCERRADA'
  | 'ADIADA'
  | 'CANCELADA'
  | 'WO_MANDANTE'
  | 'WO_VISITANTE';

export interface Partida {
  id: number;
  estadio: string;
  arbitro: string;
  rodada: number;
  dataPartida: string;
  status: StatusPartida;
  golsMandante: number;
  golsVisitante: number;
  campeonatoId: number;
  campeonatoNome: string;
  timeMandanteId: number;
  timeMandanteNome: string;
  timeVisitanteId: number;
  timeVisitanteNome: string;
  criadoEm?: string;
  atualizadoEm?: string;
}

export interface PartidaRequest {
  estadio: string;
  arbitro: string;
  rodada: number;
  dataPartida: string;
  status: StatusPartida;
  golsMandante?: number;
  golsVisitante?: number;
  campeonatoId: number;
  timeMandanteId: number;
  timeVisitanteId: number;
}

export type TipoEvento =
  | 'GOL'
  | 'GOL_CONTRA'
  | 'PENALTI_GOL'
  | 'PENALTI_PERDIDO'
  | 'FINALIZACAO'
  | 'FINALIZACAO_NO_GOL'
  | 'DEFESA'
  | 'PENALTI_DEFENDIDO'
  | 'FALTA'
  | 'CARTAO_AMARELO'
  | 'CARTAO_VERMELHO'
  | 'IMPEDIMENTO'
  | 'ESCANTEIO'
  | 'VAR_GOL_CONFIRMADO'
  | 'VAR_GOL_ANULADO'
  | 'SUBSTITUICAO'
  | 'INICIO_PRIMEIRO_TEMPO'
  | 'FIM_PRIMEIRO_TEMPO'
  | 'INICIO_SEGUNDO_TEMPO'
  | 'FIM_PARTIDA';

export interface EventoPartida {
  id: number;
  tipoEvento: TipoEvento;
  minuto: number;
  minutoExtra?: number;
  descricao?: string;
  eventoRelacionadoId?: number;
  anulado?: boolean;
  partidaId: number;
  jogadorId?: number;
  nomeJogador?: string;
  nomeTime?: string;
  assistenteId?: number;
  nomeAssistente?: string;
}

export interface EventoTimeline {
  id: number;
  tipo: TipoEvento;
  minuto: number;
  minutoExtra?: number;
  tempo?: string;
  timeId?: number;
  nomeTime?: string;
  jogadorId?: number;
  jogador?: string;
  jogadorSecundarioId?: number;
  jogadorSecundario?: string;
}

export type FuncaoEscalacao = 'TITULAR' | 'RESERVA';

export type Posicao =
  | 'GOLEIRO'
  | 'ZAGUEIRO'
  | 'LATERAL_DIREITO'
  | 'LATERAL_ESQUERDO'
  | 'VOLANTE'
  | 'MEIO_CAMPO'
  | 'PONTA_DIREITA'
  | 'PONTA_ESQUERDA'
  | 'MEIA_ATACANTE'
  | 'CENTROAVANTE';

export interface TimePartidas {
  timeId: number;
  ultimasPartidas: Partida[];
  proximasPartidas: Partida[];
}

export interface EscalacaoPartida {
  id: number;
  jogadorId: number;
  nomeJogador: string;
  fotoUrl?: string;
  nomeTime: string;
  escudoTime?: string;
  funcao: FuncaoEscalacao;
  posicao: Posicao;
  numeroCamisa: number;
  ativo: boolean;
}

export interface EscalacaoPartidaList {
  partidaId: number;
  titulares: EscalacaoPartida[];
  reservas: EscalacaoPartida[];
}

export interface EstatisticasPartida {
  partidaId: number;
  posseBolaMandante: number;
  posseBolaVisitante: number;
  finalizacoesMandante: number;
  finalizacoesVisitante: number;
  finalizacoesGolMandante: number;
  finalizacoesGolVisitante: number;
  faltasMandante: number;
  faltasVisitante: number;
  escanteiosMandante: number;
  escanteiosVisitante: number;
  cartoesAmarelosMandante: number;
  cartoesAmarelosVisitante: number;
  cartoesVermelhosMandante: number;
  cartoesVermelhosVisitante: number;
  defesasMandante: number;
  defesasVisitante: number;
  penaltisDefendidosMandante: number;
  penaltisDefendidosVisitante: number;
}
