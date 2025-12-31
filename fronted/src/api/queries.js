import http from "@/utils/http";
import axios from "axios";
import { mockListMovies } from "@/mock/mockService";

// Hive 专用的 axios 实例，端口为 8081
const hiveHttp = axios.create({
  baseURL: "http://localhost:8081",
  timeout: 120000, // Hive 查询可能较慢，增加超时时间
});

// Neo4j 专用的 axios 实例，端口为 3000
const neo4jHttp = axios.create({
  baseURL: "http://106.15.53.23:3000",
  timeout: 20000,
});

// 响应拦截器：打印响应日志并直接返回数据
hiveHttp.interceptors.response.use(
  (response) => {
    console.log(`[Hive Response] ${response.config.method.toUpperCase()} ${response.config.url}`, response.data);
    return response.data;
  },
  (error) => {
    console.error("[Hive Response Error]", error);
    return Promise.reject(error);
  }
);

neo4jHttp.interceptors.response.use(
  (response) => {
    console.log(`[Neo4j Response] ${response.config.method.toUpperCase()} ${response.config.url}`, response.data);
    return response.data;
  },
  (error) => {
    console.error("[Neo4j Response Error]", error);
    return Promise.reject(error);
  }
);

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
    // fallback: return empty list when mock is gone
    return { items: [], total: 0 }
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

export async function getActorCollaborations(params) {
  try {
    const res = await neo4jHttp.get('/api/neo4j/stats/collaborations', { params });
    return res;
  } catch (err) {
    console.error("getActorCollaborations failed", err);
    return [];
  }
}

export async function getDirectorActorCollaborations(params) {
  try {
    const res = await neo4jHttp.get('/api/neo4j/stats/director-actor', { params });
    return res;
  } catch (err) {
    console.error("getDirectorActorCollaborations failed", err);
    return [];
  }
}

export async function getActorAttention(params) {
  try {
    const res = await neo4jHttp.get('/api/neo4j/stats/collaborations_reviews', { params });
    return res;
  } catch (err) {
    console.error("getActorAttention failed", err);
    return [];
  }
}

export async function getDirectorActorAttention(params) {
  try {
    const res = await neo4jHttp.get('/api/neo4j/stats/director-actor_reviews', { params });
    return res;
  } catch (err) {
    console.error("getDirectorActorAttention failed", err);
    return [];
  }
}

export async function getActorAttentionByGenre(params) {
  try {
    const res = await neo4jHttp.get('/api/neo4j/stats/collaborations_by_genre', { params });
    return res;
  } catch (err) {
    console.error("getActorAttentionByGenre failed", err);
    return [];
  }
}

export async function getDirectorActorAttentionByGenre(params) {
  try {
    const res = await neo4jHttp.get('/api/neo4j/stats/director-actor_by_genre', { params });
    return res;
  } catch (err) {
    console.error("getDirectorActorAttentionByGenre failed", err);
    return [];
  }
}

export async function getHiveActorCollaborations(params) {
  try {
    const res = await hiveHttp.get('/api/hive/actor/collaborations', { params });
    return res;
  } catch (err) {
    console.error("getHiveActorCollaborations failed", err);
    return [];
  }
}

export async function getHiveDirectorActorCollaborations(params) {
  try {
    const res = await hiveHttp.get('/api/hive/actor/director-collaborations', { params });
    return res;
  } catch (err) {
    console.error("getHiveDirectorActorCollaborations failed", err);
    return [];
  }
}

export async function getHiveActorAttention(params) {
  try {
    const res = await hiveHttp.get('/api/hive/actor/collaborations-reviews', { params });
    return res;
  } catch (err) {
    console.error("getHiveActorAttention failed", err);
    return [];
  }
}

export async function getHiveDirectorActorAttention(params) {
  try {
    const res = await hiveHttp.get('/api/hive/actor/director-collaborations-reviews', { params });
    return res;
  } catch (err) {
    console.error("getHiveDirectorActorAttention failed", err);
    return [];
  }
}

// --- MySQL Stats API (Multi-table and Wide-table) ---
export async function getMysqlActorCollaborations(params, fast = false) {
  const url = fast ? '/api/mysql/stats/actor-collaboration/fast' : '/api/mysql/stats/actor-collaboration';
  return http.get(url, { params }).catch(err => { console.error("getMysqlActorCollaborations failed", err); return []; });
}

export async function getMysqlDirectorActorCollaborations(params, fast = false) {
  const url = fast ? '/api/mysql/stats/director-actor/fast' : '/api/mysql/stats/director-actor';
  return http.get(url, { params }).catch(err => { console.error("getMysqlDirectorActorCollaborations failed", err); return []; });
}

export async function getMysqlActorAttention(params, fast = false) {
  const url = fast ? '/api/mysql/stats/actor-collaboration-reviews/fast' : '/api/mysql/stats/actor-collaboration-reviews';
  return http.get(url, { params }).catch(err => { console.error("getMysqlActorAttention failed", err); return []; });
}

export async function getMysqlActorAttentionByGenre(params, fast = false) {
  const url = fast ? '/api/mysql/stats/actor-collaboration-by-genre/fast' : '/api/mysql/stats/actor-collaboration-by-genre';
  return http.get(url, { params }).catch(err => { console.error("getMysqlActorAttentionByGenre failed", err); return []; });
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
  const mode = filters.mode || "multi";
  const selectedDBs = filters.databases || ["mysql", "hive"];
  const searchDto = {
    movieTitle: filters.title || "",
    actorName: filters.actor || "",
    directorName: filters.director || "",
    movieGenre: filters.genre || "",
    startYear: filters.yearFrom || 0,
    endYear: filters.yearTo || 0,
    minScore: filters.minRating || 0,
    maxScore: filters.maxRating || 5,
    size: 20,
    page: -1,
  };

  if (filters.releaseDate) {
    const parts = filters.releaseDate.split("-");
    if (parts.length >= 2) {
      searchDto.month = parseInt(parts[1], 10);
    }
    if (parts.length >= 3) {
      searchDto.day = parseInt(parts[2], 10);
    }
  }

  console.log(`[API] complexQuery (${mode}) starting selected queries:`, selectedDBs);

  const promises = [];
  const mysqlIndex = 0;
  const hiveIndex = selectedDBs.includes("mysql") ? 1 : 0;

  if (selectedDBs.includes("mysql")) {
    const mysqlEndpoint = mode === "wide" ? "/api/mysql/movie/search/fast" : "/api/mysql/movie/search";
    promises.push(
      http.post(mysqlEndpoint, searchDto).catch((err) => {
        console.error("[MySQL Query Error]", err);
        return { data: [], totalExecutionTime: 0 };
      })
    );
  } else {
    promises.push(Promise.resolve(null));
  }

  if (selectedDBs.includes("hive")) {
    const hiveEndpoint = mode === "wide" ? "/api/hive/movie/search/fast" : "/api/hive/movie/search";
    promises.push(
      hiveHttp.post(hiveEndpoint, searchDto).catch((err) => {
        console.error("[Hive Query Error]", err);
        return { data: [], totalExecutionTime: 0 };
      })
    );
  } else {
    promises.push(Promise.resolve(null));
  }

  try {
    const [mysqlRes, hiveRes] = await Promise.all(promises);

    // 数据显示优先级：MySQL (如果选了) > Hive
    let finalRes = mysqlRes || hiveRes;
    const items = Array.isArray(finalRes) ? finalRes : finalRes?.data || [];

    const byStorage = {};
    const sample = { query: mode === "wide" ? "组合查询 (宽表)" : "组合查询 (多表)" };

    if (mysqlRes) {
      const mysqlTime = mysqlRes.totalExecutionTime || 0;
      byStorage.mysql = mysqlTime;
      sample.mysql = mysqlTime;
    }
    if (hiveRes) {
      const hiveTime = hiveRes.totalExecutionTime || 0;
      byStorage.hive = hiveTime;
      sample.hive = hiveTime;
    }

    return {
      items: items,
      byStorage,
      samples: [sample],
    };
  } catch (err) {
    console.error("[API] complexQuery failed", err);
    const mod = await import("@/mock/mockService");
    return mod.mockComplexQuery(filters);
  }
}


