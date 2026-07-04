import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import FormationView from '../components/FormationView';
import type { EscalacaoPartida } from '../types/partida';

const makePlayer = (id: number, nome: string, posicao: string, numeroCamisa: number): EscalacaoPartida => ({
  id,
  jogadorId: id,
  nomeJogador: nome,
  numeroCamisa,
  posicao: posicao as EscalacaoPartida['posicao'],
  funcao: 'TITULAR',
  nomeTime: 'Time A',
  ativo: true,
});

describe('FormationView', () => {
  it('renders formation label when formacao is provided', () => {
    const titulares = [
      makePlayer(1, 'Alisson', 'GOLEIRO', 1),
      makePlayer(2, 'Marquinhos', 'ZAGUEIRO', 4),
      makePlayer(3, 'Neymar', 'CENTROAVANTE', 10),
    ];
    render(<FormationView titulares={titulares} formacao="_4_3_3" />);
    expect(screen.getByText('4-3-3')).toBeInTheDocument();
  });

  it('renders calculated formation when no formacao is provided', () => {
    const titulares = [
      makePlayer(1, 'Alisson', 'GOLEIRO', 1),
      makePlayer(2, 'Marquinhos', 'ZAGUEIRO', 4),
      makePlayer(3, 'Casemiro', 'VOLANTE', 5),
      makePlayer(4, 'Neymar', 'PONTA_ESQUERDA', 10),
    ];
    render(<FormationView titulares={titulares} />);
    // 1 GK, 1 DEF, 1 MID, 1 FWD = "1-1-1"
    expect(screen.getByText('1-1-1')).toBeInTheDocument();
  });

  it('renders player names and numbers', () => {
    const titulares = [makePlayer(1, 'Alisson', 'GOLEIRO', 1), makePlayer(2, 'Neymar', 'CENTROAVANTE', 10)];
    render(<FormationView titulares={titulares} />);
    expect(screen.getByText('Alisson')).toBeInTheDocument();
    expect(screen.getByText('Neymar')).toBeInTheDocument();
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
  });

  it('renders empty container when no titulares', () => {
    const { container } = render(<FormationView titulares={[]} />);
    const field = container.querySelector('.aspect-\\[3\\/4\\]');
    expect(field).toBeInTheDocument();
  });

  it('renders formation badge with known template', () => {
    const titulares = Array.from({ length: 11 }, (_, i) => makePlayer(i + 1, `Jogador ${i + 1}`, 'ZAGUEIRO', i + 1));
    render(<FormationView titulares={titulares} formacao="_4_4_2" />);
    expect(screen.getByText('4-4-2')).toBeInTheDocument();
    expect(screen.getByText('4-4-2')).toHaveClass('text-accent');
  });
});
