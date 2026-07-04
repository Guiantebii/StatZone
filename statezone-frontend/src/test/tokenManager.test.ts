import { describe, expect, it, vi, beforeEach } from 'vitest';

describe('tokenManager', () => {
  let tokenManager: typeof import('../api/tokenManager');

  beforeEach(async () => {
    vi.resetModules();
    tokenManager = await import('../api/tokenManager');
  });

  it('clearToken does not throw', () => {
    expect(() => tokenManager.clearToken()).not.toThrow();
  });

  it('notifyTokenChanged dispatches a custom event', () => {
    const listener = vi.fn();
    window.addEventListener('auth:token-changed', listener);
    tokenManager.notifyTokenChanged('some-token');
    expect(listener).toHaveBeenCalledWith(
      expect.objectContaining({ detail: 'some-token' }),
    );
    window.removeEventListener('auth:token-changed', listener);
  });
});
