import http from "@/utils/http";
import axios from "axios";
import { mockListMovies, mockStats } from "@/mock/mockService";

// Hive 专用的 axios 实例，端口为 8081
const hiveHttp = axios.create({
  baseURL: "http://localhost:8081",
  timeout: 60000, // Hive 查询可能较慢，增加超时时间
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
  const mode = filters.mode || "multi";
  const searchDto = {
    movieTitle: filters.title || "",
    actorName: filters.actor || "",
    directorName: filters.director || "",
    movieGenre: filters.genre || "",
    startYear: filters.yearFrom || 0,
    endYear: filters.yearTo || 0,
    minScore: filters.minRating || 0,
    maxScore: filters.maxRating || 5,
    size: filters.limit || 20,
    page: -1,
  };

  if (filters.releaseDate) {
    const d = new Date(filters.releaseDate);
    searchDto.month = d.getMonth() + 1;
    searchDto.day = d.getDate();
  }

  console.log(`[API] complexQuery (${mode}) starting both MySQL and Hive queries...`);

  // 同时发起 MySQL 和 Hive 查询
  const mysqlEndpoint = mode === "wide" ? "/api/mysql/movie/search/fast" : "/api/mysql/movie/search";
  const hiveEndpoint = mode === "wide" ? "/api/hive/movie/search/fast" : "/api/hive/movie/search";

  const mysqlPromise = http.post(mysqlEndpoint, searchDto).catch((err) => {
    console.error("[MySQL Query Error]", err);
    return { data: [], totalExecutionTime: 0 };
  });

  const hivePromise = hiveHttp.post(hiveEndpoint, searchDto).catch((err) => {
    console.error("[Hive Query Error]", err);
    return { data: [], totalExecutionTime: 0 };
  });

  try {
    // 等待两个查询都完成
    const [mysqlRes, hiveRes] = await Promise.all([mysqlPromise, hivePromise]);

    const items = Array.isArray(mysqlRes) ? mysqlRes : mysqlRes?.data || [];
    const mysqlTime = mysqlRes?.totalExecutionTime || 0;
    const hiveTime = hiveRes?.totalExecutionTime || 0;

    console.log(`[API] Queries finished. MySQL: ${mysqlTime}ms, Hive: ${hiveTime}ms`);

    return {
      items: items, // 使用 MySQL 返回的结果展示表格
      byStorage: {
        mysql: mysqlTime,
        hive: hiveTime,
      },
      samples: [
        {
          query: mode === "wide" ? "组合查询 (宽表)" : "组合查询 (多表)",
          mysql: mysqlTime,
          hive: hiveTime,
        },
      ],
    };
  } catch (err) {
    console.error("[API] complexQuery failed", err);
    const mod = await import("@/mock/mockService");
    return mod.mockComplexQuery(filters);
  }
}


