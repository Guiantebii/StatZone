const STORAGE_KEY = 'auth_token';
let currentToken: string | null = null;

export function setToken(token: string | null) {
  currentToken = token;
  if (token) {
    localStorage.setItem(STORAGE_KEY, token);
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
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
}
