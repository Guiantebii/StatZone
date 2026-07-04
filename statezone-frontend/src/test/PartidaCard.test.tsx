import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import PartidaCard from '../components/PartidaCard';
import type { Partida } from '../types/partida';

const basePartida: Partida = {
  id: 1,
  campeonatoId: 1,
  campeonatoNome: 'Brasileirão',
  rodada: 5,
  timeMandanteId: 10,
  timeMandanteNome: 'Flamengo',
  timeVisitanteId: 20,
  timeVisitanteNome: 'Palmeiras',
  golsMandante: 2,
  golsVisitante: 1,
  status: 'ENCERRADA',
  dataPartida: '2026-06-15T20:00:00',
  estadio: 'Maracanã',
  arbitro: 'Árbitro Teste',
};

function renderCard(partida: Partida = basePartida) {
  return render(
    <MemoryRouter>
      <PartidaCard partida={partida} />
    </MemoryRouter>,
  );
}

describe('PartidaCard', () => {
  it('renderiza nomes dos times', () => {
    renderCard();
    expect(screen.getByText('Flamengo')).toBeInTheDocument();
    expect(screen.getByText('Palmeiras')).toBeInTheDocument();
  });

  it('renderiza placar para partidas encerradas', () => {
    renderCard();
    expect(screen.getByText('2 - 1')).toBeInTheDocument();
  });

  it('renderiza data para partidas agendadas', () => {
    renderCard({ ...basePartida, status: 'AGENDADA' });
    expect(screen.getByText('15 de jun.')).toBeInTheDocument();
  });

  it('renderiza badge de status', () => {
    renderCard();
    expect(screen.getByText('ENCERRADA')).toBeInTheDocument();
  });

  it('renderiza nome do campeonato e rodada', () => {
    renderCard();
    expect(screen.getByText('Brasileirão')).toBeInTheDocument();
    expect(screen.getByText('5ª rodada')).toBeInTheDocument();
  });

  it('renderiza estádio para partidas agendadas', () => {
    renderCard({ ...basePartida, status: 'AGENDADA' });
    expect(screen.getByText('Maracanã')).toBeInTheDocument();
  });

  it('não renderiza estádio para partidas encerradas', () => {
    renderCard();
    expect(screen.queryByText('Maracanã')).not.toBeInTheDocument();
  });
});
