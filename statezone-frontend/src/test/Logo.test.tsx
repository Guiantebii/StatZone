import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import Logo from '../components/Logo';

describe('Logo', () => {
  it('renderiza SZ e StateZone', () => {
    render(
      <MemoryRouter>
        <Logo />
      </MemoryRouter>,
    );
    expect(screen.getByText('SZ')).toBeInTheDocument();
    expect(screen.getByText('StateZone')).toBeInTheDocument();
    expect(screen.getByText('Sports Analytics')).toBeInTheDocument();
  });

  it('não renderiza texto quando collapse é true', () => {
    render(
      <MemoryRouter>
        <Logo collapse />
      </MemoryRouter>,
    );
    expect(screen.getByText('SZ')).toBeInTheDocument();
    expect(screen.queryByText('StateZone')).not.toBeInTheDocument();
    expect(screen.queryByText('Sports Analytics')).not.toBeInTheDocument();
  });

  it('link aponta para o caminho correto', () => {
    render(
      <MemoryRouter>
        <Logo to="/custom" />
      </MemoryRouter>,
    );
    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/custom');
  });
});
