const STORAGE_KEY = 'auth_token';
let currentToken: string | null = null;

// Persists token by default to localStorage to keep UX (reloads).
// For better security prefer httpOnly cookies from the server.
export function setToken(token: string | null) {
  currentToken = token;
  if (token) {
    localStorage.setItem(STORAGE_KEY, token);
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
  // Notify listeners (e.g., websocket clients) that token changed
  try {
    window.dispatchEvent(new CustomEvent('auth:token-changed', { detail: token }));
  } catch {}
}

export function getToken(): string | null {
  if (currentToken) return currentToken;
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored) {
    currentToken = stored;
    return stored;
  }
  return null;
}

export function clearToken() {
  currentToken = null;
  localStorage.removeItem(STORAGE_KEY);
  try {
    window.dispatchEvent(new CustomEvent('auth:token-changed', { detail: null }));
  } catch {}
}
