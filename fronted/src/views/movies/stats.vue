<template>
  <el-card>
    <div style="display:flex; justify-content:space-between; align-items:center;">
      <div>
        <h3 style="margin:0">电影统计</h3>
        <div class="muted">总数与按类别分布（演示）</div>
      </div>
      <el-button @click="refresh">刷新</el-button>
    </div>

    <div style="margin-top:18px;">
      <el-skeleton :loading="isLoading" animated>
        <div v-if="!isLoading">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card>
                <div style="font-size:20px; font-weight:700">{{ stats.total }}</div>
                <div class="muted">电影总数</div>
              </el-card>
            </el-col>
            <el-col :span="18">
              <el-card>
                <div ref="chartRef" style="width:100%;height:320px;"></div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-skeleton>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { movieStats } from '@/api/movie'

const stats = ref({ total: 0, byGenre: {} })
const isLoading = ref(false)
const chartRef = ref(null)
let chartInstance = null

async function load() {
  isLoading.value = true
  try {
    const res = await movieStats()
    stats.value = res || { total: 0, byGenre: {} }
  } catch (err) {
    console.error(err)
  } finally {
    isLoading.value = false
  }
}

function refresh() {
  load()
}

onMounted(load)
// initialize chart when stats change
watch(stats, async () => {
  await nextTick()
  const el = chartRef.value
  if (!el) return
  if (!chartInstance) {
    try {
      const mod = await import(/* @vite-ignore */ 'echarts')
      const echarts = mod && (mod.default || mod)
      if (!echarts || !echarts.init) {
        console.warn('echarts module not available')
        return
      }
      chartInstance = echarts.init(el)
    } catch (e) {
      console.warn('echarts import failed', e)
      return
    }
  }
  const genres = Object.keys(stats.value.byGenre || {})
  const counts = genres.map(g => stats.value.byGenre[g])
  const option = {
    tooltip: {},
    xAxis: { type: 'category', data: genres },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: counts, itemStyle: { color: '#2d8cf0' } }]
  }
  chartInstance.setOption(option)
}, { deep: true })
</script>

<style scoped>
.muted { color:#7b8794; }
</style>


