import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useWebSocket } from '../hooks/useWebSocket';

vi.mock('@stomp/stompjs', () => {
  const mockClient = {
    subscribe: vi.fn(),
    deactivate: vi.fn(),
    activate: vi.fn(),
    connected: true,
    onConnect: null,
    onDisconnect: null,
  };
  const MockClient = vi.fn(function () {
    return mockClient;
  });
  return { Client: MockClient };
});

vi.mock('sockjs-client', () => ({ default: vi.fn() }));

vi.mock('../utils/logger', () => ({ default: { info: vi.fn(), warn: vi.fn() } }));

describe('useWebSocket', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('creates client and activates on mount', async () => {
    const { Client } = await import('@stomp/stompjs');
    const { unmount } = renderHook(() => useWebSocket());
    expect(Client).toHaveBeenCalledTimes(1);
    const instance = vi.mocked(Client).mock.results[0]?.value;
    expect(instance.activate).toHaveBeenCalledTimes(1);
    unmount();
  });

  it('calls onConnect when client connects', async () => {
    const { Client } = await import('@stomp/stompjs');
    const onConnect = vi.fn();
    renderHook(() => useWebSocket({ onConnect }));
    const instance = vi.mocked(Client).mock.results[0]?.value;
    act(() => instance.onConnect());
    expect(onConnect).toHaveBeenCalledTimes(1);
  });

  it('calls onDisconnect when client disconnects', async () => {
    const { Client } = await import('@stomp/stompjs');
    const onDisconnect = vi.fn();
    renderHook(() => useWebSocket({ onDisconnect }));
    const instance = vi.mocked(Client).mock.results[0]?.value;
    act(() => instance.onDisconnect());
    expect(onDisconnect).toHaveBeenCalledTimes(1);
  });

  it('deactivates client on unmount', async () => {
    const { Client } = await import('@stomp/stompjs');
    const { unmount } = renderHook(() => useWebSocket());
    const instance = vi.mocked(Client).mock.results[0]?.value;
    unmount();
    expect(instance.deactivate).toHaveBeenCalledTimes(1);
  });

  it('recreates client on auth:token-changed event', async () => {
    const { Client } = await import('@stomp/stompjs');
    renderHook(() => useWebSocket());
    const initialInstance = vi.mocked(Client).mock.results[0]?.value;
    act(() => { window.dispatchEvent(new Event('auth:token-changed')); });
    expect(initialInstance.deactivate).toHaveBeenCalledTimes(1);
    expect(Client).toHaveBeenCalledTimes(2);
  });

  it('subscribe calls client.subscribe and returns unsubscribe function', () => {
    const { result } = renderHook(() => useWebSocket());
    const callback = vi.fn();
    const unsubscribe = result.current.subscribe('/topic/test', callback);
    expect(unsubscribe).not.toBeNull();
    expect(typeof unsubscribe).toBe('function');
  });
});
