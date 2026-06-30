<template>
  <div class="statistics-page">
    <section
      :class="[
        'statistics-card',
        {
          'is-charge': isChargePage,
          'is-patient': isPatientPage,
          'is-check-project': isCheckProjectPage,
          'is-drug': isDrugPage,
        },
      ]"
    >
      <header class="page-toolbar">
        <div v-if="!isCheckProjectPage" class="stat-tabs" role="tablist" :aria-label="pageTitle">
          <button
            v-for="tab in currentTabs"
            :key="tab.key"
            type="button"
            :class="['stat-tab', { 'is-active': activeTab === tab.key }]"
            @click="selectTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </div>

        <button type="button" class="export-btn" @click="exportStatistics">
          <Upload class="btn-icon" />
          <span>导出</span>
        </button>
      </header>

      <div class="page-divider"></div>

      <section v-if="activeConfig.filters.length || activeConfig.search" class="filters-row" :class="`filters-${activeTab}`">
        <div v-for="filter in activeConfig.filters" :key="filter.key" class="filter-item">
          <span>{{ filter.label }}</span>
          <el-date-picker
            v-if="filter.type === 'date'"
            v-model="filter.value"
            class="date-control"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
            @change="handleFilterChange"
          />
          <select v-else v-model="filter.value" @change="handleFilterChange">
            <option v-for="option in filter.options" :key="option" :value="option">
              {{ option }}
            </option>
          </select>
        </div>

        <label v-if="activeConfig.search" class="search-filter">
          <input
            v-model.trim="searchKeyword"
            type="text"
            :placeholder="activeConfig.search"
            @keyup.enter="applySearch"
          />
          <button type="button" aria-label="搜索" @click="applySearch">
            <Search class="search-icon" />
          </button>
        </label>
      </section>

      <section v-if="activeConfig.summaryCards" class="summary-panel">
        <article
          v-for="card in activeConfig.summaryCards"
          :key="card.label"
          :class="['summary-card', { accent: card.accent }]"
        >
          <div class="summary-label">{{ card.label }}</div>
          <div class="summary-value">{{ card.value }}</div>
        </article>
      </section>

      <section v-if="activeConfig.barChart" class="chart-card bar-card">
        <div ref="barChartRef" class="echart bar-echart"></div>
      </section>

      <section v-if="activeConfig.donutChart" class="chart-card donut-card">
        <div ref="donutChartRef" class="echart donut-echart"></div>
      </section>

      <div class="table-wrapper">
        <table :class="['stat-table', activeConfig.tableClass]">
          <colgroup v-if="activeConfig.columns.some((column) => column.width)">
            <col
              v-for="column in activeConfig.columns"
              :key="column.key"
              :style="{ width: column.width ? `${column.width}px` : undefined }"
            />
          </colgroup>

          <thead>
            <tr>
              <th v-for="column in activeConfig.columns" :key="column.key">
                <span v-html="column.label"></span>
              </th>
            </tr>
          </thead>

          <tbody>
            <tr v-if="activeConfig.totalRow" class="total-row">
              <td
                v-for="(cell, index) in activeConfig.totalRow"
                :key="`${cell.value}-${index}`"
                :colspan="cell.colspan"
              >
                <strong>{{ cell.value }}</strong>
              </td>
            </tr>

            <tr v-for="row in displayedRows" :key="row.id">
              <td
                v-for="column in activeConfig.columns"
                :key="column.key"
                :class="{
                  'status-cell': column.key === 'status' || column.key === 'chargeStatus',
                  'type-cell': column.key === 'orderType' || column.key === 'tradeType',
                  'detail-link': column.key === 'action',
                  'strong-cell': column.key === 'total',
                }"
              >
                <button v-if="column.key === 'action'" type="button" @click="openChargeDetail(row)">
                  {{ row[column.key] }}
                </button>
                <span v-else v-html="row[column.key]"></span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer v-if="activeConfig.fullPager" class="pagination" aria-label="分页">
        <button
          type="button"
          class="pager-btn"
          aria-label="上一页"
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          &lt;
        </button>
        <button
          v-for="page in visiblePages"
          :key="page"
          type="button"
          :class="['pager-btn', { 'is-active': currentPage === page }]"
          @click="changePage(page)"
        >
          {{ page }}
        </button>
        <button
          type="button"
          class="pager-btn"
          aria-label="下一页"
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          &gt;
        </button>
        <span class="pager-info">每页{{ pageSize }}条，共{{ tableRows.length }}条&nbsp;&nbsp;前往第</span>
        <span class="pager-input">{{ currentPage }}</span>
        <span class="pager-info">页</span>
      </footer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Search, Upload } from "@element-plus/icons-vue";
import { BarChart, PieChart } from "echarts/charts";
import { GridComponent, LegendComponent, TooltipComponent } from "echarts/components";
import { init, use, type EChartsType } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { staffApi, statisticsApi } from "@/api";

use([CanvasRenderer, BarChart, PieChart, GridComponent, LegendComponent, TooltipComponent]);

type FilterConfig = {
  key: string;
  label: string;
  type: "date" | "select";
  value: string | string[];
  options?: string[];
};

type ColumnConfig = {
  key: string;
  label: string;
  width?: number;
};

type TableRow = Record<string, string | number>;

type TotalCell = {
  value: string;
  colspan?: number;
};

type SummaryCard = {
  label: string;
  value: string;
  accent?: boolean;
};

type BarItem = {
  label: string;
  value: number;
};

type DonutItem = {
  label: string;
  value: number;
  color: string;
  display: string;
};

type TabConfig = {
  filters: FilterConfig[];
  search?: string;
  summaryCards?: SummaryCard[];
  columns: ColumnConfig[];
  rows: TableRow[];
  totalRow?: TotalCell[];
  tableClass: string;
  fullPager?: boolean;
  barChart?: {
    items: BarItem[];
    max: number;
  };
  donutChart?: {
    items: DonutItem[];
    legendColumns?: number;
  };
};

type Tab = {
  key: string;
  label: string;
};

const route = useRoute();
const router = useRouter();
const barChartRef = ref<HTMLElement | null>(null);
const donutChartRef = ref<HTMLElement | null>(null);
let barChart: EChartsType | null = null;
let donutChart: EChartsType | null = null;

const chargeTabs: Tab[] = [
  { key: "detail", label: "收支明细" },
  { key: "cashier", label: "收费员收费统计" },
  { key: "department", label: "科室收费统计" },
  { key: "doctor", label: "医生业绩统计" },
  { key: "stored", label: "储值明细" },
];

const patientTabs: Tab[] = [
  { key: "log", label: "门诊日志" },
  { key: "age", label: "患者年龄分析" },
  { key: "gender", label: "患者性别分析" },
  { key: "marriage", label: "患者婚姻情况分析" },
  { key: "member", label: "会员类型分析" },
  { key: "visit", label: "就诊类型分析" },
  { key: "source", label: "患者来源分析" },
];

const dateFilter = (key: string, label: string): FilterConfig => ({
  key,
  label,
  type: "date",
  value: [],
});

const selectFilter = (key: string, label: string, options = ["全部"]): FilterConfig => ({
  key,
  label,
  type: "select",
  value: options[0],
  options,
});

const cashierNames = ["王冕", "林鹤", "李忠云"];
const departments = ["全科", "内科", "儿科"];
const doctorNames = ["王冕", "林鹤", "李忠云"];
const orderTypes = ["处方开立", "药品零售", "挂号费"];
const colors = {
  green: "#2dce9a",
  red: "#ff5b61",
  yellow: "#ffc322",
  blue: "#2c9fe5",
  indigo: "#636be8",
  gray: "#d2d2d2",
};

const patientAnalysisLabels: Record<string, string[]> = {
  age: ["0-10岁", "11-20岁", "21-30岁", "31-40岁", "41-50岁", "51-60岁", "61-70岁", "71-80岁", "80岁以上"],
  gender: ["男", "女", "未知"],
  marriage: ["未婚", "已婚", "未知"],
  member: ["普通会员", "初级会员", "高级会员", "白银会员", "黄金会员", "钻石会员"],
  visit: ["初诊", "复诊"],
  source: ["朋友介绍", "广告", "自来"],
};

const statusTextMap: Record<string, string> = {
  PENDING: "待收费",
  PARTIAL: "待收费",
  PAID: "已收费",
  REFUNDED: "已退费",
  CANCELLED: "已取消",
};

const tradeTypeTextMap: Record<string, string> = {
  RECHARGE: "充值",
  REFUND: "充值退款",
  CONSUME: "消费",
  CONSUMPTION: "消费",
  CONSUME_REFUND: "消费退款",
  CONSUMPTION_REFUND: "消费退款",
  ADJUST_BALANCE: "余额调整",
  BALANCE_ADJUST: "余额调整",
  ADJUST_POINTS: "积分调整",
  POINTS_ADJUST: "积分调整",
};

const detailRows = Array.from({ length: 10 }, (_, index) => ({
  id: `detail-${index}`,
  index: index + 1,
  status: index % 2 === 0 ? "已收费" : "已退费",
  orderType: orderTypes[index % orderTypes.length],
  orderNo: String(201912090030 - index),
  patientName: `姓名${index * 2 + 1}`,
  department: "全科",
  doctor: index % 3 === 1 ? "" : doctorNames[index % doctorNames.length],
  receivable: "210.00",
  discount: "10.00",
  received: "200.00",
  medical: "50.00",
  memberCard: "150.00",
  cash: "0.00",
  alipay: "0/00",
  wechat: "0.00",
  bank: "0.00",
  chargeTime: "2019-10-30 10:34:45",
  operator: "王冕",
  action: "查看明细",
}));

const cashierRows = cashierNames.map((cashier, index) => ({
  id: `cashier-${index}`,
  index: index + 1,
  cashier,
  department: departments[index],
  chargeCount: 50,
  refundCount: 10,
  receivable: "2200.00",
  discount: "200.00",
  received: "2000.00",
  medical: "1500.00",
  memberCard: "200.00",
  cash: "200.00",
  alipay: "200.00",
  wechat: "200.00",
  bank: "200.00",
}));

const departmentRows = ["全科", "内科", "外科"].map((department, index) => ({
  id: `department-${index}`,
  index: index + 1,
  department,
  visits: 10,
  received: "2000.00",
  medical: "1500.00",
  memberCard: "200.00",
  cash: "200.00",
  alipay: "200.00",
  wechat: "200.00",
  bank: "200.00",
}));

const doctorRows = doctorNames.map((doctor, index) => ({
  id: `doctor-${index}`,
  index: index + 1,
  doctor,
  receivable: "2200.00",
  discount: "200.00",
  received: "2000.00",
  visits: 10,
  firstVisits: 7,
  returnVisits: 3,
  westernDrug: "200.00",
  chineseDrug: "200.00",
  checkProject: "200.00",
  registration: "200.00",
  diagnosis: "200.00",
}));

const storedTypes = ["充值", "充值退款", "消费", "消费退款"];
const amountMap: Record<string, string> = {
  充值: "1000.00",
  充值退款: "-800.00",
  消费: "-56.00",
  消费退款: "56.00",
};

const storedRows = Array.from({ length: 10 }, (_, index) => {
  const tradeType = storedTypes[index % storedTypes.length];
  return {
    id: `stored-${index}`,
    index: index + 1,
    tradeType,
    cardNo: `02810015${9 - index}`,
    memberName: `姓名${59 - index}`,
    amount: amountMap[tradeType],
    gift: "200.00",
    total: "1200.00",
    balance: "2000.00",
    payment: "现金",
    tradeTime: "2019-10-30 10:34:45",
    operator: "王冕",
  };
});

const patientDoctors = ["王冕", "林鹤", "李忠云"];
const patientStatuses = ["已收费", "已退费", "待收费"];
const patientRows = Array.from({ length: 10 }, (_, index) => ({
  id: `patient-${index}`,
  index: index + 1,
  visitTime: "2019-10-30 10:34:45",
  patientNo: 100130 - index,
  patientName: `姓名${59 - index}`,
  gender: "女",
  age: 34,
  phone: String(17754138769 + index * 2),
  visitType: index % 2 === 0 ? "初诊" : "复诊",
  diagnosis: "流行性感冒",
  department: "科室",
  doctor: patientDoctors[index % patientDoctors.length],
  chargeStatus: patientStatuses[index % patientStatuses.length],
}));

const ageItems: BarItem[] = [
  { label: "0-10岁", value: 10 },
  { label: "11-20岁", value: 15 },
  { label: "21-30岁", value: 30 },
  { label: "31-40岁", value: 50 },
  { label: "41-50岁", value: 60 },
  { label: "51-60岁", value: 40 },
  { label: "61-70岁", value: 30 },
  { label: "71-80岁", value: 20 },
  { label: "80岁以上", value: 5 },
];

const chargeDetailColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "status", label: "订单状态", width: 76 },
  { key: "orderType", label: "订单类型", width: 76 },
  { key: "orderNo", label: "订单编号", width: 122 },
  { key: "patientName", label: "姓名", width: 68 },
  { key: "department", label: "科室", width: 53 },
  { key: "doctor", label: "接诊医<br>生", width: 56 },
  { key: "receivable", label: "应收/退<br>（元）", width: 72 },
  { key: "discount", label: "优惠<br>（元）", width: 72 },
  { key: "received", label: "实收/退<br>（元）", width: 72 },
  { key: "medical", label: "医保<br>（元）", width: 72 },
  { key: "memberCard", label: "会员卡<br>（元）", width: 72 },
  { key: "cash", label: "现金<br>（元）", width: 72 },
  { key: "alipay", label: "支付宝<br>（元）", width: 72 },
  { key: "wechat", label: "微信<br>（元）", width: 72 },
  { key: "bank", label: "银行卡<br>（元）", width: 72 },
  { key: "chargeTime", label: "收费日期", width: 156 },
  { key: "operator", label: "收费员", width: 64 },
  { key: "action", label: "操作", width: 110 },
];

const cashierColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "cashier", label: "收费员", width: 106 },
  { key: "department", label: "所属科室", width: 132 },
  { key: "chargeCount", label: "收费总数", width: 112 },
  { key: "refundCount", label: "退费总数", width: 112 },
  { key: "receivable", label: "应收金额<br>（元）", width: 112 },
  { key: "discount", label: "优惠金额<br>（元）", width: 124 },
  { key: "received", label: "实收金额<br>（元）", width: 112 },
  { key: "medical", label: "医保(元)", width: 112 },
  { key: "memberCard", label: "会员卡(元)", width: 112 },
  { key: "cash", label: "现金(元)", width: 112 },
  { key: "alipay", label: "支付宝(元)", width: 112 },
  { key: "wechat", label: "微信(元)", width: 112 },
  { key: "bank", label: "银行卡(元)", width: 112 },
];

const departmentColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "department", label: "科室", width: 190 },
  { key: "visits", label: "接诊人次", width: 136 },
  { key: "received", label: "实收金额（元）", width: 162 },
  { key: "medical", label: "医保(元)", width: 162 },
  { key: "memberCard", label: "会员卡(元)", width: 162 },
  { key: "cash", label: "现金(元)", width: 162 },
  { key: "alipay", label: "支付宝(元)", width: 162 },
  { key: "wechat", label: "微信(元)", width: 162 },
  { key: "bank", label: "银行卡(元)", width: 162 },
];

const doctorColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "doctor", label: "医生", width: 150 },
  { key: "receivable", label: "应收金额<br>（元）", width: 126 },
  { key: "discount", label: "优惠金额（元）", width: 136 },
  { key: "received", label: "实收金额（元）", width: 126 },
  { key: "visits", label: "接诊人次", width: 104 },
  { key: "firstVisits", label: "初诊人次", width: 104 },
  { key: "returnVisits", label: "复诊人次", width: 104 },
  { key: "westernDrug", label: "西/成药费<br>（元）", width: 126 },
  { key: "chineseDrug", label: "中药费（元）", width: 126 },
  { key: "checkProject", label: "检查项目<br>（元）", width: 126 },
  { key: "registration", label: "挂号费（元）", width: 126 },
  { key: "diagnosis", label: "诊疗费（元）", width: 126 },
];

const storedColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "tradeType", label: "交易类型", width: 198 },
  { key: "cardNo", label: "卡号", width: 144 },
  { key: "memberName", label: "会员姓名", width: 128 },
  { key: "amount", label: "交易金额<br>（元）", width: 126 },
  { key: "gift", label: "赠送金额<br>（元）", width: 128 },
  { key: "total", label: "合计金额<br>（元）", width: 126 },
  { key: "balance", label: "余额（元）", width: 128 },
  { key: "payment", label: "交易方式", width: 128 },
  { key: "tradeTime", label: "交易时间", width: 235 },
  { key: "operator", label: "操作员", width: 120 },
];

const patientLogColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 56 },
  { key: "visitTime", label: "就诊时间", width: 210 },
  { key: "patientNo", label: "患者编号", width: 128 },
  { key: "patientName", label: "姓名", width: 110 },
  { key: "gender", label: "性别", width: 74 },
  { key: "age", label: "年龄", width: 74 },
  { key: "phone", label: "手机号码", width: 150 },
  { key: "visitType", label: "门诊类型", width: 150 },
  { key: "diagnosis", label: "诊断", width: 180 },
  { key: "department", label: "科室", width: 134 },
  { key: "doctor", label: "接诊医生", width: 164 },
  { key: "chargeStatus", label: "收费状态", width: 174 },
];

const ageColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "segment", label: "年龄段" },
  { key: "count", label: "人次" },
];

const rowColumns = (labels: string[]): ColumnConfig[] => [
  { key: "index", label: "序号", width: 56 },
  ...labels.map((label) => ({ key: label, label })),
  { key: "total", label: "合计" },
];

const ageRows = ageItems.map((item, index) => ({
  id: `age-${index}`,
  index: index + 1,
  segment: item.label,
  count: item.value,
}));

const chargeConfigs = reactive<Record<string, TabConfig>>({
  detail: {
    filters: [
      dateFilter("orderTime", "订单时间"),
      selectFilter("orderType", "订单类型", ["全部", "处方开立", "药品零售", "挂号费"]),
    ],
    search: "输入订单编号/患者姓名",
    summaryCards: [
      { label: "合计收入（元）", value: "1200.00", accent: true },
      { label: "应收合计（元）", value: "1400.00" },
      { label: "优惠合计（元）", value: "200.00" },
      { label: "实收合计（元）", value: "1200.00" },
      { label: "医保（元）", value: "200.00" },
      { label: "会员（元）", value: "200.00" },
      { label: "现金（元）", value: "200.00" },
      { label: "支付宝（元）", value: "200.00" },
      { label: "微信（元）", value: "200.00" },
      { label: "银行卡（元）", value: "200.00" },
    ],
    columns: chargeDetailColumns,
    rows: detailRows,
    tableClass: "table-detail",
    fullPager: true,
  },
  cashier: {
    filters: [dateFilter("orderTime", "订单时间"), selectFilter("department", "科室"), selectFilter("cashier", "收费员")],
    columns: cashierColumns,
    rows: cashierRows,
    tableClass: "table-cashier",
    totalRow: [
      { value: "合计", colspan: 3 },
      { value: "1500" },
      { value: "300" },
      { value: "66000.00" },
      { value: "6000.00" },
      { value: "60000.00" },
      { value: "9000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
    ],
  },
  department: {
    filters: [dateFilter("orderTime", "订单时间"), selectFilter("department", "所属科室")],
    columns: departmentColumns,
    rows: departmentRows,
    tableClass: "table-department",
    totalRow: [
      { value: "合计", colspan: 2 },
      { value: "300" },
      { value: "60000.00" },
      { value: "45000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
    ],
  },
  doctor: {
    filters: [dateFilter("orderTime", "订单时间"), selectFilter("doctor", "医生")],
    columns: doctorColumns,
    rows: doctorRows,
    tableClass: "table-doctor",
    totalRow: [
      { value: "合计", colspan: 2 },
      { value: "66000.00" },
      { value: "6000.00" },
      { value: "600000.00" },
      { value: "300" },
      { value: "230" },
      { value: "70" },
      { value: "6000.00" },
      { value: "6000.00" },
      { value: "6000.00" },
      { value: "30000.00" },
      { value: "12000.00" },
    ],
  },
  stored: {
    filters: [
      dateFilter("tradeTime", "交易时间"),
      selectFilter("tradeType", "交易类型", ["全部", "充值", "充值退款", "消费", "消费退款"]),
    ],
    search: "输入会员卡号/患者姓名",
    columns: storedColumns,
    rows: storedRows,
    tableClass: "table-stored",
    fullPager: true,
  },
});

const patientConfigs = reactive<Record<string, TabConfig>>({
  log: {
    filters: [
      dateFilter("visitTime", "就诊时间"),
      selectFilter("doctor", "接诊医生"),
      selectFilter("chargeStatus", "收费状态", ["全部", "待收费", "已收费", "已退费"]),
    ],
    search: "患者姓名/手机号码",
    columns: patientLogColumns,
    rows: patientRows,
    tableClass: "table-patient-log",
    fullPager: true,
  },
  age: {
    filters: [dateFilter("visitTime", "就诊时间")],
    barChart: { items: ageItems, max: 60 },
    columns: ageColumns,
    totalRow: [{ value: "" }, { value: "合计" }, { value: "260" }],
    rows: ageRows,
    tableClass: "table-age",
  },
  gender: {
    filters: [dateFilter("visitTime", "就诊时间")],
    donutChart: {
      items: [
        { label: "男", value: 120, color: colors.green, display: "男 120人次 46%" },
        { label: "女", value: 120, color: colors.red, display: "女 120人次 46%" },
        { label: "未知", value: 20, color: colors.yellow, display: "未知 20人次 8%" },
      ],
    },
    columns: rowColumns(["男", "女", "未知"]),
    rows: [{ id: "gender-row", index: 1, 男: "120（46%）", 女: "120（46%）", 未知: "20（8%）", total: 260 }],
    tableClass: "table-donut",
  },
  marriage: {
    filters: [dateFilter("visitTime", "就诊时间")],
    donutChart: {
      items: [
        { label: "已婚", value: 120, color: colors.green, display: "已婚 120人次 46%" },
        { label: "未婚", value: 120, color: colors.red, display: "未婚 120人次 46%" },
        { label: "未知", value: 20, color: colors.yellow, display: "未知 20人次 8%" },
      ],
    },
    columns: rowColumns(["未婚", "已婚", "未知"]),
    rows: [{ id: "marriage-row", index: 1, 未婚: "120（46%）", 已婚: "120（46%）", 未知: "20（8%）", total: 260 }],
    tableClass: "table-donut",
  },
  member: {
    filters: [],
    donutChart: {
      legendColumns: 2,
      items: [
        { label: "普通会员", value: 240, color: colors.red, display: "普通会员 240人次 40%" },
        { label: "初级会员", value: 30, color: colors.yellow, display: "初级会员 30人次 5%" },
        { label: "高级会员", value: 150, color: colors.blue, display: "高级会员 150人次 25%" },
        { label: "白银会员", value: 90, color: colors.indigo, display: "白银会员 90人次 15%" },
        { label: "黄金会员", value: 42, color: colors.gray, display: "黄金会员 42人次 7%" },
        { label: "钻石会员", value: 48, color: colors.green, display: "钻石会员 48人次 8%" },
      ],
    },
    columns: rowColumns(["普通会员", "初级会员", "高级会员", "白银会员", "黄金会员", "钻石会员"]),
    rows: [
      {
        id: "member-row",
        index: 1,
        普通会员: "240（40%）",
        初级会员: "30（5%）",
        高级会员: "150（25%）",
        白银会员: "90（15%）",
        黄金会员: "42（7%）",
        钻石会员: "48（8%）",
        total: 600,
      },
    ],
    tableClass: "table-donut table-member",
  },
  visit: {
    filters: [dateFilter("visitTime", "就诊时间")],
    donutChart: {
      items: [
        { label: "初诊", value: 180, color: colors.green, display: "初诊 180人次 60%" },
        { label: "复诊", value: 120, color: colors.red, display: "复诊 120人次 40%" },
      ],
    },
    columns: rowColumns(["初诊", "复诊"]),
    rows: [{ id: "visit-row", index: 1, 初诊: "180（60%）", 复诊: "120（40%）", total: 300 }],
    tableClass: "table-donut table-visit",
  },
  source: {
    filters: [dateFilter("visitTime", "就诊时间")],
    donutChart: {
      items: [
        { label: "朋友介绍", value: 120, color: colors.green, display: "朋友介绍 120人次 46%" },
        { label: "自来", value: 120, color: colors.red, display: "自来 120人次 46%" },
        { label: "广告", value: 20, color: colors.yellow, display: "广告 20人次 8%" },
      ],
    },
    columns: rowColumns(["朋友介绍", "广告", "自来"]),
    rows: [{ id: "source-row", index: 1, 朋友介绍: "120（46%）", 广告: "120（46%）", 自来: "20（8%）", total: 260 }],
    tableClass: "table-donut",
  },
});

const checkProjectRows = Array.from({ length: 10 }, (_, index) => ({
  id: `check-project-${index}`,
  index: index + 1,
  orderNo: String(201912090030 - index),
  itemType: ["煎药费", "拔牙", "手术费"][index % 3],
  itemCode: String(1000001 + index),
  itemName: ["脑电图", "血压测量", "超声检查", "针灸"][index % 4],
  invoiceType: ["注射费", "检查费", "材料费"][index % 3],
  quantity: 1,
  purchasePrice: "12.00",
  retailPrice: "15.00",
  purchaseTotal: "24.00",
  retailTotal: "30.00",
  profit: "6.00",
  orderDate: "2019-10-23<br>10:23:34",
}));

const checkProjectColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "orderNo", label: "订单编号", width: 176 },
  { key: "itemType", label: "项目类型", width: 124 },
  { key: "itemCode", label: "项目编码", width: 98 },
  { key: "itemName", label: "项目名称", width: 124 },
  { key: "invoiceType", label: "发票类型", width: 122 },
  { key: "quantity", label: "数量", width: 90 },
  { key: "purchasePrice", label: "采购单价<br>（元）", width: 118 },
  { key: "retailPrice", label: "零售单价<br>（元）", width: 118 },
  { key: "purchaseTotal", label: "采购总价<br>（元）", width: 118 },
  { key: "retailTotal", label: "零售总价<br>（元）", width: 118 },
  { key: "profit", label: "利润<br>（元）", width: 118 },
  { key: "orderDate", label: "订单日期", width: 170 },
];

const checkProjectConfigs = reactive<Record<string, TabConfig>>({
  detail: {
    filters: [dateFilter("orderTime", "订单时间")],
    search: "输入单号/项目名称",
    columns: checkProjectColumns,
    rows: checkProjectRows,
    tableClass: "table-check-project",
    totalRow: [
      { value: "合计", colspan: 9 },
      { value: "7200.00" },
      { value: "9000.00" },
      { value: "1800.00" },
      { value: "" },
    ],
    fullPager: true,
  },
});

const checkProjectTabs: Tab[] = [{ key: "detail", label: "" }];

const drugTabs: Tab[] = [
  { key: "stockIn", label: "入库统计" },
  { key: "stockOut", label: "出库统计" },
  { key: "inventory", label: "进销存统计" },
  { key: "price", label: "调价统计" },
  { key: "sale", label: "药品销售明细" },
  { key: "prescriptionRank", label: "处方药品排行" },
  { key: "retailRank", label: "零售药品排行" },
];

const drugNames = [
  "复方氯化钠注射液",
  "替硝唑氯化钠注射液",
  "0.9%氯化钠注射液",
  "克林霉素磷酸酯注射液",
  "生脉注射液",
  "抗骨增生片",
  "田七花叶颗粒",
  "兰索拉唑肠溶片",
  "甲钴胺片",
  "复方氨酚沙宗胶囊",
];
const drugOperators = ["王冕", "林鹤", "李忠云"];
const drugSuppliers = ["白云制药厂", "白云制药厂", "蓝天制药厂"];

const drugStockInRows = Array.from({ length: 10 }, (_, index) => ({
  id: `drug-stock-in-${index}`,
  index: index + 1,
  orderNo: `SP2019110908${18 + index}`,
  drugCode: String(1000001 + index),
  drugName: drugNames[index],
  spec: "6g*10袋/盒",
  manufacturer: "昆明制药集团",
  stockType: "采购入库",
  batchNo: "20191109897",
  supplier: drugSuppliers[index % drugSuppliers.length],
  purchasePrice: "20.00",
  quantity: "40盒",
  purchaseCost: "800.00",
  operator: drugOperators[index % drugOperators.length],
  orderDate: "2019-10-23",
  reviewer: drugOperators[index % drugOperators.length],
  reviewDate: "2019-10-23",
}));

const drugStockOutRows = Array.from({ length: 10 }, (_, index) => ({
  id: `drug-stock-out-${index}`,
  index: index + 1,
  orderNo: `CB2019111900${30 - index}`,
  drugCode: String(1000001 + index),
  drugName: drugNames[index],
  spec: "6g*10袋/盒",
  manufacturer: "昆明制药集团",
  stockType: ["科室领药", "报损出库", "采购退货"][index % 3],
  batchNo: "20191109897",
  supplier: "白云制药厂",
  purchasePrice: "10.00",
  quantity: "50盒",
  refundAmount: "500.00",
  operator: drugOperators[index % drugOperators.length],
  orderDate: "2019-10-23",
  reviewer: drugOperators[index % drugOperators.length],
  reviewDate: "2019-10-23",
}));

const drugInventoryRows = Array.from({ length: 10 }, (_, index) => ({
  id: `drug-inventory-${index}`,
  index: index + 1,
  drugCode: String(1000001 + index),
  drugName: drugNames[index],
  spec: "6g*10袋/盒",
  supplier: "白云制药厂",
  manufacturer: "昆明制药集团",
  batchNo: String(201912090030 - index),
  initialPrice: "0.00",
  initialQty: "0盒",
  initialAmount: "0.00",
  increasePrice: "20.00",
  increaseQty: "40盒",
  increaseAmount: "800.00",
  decreasePrice: "-10.00",
  decreaseQty: "20盒",
  decreaseAmount: "200.00",
  endPrice: "20.00",
  endQty: "20盒",
  endAmount: "800.00",
}));

const drugPriceRows = Array.from({ length: 10 }, (_, index) => ({
  id: `drug-price-${index}`,
  index: index + 1,
  orderNo: `SP2019110908${18 + index}`,
  drugCode: String(1000001 + index),
  drugName: drugNames[index],
  spec: "6g*10袋/盒",
  manufacturer: "昆明制药集团",
  batchNo: String(201912090030 - index),
  stock: "40盒",
  purchasePrice: "10.00",
  oldRetailPrice: "12.00",
  newRetailPrice: "15.00",
  diffPrice: "3.00",
  ratio: "25%",
  orderDate: "2019-10-23",
  operator: drugOperators[index % drugOperators.length],
}));

const drugSaleRows = Array.from({ length: 10 }, (_, index) => ({
  id: `drug-sale-${index}`,
  index: index + 1,
  orderNo: String(201912090030 - index),
  orderType: ["处方开立", "药品零售"][index % 2],
  drugCode: String(1000001 + index),
  drugName: drugNames[index],
  manufacturer: "白云制药厂",
  spec: "6g*10袋/盒",
  quantity: "2盒",
  purchasePrice: "12.00",
  retailPrice: "15.00",
  purchaseTotal: "24.00",
  retailTotal: "30.00",
  profit: "6.00",
  orderDate: "2019-10-23<br>10:23:34",
}));

const drugRankRows = Array.from({ length: 10 }, (_, index) => ({
  id: `drug-rank-${index}`,
  index: index + 1,
  drugCategory: "西/成药",
  drugCode: String(1000001 + index),
  drugName: drugNames[index],
  manufacturer: "白云制药厂",
  spec: "6g*10袋/盒",
  quantity: "2盒",
  amount: "12.00",
}));

const drugStockInColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "orderNo", label: "单号", width: 150 },
  { key: "drugCode", label: "药品编码", width: 82 },
  { key: "drugName", label: "药品名称", width: 104 },
  { key: "spec", label: "规格", width: 110 },
  { key: "manufacturer", label: "生产厂商", width: 124 },
  { key: "stockType", label: "入库类型", width: 108 },
  { key: "batchNo", label: "批号", width: 132 },
  { key: "supplier", label: "供应商", width: 104 },
  { key: "purchasePrice", label: "采购价<br>(元)", width: 64 },
  { key: "quantity", label: "数量", width: 88 },
  { key: "purchaseCost", label: "采购成本<br>(元)", width: 96 },
  { key: "operator", label: "入库人员", width: 78 },
  { key: "orderDate", label: "入库日期", width: 96 },
  { key: "reviewer", label: "审核人员", width: 74 },
  { key: "reviewDate", label: "审核日期", width: 104 },
];

const drugStockOutColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "orderNo", label: "单号", width: 144 },
  { key: "drugCode", label: "药品编码", width: 80 },
  { key: "drugName", label: "药品名称", width: 102 },
  { key: "spec", label: "规格", width: 106 },
  { key: "manufacturer", label: "生产厂商", width: 120 },
  { key: "stockType", label: "出库类型", width: 106 },
  { key: "batchNo", label: "批号", width: 128 },
  { key: "supplier", label: "供应商", width: 102 },
  { key: "purchasePrice", label: "采购价<br>(元)", width: 62 },
  { key: "quantity", label: "数量", width: 86 },
  { key: "refundAmount", label: "退货金额<br>(元)", width: 92 },
  { key: "operator", label: "出库人员", width: 76 },
  { key: "orderDate", label: "出库日期", width: 94 },
  { key: "reviewer", label: "审核人员", width: 72 },
  { key: "reviewDate", label: "审核日期", width: 102 },
];

const drugInventoryColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "drugCode", label: "药品编码", width: 80 },
  { key: "drugName", label: "药品名称", width: 104 },
  { key: "spec", label: "规格", width: 110 },
  { key: "supplier", label: "供应商", width: 120 },
  { key: "manufacturer", label: "生产厂商", width: 136 },
  { key: "batchNo", label: "批号", width: 158 },
  { key: "initialPrice", label: "期初库存<br>采购价", width: 62 },
  { key: "initialQty", label: "期初库存<br>数量", width: 60 },
  { key: "initialAmount", label: "期初库存<br>采购金额", width: 74 },
  { key: "increasePrice", label: "库存增加<br>采购价", width: 62 },
  { key: "increaseQty", label: "库存增加<br>数量", width: 60 },
  { key: "increaseAmount", label: "库存增加<br>采购金额", width: 74 },
  { key: "decreasePrice", label: "库存减少<br>采购价", width: 62 },
  { key: "decreaseQty", label: "库存减少<br>数量", width: 60 },
  { key: "decreaseAmount", label: "库存减少<br>采购金额", width: 74 },
  { key: "endPrice", label: "期末库存<br>采购价", width: 62 },
  { key: "endQty", label: "期末库存<br>数量", width: 60 },
  { key: "endAmount", label: "期末库存<br>采购金额", width: 74 },
];

const drugPriceColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 54 },
  { key: "orderNo", label: "入库单号", width: 150 },
  { key: "drugCode", label: "药品编码", width: 82 },
  { key: "drugName", label: "药品名称", width: 140 },
  { key: "spec", label: "规格", width: 100 },
  { key: "manufacturer", label: "生产厂商", width: 124 },
  { key: "batchNo", label: "批号", width: 130 },
  { key: "stock", label: "库存", width: 76 },
  { key: "purchasePrice", label: "采购价<br>(元)", width: 98 },
  { key: "oldRetailPrice", label: "原零售价<br>(元)", width: 98 },
  { key: "newRetailPrice", label: "现零售价<br>(元)", width: 98 },
  { key: "diffPrice", label: "差价<br>(元)", width: 98 },
  { key: "ratio", label: "比例", width: 96 },
  { key: "orderDate", label: "操作日期", width: 130 },
  { key: "operator", label: "操作人员", width: 104 },
];

const drugSaleColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 56 },
  { key: "orderNo", label: "订单编号", width: 152 },
  { key: "orderType", label: "订单类型", width: 108 },
  { key: "drugCode", label: "药品编码", width: 82 },
  { key: "drugName", label: "药品名称", width: 108 },
  { key: "manufacturer", label: "生产厂商", width: 106 },
  { key: "spec", label: "规格", width: 114 },
  { key: "quantity", label: "数量", width: 90 },
  { key: "purchasePrice", label: "采购单价<br>(元)", width: 122 },
  { key: "retailPrice", label: "零售单价<br>(元)", width: 122 },
  { key: "purchaseTotal", label: "采购总价<br>(元)", width: 120 },
  { key: "retailTotal", label: "零售总价<br>(元)", width: 120 },
  { key: "profit", label: "利润<br>(元)", width: 120 },
  { key: "orderDate", label: "订单日期", width: 176 },
];

const drugRankColumns: ColumnConfig[] = [
  { key: "index", label: "序号", width: 56 },
  { key: "drugCategory", label: "药品类别", width: 226 },
  { key: "drugCode", label: "药品编码", width: 174 },
  { key: "drugName", label: "药品名称", width: 226 },
  { key: "manufacturer", label: "生产厂商", width: 220 },
  { key: "spec", label: "规格", width: 240 },
  { key: "quantity", label: "数量", width: 190 },
  { key: "amount", label: "金额（元）", width: 258 },
];

const drugConfigs = reactive<Record<string, TabConfig>>({
  stockIn: {
    filters: [dateFilter("stockInTime", "入库时间"), selectFilter("stockInType", "入库类型")],
    search: "输入入库单号/供应商/药品名称",
    columns: drugStockInColumns,
    rows: drugStockInRows,
    tableClass: "table-drug-stock table-drug-stock-in",
    totalRow: [{ value: "合计", colspan: 10 }, { value: "1200盒" }, { value: "24000.00" }, { value: "" }, { value: "" }, { value: "" }, { value: "" }],
    fullPager: true,
  },
  stockOut: {
    filters: [dateFilter("stockOutTime", "出库时间"), selectFilter("stockOutType", "出库类型")],
    search: "输入出库单号/药品名称",
    columns: drugStockOutColumns,
    rows: drugStockOutRows,
    tableClass: "table-drug-stock table-drug-stock-out",
    totalRow: [{ value: "合计", colspan: 10 }, { value: "1500盒" }, { value: "15000.00" }, { value: "" }, { value: "" }, { value: "" }, { value: "" }],
    fullPager: true,
  },
  inventory: {
    filters: [dateFilter("lastOperateTime", "最后操作时间")],
    search: "输入药品名称/批号/供应商",
    columns: drugInventoryColumns,
    rows: drugInventoryRows,
    tableClass: "table-drug-inventory",
    totalRow: [
      { value: "合计", colspan: 9 },
      { value: "0.00" },
      { value: "24000.00", colspan: 3 },
      { value: "-4000.00", colspan: 3 },
      { value: "20000.00", colspan: 3 },
    ],
    fullPager: true,
  },
  price: {
    filters: [dateFilter("operateTime", "操作时间")],
    search: "输入药品名称/批号/入库单号",
    columns: drugPriceColumns,
    rows: drugPriceRows,
    tableClass: "table-drug-price",
    fullPager: true,
  },
  sale: {
    filters: [dateFilter("orderTime", "订单时间"), selectFilter("orderType", "订单类型", ["全部", "处方开立", "药品零售"])],
    search: "输入单号/药品名称",
    columns: drugSaleColumns,
    rows: drugSaleRows,
    tableClass: "table-drug-sale",
    totalRow: [{ value: "合计", colspan: 10 }, { value: "7200.00" }, { value: "9000.00" }, { value: "1800.00" }, { value: "" }],
    fullPager: true,
  },
  prescriptionRank: {
    filters: [dateFilter("orderTime", "订单时间"), selectFilter("prescriptionType", "处方类别")],
    search: "药品名称",
    columns: drugRankColumns,
    rows: drugRankRows,
    tableClass: "table-drug-rank",
    fullPager: true,
  },
  retailRank: {
    filters: [dateFilter("orderTime", "订单时间"), selectFilter("prescriptionType", "处方类别")],
    search: "药品名称",
    columns: drugRankColumns,
    rows: drugRankRows,
    tableClass: "table-drug-rank",
    fullPager: true,
  },
});

const isPatientPage = computed(() => route.path.startsWith("/statistics/patients"));
const isCheckProjectPage = computed(() => route.path.startsWith("/statistics/check-projects"));
const isDrugPage = computed(() => route.path.startsWith("/statistics/drugs"));
const isChargePage = computed(() => route.path.startsWith("/statistics/charge"));
const pageTitle = computed(() => {
  if (isPatientPage.value) return "患者统计";
  if (isCheckProjectPage.value) return "检查项目统计";
  if (isDrugPage.value) return "药品统计";
  return "收费统计";
});
const currentTabs = computed(() => {
  if (isPatientPage.value) return patientTabs;
  if (isCheckProjectPage.value) return checkProjectTabs;
  if (isDrugPage.value) return drugTabs;
  return chargeTabs;
});
const currentConfigs = computed(() => {
  if (isPatientPage.value) return patientConfigs;
  if (isCheckProjectPage.value) return checkProjectConfigs;
  if (isDrugPage.value) return drugConfigs;
  return chargeConfigs;
});
const defaultTab = computed(() => currentTabs.value[0].key);
const activeTab = ref(defaultTab.value);
const remoteRows = ref<TableRow[]>([]);
const searchKeyword = ref("");
const currentPage = ref(1);
const pageSize = 10;
const baseConfig = computed(
  () => currentConfigs.value[activeTab.value] ?? currentConfigs.value[defaultTab.value],
);

const numberValue = (value: unknown) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

const money = (value: unknown) => numberValue(value).toFixed(2);

const textValue = (...values: unknown[]) => {
  for (const value of values) {
    if (value !== undefined && value !== null && String(value) !== "") return String(value);
  }
  return "";
};

const formatDateText = (value: unknown) => {
  const text = textValue(value);
  if (!text) return "";
  return text.replace("T", " ").slice(0, 19);
};

const quantityText = (value: unknown, unit?: unknown) => {
  const count = numberValue(value);
  const unitText = textValue(unit);
  return `${count}${unitText}`;
};

const selectedFilter = (key: string) => {
  const value = baseConfig.value.filters.find((filter) => filter.key === key)?.value;
  return Array.isArray(value) ? "" : String(value || "");
};

const setFilterOptions = (key: string, options: string[]) => {
  const filter = baseConfig.value.filters.find((item) => item.key === key);
  if (!filter) return;
  const currentValue = String(filter.value || "");
  const uniqueOptions = ["全部", ...Array.from(new Set([currentValue, ...options].filter((item) => item && item !== "全部")))];
  filter.options = uniqueOptions;
  if (!uniqueOptions.includes(currentValue)) {
    filter.value = "全部";
  }
};

const filteredRows = computed(() => {
  return remoteRows.value;
});

const sumRows = (key: string) =>
  filteredRows.value.reduce((sum, row) => sum + numberValue(row[key]), 0);

const chargeSummaryCards = computed<SummaryCard[] | undefined>(() => {
  if (!isChargePage.value || activeTab.value !== "detail") return undefined;
  return [
    { label: "合计收入（元）", value: money(sumRows("received")), accent: true },
    { label: "应收合计（元）", value: money(sumRows("receivable")) },
    { label: "优惠合计（元）", value: money(sumRows("discount")) },
    { label: "实收合计（元）", value: money(sumRows("received")) },
    { label: "医保（元）", value: money(sumRows("medical")) },
    { label: "会员（元）", value: money(sumRows("memberCard")) },
    { label: "现金（元）", value: money(sumRows("cash")) },
    { label: "支付宝（元）", value: money(sumRows("alipay")) },
    { label: "微信（元）", value: money(sumRows("wechat")) },
    { label: "银行卡（元）", value: money(sumRows("bank")) },
  ];
});

const chargeTotalRow = computed<TotalCell[] | undefined>(() => {
  if (!isChargePage.value || !["cashier", "department", "doctor"].includes(activeTab.value)) {
    return undefined;
  }
  if (activeTab.value === "cashier") {
    return [
      { value: "合计", colspan: 3 },
      { value: String(sumRows("chargeCount")) },
      { value: String(sumRows("refundCount")) },
      ...["receivable", "discount", "received", "medical", "memberCard", "cash", "alipay", "wechat", "bank"]
        .map((key) => ({ value: money(sumRows(key)) })),
    ];
  }
  if (activeTab.value === "department") {
    return [
      { value: "合计", colspan: 2 },
      { value: String(sumRows("visits")) },
      ...["received", "medical", "memberCard", "cash", "alipay", "wechat", "bank"]
        .map((key) => ({ value: money(sumRows(key)) })),
    ];
  }
  return [
    { value: "合计", colspan: 2 },
    ...["receivable", "discount", "received"].map((key) => ({ value: money(sumRows(key)) })),
    ...["visits", "firstVisits", "returnVisits"].map((key) => ({ value: String(sumRows(key)) })),
    ...["westernDrug", "chineseDrug", "checkProject", "registration", "diagnosis"]
      .map((key) => ({ value: money(sumRows(key)) })),
  ];
});

const reportTotalRow = computed<TotalCell[] | undefined>(() => {
  if (isCheckProjectPage.value) {
    return [
      { value: "合计", colspan: 9 },
      { value: money(sumRows("purchaseTotal")) },
      { value: money(sumRows("retailTotal")) },
      { value: money(sumRows("profit")) },
      { value: "" },
    ];
  }
  if (!isDrugPage.value) return undefined;
  if (activeTab.value === "stockIn") {
    return [
      { value: "合计", colspan: 10 },
      { value: quantityText(sumRows("quantity"), remoteRows.value[0]?.unit) },
      { value: money(sumRows("purchaseCost")) },
      { value: "" },
      { value: "" },
      { value: "" },
      { value: "" },
    ];
  }
  if (activeTab.value === "stockOut") {
    return [
      { value: "合计", colspan: 10 },
      { value: quantityText(sumRows("quantity"), remoteRows.value[0]?.unit) },
      { value: money(sumRows("refundAmount")) },
      { value: "" },
      { value: "" },
      { value: "" },
      { value: "" },
    ];
  }
  if (activeTab.value === "inventory") {
    return [
      { value: "合计", colspan: 9 },
      { value: money(sumRows("initialAmount")) },
      { value: money(sumRows("increaseAmount")), colspan: 3 },
      { value: money(sumRows("decreaseAmount")), colspan: 3 },
      { value: money(sumRows("endAmount")), colspan: 3 },
    ];
  }
  if (activeTab.value === "sale") {
    return [
      { value: "合计", colspan: 10 },
      { value: money(sumRows("purchaseTotal")) },
      { value: money(sumRows("retailTotal")) },
      { value: money(sumRows("profit")) },
      { value: "" },
    ];
  }
  return undefined;
});

const normalizedAnalysisItems = () => {
  const preset = patientAnalysisLabels[activeTab.value];
  const valueMap = new Map<string, number>();
  remoteRows.value.forEach((item: any) => {
    const label = String(item.name || item.label || "").trim() || "未知";
    valueMap.set(label, (valueMap.get(label) || 0) + Number(item.value || 0));
  });
  const labels = preset?.length ? preset : Array.from(valueMap.keys());
  return labels.map((label) => ({
    label,
    value: valueMap.get(label) || 0,
  }));
};

const activeConfig = computed(() => {
  const base = baseConfig.value;
  const dynamic = {
    ...base,
    rows: remoteRows.value,
    summaryCards: chargeSummaryCards.value,
    totalRow: reportTotalRow.value ?? chargeTotalRow.value ?? base.totalRow,
    fullPager: isChargePage.value && activeTab.value !== "stored" ? true : base.fullPager,
  };
  if (base.barChart) {
    const items = normalizedAnalysisItems();
    return {
      ...dynamic,
      rows: items.map((item, index) => ({
        id: `bar-${index}`,
        index: index + 1,
        segment: item.label,
        count: item.value,
      })),
      totalRow: [{ value: "" }, { value: "合计" }, { value: String(items.reduce((sum, item) => sum + item.value, 0)) }],
      barChart: {
        ...base.barChart,
        items,
        max: Math.max(10, ...items.map((item) => item.value)),
      },
    };
  }
  if (base.donutChart) {
    const colors = ["#6574f7", "#ff7d87", "#31c58d", "#f7b500", "#8f7cf6", "#41b6e6"];
    const items = normalizedAnalysisItems();
    const total = items.reduce((sum, item) => sum + item.value, 0);
    const row: TableRow = { id: `donut-${activeTab.value}`, index: 1, total };
    items.forEach((item) => {
      row[item.label] = `${item.value}（${total ? Math.round((item.value / total) * 100) : 0}%）`;
    });
    return {
      ...dynamic,
      rows: [row],
      donutChart: {
        ...base.donutChart,
        items: items.map((item, index) => {
          return {
            label: item.label,
            value: item.value,
            display: total ? `${Math.round((item.value / total) * 100)}%` : "0%",
            color: colors[index % colors.length],
          };
        }),
      },
    };
  }
  return dynamic;
});

const tableRows = computed(() => activeConfig.value.rows || filteredRows.value);
const totalPages = computed(() =>
  Math.max(1, Math.ceil(tableRows.value.length / pageSize)),
);
const displayedRows = computed(() => {
  if (!activeConfig.value.fullPager) return tableRows.value;
  const start = (currentPage.value - 1) * pageSize;
  return tableRows.value.slice(start, start + pageSize);
});
const visiblePages = computed(() =>
  Array.from({ length: totalPages.value }, (_, index) => index + 1),
);

const changePage = (page: number) => {
  currentPage.value = Math.min(Math.max(1, page), totalPages.value);
};

const applySearch = () => {
  currentPage.value = 1;
  if (activeConfig.value.search) {
    loadStatistics();
  }
};

const handleFilterChange = () => {
  currentPage.value = 1;
  loadStatistics();
};

const statisticsRouteName = computed(() => {
  if (isPatientPage.value) return "StatisticsPatient";
  if (isDrugPage.value) return "StatisticsDrug";
  if (isCheckProjectPage.value) return "StatisticsCheckProject";
  return "StatisticsCharge";
});

const selectTab = (tab: string) => {
  activeTab.value = tab;
  currentPage.value = 1;
  searchKeyword.value = "";
  if (!isCheckProjectPage.value) {
    router.replace({ name: statisticsRouteName.value, params: { type: tab } });
  }
};

const openChargeDetail = (row: TableRow) => {
  if (!route.path.startsWith("/statistics/charge") || activeTab.value !== "detail") {
    return;
  }

  router.push({
    name: "StatisticsChargeDetail",
    query: { id: String(row.id || "") },
    state: { id: String(row.id || "") },
  });
};

const exportStatistics = () => {
  const columns = activeConfig.value.columns.filter((column) => column.key !== "action");
  const escapeCsv = (value: unknown) => `"${String(value ?? "").replace(/"/g, '""')}"`;
  const lines = [
    columns.map((column) => escapeCsv(column.label.replace(/<br>/g, ""))).join(","),
    ...tableRows.value.map((row) =>
      columns.map((column) => escapeCsv(row[column.key])).join(","),
    ),
  ];
  const blob = new Blob([`\uFEFF${lines.join("\r\n")}`], {
    type: "text/csv;charset=utf-8",
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `${pageTitle.value}-${currentTabs.value.find((tab) => tab.key === activeTab.value)?.label || ""}.csv`;
  link.click();
  URL.revokeObjectURL(url);
};

const barOption = (items: BarItem[], max: number) => ({
  animation: true,
  animationDuration: 1100,
  animationEasing: "cubicOut",
  grid: { left: 58, right: 20, top: 64, bottom: 40 },
  tooltip: { trigger: "axis" },
  xAxis: {
    type: "category",
    data: items.map((item) => item.label),
    axisTick: { show: false },
    axisLine: { lineStyle: { color: "#eeeeee" } },
    axisLabel: { color: "#000", fontSize: 12 },
  },
  yAxis: {
    name: "人次",
    nameTextStyle: { color: "#000", fontSize: 14, align: "right", padding: [0, 34, 10, 0] },
    type: "value",
    min: 0,
    max,
    interval: 10,
    axisLine: { show: true, lineStyle: { color: "#eeeeee" } },
    axisTick: { show: false },
    axisLabel: { color: "#000", fontSize: 12 },
    splitLine: { lineStyle: { color: "#eeeeee" } },
  },
  series: [
    {
      type: "bar",
      data: items.map((item) => item.value),
      barWidth: 80,
      itemStyle: { color: "#666ee8", borderRadius: [4, 4, 0, 0] },
    },
  ],
});

const donutOption = (items: DonutItem[], legendColumns?: number) => ({
  animation: true,
  animationType: "scale",
  animationDuration: 1000,
  animationEasing: "cubicOut",
  tooltip: { trigger: "item" },
  legend: {
    orient: legendColumns === 2 ? "horizontal" : "vertical",
    right: legendColumns === 2 ? 260 : 400,
    top: legendColumns === 2 ? 150 : 135,
    itemWidth: 16,
    itemHeight: 16,
    itemGap: legendColumns === 2 ? 34 : 42,
    width: legendColumns === 2 ? 310 : undefined,
    textStyle: { color: "#111722", fontSize: 20 },
    data: items.map((item) => item.label),
  },
  series: [
    {
      type: "pie",
      radius: ["33%", "48%"],
      center: ["46%", "50%"],
      startAngle: 120,
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: ({ data }: { data: DonutItem }) => data.display,
        color: "inherit",
        fontSize: 16,
      },
      labelLine: { show: true, length: 28, length2: 90 },
      data: items.map((item) => ({
        name: item.label,
        value: item.value,
        display: item.display,
        itemStyle: { color: item.color },
        label: { color: item.color },
        labelLine: { lineStyle: { color: item.color } },
      })),
    },
  ],
});

const disposeCharts = () => {
  barChart?.dispose();
  donutChart?.dispose();
  barChart = null;
  donutChart = null;
};

const renderPatientChart = async () => {
  await nextTick();
  if (!isPatientPage.value) {
    disposeCharts();
    return;
  }

  if (activeConfig.value.barChart && barChartRef.value) {
    donutChart?.dispose();
    donutChart = null;
    barChart?.dispose();
    barChart = init(barChartRef.value);
    barChart.setOption(barOption(activeConfig.value.barChart.items, activeConfig.value.barChart.max), true);
  } else if (activeConfig.value.donutChart && donutChartRef.value) {
    barChart?.dispose();
    barChart = null;
    donutChart?.dispose();
    donutChart = init(donutChartRef.value);
    donutChart.setOption(donutOption(activeConfig.value.donutChart.items, activeConfig.value.donutChart.legendColumns), true);
  } else {
    disposeCharts();
  }
};

let doctorOptionsLoaded = false;

async function loadDoctorOptions() {
  if (doctorOptionsLoaded) return;
  doctorOptionsLoaded = true;
  try {
    const response: any = await staffApi.page({ page: 1, size: 500 });
    const records = response?.data?.records || response?.data || [];
    const doctors = records
      .filter((item: any) => {
        const roleText = `${item.roleName || item.role || item.position || item.jobTitle || item.departmentName || ""}`;
        return roleText.includes("医生") || roleText.includes("医师") || roleText.includes("全科") || roleText === "";
      })
      .map((item: any) => item.name || item.realName || item.employeeName || item.username || "")
      .filter(Boolean);
    const doctorFilter = patientConfigs.log.filters.find((filter) => filter.key === "doctor");
    if (doctorFilter) doctorFilter.options = ["全部", ...Array.from(new Set(doctors))];
  } catch {
    doctorOptionsLoaded = false;
  }
}

const appendSelectedParams = (params: Record<string, string>) => {
  ["orderType", "department", "cashier", "doctor", "tradeType", "chargeStatus", "stockInType", "stockOutType"].forEach((key) => {
    const value = selectedFilter(key);
    if (value && value !== "全部") params[key] = value;
  });
  if (searchKeyword.value.trim()) {
    params.keyword = searchKeyword.value.trim();
  }
};


async function loadStatistics() {
  try {
    if (isPatientPage.value && activeTab.value === "log") {
      await loadDoctorOptions();
    }
    let response: any;
    const dateRange = baseConfig.value.filters.find((filter) => filter.type === "date")?.value;
    const params: Record<string, string> = Array.isArray(dateRange) && dateRange.length === 2
      ? { startDate: dateRange[0], endDate: dateRange[1] }
      : {};
    appendSelectedParams(params);
    if (isPatientPage.value) response = await statisticsApi.patients(activeTab.value, params);
    else if (isCheckProjectPage.value) response = await statisticsApi.checkProjects(params);
    else if (isDrugPage.value) response = await statisticsApi.drugs(activeTab.value, params);
    else response = await statisticsApi.charge(activeTab.value, params);
    const data = response?.data;
    const records = Array.isArray(data) ? data : data?.records || data?.rows || [];
    remoteRows.value = records.map((item: any, index: number) => {
    const common = { id: item.id || `${activeTab.value}-${index}`, index: index + 1, ...item };
    if (isPatientPage.value && activeTab.value === "log") {
      const rawStatus = String(item.chargeStatus || item.status || "").toUpperCase();
      return {
        ...common,
        patientNo: item.patientId || "",
        department: item.departmentName || "",
        doctor: item.doctorName || "",
        chargeStatus: statusTextMap[rawStatus] || item.chargeStatus || item.status || "",
      };
    }
    if (!isPatientPage.value && !isDrugPage.value && !isCheckProjectPage.value && activeTab.value === "detail") {
      const paidAmount = numberValue(item.paidAmount);
      const refundAmount = numberValue(item.refundAmount);
      const received = paidAmount - refundAmount;
      const rawChargeType = String(item.chargeType || "");
      const upperChargeType = rawChargeType.toUpperCase();
      const orderType = rawChargeType.includes("挂号") || upperChargeType.includes("REGISTRATION")
        ? "挂号费"
        : rawChargeType.includes("药品零售") || upperChargeType.includes("DRUG_RETAIL") || upperChargeType.includes("RETAIL")
          ? "药品零售"
          : rawChargeType.includes("门诊") || rawChargeType.includes("处方") || upperChargeType.includes("PRESCRIPTION") || upperChargeType.includes("OUTPATIENT")
            ? "处方开立"
            : rawChargeType;
      const paymentAmount = (method: string) =>
        (item.payMethod === method ? paidAmount : 0)
        - (item.refundMethod === method ? refundAmount : 0);
      const statusLabels: Record<string, string> = {
        PAID: "已收费",
        REFUNDED: "已退费",
        PENDING: "待收费",
        PARTIAL: "部分收费",
        CANCELLED: "已取消",
      };
      return {
        ...common,
        status: statusLabels[String(item.status || "").toUpperCase()] || item.status || "",
        orderType,
        department: item.departmentName || "",
        doctor: item.doctorName || "",
        receivable: money(item.receivableAmount),
        discount: money(item.discountAmount),
        received: money(received),
        medical: money(paymentAmount("医保")),
        memberCard: money(paymentAmount("会员卡")),
        cash: money(paymentAmount("现金")),
        alipay: money(paymentAmount("支付宝")),
        wechat: money(paymentAmount("微信")),
        bank: money(paymentAmount("银行卡")),
        chargeTime: item.paidAt || item.refundedAt || "",
        operator: item.cashier || item.operator || "",
        action: "查看详情",
      };
    }
    if (isChargePage.value && activeTab.value === "cashier") {
      return {
        ...common,
        receivable: money(item.receivable),
        discount: money(item.discount),
        received: money(item.received),
        medical: money(item.medical),
        memberCard: money(item.memberCard),
        cash: money(item.cash),
        alipay: money(item.alipay),
        wechat: money(item.wechat),
        bank: money(item.bank),
      };
    }
    if (isChargePage.value && activeTab.value === "department") {
      return {
        ...common,
        received: money(item.received),
        medical: money(item.medical),
        memberCard: money(item.memberCard),
        cash: money(item.cash),
        alipay: money(item.alipay),
        wechat: money(item.wechat),
        bank: money(item.bank),
      };
    }
    if (isChargePage.value && activeTab.value === "doctor") {
      return {
        ...common,
        receivable: money(item.receivable),
        discount: money(item.discount),
        received: money(item.received),
        westernDrug: money(item.westernDrug),
        chineseDrug: money(item.chineseDrug),
        checkProject: money(item.checkProject),
        registration: money(item.registration),
        diagnosis: money(item.diagnosis),
      };
    }
    if (isChargePage.value && activeTab.value === "stored") {
      const rawTradeType = String(item.tradeType || "").toUpperCase();
      return {
        ...common,
        tradeType: tradeTypeTextMap[rawTradeType] || item.tradeType || "",
        amount: money(item.amount),
        gift: money(item.gift),
        total: money(item.total),
        balance: money(item.balance),
      };
    }
    if (isCheckProjectPage.value) {
      const quantity = numberValue(item.quantity || 1) || 1;
      const retailPrice = numberValue(item.price || item.retailPrice || item.retailAmount);
      const purchasePrice = numberValue(item.purchasePrice || item.purchaseAmount);
      const purchaseTotal = purchasePrice * quantity;
      const retailTotal = retailPrice * quantity;
      return {
        ...common,
        orderNo: item.orderNo || `CHK${item.id || index + 1}`,
        itemCode: item.projectCode || item.itemCode || "",
        itemName: item.name || item.itemName || "",
        itemType: item.category || item.itemType || "",
        invoiceType: item.invoiceType || item.category || "",
        quantity,
        purchasePrice: money(purchasePrice),
        retailPrice: money(retailPrice),
        purchaseTotal: money(purchaseTotal),
        retailTotal: money(retailTotal),
        profit: money(retailTotal - purchaseTotal),
        orderDate: formatDateText(item.orderDate || item.createdAt),
      };
    }
    if (isDrugPage.value) {
      const quantity = Math.abs(numberValue(item.quantity || item.changeQuantity));
      const purchasePrice = numberValue(item.purchasePrice || item.newPurchasePrice || item.purchaseUnitPrice);
      const retailPrice = numberValue(item.retailPrice || item.newSellPrice || item.sellPrice || item.retailUnitPrice);
      const purchaseTotal = numberValue(item.purchaseTotal || item.purchaseCost || item.purchaseAmount || purchasePrice * quantity);
      const retailTotal = numberValue(item.retailTotal || item.retailAmount || item.amount || retailPrice * quantity);
      if (activeTab.value === "stockIn") {
        return {
          ...common,
          drugName: item.drugName || item.name || "",
          spec: item.specification || item.spec || "",
          manufacturer: item.manufacturer || "",
          stockType: item.stockType || "",
          batchNo: item.batchNo || "",
          supplier: item.supplierName || item.supplier || "",
          purchasePrice: money(purchasePrice),
          quantity,
          unit: item.unit || "",
          purchaseCost: money(purchaseTotal),
          operator: item.operator || item.maker || "",
          orderDate: formatDateText(item.createdAt || item.orderDate),
          reviewer: item.reviewer || item.auditor || item.maker || "",
          reviewDate: formatDateText(item.auditAt || item.reviewDate),
        };
      }
      if (activeTab.value === "stockOut") {
        return {
          ...common,
          drugName: item.drugName || item.name || "",
          spec: item.specification || item.spec || "",
          manufacturer: item.manufacturer || "",
          stockType: item.stockType || "",
          batchNo: item.batchNo || "",
          supplier: item.supplierName || item.supplier || "",
          purchasePrice: money(purchasePrice),
          quantity,
          unit: item.unit || "",
          refundAmount: money(purchaseTotal),
          operator: item.operator || item.maker || "",
          orderDate: formatDateText(item.createdAt || item.orderDate),
          reviewer: item.reviewer || item.auditor || item.maker || "",
          reviewDate: formatDateText(item.auditAt || item.reviewDate),
        };
      }
      if (activeTab.value === "inventory") {
        return {
          ...common,
          drugName: item.drugName || item.name || "",
          spec: item.specification || item.spec || "",
          supplier: item.supplierName || item.supplier || "",
          manufacturer: item.manufacturer || "",
          batchNo: item.batchNo || "",
          initialPrice: money(item.initialPrice),
          initialQty: quantityText(item.initialQty, item.unit),
          initialAmount: money(item.initialAmount),
          increasePrice: money(item.increasePrice),
          increaseQty: quantityText(item.increaseQty, item.unit),
          increaseAmount: money(item.increaseAmount),
          decreasePrice: money(item.decreasePrice),
          decreaseQty: quantityText(item.decreaseQty, item.unit),
          decreaseAmount: money(item.decreaseAmount),
          endPrice: money(item.endPrice || purchasePrice),
          endQty: quantityText(item.endQty ?? item.quantity, item.unit),
          endAmount: money(item.endAmount || item.purchaseAmount),
        };
      }
      if (activeTab.value === "price") {
        const oldRetailPrice = numberValue(item.oldSellPrice || item.oldRetailPrice);
        const newRetailPrice = numberValue(item.newSellPrice || item.newRetailPrice);
        const diffPrice = newRetailPrice - oldRetailPrice;
        return {
          ...common,
          orderNo: item.orderNo || `ADJ${item.id || index + 1}`,
          drugName: item.drugName || item.name || "",
          spec: item.specification || item.spec || "",
          manufacturer: item.manufacturer || "",
          batchNo: item.batchNo || "",
          stock: quantityText(item.stock || item.quantity, item.unit),
          purchasePrice: money(item.newPurchasePrice || item.purchasePrice),
          oldRetailPrice: money(oldRetailPrice),
          newRetailPrice: money(newRetailPrice),
          diffPrice: money(diffPrice),
          ratio: oldRetailPrice ? `${Math.round((diffPrice / oldRetailPrice) * 100)}%` : "0%",
          orderDate: formatDateText(item.createdAt || item.orderDate),
          operator: item.operator || "",
        };
      }
      if (activeTab.value === "sale") {
        return {
          ...common,
          orderType: item.orderType || item.chargeType || "",
          drugName: item.drugName || item.name || "",
          manufacturer: item.manufacturer || "",
          spec: item.specification || item.spec || "",
          quantity,
          unit: item.unit || "",
          purchasePrice: money(purchasePrice),
          retailPrice: money(retailPrice),
          purchaseTotal: money(purchaseTotal),
          retailTotal: money(retailTotal),
          profit: money(retailTotal - purchaseTotal),
          orderDate: formatDateText(item.paidAt || item.createdAt || item.orderDate),
        };
      }
      return {
        ...common,
        spec: item.specification || "",
        supplier: item.supplierName || "",
        stockType: item.stockType || "",
        purchaseCost: item.purchaseAmount || 0,
        orderDate: item.createdAt || item.expireDate || "",
        reviewDate: item.auditAt || "",
        purchasePrice: item.newPurchasePrice || item.purchasePrice || 0,
        oldRetailPrice: item.oldSellPrice || 0,
        newRetailPrice: item.newSellPrice || 0,
        endQty: item.quantity || 0,
        endAmount: item.purchaseAmount || 0,
        amount: item.retailAmount || item.newSellPrice || 0,
      };
    }
    return common;
    });

    if (isChargePage.value) {
      setFilterOptions("department", remoteRows.value.map((row) => String(row.department || "")));
      setFilterOptions("cashier", remoteRows.value.map((row) => String(row.cashier || "")));
      setFilterOptions("doctor", remoteRows.value.map((row) => String(row.doctor || "")));
    }
  } catch {
    remoteRows.value = [];
  }
}

watch(
  () => route.fullPath,
  () => {
    const requestedTab = String(route.params.type || "");
    activeTab.value = currentConfigs.value[requestedTab] ? requestedTab : defaultTab.value;
    currentPage.value = 1;
    searchKeyword.value = "";
    loadStatistics();
  },
  { immediate: true },
);

watch([isPatientPage, activeTab, remoteRows], renderPatientChart, {
  immediate: true,
  flush: "post",
});

watch(searchKeyword, () => {
  currentPage.value = 1;
});

watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages;
});

onBeforeUnmount(() => {
  disposeCharts();
});
</script>

<style scoped>
.statistics-page {
  min-height: 100%;
  padding: 15px 0 58px;
  overflow-x: auto;
}

.statistics-card {
  width: 1564px;
  min-height: 688px;
  margin: 0 auto;
  padding: 31px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 8px rgba(20, 27, 52, 0.06);
}

.statistics-card.is-charge {
  width: calc(100vw - 150px);
  min-width: 1080px;
  max-width: 1640px;
  min-height: 820px;
  padding: 31px;
}

.statistics-card.is-patient {
  width: 1638px;
  min-height: 885px;
  padding: 31px;
}

.statistics-card.is-check-project {
  width: 1640px;
  min-height: 978px;
  padding: 31px;
}

.statistics-card.is-drug {
  width: 1640px;
  min-height: 1050px;
  padding: 31px;
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 46px;
  gap: 24px;
}

.is-check-project .page-toolbar {
  justify-content: flex-end;
}

.stat-tabs {
  display: inline-flex;
  height: 46px;
  overflow: hidden;
  border: 1px solid #c7c8cf;
  border-radius: 4px;
}

.stat-tab {
  min-width: 132px;
  height: 44px;
  padding: 0 18px;
  border: 0;
  border-right: 1px solid #c7c8cf;
  background: #fff;
  color: #b8b8b8;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0;
  cursor: pointer;
}

.stat-tab:nth-child(2) {
  min-width: 153px;
}

.is-patient .stat-tab {
  min-width: auto;
  padding: 0 18px;
}

.is-patient .stat-tab:nth-child(4) {
  padding: 0 20px;
}

.is-drug .stat-tab {
  min-width: 132px;
  padding: 0 18px;
}

.is-drug .stat-tab:nth-child(5),
.is-drug .stat-tab:nth-child(6),
.is-drug .stat-tab:nth-child(7) {
  min-width: 136px;
}

.stat-tab:last-child {
  border-right: 0;
}

.stat-tab.is-active {
  background: #636be8;
  color: #fff;
}

.export-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 141px;
  height: 46px;
  border: 1px solid #636be8;
  border-radius: 5px;
  background: #fff;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.btn-icon {
  width: 24px;
  height: 24px;
}

.page-divider {
  height: 1px;
  margin: 27px 0 30px;
  background: #eeeeee;
}

.is-patient .page-divider {
  margin-top: 25px;
  margin-bottom: 32px;
}

.is-check-project .page-divider {
  margin-top: 27px;
  margin-bottom: 30px;
}

.is-drug .page-divider {
  margin-top: 27px;
  margin-bottom: 31px;
}

.filters-row {
  display: flex;
  align-items: center;
  min-height: 46px;
  margin-bottom: 26px;
  gap: 64px;
}

.filters-cashier,
.filters-department,
.filters-doctor,
.filters-stored,
.is-patient .filters-row {
  gap: 36px;
}

.is-check-project .filters-row {
  margin-bottom: 32px;
  gap: 40px;
}

.is-drug .filters-row {
  margin-bottom: 32px;
  gap: 36px;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  gap: 15px;
  color: #111722;
  font-size: 16px;
  white-space: nowrap;
}

.date-control,
.filter-item select,
.search-filter {
  height: 44px;
  box-sizing: border-box;
  border: 1px solid #c7c7c7;
  border-radius: 4px;
  background: #fff;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.04);
}

.date-control {
  position: relative;
  display: flex;
  align-items: center;
  width: 288px;
}

.is-patient .date-control {
  width: 309px;
}

.date-control input {
  width: 100%;
  height: 100%;
  padding: 0 48px 0 20px;
  box-sizing: border-box;
  border: 0;
  outline: none;
  color: #1f2430;
  font-size: 16px;
}

.date-icon {
  position: absolute;
  right: 14px;
  width: 21px;
  height: 21px;
  color: #c7c7c7;
  pointer-events: none;
}

.filter-item select {
  width: 188px;
  padding: 0 44px 0 20px;
  color: #1f2430;
  font-size: 16px;
  outline: none;
}

.is-patient .filter-item select {
  width: 200px;
}

.search-filter {
  display: grid;
  grid-template-columns: 397px 62px;
  overflow: hidden;
}

.is-patient .search-filter {
  grid-template-columns: 420px 66px;
}

.is-check-project .search-filter {
  grid-template-columns: 410px 64px;
}

.is-drug .search-filter {
  grid-template-columns: 430px 64px;
}

.search-filter input {
  width: 100%;
  height: 100%;
  padding: 0 14px;
  box-sizing: border-box;
  border: 0;
  outline: none;
  color: #1f2430;
  font-size: 16px;
}

.search-filter input::placeholder {
  color: #c2c2c2;
}

.search-filter button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-left: 1px solid #c7c7c7;
  background: #636be8;
  color: #fff;
  cursor: pointer;
}

.search-icon {
  width: 27px;
  height: 27px;
}

.is-patient .search-icon {
  transform: translateX(-4px);
}

.summary-panel {
  display: grid;
  grid-template-columns: 191px 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
  min-height: 165px;
  margin-top: -6px;
  margin-bottom: 20px;
  overflow: hidden;
  border-radius: 4px;
  background: #fff8e7;
}

.is-charge .summary-panel {
  grid-template-columns: 191px repeat(6, 1fr);
  grid-template-rows: repeat(2, 82px);
}

.summary-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
  padding: 14px 18px;
  box-sizing: border-box;
}

.is-charge .summary-card.accent {
  align-items: center;
  grid-column: 1;
  grid-row: 1 / 3;
}

.is-charge .summary-card:nth-child(2) { grid-column: 2; grid-row: 1; }
.is-charge .summary-card:nth-child(3) { grid-column: 3; grid-row: 1; }
.is-charge .summary-card:nth-child(4) { grid-column: 4; grid-row: 1; }
.is-charge .summary-card:nth-child(5) { grid-column: 2; grid-row: 2; }
.is-charge .summary-card:nth-child(6) { grid-column: 3; grid-row: 2; }
.is-charge .summary-card:nth-child(7) { grid-column: 4; grid-row: 2; }
.is-charge .summary-card:nth-child(8) { grid-column: 5; grid-row: 2; }
.is-charge .summary-card:nth-child(9) { grid-column: 6; grid-row: 2; }
.is-charge .summary-card:nth-child(10) { grid-column: 7; grid-row: 2; }

.is-charge .summary-card {
  padding-left: 30px;
}

.is-charge .table-wrapper {
  overflow-x: auto;
}

.summary-label {
  color: #242a35;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.3;
}

.summary-value {
  margin-top: 10px;
  color: #ffb700;
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
}

.summary-card.accent .summary-value {
  margin-top: 20px;
  font-size: 38px;
}

.chart-card {
  width: 100%;
  box-sizing: border-box;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 18px rgba(20, 27, 52, 0.08);
}

.bar-card {
  height: 337px;
  margin-bottom: 22px;
}

.donut-card {
  height: 449px;
  margin-bottom: 29px;
}

.echart {
  width: 100%;
  height: 100%;
}

.table-wrapper {
  width: 100%;
  overflow: visible;
}

.stat-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.stat-table th,
.stat-table td {
  box-sizing: border-box;
  border: 1px solid #e2e5ff;
  text-align: center;
}

.stat-table th {
  height: 70px;
  padding: 0 6px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.25;
}

.is-patient .stat-table th {
  height: 74px;
  font-size: 20px;
}

.stat-table td {
  height: 70px;
  padding: 0 6px;
  color: #000;
  font-size: 16px;
  line-height: 1.3;
  white-space: nowrap;
}

.is-patient .stat-table td {
  height: 75px;
}

.total-row td {
  height: 70px;
  font-size: 20px;
  font-weight: 700;
}

.status-cell,
.type-cell {
  color: #09c776 !important;
}

.table-detail .type-cell,
.detail-link button {
  color: #5d65ff !important;
}

.detail-link button {
  padding: 0;
  border: 0;
  background: transparent;
  font-size: 16px;
  cursor: pointer;
}

.strong-cell {
  font-size: 24px !important;
  font-weight: 700;
}

.table-detail,
.table-cashier,
.table-department,
.table-doctor,
.table-stored,
.table-check-project {
  min-width: 1502px;
}

.table-check-project {
  min-width: 1582px;
}

.table-drug-stock,
.table-drug-inventory,
.table-drug-price,
.table-drug-sale,
.table-drug-rank {
  min-width: 1588px;
}

.table-check-project th {
  height: 74px;
  font-size: 18px;
}

.is-drug .stat-table th {
  height: 72px;
  font-size: 18px;
  line-height: 1.28;
}

.table-check-project td {
  height: 74px;
  font-size: 18px;
  line-height: 1.35;
}

.is-drug .stat-table td {
  height: 72px;
  font-size: 17px;
  line-height: 1.28;
}

.table-check-project .total-row td {
  height: 72px;
  font-size: 24px;
}

.is-drug .total-row td {
  height: 72px;
  font-size: 22px;
}

.table-patient-log {
  min-width: 1576px;
}

.table-age .total-row td:nth-child(2),
.table-age .total-row td:nth-child(3) {
  font-size: 18px;
  font-weight: 700;
}

.table-donut th,
.table-donut td {
  font-size: 20px;
}

.table-donut .strong-cell {
  font-size: 24px !important;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 32px;
  color: #1d2530;
  font-size: 16px;
}

.pager-btn,
.pager-input {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  box-sizing: border-box;
  border: 2px solid #b8bac0;
  border-radius: 50%;
  background: #fff;
  color: #777;
  font-size: 16px;
  line-height: 1;
}

.pager-btn {
  cursor: pointer;
}

.pager-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.pager-btn.is-active {
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

@media (max-width: 1350px) {
  .is-charge .filters-row {
    flex-wrap: wrap;
    gap: 20px 32px;
  }

  .is-charge .search-filter {
    grid-template-columns: 310px 62px;
  }
}

.pager-ellipsis {
  margin: 0 8px 0 14px;
  color: #111722;
}

.pager-info {
  color: #111722;
}

@media (max-width: 1680px) {
  .statistics-card,
  .statistics-card.is-patient,
  .statistics-card.is-check-project,
  .statistics-card.is-drug {
    margin-left: 16px;
    margin-right: 16px;
  }
}
</style>
