<template>
  <div class="dashboard-page">
    <section class="stats-grid">
      <article
        v-for="item in stats"
        :key="item.title"
        class="metric-card"
      >
        <div class="metric-copy">
          <div class="metric-value">{{ item.value }}</div>
          <div class="metric-title">{{ item.title }}</div>
          <div class="metric-trend">
            <span>数据库实时统计</span>
          </div>
        </div>
        <div
          class="metric-icon"
          :style="{ background: item.bg, color: item.color }"
        >
          <el-icon :size="30">
            <component :is="item.icon" />
          </el-icon>
        </div>
      </article>
    </section>

    <section class="content-grid">
      <article class="panel panel-wide">
        <header class="panel-header">
          <h3>诊所收入趋势</h3>
          <div class="panel-switch">
            <button
              type="button"
              :class="['switch-btn', { active: trendDays === 7 }]"
              @click="setTrendDays(7)"
            >
              近7天
            </button>
            <button
              type="button"
              :class="['switch-btn', { active: trendDays === 30 }]"
              @click="setTrendDays(30)"
            >
              近30天
            </button>
          </div>
        </header>
        <div class="panel-subtitle">单位（元）</div>
        <div
          ref="lineRef"
          class="chart chart-line"
        />
      </article>

      <article class="panel panel-side">
        <header class="panel-header">
          <h3>今日会员消费占比</h3>
        </header>
        <div
          ref="pieRef"
          class="chart chart-pie"
        />
        <div class="pie-legend">
          <div class="legend-item">
            <span class="legend-dot success" />
            <span>会员消费占比{{ consumptionLegend.member }}%</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot danger" />
            <span>非会员消费占比{{ consumptionLegend.nonMember }}%</span>
          </div>
        </div>
      </article>
    </section>

    <article class="panel records-panel">
      <header class="panel-header">
        <h3>今日门诊记录</h3>
      </header>

      <el-table
        :data="pagedTableData"
        border
        class="records-table"
      >
        <el-table-column
          prop="id"
          label="序号"
          width="72"
        />
        <el-table-column
          prop="status"
          label="就诊状态"
          width="110"
        >
          <template #default="{ row }">
            <span :class="['table-status', row.status]">
              {{ statusMap[row.status].label }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="code"
          label="患者编号"
          width="110"
        />
        <el-table-column
          prop="name"
          label="患者姓名"
          width="110"
        />
        <el-table-column
          prop="gender"
          label="性别"
          width="70"
        />
        <el-table-column
          prop="age"
          label="年龄"
          width="70"
        />
        <el-table-column
          prop="phone"
          label="手机号"
          width="150"
        />
        <el-table-column
          prop="type"
          label="门诊类型"
          width="100"
        />
        <el-table-column
          prop="dept"
          label="科室"
          width="90"
        />
        <el-table-column
          prop="doctor"
          label="挂号医生"
          width="100"
        />
        <el-table-column
          prop="time"
          label="最后更新时间"
          min-width="170"
        />
      </el-table>

      <div class="records-pagination">
        <div class="pagination-nav">
          <button
            type="button"
            class="circle-btn"
            :disabled="currentPage <= 1"
            @click="currentPage = Math.max(1, currentPage - 1)"
          >
            ‹
          </button>
          <button
            v-for="page in pages"
            :key="page"
            type="button"
            :class="['circle-btn', { active: currentPage === page }]"
            @click="currentPage = page"
          >
            {{ page }}
          </button>
          <button
            type="button"
            class="circle-btn"
            :disabled="currentPage >= totalPages"
            @click="currentPage = Math.min(totalPages, currentPage + 1)"
          >
            ›
          </button>
        </div>

        <div class="pagination-meta">
          <span>每页10条, 共{{ tableData.length }}条</span>
          <span>前往第</span>
          <button
            type="button"
            class="page-input"
          >
            1
          </button>
          <span>页</span>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { Document, Money, User, Wallet } from "@element-plus/icons-vue";
import { LineChart, PieChart } from "echarts/charts";
import { GridComponent, GraphicComponent, LegendComponent, TooltipComponent } from "echarts/components";
import { init, use, type EChartsType } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { dashboardApi, workbenchApi } from "@/api";

use([CanvasRenderer, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, GraphicComponent]);

const stats = ref([
  { title: "今日挂号人次", value: 0, icon: Document, bg: "#eef0ff", color: "#6475f6" },
  { title: "今日接诊人次", value: 0, icon: User, bg: "#ffeded", color: "#ff6e73" },
  { title: "今日收入合计", value: 0, icon: Money, bg: "#e8fbf3", color: "#31c58d" },
  { title: "今日新增会员数", value: 0, icon: Wallet, bg: "#fff8e5", color: "#f7b500" },
]);

const statusMap = {
  pending: { label: "待接诊" },
  doing: { label: "接诊中" },
  done: { label: "已接诊" },
};

const lineRef = ref<HTMLElement | null>(null);
const pieRef = ref<HTMLElement | null>(null);
const lineChart = ref<EChartsType | null>(null);
const pieChart = ref<EChartsType | null>(null);

const trendDays = ref(7);
const tableData = ref<any[]>([]);
const consumptionLegend = ref({ member: 0, nonMember: 0 });
const currentPage = ref(1);
const pageSize = 10;

const totalPages = computed(() => Math.max(1, Math.ceil(tableData.value.length / pageSize)));
const pages = computed(() => Array.from({ length: totalPages.value }, (_, index) => index + 1));
const pagedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return tableData.value.slice(start, start + pageSize);
});

const pad = (value: number) => String(value).padStart(2, "0");
const formatDate = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const rangeForDays = (days: number) => {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - days + 1);
  return {
    startDate: formatDate(start),
    endDate: formatDate(end),
  };
};

const renderTrendChart = (trends: any[]) => {
  lineChart.value?.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 20, right: 18, top: 16, bottom: 16, containLabel: true },
    xAxis: {
      type: "category",
      boundaryGap: false,
      axisLine: { lineStyle: { color: "#e8eaf3" } },
      axisTick: { show: false },
      axisLabel: { color: "#7e879f" },
      data: trends.map((item) => String(item.statDate || "")),
    },
    yAxis: {
      type: "value",
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: "#edf0f7" } },
      axisLabel: { color: "#7e879f" },
    },
    series: [
      {
        data: trends.map((item) => Number(item.income || 0)),
        type: "line",
        smooth: true,
        symbol: "circle",
        symbolSize: 14,
        lineStyle: { width: 5, color: "#6574f7" },
        itemStyle: { color: "#ffffff", borderColor: "#f7b500", borderWidth: 4 },
        areaStyle: {
          color: {
            type: "linear",
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: "rgba(101, 116, 247, 0.34)" },
              { offset: 1, color: "rgba(101, 116, 247, 0.04)" },
            ],
          },
        },
      },
    ],
  });
};

const loadTrendData = async () => {
  const range = rangeForDays(trendDays.value);
  const response: any = await dashboardApi.trends(range);
  const trends = Array.isArray(response?.data) ? response.data : [];
  renderTrendChart(trends);
};

const setTrendDays = async (days: number) => {
  if (trendDays.value === days) return;
  trendDays.value = days;
  await loadTrendData();
};

const loadSummary = async () => {
  const response: any = await dashboardApi.summary();
  const data = response?.data || {};
  stats.value = [
    { ...stats.value[0], value: data.todayRegistrationCount ?? 0 },
    { ...stats.value[1], value: data.todayVisitCount ?? 0 },
    { ...stats.value[2], value: data.todayIncome ?? 0 },
    { ...stats.value[3], value: data.todayMemberCount ?? 0 },
  ];
};

const loadTodayVisits = async () => {
  const today = formatDate(new Date());
  const response: any = await workbenchApi.visits({ startDate: today, endDate: today });
  const responseData = response?.data;
  const records = Array.isArray(responseData) ? responseData : responseData?.records || [];
  tableData.value = records.map((item: any, index: number) => ({
    id: index + 1,
    status: ["DONE", "COMPLETED", "FINISHED", "VISITED"].includes(item.status)
      ? "done"
      : ["PROCESSING", "IN_PROGRESS"].includes(item.status) ? "doing" : "pending",
    code: item.patientId || item.registrationNo || "",
    name: item.patientName || "",
    gender: item.gender || "",
    age: item.age || "",
    phone: item.phone || "",
    type: item.visitType || "",
    dept: item.departmentName || "",
    doctor: item.doctorName || "",
    time: item.visitTime || item.updatedAt || item.createdAt || "",
  }));
  currentPage.value = 1;
};

const renderConsumptionChart = (rankings: any[]) => {
  const total = rankings.reduce((sum, item) => sum + Number(item.value || 0), 0);
  const memberValue = Number(rankings.find((item) => item.name === "会员消费")?.value || 0);
  const nonMemberValue = Math.max(total - memberValue, 0);
  consumptionLegend.value = {
    member: total ? Math.round((memberValue / total) * 100) : 0,
    nonMember: total ? Math.round((nonMemberValue / total) * 100) : 0,
  };
  pieChart.value?.setOption({
    tooltip: { trigger: "item" },
    graphic: [
      {
        type: "text",
        left: "center",
        top: "43%",
        style: {
          text: "今日消费总额",
          fill: "#7f879c",
          fontSize: 14,
          fontWeight: 400,
        },
      },
      {
        type: "text",
        left: "center",
        top: "51%",
        style: {
          text: `¥${total.toFixed(2)}`,
          fill: "#232733",
          fontSize: 24,
          fontWeight: 700,
        },
      },
    ],
    series: [
      {
        type: "pie",
        radius: ["56%", "76%"],
        center: ["50%", "44%"],
        label: { show: false },
        labelLine: { show: false },
        data: rankings.map((item) => ({
          value: Number(item.value || 0),
          name: item.name,
          itemStyle: { color: item.name === "会员消费" ? "#31d09b" : "#ff5d63" },
        })),
      },
    ],
  });
};

const loadConsumptionData = async () => {
  const response: any = await dashboardApi.rankings({ type: "member", limit: 10 });
  const rankings = Array.isArray(response?.data) ? response.data : [];
  renderConsumptionChart(rankings);
};

onMounted(async () => {
  await nextTick();
  if (lineRef.value) lineChart.value = init(lineRef.value);
  if (pieRef.value) pieChart.value = init(pieRef.value);

  await Promise.all([
    loadSummary(),
    loadTrendData(),
    loadTodayVisits(),
    loadConsumptionData(),
  ]);
});

onBeforeUnmount(() => {
  lineChart.value?.dispose();
  pieChart.value?.dispose();
});
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.metric-card,
.panel {
  background: #fff;
  border-radius: 26px;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.06);
}

.metric-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 170px;
  padding: 30px 36px;
}

.metric-value {
  font-size: 64px;
  line-height: 1;
  font-weight: 700;
  color: #2c313c;
}

.metric-title {
  margin-top: 20px;
  font-size: 22px;
  font-weight: 700;
  color: #404756;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  color: #98a1b6;
  font-size: 16px;
}

.metric-arrow {
  color: #6574f7;
  font-size: 18px;
  font-weight: 700;
}

.metric-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 78px;
  height: 78px;
  border-radius: 50%;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 2.2fr) minmax(300px, 0.8fr);
  gap: 22px;
}

.panel {
  padding: 22px 28px 26px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.panel-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2430;
}

.panel-subtitle {
  margin-bottom: 8px;
  color: #5d6476;
  font-size: 14px;
}

.panel-switch {
  display: flex;
  gap: 10px;
}

.switch-btn {
  min-width: 88px;
  height: 34px;
  border: 1px solid #eceff7;
  border-radius: 999px;
  background: #fff;
  color: #a1a8bb;
  font-size: 15px;
  cursor: pointer;
}

.switch-btn.active {
  border-color: #ff6263;
  background: #ff6263;
  color: #fff;
}

.chart-line {
  height: 330px;
}

.chart-pie {
  height: 320px;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 4px 18px 0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #596174;
  font-size: 16px;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
}

.legend-dot.success {
  background: #31d09b;
}

.legend-dot.danger {
  background: #ff5d63;
}

.records-panel {
  padding-bottom: 20px;
}

.records-pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
  padding: 26px 6px 4px;
}

.pagination-nav,
.pagination-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.circle-btn,
.page-input {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 40px;
  border: 1px solid #cfd4e2;
  border-radius: 50%;
  background: #fff;
  color: #7f879c;
  font-size: 18px;
  cursor: pointer;
}

.circle-btn.active {
  border-color: #6574f7;
  background: #6574f7;
  color: #fff;
}

.page-input {
  font-size: 16px;
  color: #6d7589;
}

.pagination-meta {
  color: #4d5568;
  font-size: 16px;
}

.records-table :deep(.el-table__header th) {
  background: #ffffff;
  color: #a8afc0;
  font-size: 14px;
  font-weight: 700;
}

.records-table :deep(.el-table__row td) {
  height: 66px;
  color: #2f3747;
}

.table-status {
  font-weight: 600;
}

.table-status.pending {
  color: #2ed18f;
}

.table-status.doing {
  color: #f59e0b;
}

.table-status.done {
  color: #64748b;
}

@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .metric-card {
    padding: 24px 22px;
  }

  .metric-value {
    font-size: 48px;
  }

  .panel {
    padding: 18px;
  }
}
</style>
