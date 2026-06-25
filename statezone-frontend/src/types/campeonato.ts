export interface Campeonato {
  id: number;
  nome: string;
  pais: string;
  temporada: string;
  logoUrl: string;
  tipoFormato?: string;
  amarelosParaSuspensao?: number;
}