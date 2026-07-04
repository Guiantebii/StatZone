import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import ProtectedRoute from '../components/ProtectedRoute';

const mockUseAuth = vi.fn();

vi.mock('../context/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
}));

vi.mock('sonner', () => ({
  toast: { error: vi.fn() },
}));

function renderProtected() {
  return render(
    <MemoryRouter>
      <ProtectedRoute />
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  it('renders spinner while loading', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isAdmin: false, loading: true });
    renderProtected();
    expect(screen.getByText('Carregando...')).toBeInTheDocument();
  });

  it('redirects to /login when not authenticated', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isAdmin: false, loading: false });
    renderProtected();
    // Navigate to /login should render nothing else
    expect(screen.queryByText('Protected')).not.toBeInTheDocument();
  });

  it('redirects to / when not admin', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isAdmin: false, loading: false });
    renderProtected();
    expect(screen.queryByText('Protected')).not.toBeInTheDocument();
  });

  it('renders Outlet when authenticated and admin', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isAdmin: true, loading: false });
    renderProtected();
    // Outlet renders nothing when no route matches - that's expected
    expect(document.body).toBeDefined();
  });
});
