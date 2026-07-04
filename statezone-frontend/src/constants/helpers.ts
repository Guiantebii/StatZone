const POSICAO_LABEL: Record<string, string> = {
  GOLEIRO: 'Goleiro', ZAGUEIRO: 'Zagueiro', LATERAL_DIREITO: 'Lateral Direito',
  LATERAL_ESQUERDO: 'Lateral Esquerdo', VOLANTE: 'Volante', MEIO_CAMPO: 'Meio-Campo',
  MEIO_ESQUERDO: 'Meio Esquerdo', MEIO_DIREITO: 'Meio Direito',
  PONTA_DIREITA: 'Ponta Direita', PONTA_ESQUERDA: 'Ponta Esquerda',
  MEIA_ATACANTE: 'Meia-Atacante', CENTROAVANTE: 'Centroavante',
};

export function posicaoLabel(posicao: string): string {
  return POSICAO_LABEL[posicao] || posicao;
}

export function getLogoUrl(nome: string, size = 48): string {
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=1a3460&color=FFD700&size=${size}&bold=true`;
}

export function getAvatarUrl(nome: string, size = 32, bg = '1a3460', color = 'fff'): string {
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=${bg}&color=${color}&size=${size}`;
}

export function getJogadorAvatarUrl(nome: string, size = 32): string {
  return getAvatarUrl(nome, size, '1B5E20', 'fff');
}
