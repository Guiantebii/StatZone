import type { AxiosError } from 'axios';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import api from '../api/client';

describe('client axios interceptor', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('response interceptor rejeita sem redirect para rotas de auth', async () => {
    const handler = api.interceptors.response.handlers![0];
    const error = {
      response: { status: 401 },
      config: { url: '/api/auth/login' },
    } as AxiosError;

    await expect(handler.rejected?.(error)).rejects.toBe(error);
  });

  it('response interceptor rejeita na pagina de login', async () => {
    const originalPath = window.location.pathname;
    Object.defineProperty(window, 'location', { value: { pathname: '/login' } });

    const handler = api.interceptors.response.handlers![0];
    const error = {
      response: { status: 401 },
      config: { url: '/partidas' },
    } as AxiosError;

    await expect(handler.rejected?.(error)).rejects.toBe(error);

    Object.defineProperty(window, 'location', { value: { pathname: originalPath } });
  });
});
