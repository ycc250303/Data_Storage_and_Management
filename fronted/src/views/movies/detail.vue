<template>
  <el-card class="detail-card">
    <div style="display:flex; gap:18px;">
      <div style="width:260px;">
        <el-image
          v-if="movie && movie.poster"
          :src="movie.poster"
          fit="cover"
          style="width:100%; height:360px; border-radius:6px;"
        />
        <div v-else style="width:100%; height:360px; background:#eee; border-radius:6px;"></div>
      </div>
      <div style="flex:1;">
        <div v-if="movie">
          <h2 style="margin:0 0 8px 0;">{{ movie.title }} <small class="muted">（{{ movie.year }}）</small></h2>
          <div class="muted" style="margin-bottom:12px;">{{ movie.genres }} • 评分：{{ movie.rating ?? '-' }}</div>
          <p style="line-height:1.8;">{{ movie.summary || movie.description || '暂无简介' }}</p>
          <div style="margin-top:18px;">
            <el-button type="primary" @click="goBack">返回</el-button>
            <el-button plain @click="goToSource">查看来源</el-button>
          </div>
        </div>
        <div v-else>
          <el-skeleton rows="6" animated />
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMovie } from '@/api/movie'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const movie = ref(null)

async function load() {
  try {
    const id = route.params.id
    const res = await getMovie(id)
    movie.value = res || res.data || null
  } catch (err) {
    console.error('获取详情失败', err)
  }
}

function goToSource() {
  if (movie.value && movie.value.sourceUrl) {
    window.open(movie.value.sourceUrl, '_blank')
  }
}

function goBack() {
  router.back()
}

onMounted(load)
</script>

<style scoped>
.muted { color:#7b8794; }
.detail-card { padding:18px; }
.detail-card h2 { font-size:22px; margin:0 0 6px 0; }
.detail-card p { color:#2b3942; }
</style>
</style>


