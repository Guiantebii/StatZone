import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import ErrorBoundary from '../components/ErrorBoundary';

function BrokenComponent({ shouldThrow }: { shouldThrow?: boolean }) {
  if (shouldThrow) throw new Error('Test error');
  return <p>Componente funcionando</p>;
}

describe('ErrorBoundary', () => {
  it('renderiza children quando não há erro', () => {
    render(
      <MemoryRouter>
        <ErrorBoundary>
          <BrokenComponent />
        </ErrorBoundary>
      </MemoryRouter>,
    );

    expect(screen.getByText('Componente funcionando')).toBeInTheDocument();
  });

  it('renderiza fallback quando ocorre erro', () => {
    // Suppress console.error from React during this test
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <MemoryRouter>
        <ErrorBoundary>
          <BrokenComponent shouldThrow />
        </ErrorBoundary>
      </MemoryRouter>,
    );

    expect(screen.getByText('Algo deu errado')).toBeInTheDocument();
    expect(screen.getByText('Recarregar')).toBeInTheDocument();

    spy.mockRestore();
  });
});
