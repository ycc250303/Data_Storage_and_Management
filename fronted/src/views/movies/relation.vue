<template>
  <el-card>
    <div class="controls" style="display:flex; gap:16px; align-items:center; flex-wrap:wrap;">
      <div style="display:flex; gap:8px; align-items:center;">
        <label style="width:90px; text-align:right; margin-right:8px;">关系来源</label>
        <el-select v-model="source" placeholder="选择来源" size="small" style="width:260px">
          <el-option label="演员" value="actor" />
          <el-option label="导演" value="director" />
        </el-select>
      </div>

      <div style="display:flex; gap:8px; align-items:center;">
        <label style="width:90px; text-align:right; margin-right:8px;">合作对象</label>
        <el-input v-model="partner" placeholder="演员/导演姓名" style="width:260px" clearable />
      </div>

      <div>
        <el-button type="primary" @click="run">查询</el-button>
      </div>

      <div style="width:100%"></div>

      <div style="margin-top:12px; width:100%;">
        <div style="font-weight:600; margin-bottom:8px;">最受关注(评论最多)的演员组合</div>
        <div style="display:flex; gap:8px; align-items:center;">
          <label style="width:90px; text-align:right; margin-right:8px;">电影类型</label>
          <el-input v-model="movieType" placeholder="请输入电影类型" style="width:260px" clearable />
          <el-button @click="runType" size="small">查询</el-button>
        </div>
      </div>
    </div>

    <div style="margin-top:12px;">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="查询结果" name="results">
          <div v-if="graph.nodes && graph.nodes.length">
            <div style="display:flex; gap:16px;">
              <div style="flex:1">
                <div ref="graphRef" style="width:100%;min-height:360px;"></div>
              </div>
              <div style="width:320px">
                <el-card>
                  <div><strong>合作组合（Top）</strong></div>
                  <el-table :data="combos" stripe style="margin-top:8px;">
                    <el-table-column prop="a" label="合作者1" />
                    <el-table-column prop="b" label="合作者2" />
                    <el-table-column prop="count" label="合作次数" width="100" />
                  </el-table>
                </el-card>
              </div>
            </div>
          </div>
          <div v-else class="empty-wrap">
            <el-empty description="没有关系数据" />
          </div>
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
import { queryRelation, compareTiming } from '@/api/queries'

const q = ref('')
const source = ref('actor')
const partner = ref('')
const movieType = ref('')
const graph = ref({ nodes: [], edges: [] })
const activeTab = ref('results')

const compare = ref({ byStorage: {}, samples: [] })
const chartRef = ref(null)
const graphRef = ref(null)
const combos = ref([])
let chartInstance = null

async function run() {
  const params = { source: source.value, partner: partner.value, q: q.value }
  const res = await queryRelation(params)
  graph.value = res || { nodes: [], edges: [] }
  combos.value = res.combos || []
  const cmp = await compareTiming({ type: 'byRelation', ...params })
  compare.value = cmp || { byStorage: {}, samples: [] }
  activeTab.value = 'results'
}

async function runType() {
  const params = { source: source.value, movieType: movieType.value }
  const res = await queryRelation(params)
  graph.value = res || { nodes: [], edges: [] }
  combos.value = res.combos || []
  const cmp = await compareTiming({ type: 'byRelation', ...params })
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

// render force graph when graph data changes
watch(graph, async () => {
  await nextTick()
  const el = graphRef.value
  if (!el) return
  try {
    const mod = await import(/* @vite-ignore */ 'echarts')
    const echarts = mod && (mod.default || mod)
    if (!echarts || !echarts.init) return
    const instance = echarts.init(el)
    const categories = [{ name: 'actor' }]
    const nodes = (graph.value.nodes || []).map(n => ({ id: n.id || n.name, name: n.name || n.label, value: n.value || 1, category: 0 }))
    const links = (graph.value.links || []).map(l => ({ source: l.source, target: l.target, value: l.weight || 1 }))
    const option = {
      tooltip: { formatter: '{b}' },
      series: [{
        type: 'graph',
        layout: 'force',
        roam: true,
        data: nodes,
        links: links,
        categories,
        label: { position: 'right' },
        force: { repulsion: 200 }
      }]
    }
    instance.setOption(option)
  } catch (e) {
    console.warn('graph render failed', e)
  }
}, { deep: true })
</script>

<style scoped></style>


