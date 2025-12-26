<template>
  <section class="complex-page">
    <el-card>
      <h3>组合查询</h3>
      <el-form :model="filters" label-width="110px" class="complex-form">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="影片标题">
              <el-input v-model="filters.title" placeholder="影片标题或关键字" clearable/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="导演">
              <el-input v-model="filters.director" placeholder="导演姓名" clearable/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="演员">
              <el-input v-model="filters.actor" placeholder="演员姓名" clearable/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="电影类别">
              <el-input v-model="filters.genre" placeholder="类别（逗号分隔）" clearable/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="年份从">
              <el-input-number v-model="filters.yearFrom" :min="1900" :max="2100"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="年份到">
              <el-input-number v-model="filters.yearTo" :min="1900" :max="2100"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="最低评分">
              <el-input-number v-model="filters.minRating" :min="0" :max="10"/>
            </el-form-item>
          </el-col>
          <el-col :span="6" class="align-end">
            <el-form-item>
              <el-button type="primary" @click="run">查询</el-button>
              <el-button @click="reset">清空</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div style="margin-top:18px;">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="查询结果" name="results">
            <el-table :data="items" stripe style="width:100%">
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="year" label="年份" width="120" />
              <el-table-column prop="genres" label="类别" />
              <el-table-column prop="rating" label="评分" width="100" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="性能对比" name="compare">
            <div style="display:flex; gap:16px;">
              <div style="flex:1">
                <div ref="chartRef" style="width:100%;min-height:360px;"></div>
              </div>
              <div style="width:320px">
                <el-card>
                  <div><strong>总体耗时（ms）</strong></div>
                  <div class="muted" style="margin-top:8px">
                    MySQL: {{ compare.byStorage?.mysql ?? '-' }}<br/>
                    Hive: {{ compare.byStorage?.hive ?? '-' }}<br/>
                    Neo4j: {{ compare.byStorage?.neo4j ?? '-' }}
                  </div>
                  <div style="margin-top:12px;">
                    <el-table :data="compare.samples" stripe>
                      <el-table-column prop="query" label="查询样例" />
                      <el-table-column prop="mysql" label="MySQL" width="100" />
                      <el-table-column prop="hive" label="Hive" width="100" />
                      <el-table-column prop="neo4j" label="Neo4j" width="100" />
                    </el-table>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { complexQuery } from '@/api/queries'

const filters = ref({
  title: '', director: '', actor: '', yearFrom: null, yearTo: null, minRating: null
})
const items = ref([])
const activeTab = ref('results')

const compare = ref({ byStorage: {}, samples: [] })
const chartRef = ref(null)
let chartInstance = null

async function run() {
  const res = await complexQuery(filters.value)
  items.value = res.items || []
  compare.value = { byStorage: res.byStorage || {}, samples: res.samples || [] }
  activeTab.value = 'results'
}

function reset() {
  filters.value = { title: '', director: '', actor: '', yearFrom: null, yearTo: null, minRating: null }
  items.value = []
  compare.value = { byStorage: {}, samples: [] }
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

<style scoped>
.complex-form .el-col .el-form-item { margin-bottom:10px; }
.align-end { display:flex; align-items:flex-end; }
.muted { color:#7b8794; }
</style>


