import axios from 'axios'
import { ElMessage } from 'element-plus'

const DEFAULT_BASE = 'http://localhost:8081'

const http = axios.create({
  baseURL: DEFAULT_BASE,
  timeout: 20000
})

http.interceptors.request.use(config => {
  // Add auth header placeholder
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

http.interceptors.response.use(response => {
  // normalized: return data directly
  return response.data
}, error => {
  // Only show visible toast in production; in development log to console to avoid noisy UI when using mock fallback
  try {
    const mode = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.MODE) ? import.meta.env.MODE : process.env.NODE_ENV
    if (mode === 'production') {
      ElMessage.error(error?.message || '请求出错')
    } else {
      // development: quieter, allow mock fallback without alert
      // console.debug for developer visibility
      // eslint-disable-next-line no-console
      console.debug('HTTP request error (suppressed in dev):', error?.message || error)
    }
  } catch (e) {
    // fallback behavior: don't crash
    // eslint-disable-next-line no-console
    console.debug('HTTP error handling fallback', e)
  }
  return Promise.reject(error)
})

export default http


