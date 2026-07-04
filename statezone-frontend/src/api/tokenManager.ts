// Auth is handled via httpOnly cookies.
// This module provides an event bus so WebSocket can react to auth changes.

export function notifyTokenChanged(token: string | null) {
  try {
    window.dispatchEvent(new CustomEvent('auth:token-changed', { detail: token }));
  } catch {}
}

export function clearToken() {
  notifyTokenChanged(null);
}
