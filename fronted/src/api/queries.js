import http from '@/utils/http'
import { mockListMovies, mockStats } from '@/mock/mockService'

// Generic pattern: try backend endpoint, fallback to mock
export async function queryByTime(params) {
  try {
    const res = await http.get('/queries/time', { params })
    return res || res.data || { items: [], total: 0 }
  } catch (err) {
    // simple fallback: reuse mock list (filter by year range if provided)
    const all = await mockListMovies({ page: 1, pageSize: 1000 })
    let items = all.items || []
    if (params?.startYear || params?.endYear) {
      const s = params.startYear || 0
      const e = params.endYear || 9999
      items = items.filter(m => (m.year || 0) >= s && (m.year || 0) <= e)
    }
    return { items, total: items.length }
  }
}

export async function queryByDirector(params) {
  try {
    const res = await http.get('/queries/director', { params })
    return res || res.data || { items: [], total: 0 }
  } catch (err) {
    // fallback: no director data in mock, return empty
    return { items: [], total: 0 }
  }
}

export async function queryByActor(params) {
  try {
    const res = await http.get('/queries/actor', { params })
    return res || res.data || { items: [], total: 0 }
  } catch (err) {
    return { items: [], total: 0 }
  }
}

export async function queryByGenre(params) {
  try {
    const res = await http.get('/queries/genre', { params })
    return res || res.data || { items: [], total: 0 }
  } catch (err) {
    // use movie stats mock to build genre list
    const s = await mockStats()
    // convert to items array
    const items = Object.entries(s.byGenre || {}).map(([genre, count]) => ({ genre, count }))
    return { items, total: items.length }
  }
}

export async function queryByUserRating(params) {
  try {
    const res = await http.get('/queries/userrating', { params })
    return res || res.data || { items: [], total: 0 }
  } catch (err) {
    return { items: [], total: 0 }
  }
}

export async function queryRelation(params) {
  try {
    const res = await http.get('/queries/relation', { params })
    return res || res.data || { graph: {}, nodes: [], edges: [] }
  } catch (err) {
    // fallback: try mock relation
    const mod = await import('@/mock/mockService')
    return mod.mockRelation(params)
  }
}

export async function compareTiming(params) {
  try {
    const res = await http.get('/queries/compare', { params })
    return res || res.data || {}
  } catch (err) {
    // fallback to mock
    const mod = await import('@/mock/mockService')
    return mod.mockCompareTimes()
  }
}

export async function complexQuery(filters) {
  try {
    const res = await http.post('/queries/complex', filters)
    return res || res.data || {}
  } catch (err) {
    const mod = await import('@/mock/mockService')
    return mod.mockComplexQuery(filters)
  }
}


