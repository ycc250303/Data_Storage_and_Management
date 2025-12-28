import axios from 'axios'
import { ElMessage } from 'element-plus'

const DEFAULT_BASE = 'http://localhost:8080'

const http = axios.create({
  baseURL: DEFAULT_BASE,
  timeout: 20000
})

http.interceptors.request.use(config => {
  // 打印请求日志
  console.log(`[HTTP Request] ${config.method.toUpperCase()} ${config.url}`, config.params || config.data || '')

  // Add auth header placeholder
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  console.error('[HTTP Request Error]', error)
  return Promise.reject(error)
})

http.interceptors.response.use(response => {
  // 打印响应日志
  console.log(`[HTTP Response] ${response.config.method.toUpperCase()} ${response.config.url}`, response.data)

  // normalized: return data directly
  return response.data
}, error => {
  // 打印错误详情
  console.error('[HTTP Response Error]', {
    url: error.config?.url,
    method: error.config?.method,
    status: error.response?.status,
    data: error.response?.data,
    message: error.message
  })
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


