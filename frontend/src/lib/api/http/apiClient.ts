import axios from 'axios'
import { env } from '../../../config/env'

export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('taskflow.token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
