<template>
  <el-card>
    <div style="display:flex; justify-content:space-between; align-items:center;">
      <div>
        <el-input v-model="q" placeholder="按类别名筛选" clearable />
      </div>
      <el-button @click="run">查询</el-button>
    </div>

    <div style="margin-top:12px;">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="查询结果" name="results">
          <el-table :data="items" style="width:100%">
            <el-table-column prop="genre" label="类别" />
            <el-table-column prop="count" label="数量" width="120" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="性能对比" name="compare">
          <div style="display:flex; gap:16px;">
            <div style="flex:1">
              <div ref="chartRef" style="width:100%;min-height:300px;"></div>
            </div>
            <div style="width:260px">
              <el-card>
                <div><strong>耗时（ms）</strong></div>
                <div class="muted" style="margin-top:8px">
                  MySQL: {{ compare.byStorage?.mysql ?? '-' }}<br/>
                  Hive: {{ compare.byStorage?.hive ?? '-' }}<br/>
                  Neo4j: {{ compare.byStorage?.neo4j ?? '-' }}
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-card>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { queryByGenre, compareTiming } from '@/api/queries'

const q = ref('')
const items = ref([])
const activeTab = ref('results')

const compare = ref({ byStorage: {}, samples: [] })
const chartRef = ref(null)
let chartInstance = null

async function run() {
  const res = await queryByGenre({ q: q.value })
  items.value = res.items || []
  const cmp = await compareTiming({ type: 'byGenre', q: q.value })
  compare.value = cmp || { byStorage: {}, samples: [] }
  activeTab.value = 'results'
}

watch(compare, async () => {
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
      console.warn('echarts import failed', e)
      return
    }
  }
  const categories = Object.keys(compare.value.byStorage || {})
  const values = categories.map(k => compare.value.byStorage[k] || 0)
  const option = {
    tooltip: {},
    xAxis: { type: 'category', data: categories },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: values, itemStyle: { color: '#2d8cf0' } }]
  }
  chartInstance.setOption(option)
}, { deep: true })
</script>

<style scoped>.muted { color:#7b8794; }</style>


