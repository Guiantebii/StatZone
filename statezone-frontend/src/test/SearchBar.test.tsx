import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import SearchBar from '../components/SearchBar';
import api from '../api/client';

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}));

function renderSearchBar() {
  return render(
    <MemoryRouter>
      <SearchBar />
    </MemoryRouter>,
  );
}

describe('SearchBar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renderiza input com placeholder', () => {
    renderSearchBar();
    const input = screen.getByPlaceholderText('Buscar...');
    expect(input).toBeInTheDocument();
    expect(input).toHaveAttribute('aria-label', 'Buscar...');
  });

  it('não mostra resultados sem query', () => {
    renderSearchBar();
    expect(screen.queryByText('Times')).not.toBeInTheDocument();
  });

  it('mostra "Buscando..." durante carregamento', async () => {
    vi.mocked(api.get).mockImplementation(() => new Promise(() => {}));

    const user = userEvent.setup();
    renderSearchBar();

    const input = screen.getByPlaceholderText('Buscar...');
    await user.type(input, 'Flamengo');

    // Wait for debounce
    await new Promise((r) => setTimeout(r, 350));

    expect(screen.getByText('Buscando...')).toBeInTheDocument();
  });

  it('mostra mensagem quando nenhum resultado é encontrado', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    const user = userEvent.setup();
    renderSearchBar();

    const input = screen.getByPlaceholderText('Buscar...');
    await user.type(input, 'xyz');

    // Wait for debounce
    await new Promise((r) => setTimeout(r, 350));

    expect(screen.getByText(/Nenhum resultado encontrado/)).toBeInTheDocument();
  });

  it('mostra resultados de times', async () => {
    vi.mocked(api.get)
      .mockResolvedValueOnce({ data: [{ id: 1, nome: 'Flamengo', sigla: 'FLA' }] })
      .mockResolvedValueOnce({ data: [] });

    const user = userEvent.setup();
    renderSearchBar();

    const input = screen.getByPlaceholderText('Buscar...');
    await user.type(input, 'Fla');

    // Wait for debounce
    await new Promise((r) => setTimeout(r, 350));

    expect(screen.getByText('Times')).toBeInTheDocument();
    expect(screen.getByText('Flamengo')).toBeInTheDocument();
    expect(screen.getByText('FLA')).toBeInTheDocument();
  });

  it('mostra resultados de jogadores', async () => {
    vi.mocked(api.get)
      .mockResolvedValueOnce({ data: [] })
      .mockResolvedValueOnce({ data: [{ id: 1, nome: 'Neymar', nomeTime: 'Santos' }] });

    const user = userEvent.setup();
    renderSearchBar();

    const input = screen.getByPlaceholderText('Buscar...');
    await user.type(input, 'Ney');

    // Wait for debounce
    await new Promise((r) => setTimeout(r, 350));

    expect(screen.getByText('Jogadores')).toBeInTheDocument();
    expect(screen.getByText('Neymar')).toBeInTheDocument();
    expect(screen.getByText('Santos')).toBeInTheDocument();
  });

  it('fecha dropdown com Escape', async () => {
    vi.mocked(api.get)
      .mockResolvedValueOnce({ data: [{ id: 1, nome: 'Flamengo', sigla: 'FLA' }] })
      .mockResolvedValueOnce({ data: [] });

    const user = userEvent.setup();
    renderSearchBar();

    const input = screen.getByPlaceholderText('Buscar...');
    await user.type(input, 'Fla');

    // Wait for debounce
    await new Promise((r) => setTimeout(r, 350));

    expect(screen.getByText('Flamengo')).toBeInTheDocument();

    await user.keyboard('{Escape}');

    expect(screen.queryByText('Flamengo')).not.toBeInTheDocument();
  });
});
