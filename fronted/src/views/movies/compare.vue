<template>
  <section class="compare-page">
    <el-card>
      <div class="compare-layout">
        <div class="controls-col">
          <h3>组合查询耗时对比</h3>
          <el-form :model="filters" label-width="90px">
            <el-form-item label="查询类型">
              <el-select v-model="filters.type" placeholder="选择查询类型">
                <el-option label="按电影名称" value="byTitle" />
                <el-option label="按导演" value="byDirector" />
                <el-option label="按演员" value="byActor" />
              </el-select>
            </el-form-item>
            <el-form-item label="样本大小">
              <el-input-number v-model="filters.samples" :min="1" :max="1000" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="run">查询</el-button>
              <el-button @click="reset">重置</el-button>
            </el-form-item>
          </el-form>

          <div style="margin-top:24px;">
            <el-table :data="samples" stripe style="width:100%">
              <el-table-column prop="query" label="查询" />
              <el-table-column prop="mysql" label="MySQL(ms)" width="120" />
              <el-table-column prop="hive" label="Hive(ms)" width="120" />
              <el-table-column prop="neo4j" label="Neo4j(ms)" width="120" />
            </el-table>
          </div>
        </div>

        <div class="chart-col">
          <div ref="chartRef" style="width:100%;min-height:360px;"></div>
          <div style="margin-top:12px;">
            <el-card>
              <div style="display:flex; gap:12px; align-items:center;">
                <div><strong>总体耗时（ms）</strong></div>
                <div class="muted">MySQL: {{ byStorage.mysql }} &nbsp; Hive: {{ byStorage.hive }} &nbsp; Neo4j: {{ byStorage.neo4j }}</div>
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { compareTiming } from '@/api/queries'

const filters = ref({ type: 'byTitle', samples: 10 })
const byStorage = ref({ mysql: 0, hive: 0, neo4j: 0 })
const samples = ref([])
const chartRef = ref(null)
let chartInstance = null

async function load() {
  const res = await compareTiming(filters.value)
  byStorage.value = res.byStorage || { mysql: 0, hive: 0, neo4j: 0 }
  samples.value = res.samples || []
}

function reset() {
  filters.value = { type: 'byTitle', samples: 10 }
  load()
}

async function run() {
  await load()
}

watch([byStorage, samples], async () => {
  await nextTick()
  const el = chartRef.value
  if (!el) return
  if (!chartInstance) {
    try {
      const mod = await import(/* @vite-ignore */ 'echarts')
      const echarts = mod && (mod.default || mod)
      if (!echarts || !echarts.init) return
      chartInstance = echarts.init(el)
    } catch (e) {
      console.warn('echarts not available', e)
      return
    }
  }
  const categories = Object.keys(byStorage.value)
  const values = categories.map(k => byStorage.value[k])
  const option = {
    tooltip: {},
    xAxis: { type: 'category', data: categories },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: values, itemStyle: { color: '#2d8cf0' } }]
  }
  chartInstance.setOption(option)
}, { deep: true })

onMounted(load)
</script>

<style scoped>
.compare-layout { display:flex; gap:24px; }
.controls-col { flex:1; min-width:360px; }
.chart-col { flex:1; padding-left:18px; border-left:1px solid #eee; }
.muted { color:#7b8794; }
</style>


