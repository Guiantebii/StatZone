import type { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import api from '../api/client';

function makeError(status: number, url: string, retry = false): AxiosError {
  return {
    response: { status },
    config: {
      url,
      _retry: retry,
    } as unknown as InternalAxiosRequestConfig,
    isAxiosError: true,
    toJSON: () => ({}),
    name: 'AxiosError',
    message: 'error',
  } as AxiosError;
}

describe('client axios interceptor', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('passes through non-401 errors', async () => {
    const handler = api.interceptors.response.handlers![0];
    const error = makeError(500, '/partidas');

    await expect(handler.rejected?.(error)).rejects.toBe(error);
  });

  it('rejects without redirect for auth routes', async () => {
    const handler = api.interceptors.response.handlers![0];
    const error = makeError(401, '/api/auth/login');

    await expect(handler.rejected?.(error)).rejects.toBe(error);
  });

  it('rejects when on /login page', async () => {
    const originalPath = window.location.pathname;
    Object.defineProperty(window, 'location', {
      value: { pathname: '/login', href: '' },
      writable: true,
    });

    const handler = api.interceptors.response.handlers![0];
    const error = makeError(401, '/partidas');

    await expect(handler.rejected?.(error)).rejects.toBe(error);

    Object.defineProperty(window, 'location', {
      value: { pathname: originalPath, href: '' },
      writable: true,
    });
  });

  it('redirects to /login on retry failure', async () => {
    const handler = api.interceptors.response.handlers![0];
    const error = makeError(401, '/dashboard', true);

    const originalHref = window.location.href;
    Object.defineProperty(window, 'location', {
      value: { href: '', pathname: '/dashboard' },
      writable: true,
    });

    await expect(handler.rejected?.(error)).rejects.toBe(error);
    expect(window.location.href).toBe('/login');

    Object.defineProperty(window, 'location', {
      value: { href: originalHref, pathname: '/' },
      writable: true,
    });
  });
});
