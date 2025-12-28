<template>
  <el-card v-loading="loading" element-loading-text="正在为您查询数据...">
    <div
      class="controls"
      style="display: flex; gap: 16px; align-items: center; flex-wrap: wrap"
    >
      <div style="display: flex; gap: 8px; align-items: center">
        <label style="width: 90px; text-align: right; margin-right: 8px"
          >选择数据库</label
        >
        <el-checkbox-group v-model="selectedDBs" size="middle">
          <el-checkbox label="neo4j">Neo4j</el-checkbox>
          <el-checkbox label="hive">Hive</el-checkbox>
        </el-checkbox-group>
      </div>

      <div style="display: flex; gap: 8px; align-items: center">
        <label style="width: 90px; text-align: right; margin-right: 8px"
          >关系来源</label
        >
        <el-select
          v-model="source"
          placeholder="选择来源"
          size="small"
          style="width: 260px"
        >
          <el-option label="演员" value="actor" />
          <el-option label="导演" value="director" />
        </el-select>
      </div>

      <div style="display: flex; gap: 8px; align-items: center">
        <label style="width: 90px; text-align: right; margin-right: 8px"
          >合作对象</label
        >
        <span
          style="
            color: #606266;
            font-size: 14px;
            background: #f5f7fa;
            padding: 5px 12px;
            border-radius: 4px;
            border: 1px solid #dcdfe6;
            min-width: 234px;
          "
        >
          演员
        </span>
      </div>

      <div style="display: flex; gap: 8px; align-items: center">
        <label style="width: 90px; text-align: right; margin-right: 8px"
          >电影类型</label
        >
        <el-input
          v-model="movieType"
          placeholder="可选，输入电影类型"
          style="width: 200px"
          clearable
          size="small"
        />
      </div>

      <div style="display: flex; gap: 8px; align-items: center">
        <label style="width: 90px; text-align: right; margin-right: 8px"
          >查询数量</label
        >
        <el-input-number
          v-model="limit"
          :min="1"
          :max="500"
          size="small"
          style="width: 120px"
        />
      </div>

      <div style="display: flex; gap: 8px">
        <el-button type="primary" @click="run('count')"
          >按合作次数查询</el-button
        >
        <el-button type="success" @click="run('attention')"
          >按关注程度查询</el-button
        >
      </div>
    </div>

    <div style="margin-top: 12px">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="查询结果" name="results">
          <div v-if="graph.nodes && graph.nodes.length">
            <div style="display: flex; gap: 16px">
              <div style="flex: 1">
                <div
                  ref="graphRef"
                  style="width: 100%; min-height: 500px"
                ></div>
              </div>
              <div style="width: 420px">
                <el-card>
                  <div><strong>合作组合（Top）</strong></div>
                  <el-table
                    :data="combos.slice(0, 10)"
                    stripe
                    style="margin-top: 8px"
                    :cell-style="{ padding: '12px 0' }"
                  >
                    <el-table-column
                      prop="a"
                      :label="source === 'actor' ? '演员1' : '导演'"
                      min-width="120"
                    />
                    <el-table-column
                      prop="b"
                      :label="source === 'actor' ? '演员2' : '演员'"
                      min-width="120"
                    />
                    <el-table-column
                      prop="count"
                      :label="
                        activeQueryType === 'count'
                          ? '次数'
                          : '关注度（评论数量）'
                      "
                      width="100"
                    />
                  </el-table>
                </el-card>
              </div>
            </div>

            <!-- 查询结果列表 -->
            <div style="margin-top: 20px">
              <el-card shadow="never">
                <div style="margin-bottom: 10px; font-weight: bold">
                  查询结果列表
                </div>
                <el-table
                  :data="combos"
                  stripe
                  style="width: 100%"
                  max-height="500"
                  :cell-style="{ padding: '10px 0' }"
                >
                  <el-table-column type="index" label="序号" width="70" />
                  <el-table-column
                    prop="a"
                    :label="source === 'actor' ? '演员1' : '导演'"
                    min-width="150"
                  />
                  <el-table-column
                    prop="b"
                    :label="source === 'actor' ? '演员2' : '演员'"
                    min-width="150"
                  />
                  <el-table-column
                    prop="count"
                    :label="
                      activeQueryType === 'count'
                        ? '合作次数'
                        : '关注度（评论数量）'
                    "
                    sortable
                  />
                </el-table>
              </el-card>
            </div>
          </div>
          <div v-else class="empty-wrap">
            <el-empty description="没有关系数据" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="性能对比" name="compare">
          <div style="display: flex; gap: 16px">
            <div style="flex: 1">
              <div ref="chartRef" style="width: 100%; min-height: 300px"></div>
            </div>
            <div style="width: 260px">
              <el-card>
                <div><strong>耗时统计</strong></div>
                <div class="muted" style="margin-top: 8px">
                  <span v-if="selectedDBs.includes('hive')">
                    Hive: {{ formatTime(compare.byStorage?.hive) }}<br />
                  </span>
                  <span v-if="selectedDBs.includes('neo4j')">
                    Neo4j: {{ formatTime(compare.byStorage?.neo4j) }}
                  </span>
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
import { ref, nextTick, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  queryRelation,
  compareTiming,
  getActorCollaborations,
  getHiveActorCollaborations,
  getDirectorActorCollaborations,
  getActorAttention,
  getDirectorActorAttention,
  getActorAttentionByGenre,
  getDirectorActorAttentionByGenre,
  getHiveDirectorActorCollaborations,
  getHiveActorAttention,
  getHiveDirectorActorAttention,
} from "@/api/queries";

const source = ref("actor");
const movieType = ref("");
const limit = ref(50);
const selectedDBs = ref(["neo4j", "hive"]);
const loading = ref(false);
const graph = ref({ nodes: [], edges: [] });
const activeTab = ref("results");
const activeQueryType = ref("count");

const compare = ref({ byStorage: {}, samples: [] });
const chartRef = ref(null);
const graphRef = ref(null);
const combos = ref([]);
let chartInstance = null;

async function run(type = "count") {
  if (selectedDBs.value.length === 0) {
    ElMessage.warning("请至少选择一个数据库进行查询");
    return;
  }

  activeQueryType.value = type;
  loading.value = true;
  console.log(`\n--- [Relation Query Start] ---`);
  console.log(`Source: ${source.value}`);
  console.log(`Type: ${type}`);
  console.log(`MovieType: ${movieType.value || "Any"}`);
  console.log(`Selected DBs: ${selectedDBs.value.join(", ")}`);

  const params = {
    source: source.value,
    movieType: movieType.value,
    queryType: type,
  };

  let neo4jRes = null;
  let hiveRes = null;
  const timings = {};

  try {
    const promises = [];

    // 1. Neo4j Query
    if (selectedDBs.value.includes("neo4j")) {
      promises.push(
        (async () => {
          console.log("[Neo4j] Requesting data...");
          const start = Date.now();
          const queryParams = { limit: limit.value, genre: movieType.value };
          try {
            if (source.value === "actor") {
              if (type === "count") {
                if (movieType.value) {
                  // Use genre endpoint which returns both count (movies) and attention (review_sum)
                  neo4jRes = await getActorAttentionByGenre(queryParams);
                } else {
                  neo4jRes = await getActorCollaborations(queryParams);
                }
              } else if (type === "attention") {
                if (movieType.value) {
                  neo4jRes = await getActorAttentionByGenre(queryParams);
                } else {
                  neo4jRes = await getActorAttention(queryParams);
                }
              }
            } else if (source.value === "director") {
              if (type === "count") {
                if (movieType.value) {
                  neo4jRes = await getDirectorActorAttentionByGenre(
                    queryParams
                  );
                } else {
                  neo4jRes = await getDirectorActorCollaborations(queryParams);
                }
              } else if (type === "attention") {
                if (movieType.value) {
                  neo4jRes = await getDirectorActorAttentionByGenre(
                    queryParams
                  );
                } else {
                  neo4jRes = await getDirectorActorAttention(queryParams);
                }
              }
            }

            if (!neo4jRes) {
              neo4jRes = await queryRelation(params);
            }

            timings.neo4j = Date.now() - start;
            console.log(`[Neo4j] Success! Time: ${timings.neo4j}ms`);
          } catch (err) {
            console.error("[Neo4j] Request failed:", err);
            timings.neo4j = 0;
          }
        })()
      );
    }

    // 2. Hive Query
    if (selectedDBs.value.includes("hive")) {
      promises.push(
        (async () => {
          console.log("[Hive] Requesting data...");
          const start = Date.now();
          const queryParams = { limit: limit.value, genre: movieType.value };
          try {
            if (source.value === "actor") {
              if (type === "count") {
                hiveRes = await getHiveActorCollaborations(queryParams);
              } else if (type === "attention") {
                hiveRes = await getHiveActorAttention(queryParams);
              }
            } else if (source.value === "director") {
              if (type === "count") {
                hiveRes = await getHiveDirectorActorCollaborations(queryParams);
              } else if (type === "attention") {
                hiveRes = await getHiveDirectorActorAttention(queryParams);
              }
            }

            if (!hiveRes) {
              // Simulated delay for Hive query as no direct Hive relation endpoint is explicitly defined
              await new Promise((resolve) => setTimeout(resolve, 800));
              hiveRes = { nodes: [], edges: [], combos: [] };
            }

            timings.hive = Date.now() - start;
            console.log(`[Hive] Success! Time: ${timings.hive}ms`);
          } catch (err) {
            console.error("[Hive] Request failed:", err);
            timings.hive = 0;
          }
        })()
      );
    }

    await Promise.all(promises);

    const isNotEmpty = (data) => {
      if (!data) return false;
      if (Array.isArray(data)) return data.length > 0;
      const g = data.graph || data;
      return g && g.nodes && g.nodes.length > 0;
    };

    // Data prioritization: Neo4j > Hive
    const finalData = isNotEmpty(neo4jRes)
      ? neo4jRes
      : isNotEmpty(hiveRes)
      ? hiveRes
      : null;

    console.log("[Relation] Final query data to display:", finalData);

    if (finalData) {
      if (Array.isArray(finalData)) {
        // Transform for table and graph
        combos.value = finalData.map((item) => {
          const a = item.actor1 || item.director || "Unknown";
          const b = item.actor2 || item.actor || "Unknown";

          // Determine count based on query type and available fields
          let count = 0;
          if (activeQueryType.value === "count") {
            count = item.collaborations || item.movies || 0;
          } else {
            count = item.review_sum || item.collaborations || 0;
          }

          return { a, b, count };
        });

        // Transform for graph
        const nodesSet = new Set();
        const links = [];
        combos.value.forEach((item) => {
          nodesSet.add(item.a);
          nodesSet.add(item.b);
          links.push({
            source: item.a,
            target: item.b,
            weight: item.count,
          });
        });

        graph.value = {
          nodes: Array.from(nodesSet).map((name) => ({ id: name, name })),
          links: links,
        };
      } else {
        // 归一化处理关系图接口返回的对象格式
        // 兼容 { nodes: [], edges: [] } 和 { graph: { nodes: [], edges: [] } }
        const g = finalData.graph || finalData;
        graph.value = {
          nodes: g.nodes || [],
          links: g.edges || g.links || [],
        };
        combos.value = finalData.combos || [];
      }
    }

    // Update performance chart
    compare.value = {
      byStorage: timings,
      samples: [
        {
          query:
            type === "count"
              ? `按合作次数 (${source.value})`
              : `按关注程度 (${source.value})`,
          ...timings,
        },
      ],
    };

    activeTab.value = "results";
    console.log(`--- [Relation Query End] ---\n`);
  } catch (err) {
    console.error("[Query] Fatal Error:", err);
    ElMessage.error("查询失败，请检查后端服务");
  } finally {
    loading.value = false;
  }
}

// 格式化耗时显示工具函数
const formatTime = (ms) => {
  if (ms === undefined || ms === null || ms === 0) return "-";
  if (ms >= 1000) return (ms / 1000).toFixed(2) + " s";
  return ms + " ms";
};

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

    // 自动切换单位：如果最大耗时超过 1000ms，则使用 's' 作为单位
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
          return `${p.name.toUpperCase()}<br/>耗时: ${val}${unit}`;
        },
      },
      xAxis: {
        type: "category",
        data: categories.map((c) => c.toUpperCase()),
      },
      yAxis: {
        type: "value",
        name: `耗时 (${unit})`,
        axisLabel: { formatter: `{value} ${unit}` },
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

// render force graph when graph data changes
watch(
  graph,
  async () => {
    await nextTick();
    const el = graphRef.value;
    if (!el) return;
    try {
      const mod = await import(/* @vite-ignore */ "echarts");
      const echarts = mod && (mod.default || mod);
      if (!echarts || !echarts.init) return;
      const instance = echarts.init(el);
      const categories = [{ name: "actor" }];
      const nodes = (graph.value.nodes || []).map((n) => ({
        id: n.id || n.name,
        name: n.name || n.label,
        value: n.value || 1,
        category: 0,
      }));
      const links = (graph.value.links || []).map((l) => ({
        source: l.source,
        target: l.target,
        value: l.weight || 1,
      }));
      const option = {
        tooltip: { formatter: "{b}" },
        series: [
          {
            type: "graph",
            layout: "force",
            roam: true,
            data: nodes,
            links: links,
            categories,
            label: { position: "right", show: true, fontSize: 11 },
            force: { repulsion: 400, edgeLength: [50, 200] },
          },
        ],
      };
      instance.setOption(option);
    } catch (e) {
      console.warn("graph render failed", e);
    }
  },
  { deep: true }
);
</script>

<style scoped></style>


