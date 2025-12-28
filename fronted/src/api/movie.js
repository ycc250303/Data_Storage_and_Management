import http from '@/utils/http'
import { mockListMovies, mockGetMovie } from '@/mock/mockService'

/**
 * 获取电影列表（支持分页和过滤）
 * params: { page, pageSize, title, genre, year }
 * If backend returns empty or fails, fall back to client mock.
 */
export async function listMovies(params) {
  try {
    const res = await http.get('/movies', { params })
    // normalize common shapes
    const items = res.items || res.data || res || []
    const total = res.total || (res.meta && res.meta.total) || (Array.isArray(res) ? res.length : items.length)
    if (items && items.length > 0) {
      return { items, total }
    }
    // fallback to mock when backend returned empty
    return await mockListMovies(params)
  } catch (err) {
    return await mockListMovies(params)
  }
}

export async function getMovie(id) {
  try {
    const res = await http.get(`/movies/${id}`)
    return res || res.data || null
  } catch (err) {
    return await mockGetMovie(id)
  }
}

export async function searchMovies(query) {
  try {
    const res = await http.get('/movies/search', { params: query })
    return res || res.data || []
  } catch (err) {
    // basic client-side search via mock
    const r = await mockListMovies(query)
    return r.items || []
  }
}


