import type { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import api from '../api/client';
import { getToken } from '../api/tokenManager';

vi.mock('../api/tokenManager', () => ({
  getToken: vi.fn(),
}));

describe('client axios interceptor', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('adiciona Authorization header quando token existe', async () => {
    vi.mocked(getToken).mockReturnValue('my-jwt-token');

    const handler = api.interceptors.request.handlers![0];
    const config = { headers: { Authorization: '' } } as unknown as InternalAxiosRequestConfig;

    const result = await handler.fulfilled(config);
    expect((result as InternalAxiosRequestConfig).headers.Authorization).toBe('Bearer my-jwt-token');
  });

  it('não adiciona header quando token é null', async () => {
    vi.mocked(getToken).mockReturnValue(null);

    const handler = api.interceptors.request.handlers![0];
    const config = { headers: {} } as unknown as InternalAxiosRequestConfig;

    const result = await handler.fulfilled(config);
    expect((result as InternalAxiosRequestConfig).headers.Authorization).toBeUndefined();
  });

  it('response interceptor rejeita sem redirect para rotas de auth', async () => {
    const handler = api.interceptors.response.handlers![0];
    const error = {
      response: { status: 401 },
      config: { url: '/api/auth/login' },
    } as AxiosError;

    await expect(handler.rejected?.(error)).rejects.toBe(error);
  });
});
