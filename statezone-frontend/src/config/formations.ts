import type { Formacao, Posicao } from '../types/partida';

export interface FormacaoSlot {
  posicao: Posicao;
  label: string;
}

export interface FormacaoConfig {
  label: string;
  lines: { label: string; slots: FormacaoSlot[] }[];
}

export const FORMACOES: Record<Formacao, FormacaoConfig> = {
  _4_4_2: {
    label: '4-4-2',
    lines: [
      {
        label: 'Goleiro',
        slots: [{ posicao: 'GOLEIRO', label: 'Goleiro' }],
      },
      {
        label: 'Defensores',
        slots: [
          { posicao: 'LATERAL_ESQUERDO', label: 'LE' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'LATERAL_DIREITO', label: 'LD' },
        ],
      },
      {
        label: 'Meio-campistas',
        slots: [
          { posicao: 'MEIO_ESQUERDO', label: 'ME' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'MEIO_DIREITO', label: 'MD' },
        ],
      },
      {
        label: 'Atacantes',
        slots: [
          { posicao: 'CENTROAVANTE', label: 'CA' },
          { posicao: 'CENTROAVANTE', label: 'CA' },
        ],
      },
    ],
  },
  _4_3_3: {
    label: '4-3-3',
    lines: [
      {
        label: 'Goleiro',
        slots: [{ posicao: 'GOLEIRO', label: 'Goleiro' }],
      },
      {
        label: 'Defensores',
        slots: [
          { posicao: 'LATERAL_ESQUERDO', label: 'LE' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'LATERAL_DIREITO', label: 'LD' },
        ],
      },
      {
        label: 'Meio-campistas',
        slots: [
          { posicao: 'VOLANTE', label: 'VOL' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
        ],
      },
      {
        label: 'Atacantes',
        slots: [
          { posicao: 'PONTA_ESQUERDA', label: 'PE' },
          { posicao: 'CENTROAVANTE', label: 'CA' },
          { posicao: 'PONTA_DIREITA', label: 'PD' },
        ],
      },
    ],
  },
  _4_2_3_1: {
    label: '4-2-3-1',
    lines: [
      {
        label: 'Goleiro',
        slots: [{ posicao: 'GOLEIRO', label: 'Goleiro' }],
      },
      {
        label: 'Defensores',
        slots: [
          { posicao: 'LATERAL_ESQUERDO', label: 'LE' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'LATERAL_DIREITO', label: 'LD' },
        ],
      },
      {
        label: 'Volantes',
        slots: [
          { posicao: 'VOLANTE', label: 'VOL' },
          { posicao: 'VOLANTE', label: 'VOL' },
        ],
      },
      {
        label: 'Meias',
        slots: [
          { posicao: 'PONTA_ESQUERDA', label: 'PE' },
          { posicao: 'MEIA_ATACANTE', label: 'MEI' },
          { posicao: 'PONTA_DIREITA', label: 'PD' },
        ],
      },
      {
        label: 'Atacante',
        slots: [{ posicao: 'CENTROAVANTE', label: 'CA' }],
      },
    ],
  },
  _3_5_2: {
    label: '3-5-2',
    lines: [
      {
        label: 'Goleiro',
        slots: [{ posicao: 'GOLEIRO', label: 'Goleiro' }],
      },
      {
        label: 'Defensores',
        slots: [
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
        ],
      },
      {
        label: 'Meio-campistas',
        slots: [
          { posicao: 'LATERAL_ESQUERDO', label: 'LE' },
          { posicao: 'VOLANTE', label: 'VOL' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'VOLANTE', label: 'VOL' },
          { posicao: 'LATERAL_DIREITO', label: 'LD' },
        ],
      },
      {
        label: 'Atacantes',
        slots: [
          { posicao: 'CENTROAVANTE', label: 'CA' },
          { posicao: 'CENTROAVANTE', label: 'CA' },
        ],
      },
    ],
  },
  _3_4_3: {
    label: '3-4-3',
    lines: [
      {
        label: 'Goleiro',
        slots: [{ posicao: 'GOLEIRO', label: 'Goleiro' }],
      },
      {
        label: 'Defensores',
        slots: [
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
        ],
      },
      {
        label: 'Meio-campistas',
        slots: [
          { posicao: 'LATERAL_ESQUERDO', label: 'LE' },
          { posicao: 'VOLANTE', label: 'VOL' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'LATERAL_DIREITO', label: 'LD' },
        ],
      },
      {
        label: 'Atacantes',
        slots: [
          { posicao: 'PONTA_ESQUERDA', label: 'PE' },
          { posicao: 'CENTROAVANTE', label: 'CA' },
          { posicao: 'PONTA_DIREITA', label: 'PD' },
        ],
      },
    ],
  },
  _4_1_4_1: {
    label: '4-1-4-1',
    lines: [
      {
        label: 'Goleiro',
        slots: [{ posicao: 'GOLEIRO', label: 'Goleiro' }],
      },
      {
        label: 'Defensores',
        slots: [
          { posicao: 'LATERAL_ESQUERDO', label: 'LE' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'LATERAL_DIREITO', label: 'LD' },
        ],
      },
      {
        label: 'Volante',
        slots: [{ posicao: 'VOLANTE', label: 'VOL' }],
      },
      {
        label: 'Meias',
        slots: [
          { posicao: 'PONTA_ESQUERDA', label: 'PE' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'PONTA_DIREITA', label: 'PD' },
        ],
      },
      {
        label: 'Atacante',
        slots: [{ posicao: 'CENTROAVANTE', label: 'CA' }],
      },
    ],
  },
  _5_3_2: {
    label: '5-3-2',
    lines: [
      {
        label: 'Goleiro',
        slots: [{ posicao: 'GOLEIRO', label: 'Goleiro' }],
      },
      {
        label: 'Defensores',
        slots: [
          { posicao: 'LATERAL_ESQUERDO', label: 'LE' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'LATERAL_DIREITO', label: 'LD' },
        ],
      },
      {
        label: 'Meio-campistas',
        slots: [
          { posicao: 'VOLANTE', label: 'VOL' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'VOLANTE', label: 'VOL' },
        ],
      },
      {
        label: 'Atacantes',
        slots: [
          { posicao: 'CENTROAVANTE', label: 'CA' },
          { posicao: 'CENTROAVANTE', label: 'CA' },
        ],
      },
    ],
  },
  _4_4_1_1: {
    label: '4-4-1-1',
    lines: [
      {
        label: 'Goleiro',
        slots: [{ posicao: 'GOLEIRO', label: 'Goleiro' }],
      },
      {
        label: 'Defensores',
        slots: [
          { posicao: 'LATERAL_ESQUERDO', label: 'LE' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'ZAGUEIRO', label: 'ZAG' },
          { posicao: 'LATERAL_DIREITO', label: 'LD' },
        ],
      },
      {
        label: 'Meio-campistas',
        slots: [
          { posicao: 'MEIO_ESQUERDO', label: 'ME' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'MEIO_CAMPO', label: 'MC' },
          { posicao: 'MEIO_DIREITO', label: 'MD' },
        ],
      },
      {
        label: 'Meia-atacante',
        slots: [{ posicao: 'MEIA_ATACANTE', label: 'MEI' }],
      },
      {
        label: 'Atacante',
        slots: [{ posicao: 'CENTROAVANTE', label: 'CA' }],
      },
    ],
  },
};

export const FORMACOES_LIST = Object.entries(FORMACOES).map(([key, config]) => ({
  value: key as Formacao,
  label: config.label,
}));

export const POSICAO_ABBR: Record<string, string> = {
  GOLEIRO: 'GOL',
  ZAGUEIRO: 'ZAG',
  LATERAL_DIREITO: 'LD',
  LATERAL_ESQUERDO: 'LE',
  VOLANTE: 'VOL',
  MEIO_CAMPO: 'MC',
  MEIO_ESQUERDO: 'ME',
  MEIO_DIREITO: 'MD',
  PONTA_DIREITA: 'PD',
  PONTA_ESQUERDA: 'PE',
  MEIA_ATACANTE: 'MEI',
  CENTROAVANTE: 'CA',
};
