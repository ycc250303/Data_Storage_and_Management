<template>
  <el-card>
    <div class="controls">
      <el-input v-model="name" placeholder="导演姓名" clearable style="flex:1" />
      <el-button type="primary" @click="run">查询</el-button>
    </div>

    <div style="margin-top:12px;">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="查询结果" name="results">
          <el-table :data="items" style="width:100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="year" label="年份" width="100" />
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
import { queryByDirector, compareTiming } from '@/api/queries'

const name = ref('')
const items = ref([])
const activeTab = ref('results')

const compare = ref({ byStorage: {}, samples: [] })
const chartRef = ref(null)
let chartInstance = null

async function run() {
  // fetch results
  const res = await queryByDirector({ name: name.value })
  items.value = res.items || []
  // fetch compare timings (fallback to mock)
  const cmp = await compareTiming({ type: 'byDirector', q: name.value })
  compare.value = cmp || { byStorage: {}, samples: [] }
  // switch to results tab by default (user can switch)
  activeTab.value = 'results'
}

// render chart when compare.byStorage updated
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

<style scoped>.controls { display:flex; gap:8px; align-items:center; }</style>


