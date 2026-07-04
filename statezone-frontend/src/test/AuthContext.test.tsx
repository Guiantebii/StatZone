import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { AuthProvider, useAuth } from '../context/AuthContext';

// Mock api client
const mockPost = vi.fn();
const mockGet = vi.fn();

vi.mock('../api/client', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
  },
}));

vi.mock('../api/tokenManager', () => ({
  setToken: vi.fn(),
  getToken: vi.fn(() => null),
  clearToken: vi.fn(),
}));

function TestComponent() {
  const auth = useAuth();
  return (
    <div>
      <div data-testid="loading">{String(auth.loading)}</div>
      <div data-testid="authenticated">{String(auth.isAuthenticated)}</div>
      <div data-testid="admin">{String(auth.isAdmin)}</div>
      <div data-testid="email">{auth.userEmail || 'null'}</div>
      <button onClick={() => auth.login('test@test.com', 'pass123')}>Login</button>
      <button onClick={() => auth.logout()}>Logout</button>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('starts loading while checking auth', () => {
    mockGet.mockReturnValue(new Promise(() => {})); // never resolves
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    );
    expect(screen.getByTestId('loading').textContent).toBe('true');
  });

  it('sets authenticated state after /api/auth/me succeeds', async () => {
    mockGet.mockResolvedValue({ data: { email: 'admin@test.com', role: 'ADMIN' } });
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    );
    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false');
    });
    expect(screen.getByTestId('authenticated').textContent).toBe('true');
    expect(screen.getByTestId('admin').textContent).toBe('true');
    expect(screen.getByTestId('email').textContent).toBe('admin@test.com');
  });

  it('sets unauthenticated when /api/auth/me fails', async () => {
    mockGet.mockRejectedValue(new Error('Unauthorized'));
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    );
    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false');
    });
    expect(screen.getByTestId('authenticated').textContent).toBe('false');
    expect(screen.getByTestId('admin').textContent).toBe('false');
  });

  it('login sets token and auth state', async () => {
    mockGet.mockResolvedValue({ data: { email: 'test@test.com', role: 'ADMIN' } });
    mockPost.mockResolvedValue({
      data: {
        token: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwicm9sZXMiOiJST0xFX0FETUlOIn0.test',
      },
    });

    const user = userEvent.setup();

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    );

    // Wait for initial auth check
    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false');
    });

    await user.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('true');
    });
    expect(screen.getByTestId('admin').textContent).toBe('true');
    expect(screen.getByTestId('email').textContent).toBe('test@test.com');
  });
});
