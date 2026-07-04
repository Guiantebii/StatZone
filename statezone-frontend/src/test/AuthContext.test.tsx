import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { AuthProvider, useAuth } from '../context/AuthContext';

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
    mockGet.mockReturnValue(new Promise(() => {}));
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

  it('sets isAdmin=false when role is USER', async () => {
    mockGet.mockResolvedValue({ data: { email: 'user@test.com', role: 'USER' } });
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    );
    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false');
    });
    expect(screen.getByTestId('authenticated').textContent).toBe('true');
    expect(screen.getByTestId('admin').textContent).toBe('false');
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
    expect(screen.getByTestId('email').textContent).toBe('null');
  });

  it('login sets auth state with ADMIN role', async () => {
    mockGet.mockResolvedValue({ data: { email: 'test@test.com', role: 'ADMIN' } });
    mockPost.mockResolvedValue({ data: { token: 'fake-token' } });

    const user = userEvent.setup();

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    );

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

  it('login sets isAdmin=false for USER role', async () => {
    mockGet.mockResolvedValueOnce({ data: { email: 'user@test.com', role: 'ADMIN' } });
    mockGet.mockResolvedValueOnce({ data: { email: 'user@test.com', role: 'USER' } });
    mockPost.mockResolvedValue({ data: { token: 'fake-token' } });

    const user = userEvent.setup();

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false');
    });

    await user.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(screen.getByTestId('admin').textContent).toBe('false');
    });
    expect(screen.getByTestId('authenticated').textContent).toBe('true');
    expect(screen.getByTestId('email').textContent).toBe('user@test.com');
  });

  it('logout clears auth state', async () => {
    mockGet.mockResolvedValue({ data: { email: 'admin@test.com', role: 'ADMIN' } });
    mockPost.mockResolvedValue({});

    const user = userEvent.setup();

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false');
    });

    await user.click(screen.getByText('Logout'));

    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('false');
    });
    expect(screen.getByTestId('admin').textContent).toBe('false');
    expect(screen.getByTestId('email').textContent).toBe('null');
  });
});

describe('useAuth', () => {
  it('throws error when used outside AuthProvider', () => {
    expect(() => render(<TestComponent />)).toThrow(
      'useAuth must be used within AuthProvider',
    );
  });
});
