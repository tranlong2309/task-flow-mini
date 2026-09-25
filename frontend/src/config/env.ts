export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  apiMode: (import.meta.env.VITE_API_MODE ?? 'mock') as 'mock' | 'http',
}
