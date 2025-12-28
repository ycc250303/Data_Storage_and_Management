<template>
  <section class="complex-page">
    <el-card>
      <h3>组合查询</h3>
      <el-form :model="filters" label-width="110px" class="complex-form">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="影片标题">
              <el-input
                v-model="filters.title"
                placeholder="影片标题或关键字"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="导演">
              <el-input
                v-model="filters.director"
                placeholder="导演姓名"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="演员">
              <el-input
                v-model="filters.actor"
                placeholder="演员姓名"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="查询模式">
              <el-radio-group v-model="filters.mode">
                <el-radio label="multi">多表查询</el-radio>
                <el-radio label="wide">宽表查询</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="选择数据库">
              <el-checkbox-group v-model="filters.databases">
                <el-checkbox label="mysql">MySQL</el-checkbox>
                <el-checkbox label="hive">Hive</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="电影类别">
              <el-input v-model="filters.genre" placeholder="类别" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="发行日期">
              <div style="display: flex; gap: 8px; width: 100%">
                <el-select
                  v-model="filters.dateGranularity"
                  style="width: 100px"
                  @change="filters.releaseDate = null"
                >
                  <el-option label="按日" value="date" />
                  <el-option label="按月" value="month" />
                </el-select>
                <el-date-picker
                  v-model="filters.releaseDate"
                  :type="filters.dateGranularity"
                  :placeholder="
                    filters.dateGranularity === 'date' ? '选择日期' : '选择月份'
                  "
                  :format="
                    filters.dateGranularity === 'date'
                      ? 'YYYY-MM-DD'
                      : 'YYYY-MM'
                  "
                  :value-format="
                    filters.dateGranularity === 'date'
                      ? 'YYYY-MM-DD'
                      : 'YYYY-MM'
                  "
                  clearable
                  style="flex: 1"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="4">
            <el-form-item label="年份从">
              <el-input-number
                v-model="filters.yearFrom"
                :min="1900"
                :max="2100"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="年份到">
              <el-input-number
                v-model="filters.yearTo"
                :min="1900"
                :max="2100"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="最低评分">
              <el-input-number
                v-model="filters.minRating"
                :min="0"
                :max="5"
                :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="最高评分">
              <el-input-number
                v-model="filters.maxRating"
                :min="0"
                :max="5"
                :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" class="align-end">
            <el-form-item label-width="0px" class="button-group">
              <el-button type="primary" :loading="loading" @click="run"
                >查询</el-button
              >
              <el-button @click="reset">清空</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div style="margin-top: 18px">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="查询结果" name="results">
            <el-table
              v-loading="loading"
              :data="displayedItems"
              stripe
              style="width: 100%"
            >
              <el-table-column
                prop="movieAsin"
                label="电影ASIN"
                min-width="12"
                show-overflow-tooltip
              />
              <el-table-column
                prop="movieTitle"
                label="电影标题"
                min-width="20"
                show-overflow-tooltip
              />
              <el-table-column prop="movieScore" label="评分" min-width="8" />
              <el-table-column
                prop="actors"
                label="演员"
                min-width="15"
                show-overflow-tooltip
              />
              <el-table-column
                prop="directors"
                label="导演"
                min-width="12"
                show-overflow-tooltip
              />
              <el-table-column
                prop="movieGenre"
                label="电影类型"
                min-width="10"
                show-overflow-tooltip
              />
              <el-table-column prop="date" label="日期" min-width="10" />
              <el-table-column
                prop="edition"
                label="版本"
                min-width="13"
                show-overflow-tooltip
              />
            </el-table>
            <div
              style="margin-top: 16px; display: flex; justify-content: flex-end"
            >
              <el-pagination
                :current-page="currentPage"
                @update:current-page="(val) => (currentPage = val)"
                :page-size="pageSize"
                @update:page-size="(val) => (pageSize = val)"
                :page-sizes="[20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="items.length"
              />
            </div>
          </el-tab-pane>
          <el-tab-pane label="性能对比" name="compare">
            <div v-loading="loading" style="display: flex; gap: 16px">
              <div style="flex: 1">
                <div
                  ref="chartRef"
                  style="width: 100%; min-height: 360px"
                ></div>
              </div>
              <div style="width: 320px">
                <el-card>
                  <div><strong>总体耗时</strong></div>
                  <div class="muted" style="margin-top: 8px">
                    <span v-if="filters.databases.includes('mysql')">
                      MySQL: {{ formatTime(compare.byStorage?.mysql) }}<br />
                    </span>
                    <span v-if="filters.databases.includes('hive')">
                      Hive: {{ formatTime(compare.byStorage?.hive) }}
                    </span>
                  </div>
                  <div style="margin-top: 12px">
                    <el-table :data="compare.samples" stripe>
                      <el-table-column prop="query" label="查询样例" />
                      <el-table-column
                        v-if="filters.databases.includes('mysql')"
                        label="MySQL"
                        width="100"
                      >
                        <template #default="scope">
                          {{ formatTime(scope.row.mysql) }}
                        </template>
                      </el-table-column>
                      <el-table-column
                        v-if="filters.databases.includes('hive')"
                        label="Hive"
                        width="100"
                      >
                        <template #default="scope">
                          {{ formatTime(scope.row.hive) }}
                        </template>
                      </el-table-column>
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
import { ref, nextTick, watch, computed } from "vue";
import { complexQuery } from "@/api/queries";

const filters = ref({
  mode: "multi",
  databases: ["mysql", "hive"], // 默认全选
  title: "",
  director: "",
  actor: "",
  genre: "",
  yearFrom: null,
  yearTo: null,
  minRating: null,
  maxRating: null,
  releaseDate: null,
  dateGranularity: "date",
});
const items = ref([]);
const loading = ref(false);
const activeTab = ref("results");

const currentPage = ref(1);
const pageSize = ref(20);

const displayedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return items.value.slice(start, end);
});

const compare = ref({ byStorage: {}, samples: [] });

// 格式化耗时显示
const formatTime = (ms) => {
  if (ms === undefined || ms === null || ms === "-") return "-";
  if (ms >= 1000) {
    return (ms / 1000).toFixed(2) + " s";
  }
  return ms + " ms";
};

const chartRef = ref(null);
let chartInstance = null;

async function run() {
  if (!filters.value.databases || filters.value.databases.length === 0) {
    import("element-plus").then((mod) => {
      mod.ElMessage.warning("请至少选择一个数据库");
    });
    return;
  }
  console.log("[View] Running complex query with filters:", filters.value);
  loading.value = true;
  try {
    const res = await complexQuery(filters.value);
    // 适配后端 MovieDetailDto 或 mock 数据
    const mappedItems = (res.items || []).map((item) => ({
      movieAsin: item.movieAsin || item.id || "",
      movieTitle: item.movieTitle || item.title || "",
      movieScore: item.movieScore || item.rating || 0,
      actors: item.actors
        ? Array.isArray(item.actors)
          ? item.actors.join(", ")
          : item.actors
        : "",
      directors: item.directors || item.director || "",
      movieGenre: item.movieGenre
        ? Array.isArray(item.movieGenre)
          ? item.movieGenre.join(", ")
          : item.movieGenre
        : Array.isArray(item.genres)
        ? item.genres.join(", ")
        : item.genres || "",
      date: item.date || item.releaseDate || "",
      edition: item.edition || "标准版",
    }));

    console.log("[View] Mapped query results:", mappedItems);
    items.value = mappedItems;
    currentPage.value = 1; // 查询后重置到第一页

    compare.value = {
      byStorage: res.byStorage || {},
      samples: res.samples || [],
    };
    activeTab.value = "results";
  } catch (error) {
    console.error("[View] Query failed:", error);
  } finally {
    loading.value = false;
  }
}

function reset() {
  filters.value = {
    mode: "multi",
    databases: ["mysql", "hive"],
    title: "",
    director: "",
    actor: "",
    genre: "",
    yearFrom: null,
    yearTo: null,
    minRating: null,
    maxRating: null,
    releaseDate: null,
    dateGranularity: "date",
  };
  items.value = [];
  currentPage.value = 1;
  compare.value = { byStorage: {}, samples: [] };
}

watch(
  compare,
  async () => {
    await nextTick();
    const el = chartRef.value;
    if (!el) return;
    if (!chartInstance) {
      try {
        const mod = await import(/* @vite-ignore */ "echarts");
        const echarts = mod && (mod.default || mod);
        if (!echarts || !echarts.init) return;
        chartInstance = echarts.init(el);
      } catch (e) {
        console.warn("echarts import failed", e);
        return;
      }
    }
    const categories = Object.keys(compare.value.byStorage || {});
    const rawValues = categories.map((k) => compare.value.byStorage[k] || 0);
    const maxValue = Math.max(...rawValues, 0);
    const useSeconds = maxValue >= 1000;

    const displayValues = useSeconds
      ? rawValues.map((v) => v / 1000)
      : rawValues;
    const unit = useSeconds ? "s" : "ms";

    const option = {
      tooltip: {
        trigger: "axis",
        formatter: (params) => {
          const p = params[0];
          const val = useSeconds ? Number(p.value).toFixed(3) : p.value;
          return `${p.name}<br/>耗时: ${val}${unit}`;
        },
      },
      xAxis: {
        type: "category",
        data: categories.map((c) => c.toUpperCase()),
      },
      yAxis: {
        type: "value",
        name: `耗时 (${unit})`,
        axisLabel: {
          formatter: `{value} ${unit}`,
        },
      },
      series: [
        {
          type: "bar",
          data: displayValues,
          barWidth: "40px",
          itemStyle: { color: "#2d8cf0" },
        },
      ],
    };
    chartInstance.setOption(option);
  },
  { deep: true }
);

// 监听标签页切换，切换到性能对比时重新渲染图表
watch(activeTab, async (newTab) => {
  if (newTab === "compare") {
    await nextTick();
    if (chartInstance) {
      chartInstance.resize();
    } else {
      // 如果还没初始化，手动触发一次 compare 监听逻辑
      compare.value = { ...compare.value };
    }
  }
});
</script>

<style scoped>
.complex-form .el-col .el-form-item {
  margin-bottom: 10px;
}
.align-end {
  display: flex;
  align-items: flex-end;
}
.button-group .el-button {
  margin-right: 8px;
}
.button-group .el-button:last-child {
  margin-right: 0;
}
.muted {
  color: #7b8794;
}
</style>


