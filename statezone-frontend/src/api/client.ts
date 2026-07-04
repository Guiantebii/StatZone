import axios, { type InternalAxiosRequestConfig } from 'axios';

const baseURL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : null);

if (!baseURL) {
  throw new Error('VITE_API_URL environment variable is required');
}

const api = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: true, // rely on httpOnly cookie set by server
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: (val: unknown) => void; reject: (err: unknown) => void; config: InternalAxiosRequestConfig }> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status === 401 &&
      !originalRequest.url?.includes('/api/auth/') &&
      !window.location.pathname.startsWith('/login')
    ) {
      if (originalRequest._retry) {
        window.location.href = '/login';
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise(function (resolve, reject) {
          failedQueue.push({ resolve, reject, config: originalRequest });
        })
          .then(() => api(originalRequest))
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        await api.post('/api/auth/refresh', null, { withCredentials: true });
        processQueue(null, null);
        return api(originalRequest);
      } catch (err) {
        processQueue(err, null);
        window.location.href = '/login';
        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  },
);

export default api;
