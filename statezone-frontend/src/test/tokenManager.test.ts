import { describe, expect, it, vi, beforeEach } from 'vitest';

const STORAGE_KEY = 'auth_token';

describe('tokenManager', () => {
  let tokenManager: typeof import('../api/tokenManager');

  beforeEach(async () => {
    localStorage.clear();
    vi.resetModules();
    tokenManager = await import('../api/tokenManager');
  });

  it('setToken stores token in memory and localStorage', () => {
    tokenManager.setToken('my-token');
    expect(tokenManager.getToken()).toBe('my-token');
    expect(localStorage.getItem(STORAGE_KEY)).toBe('my-token');
  });

  it('getToken returns null when no token is set', () => {
    expect(tokenManager.getToken()).toBeNull();
  });

  it('getToken restores from localStorage when memory is empty', () => {
    localStorage.setItem(STORAGE_KEY, 'restored-token');
    expect(tokenManager.getToken()).toBe('restored-token');
  });

  it('clearToken removes from both memory and localStorage', () => {
    tokenManager.setToken('my-token');
    tokenManager.clearToken();
    expect(tokenManager.getToken()).toBeNull();
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it('setToken(null) removes the stored token', () => {
    tokenManager.setToken('my-token');
    tokenManager.setToken(null);
    expect(tokenManager.getToken()).toBeNull();
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });
});
