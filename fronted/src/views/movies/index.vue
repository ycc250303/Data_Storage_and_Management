<template>
  <section class="movies-page">
    <el-card>
      <div class="controls">
        <div class="search-row" style="flex:1">
          <el-input v-model="filters.title" placeholder="影片名或关键字" clearable />
          <el-select v-model="filters.genre" placeholder="类别" clearable>
            <el-option label="全部" :value="''" />
            <el-option label="Action" value="Action" />
            <el-option label="Adventure" value="Adventure" />
            <el-option label="Drama" value="Drama" />
          </el-select>
          <el-button type="primary" @click="loadMovies">查询</el-button>
        </div>
        <div style="display:flex; gap:8px; align-items:center">
          <el-button @click="toggleView" size="small">{{ compactView ? '表格视图' : '卡片视图' }}</el-button>
          <el-button type="text" @click="refresh" size="small">刷新</el-button>
          <el-button type="primary" plain @click="exportCSV" size="small">导出 CSV</el-button>
        </div>
      </div>

      <div style="margin-top:14px;">
        <el-skeleton v-if="isLoading" rows="4" animated />

        <div v-else>
          <div v-if="movieList.length === 0" class="empty-wrap">
            <el-empty description="没有找到任何电影" />
          </div>

          <div v-else>
            <div v-if="!compactView" class="grid-row">
              <div v-for="m in movieList" :key="m.id" class="movie-card">
                <el-card shadow="hover" class="movie-card-card">
                  <button class="fav-btn" @click.stop="toggleFavorite(m)">{{ isFavorited(m.id) ? '★' : '☆' }}</button>
                  <template v-if="m.poster">
                    <el-image
                      :src="m.poster"
                      class="movie-thumb"
                      fit="cover"
                      alt="poster"
                    />
                  </template>
                  <template v-else>
                    <div class="movie-thumb" />
                  </template>
                  <div style="padding:8px 0">
                    <div style="font-weight:700">{{ m.title }}</div>
                    <div class="muted">{{ m.year }} • {{ (m.genres || '').toString() }}</div>
                  </div>
                  <div style="display:flex; justify-content:space-between; align-items:center;">
                    <div class="muted">评分: {{ m.rating ?? '-' }}</div>
                    <el-button type="text" @click="openQuick(m)">查看</el-button>
                  </div>
                </el-card>
              </div>
            </div>

            <div v-else>
              <el-table :data="movieList" stripe style="width:100%">
                <el-table-column prop="title" label="标题" />
                <el-table-column prop="year" label="年份" width="100" />
                <el-table-column prop="genres" label="类别" />
                <el-table-column prop="rating" label="评分" width="100" />
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button type="text" @click="openQuick(row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </div>

      <div class="pager-row" style="text-align:right; margin-top:12px;">
        <el-pagination
          :current-page="pager.page"
          :page-size="pager.pageSize"
          :total="pager.total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <el-dialog :visible.sync="quickVisible" width="680px" :before-close="() => (quickVisible = false)">
      <template #title>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <div>{{ quickMovie?.title || '电影详情' }}</div>
          <div class="muted">{{ quickMovie?.year }}</div>
        </div>
      </template>
      <div style="display:flex; gap:16px;">
        <div style="width:220px;">
          <el-image v-if="quickMovie?.poster" :src="quickMovie.poster" fit="cover" style="width:100%; height:320px; border-radius:6px;" />
          <div v-else style="width:100%; height:320px; background:#eee; border-radius:6px;"></div>
        </div>
        <div style="flex:1;">
          <p><strong>类别：</strong>{{ (quickMovie?.genres || []).join(', ') }}</p>
          <p><strong>评分：</strong>{{ quickMovie?.rating ?? '-' }}</p>
          <p style="line-height:1.6;">{{ quickMovie?.summary || quickMovie?.description || '暂无简介' }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="quickVisible = false">关闭</el-button>
        <el-button type="primary" @click="openDetailFromQuick">打开详情页</el-button>
      </template>
    </el-dialog>

  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listMovies } from '@/api/movie'
import { useRouter } from 'vue-router'

const router = useRouter()

const movieList = ref([])
const filters = ref({ title: '', genre: '' })
const pager = ref({ page: 1, pageSize: 20, total: 0 })
const isLoading = ref(false)
const compactView = ref(false)
const quickVisible = ref(false)
const quickMovie = ref(null)
const favorites = ref(new Set())

async function loadMovies() {
  isLoading.value = true
  const params = { page: pager.value.page, pageSize: pager.value.pageSize, ...filters.value }
  try {
    const res = await listMovies(params)
    movieList.value = res.items || res.data || res || []
    pager.value.total = res.total || (res.meta && res.meta.total) || (Array.isArray(res) ? res.length : movieList.value.length)
  } catch (err) {
    console.error('加载电影失败', err)
  } finally {
    isLoading.value = false
  }
}

function onPageChange(page) {
  pager.value.page = page
  loadMovies()
}

function viewDetail(id) {
  router.push({ name: 'MovieDetail', params: { id } })
}

function openQuick(movie) {
  quickMovie.value = movie || null
  quickVisible.value = true
}

function openDetailFromQuick() {
  if (quickMovie.value && quickMovie.value.id) {
    router.push({ name: 'MovieDetail', params: { id: quickMovie.value.id } })
    quickVisible.value = false
  }
}

function toggleView() {
  compactView.value = !compactView.value
}

function refresh() {
  loadMovies()
}

function exportCSV() {
  if (!movieList.value || movieList.value.length === 0) return
  const rows = movieList.value.map(m => ({
    id: m.id || '',
    title: (m.title || '').replace(/"/g, '""'),
    year: m.year || '',
    genres: (m.genres || []).join('|'),
    rating: m.rating || '',
    summary: (m.summary || m.description || '').replace(/"/g, '""')
  }))
  const header = ['id','title','year','genres','rating','summary']
  const csv = [header.join(',')].concat(rows.map(r => header.map(h => `"${r[h] || ''}"`).join(','))).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'movies.csv'
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(loadMovies)
// favorites handling
onMounted(() => {
  try {
    const raw = localStorage.getItem('favorites')
    const arr = raw ? JSON.parse(raw) : []
    favorites.value = new Set(arr || [])
  } catch (e) {
    favorites.value = new Set()
  }
})

function persistFavorites() {
  try {
    localStorage.setItem('favorites', JSON.stringify(Array.from(favorites.value)))
  } catch (e) {}
}

function toggleFavorite(movie) {
  if (!movie || !movie.id) return
  if (favorites.value.has(movie.id)) favorites.value.delete(movie.id)
  else favorites.value.add(movie.id)
  persistFavorites()
}

function isFavorited(id) {
  return favorites.value.has(id)
}
</script>

<style scoped>
.search-row { display:flex; gap:8px; align-items:center; }
.pager-row { display:flex; justify-content:flex-end; }
</style>


