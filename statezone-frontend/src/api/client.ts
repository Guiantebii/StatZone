import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : null);

if (!baseURL) {
  throw new Error('VITE_API_URL environment variable is required');
}

const api = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: true, // rely on httpOnly cookie set by server
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      error.response?.status === 401 &&
      !error.config.url?.includes('/api/auth/') &&
      !window.location.pathname.startsWith('/login')
    ) {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

export default api;
