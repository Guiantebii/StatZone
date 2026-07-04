export interface Jogador {
  id: number;
  nome: string;
  dataNascimento?: string | null;
  nacionalidade?: string | null;
  posicao: string;
  numeroCamisa?: number | null;
  altura?: number | null;
  peso?: number | null;
  valorMercado?: number | null;
  peForte?: string | null;
  fotoUrl?: string | null;
  timeId: number;
  nomeTime: string;
}
