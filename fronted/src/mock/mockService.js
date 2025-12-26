import movies from './data/movies.json'

function paginate(array, page = 1, pageSize = 20) {
  const start = (page - 1) * pageSize
  return {
    items: array.slice(start, start + pageSize),
    total: array.length
  }
}

export function mockListMovies({ page = 1, pageSize = 20, title = '', genre = '' } = {}) {
  let list = movies.slice()
  if (title) {
    const t = title.toLowerCase()
    list = list.filter(m => (m.title || '').toLowerCase().includes(t) || (m.summary || '').toLowerCase().includes(t))
  }
  if (genre) {
    list = list.filter(m => (m.genres || []).includes(genre))
  }
  return new Promise(resolve => {
    setTimeout(() => resolve(paginate(list, page, pageSize)), 300)
  })
}

export function mockGetMovie(id) {
  const found = movies.find(m => m.id === id)
  return new Promise(resolve => {
    setTimeout(() => resolve(found || null), 200)
  })
}

export function mockStats() {
  const byGenre = {}
  movies.forEach(m => {
    (m.genres || []).forEach(g => {
      byGenre[g] = (byGenre[g] || 0) + 1
    })
  })
  return new Promise(resolve => {
    setTimeout(() => resolve({ total: movies.length, byGenre }), 120)
  })
}

export function mockCompareTimes() {
  // sample timing data for demo (ms)
  const byStorage = { mysql: 420, hive: 1200, neo4j: 90 }
  const samples = [
    { query: '按电影名称', mysql: 120, hive: 450, neo4j: 40 },
    { query: '按导演查询', mysql: 220, hive: 800, neo4j: 60 },
    { query: '按演员查询', mysql: 300, hive: 1100, neo4j: 90 }
  ]
  return new Promise(resolve => {
    setTimeout(() => resolve({ byStorage, samples }), 200)
  })
}

export function mockComplexQuery(filters = {}) {
  // Very simple mock: filter the movies array by provided filters (AND)
  // normalize genre filter: accept comma-separated string or array
  let genresFilter = []
  if (filters.genre) {
    if (Array.isArray(filters.genre)) genresFilter = filters.genre
    else genresFilter = (filters.genre + '').split(',').map(s => s.trim()).filter(Boolean)
  }

  const list = movies.filter(m => {
    if (filters.title && !(m.title || '').toLowerCase().includes((filters.title + '').toLowerCase())) return false
    if (filters.director && !(m.director || '').toLowerCase().includes((filters.director + '').toLowerCase())) return false
    if (filters.actor && !(m.actors || []).some(a => (a || '').toLowerCase().includes((filters.actor + '').toLowerCase()))) return false
    if (genresFilter.length) {
      const ok = (m.genres || []).some(g => genresFilter.includes(g))
      if (!ok) return false
    }
    if (filters.yearFrom && (m.year || 0) < filters.yearFrom) return false
    if (filters.yearTo && (m.year || 0) > filters.yearTo) return false
    if (filters.minRating && (m.rating || 0) < filters.minRating) return false
    return true
  })

  // mock timing: scale with number of items and arbitrary base per storage
  const base = { mysql: 50, hive: 200, neo4j: 30 }
  const scale = Math.max(1, Math.round(list.length / 2))
  const byStorage = {
    mysql: base.mysql * scale,
    hive: base.hive * scale,
    neo4j: base.neo4j * scale
  }

  // sample per-query timings
  const samples = [
    { query: '组合查询', mysql: byStorage.mysql, hive: byStorage.hive, neo4j: byStorage.neo4j }
  ]

  return new Promise(resolve => {
    setTimeout(() => resolve({ items: list, total: list.length, byStorage, samples }), 250)
  })
}

export function mockRelation(params = {}) {
  // sample nodes/edges for force graph
  const nodes = [
    { id: 'A', name: 'Actor A', category: 0, value: 10 },
    { id: 'B', name: 'Actor B', category: 0, value: 8 },
    { id: 'C', name: 'Actor C', category: 0, value: 6 },
    { id: 'D', name: 'Actor D', category: 0, value: 5 },
    { id: 'E', name: 'Actor E', category: 0, value: 4 }
  ]
  const links = [
    { source: 'A', target: 'B', weight: 12 },
    { source: 'A', target: 'C', weight: 8 },
    { source: 'B', target: 'D', weight: 6 },
    { source: 'C', target: 'D', weight: 4 },
    { source: 'D', target: 'E', weight: 2 }
  ]
  const combos = [
    { a: 'A', b: 'B', count: 129 },
    { a: 'A', b: 'C', count: 128 },
    { a: 'B', b: 'D', count: 128 },
    { a: 'C', b: 'D', count: 109 }
  ]
  const byStorage = { mysql: 1.2, hive: 16.3, neo4j: 2.1 }
  return new Promise(resolve => {
    setTimeout(() => resolve({ nodes, links, combos, byStorage }), 200)
  })
}


