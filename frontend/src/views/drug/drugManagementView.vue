<template>
  <div class="drug-page">
    <section class="drug-card">
      <header class="page-toolbar">
        <div v-if="page.tabs" class="drug-tabs" role="tablist">
          <button
            v-for="tab in page.tabs"
            :key="tab"
            type="button"
            :class="['drug-tab', { 'is-active': tab === activePriceTab }]"
            @click="changePriceTab(tab)"
          >
            {{ tab }}
          </button>
        </div>

        <div class="toolbar-actions">
          <button
            v-for="action in page.actions"
            :key="action.label"
            type="button"
            :class="['top-btn', action.primary ? 'primary' : 'outline']"
            @click="handleAction(action.route)"
          >
            <component :is="action.icon" class="btn-icon" />
            <span>{{ action.label }}</span>
          </button>
        </div>
      </header>

      <div class="page-divider"></div>

      <section class="filters-row" aria-label="筛选条件">
        <div
          v-for="filter in page.filters"
          :key="filter.key"
          :class="['filter-item', `${filter.type}-filter`]"
        >
          <span>{{ filter.label }}</span>
          <select
            v-if="filter.type === 'select'"
            v-model="filterState[filter.key]"
            @change="handleFilterChange"
          >
            <option v-for="option in filter.options" :key="option" :value="option">
              {{ option }}
            </option>
          </select>
          <el-date-picker
            v-else-if="filter.type === 'date'"
            v-model="filterState[filter.key]"
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
        </div>

        <label v-if="page.search" class="search-filter">
          <input
            v-model="filterState.keyword"
            type="text"
            :placeholder="page.search"
            @keyup.enter="handleSearch"
          />
          <button type="button" aria-label="搜索" @click="handleSearch">
            <Search class="search-icon" />
          </button>
        </label>

        <div v-if="page.checks" class="check-filters">
          <label v-for="item in page.checks" :key="item">
            <input type="checkbox" />
            <span>{{ item }}</span>
          </label>
        </div>
      </section>

      <div class="table-wrapper">
        <table :class="['drug-table', page.tableClass]">
          <thead>
            <tr>
              <th v-for="column in page.columns" :key="column.key">
                {{ column.label }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in page.rows" :key="row.id">
              <td
                v-for="column in page.columns"
                :key="column.key"
                :class="column.className"
              >
                <template v-if="column.key === 'actions'">
                  <div class="actions">
                    <button
                      v-for="action in row.actions"
                      :key="action"
                      type="button"
                      @click="handleRowAction(action, row)"
                    >
                      {{ action }}
                    </button>
                  </div>
                </template>
                <span
                  v-else
                  :class="[
                    column.status ? 'status-text' : '',
                    column.status && row[column.key] === '未通过' ? 'status-pending' : '',
                    column.status && row[column.key] === '审核未通过' ? 'status-rejected' : '',
                    column.accent ? 'accent-text' : '',
                  ]"
                >
                  {{ row[column.key] }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer :class="['footer-row', { 'has-summary': page.summary }]">
        <div v-if="page.summary" class="summary-text">
          采购金额合计：
          <strong>{{ formatAmount(inventorySummary.purchaseAmount) }}</strong>元&nbsp;&nbsp;零售金额合计：
          <strong>{{ formatAmount(inventorySummary.retailAmount) }}</strong>元
        </div>

        <nav class="pagination" aria-label="分页">
          <button
            type="button"
            class="pager-btn"
            aria-label="上一页"
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >&lt;</button>
          <button
            v-for="pageNumber in visiblePageNumbers"
            :key="pageNumber"
            type="button"
            :class="['pager-btn', { 'is-active': pageNumber === currentPage }]"
            @click="changePage(pageNumber)"
          >
            {{ pageNumber }}
          </button>
          <button
            type="button"
            class="pager-btn"
            aria-label="下一页"
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >&gt;</button>
          <span class="pager-info">每页{{ pageSize }}条，共{{ total }}条</span>
        </nav>
      </footer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  CirclePlusFilled,
  Download,
  Search,
  Upload,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  drugApi,
  inventoryApi,
  priceAdjustOrderApi,
  stockCheckOrderApi,
  stockInOrderApi,
  stockOutOrderApi,
} from "@/api";

type RowData = Record<string, string | number | string[]>;

type Column = {
  key: string;
  label: string;
  className?: string;
  status?: boolean;
  accent?: boolean;
};

type Filter = {
  key: string;
  label: string;
  type: "select" | "date";
  options?: string[];
};

type Action = {
  label: string;
  icon: typeof CirclePlusFilled;
  primary?: boolean;
  route?: string;
};

type PageConfig = {
  actions: Action[];
  filters: Filter[];
  search?: string;
  checks?: string[];
  tabs?: string[];
  columns: Column[];
  rows: RowData[];
  tableClass: string;
  summary?: boolean;
};

const route = useRoute();
const router = useRouter();
const legacyPath = computed(() => route.path
  .replace(/^\/drugs\/stock-check/, "/drug/inventory-check")
  .replace(/^\/drugs/, "/drug"));
const activePriceTab = ref("药品调价");
const remoteRows = ref<RowData[]>([]);
const currentPage = ref(1);
const pageSize = 10;
const total = ref(0);
const inventorySummary = reactive({
  purchaseAmount: 0,
  retailAmount: 0,
});
const formatAmount = (value: unknown) => (Number(value) || 0).toFixed(2);

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)));
const visiblePageNumbers = computed(() => {
  const start = Math.max(1, Math.min(currentPage.value - 2, totalPages.value - 4));
  const end = Math.min(totalPages.value, start + 4);
  return Array.from({ length: end - start + 1 }, (_, index) => start + index);
});

const filterState = reactive<Record<string, string | string[]>>({
  prescription: "全部",
  status: "全部",
  date: [],
  audit: "全部",
  stockType: "全部",
  keyword: "",
});

const baseDrugNames = [
  "复方氯化钠注射液",
  "替硝唑氯化钠注射液",
  "0.9%氯化钠注射液",
  "克林霉素磷酸酯注射液",
  "生脉注射液",
  "抗骨增生片",
  "田七花叶颗粒",
  "兰索拉唑肠溶片",
  "甲钴胺片",
  "复方氨酚烷胺胶囊",
];

const inventoryNames = [
  "超敏C-反应蛋白",
  "甲硝唑氯化钠注射液",
  "氯化钠0.9%",
  "氯化钠注射液0.9%",
  "青霉素针注射用",
  "阿莫西林胶囊",
  "头孢曲松钠针",
  "5%葡萄糖注射液",
  "葡萄糖10%",
  "葡萄糖酸钙片",
];

const manufacturers = [
  "上海医药（集团）有限公司",
  "山东罗欣药业股份有限公司",
  "昆明制药集团",
  "武汉五景药业",
];

const listActions = ["编辑", "复制", "停用"];

const drugRows = Array.from({ length: 10 }, (_, index) => ({
  id: `drug-${index}`,
  index: index + 1,
  code: String(1000001 + index),
  name: baseDrugNames[index],
  spec: `6g*${11 + index}袋/盒`,
  category: "西/成药",
  purchasePrice: ["2.5", "1.2", "5", "5.3", "3.5", "2.5", "0.13", "0.52", "2.1", "0.7"][index],
  sellPrice: ["4", "2", "9", "5", "3", "2.5", "0.2", "2.5", "2", "1"][index],
  manufacturer: manufacturers[index % manufacturers.length],
  status: index === 0 ? "停用" : "启用",
  createTime: "2019-02-09 10:23:56",
  actions: listActions,
}));

const stockPeople = ["王冕", "林鹤", "李忠云"];
const stockRows = (prefix: string, types: string[], actionText: string) =>
  Array.from({ length: 10 }, (_, index) => ({
    id: `${prefix}-${index}`,
    index: index + 1,
    orderNo: `${prefix}${201911190030 - index}`,
    type: types[index % types.length],
    supplier: "白云制药厂",
    maker: stockPeople[index % stockPeople.length],
    purchaseAmount: `${27000 + index * 100}.00`,
    retailAmount: `${27000 + index * 100}.00`,
    operator: stockPeople[index % stockPeople.length],
    createTime: "2019-02-09 10:23:56",
    auditStatus: index === 0 ? "审核通过" : index === 1 ? "审核未通过" : "未审核",
    actions:
      index === 0
        ? ["查看", actionText]
        : index === 1
          ? ["查看", "删除", actionText]
          : ["编辑", "删除", actionText],
  }));

const inventoryRows = Array.from({ length: 10 }, (_, index) => ({
  id: `inventory-${index}`,
  index: index + 1,
  code: String(1000001 + index),
  location: String(1001 + index),
  name: inventoryNames[index],
  category: "西/成药",
  spec: `6g*${10 + index}袋/盒`,
  form: ["口服", "注射", "皮试", "其它"][index % 4],
  manufacturer: manufacturers[index % 2],
  stock: index % 3 === 1 ? `98盒${9 + index}袋` : `${110 + index}盒`,
  purchaseAmount: `${27000 + index * 100}.00`,
  retailAmount: `${27000 + index * 100}.00`,
  actions: ["查看"],
}));

const checkRows = Array.from({ length: 10 }, (_, index) => ({
  id: `check-${index}`,
  index: index + 1,
  checkNo: `SA${201911190030 - index}`,
  checkDate: "2019-02-09 10:23:56",
  maker: stockPeople[index % stockPeople.length],
  checkStatus: index === 0 ? "正在盘点" : "盘点完成",
  actions: index === 0 ? ["查看", "删除"] : ["查看"],
}));

const priceRows = Array.from({ length: 10 }, (_, index) => ({
  id: `price-${index}`,
  index: index + 1,
  code: String(1000001 + index),
  name: inventoryNames[index],
  category: "西/成药",
  spec: `6g*${10 + index}袋/盒`,
  form: ["口服", "注射", "皮试", "其它"][index % 4],
  manufacturer: manufacturers[index % 2],
  actions: ["调价"],
}));

const priceDrugNames = [
  "超敏C-反应蛋白",
  "甲硝唑氯化钠注射液",
  "氯化钠0.9%",
  "氯化钠注射液0.9%",
  "青霉素针注射用",
  "阿莫西林胶囊",
  "头孢曲松钠针",
  "5%葡萄糖注射液",
  "葡萄糖10%",
  "葡萄糖酸钙片",
];

const priceFactories = [
  "上海医药（集团）有限公司",
  "山东罗欣药业股份有限公司",
  "山东罗欣药业股份有限公司",
  "山东罗欣药业股份有限公司",
  "山东罗欣药业股份有限公司",
  "山东罗欣药业股份有限公司",
  "山东罗欣药业股份有限公司",
  "上海医药（集团）有限公司",
  "上海医药（集团）有限公司",
  "上海医药（集团）有限公司",
];

const priceForms = ["口服", "注射", "皮试", "其它", "口服", "注射", "皮试", "其它", "口服", "注射"];

const priceAdjustRows = priceDrugNames.map((name, index) => ({
  id: `price-adjust-${index}`,
  index: index + 1,
  code: String(1000001 + index),
  name,
  category: "西/成药",
  spec: `6g*${10 + index}袋/盒`,
  form: priceForms[index],
  manufacturer: priceFactories[index],
  actions: ["调价"],
}));

const priceRecordRows = priceDrugNames.map((name, index) => ({
  id: `price-record-${index}`,
  index: index + 1,
  code: String(1000001 + index),
  name,
  category: "西/成药",
  spec: `6g*${10 + index}袋/盒`,
  form: priceForms[index],
  manufacturer: priceFactories[index],
  adjustCount: index === 1 || index === 9 ? 2 : 3,
  actions: ["查看详情"],
}));

const drugColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "code", label: "编码" },
  { key: "name", label: "药品名称" },
  { key: "spec", label: "规格" },
  { key: "category", label: "处方类别" },
  { key: "purchasePrice", label: "采购价" },
  { key: "sellPrice", label: "售药价" },
  { key: "manufacturer", label: "生产厂家" },
  { key: "status", label: "状态" },
  { key: "createTime", label: "创建时间" },
  { key: "actions", label: "操作" },
];

const stockColumns = (
  noLabel: string,
  typeLabel: string,
  showSupplier = true,
): Column[] => [
  { key: "index", label: "序号" },
  { key: "orderNo", label: noLabel },
  { key: "type", label: typeLabel },
  ...(showSupplier ? [{ key: "supplier", label: "供应商名称" }] : []),
  { key: "maker", label: "制单人员" },
  { key: "purchaseAmount", label: "采购金额" },
  { key: "retailAmount", label: "零售金额" },
  { key: "operator", label: typeLabel.includes("入库") ? "入库人员" : "出库人员" },
  { key: "createTime", label: "创建时间" },
  { key: "auditStatus", label: "审核状态", status: true },
  { key: "actions", label: "操作" },
];

const inventoryColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "code", label: "药品编码" },
  { key: "location", label: "货位号" },
  { key: "name", label: "药品名称" },
  { key: "category", label: "处方类别" },
  { key: "spec", label: "规格" },
  { key: "form", label: "剂型" },
  { key: "manufacturer", label: "厂家" },
  { key: "stock", label: "库存", accent: true },
  { key: "purchaseAmount", label: "采购金额（元）" },
  { key: "retailAmount", label: "零售金额（元）" },
  { key: "actions", label: "操作" },
];

const checkColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "checkNo", label: "盘点单号" },
  { key: "checkDate", label: "盘点日期" },
  { key: "maker", label: "制单人" },
  { key: "checkStatus", label: "盘点状态" },
  { key: "actions", label: "操作" },
];

const priceColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "code", label: "药品编码" },
  { key: "name", label: "药品名称" },
  { key: "category", label: "处方类别" },
  { key: "spec", label: "规格" },
  { key: "form", label: "剂型" },
  { key: "manufacturer", label: "生产厂家" },
  { key: "actions", label: "操作" },
];

const priceRecordColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "code", label: "药品编码" },
  { key: "name", label: "药品名称" },
  { key: "category", label: "处方类别" },
  { key: "spec", label: "规格" },
  { key: "form", label: "剂型" },
  { key: "manufacturer", label: "生产厂家" },
  { key: "adjustCount", label: "调价次数" },
  { key: "actions", label: "操作" },
];

const commonPaginationActions: Action[] = [
  { label: "导出", icon: Upload },
];

const pages: Record<string, PageConfig> = {
  "/drug": {
    actions: [
      { label: "新增药品", icon: CirclePlusFilled, primary: true, route: "/drug/add" },
      { label: "扫码录入", icon: CirclePlusFilled },
      { label: "导入", icon: Download },
      { label: "导出", icon: Upload },
    ],
    filters: [
      { key: "prescription", label: "处方类别", type: "select", options: ["全部", "西/成药","中药"] },
      { key: "status", label: "药品状态", type: "select", options: ["全部", "启用", "停用"] },
      { key: "date", label: "创建时间", type: "date" },
    ],
    search: "输入药品名称/编码/生产厂家",
    columns: drugColumns,
    rows: drugRows,
    tableClass: "table-drug",
  },
  "/drug/stock-in": {
    actions: [
      { label: "新增入库", icon: CirclePlusFilled, primary: true, route: "/drug/stock-in/add?mode=add" },
      { label: "扫码入库", icon: CirclePlusFilled },
    ],
    filters: [
      { key: "audit", label: "审核状态", type: "select", options: ["全部", "未审核", "审核通过", "审核未通过"] },
      { key: "stockType", label: "入库类型", type: "select", options: ["全部", "采购入库", "退货入库", "其他入库"] },
    ],
    search: "输入入库单号/供应商",
    columns: stockColumns("入库单号", "入库类型"),
    rows: stockRows("SP", ["采购入库"], "再次入库"),
    tableClass: "table-stock",
  },
  "/drug/stock-out": {
    actions: [
      { label: "新增出库", icon: CirclePlusFilled, primary: true, route: "/drug/stock-out/add?mode=add" },
      { label: "扫码出库", icon: CirclePlusFilled },
    ],
    filters: [
      { key: "audit", label: "审核状态", type: "select", options: ["全部", "审核通过", "审核未通过", "未审核"] },
      { key: "stockType", label: "出库类型", type: "select", options: ["全部", "科室领药", "报损出库", "采购退货"] },
    ],
    search: "输入出库单号",
    columns: stockColumns("出库单号", "出库类型", false),
    rows: stockRows("CB", ["科室领药", "报损出库", "采购退货"], "再次出库"),
    tableClass: "table-stock",
  },
  "/drug/inventory": {
    actions: commonPaginationActions,
    filters: [
      { key: "prescription", label: "处方类别", type: "select", options: ["全部", "西/成药", "中药"] },
    ],
    search: "输入药品名称/编码/生产厂家",
    checks: ["库存预警", "有效期预警"],
    columns: inventoryColumns,
    rows: inventoryRows,
    tableClass: "table-inventory",
    summary: true,
  },
  "/drug/inventory-check": {
    actions: [
      { label: "库存盘点", icon: CirclePlusFilled, primary: true, route: "/drug/inventory-check/add" },
      { label: "扫码出库", icon: CirclePlusFilled },
    ],
    filters: [{ key: "date", label: "创建时间", type: "date" }],
    search: "输入盘点单号",
    columns: checkColumns,
    rows: checkRows,
    tableClass: "table-check",
  },
  "/drug/price-adjust": {
    actions: commonPaginationActions,
    tabs: ["药品调价", "调价记录"],
    filters: [
      { key: "prescription", label: "处方类别", type: "select", options: ["全部", "西/成药", "中药"] },
    ],
    search: "输入药品名称/编码/生产厂家",
    columns: priceColumns,
    rows: priceAdjustRows,
    tableClass: "table-price",
  },
};

const page = computed(() => {
  const currentPage = pages[legacyPath.value] || pages["/drug"];
  const dateRange = filterState.date;
  const currentRows = remoteRows.value.filter((row) => {
    if (!Array.isArray(dateRange) || dateRange.length !== 2) return true;
    const rowDate = String(row.createTime || row.checkDate || row.createdAt || "").slice(0, 10);
    return rowDate >= dateRange[0] && rowDate <= dateRange[1];
  });

  if (legacyPath.value !== "/drug/price-adjust") {
    return { ...currentPage, rows: currentRows };
  }

  if (activePriceTab.value === "调价记录") {
    return {
      ...currentPage,
      columns: priceRecordColumns,
      rows: currentRows,
      tableClass: "table-price-record",
    };
  }

  return {
    ...currentPage,
    columns: priceColumns,
    rows: currentRows,
    tableClass: "table-price",
  };
});

const formatStockType = (value: unknown) => {
  const stockType = String(value || "").toUpperCase();
  const labels: Record<string, string> = {
    PURCHASE_IN: "采购入库",
    RETURN_IN: "退货入库",
    OTHER_IN: "其他入库",
    采购入库: "采购入库",
    退货入库: "退货入库",
    其他入库: "其他入库",
    其它入库: "其他入库",
    DEPARTMENT_OUT: "科室领药",
    DAMAGE_OUT: "报损出库",
    PURCHASE_RETURN_OUT: "采购退货",
    科室领药: "科室领药",
    门诊领用: "科室领药",
    报损出库: "报损出库",
    采购退货: "采购退货",
    采购出库: "采购退货",
  };
  return labels[stockType] || String(value || "");
};

const formatAuditStatus = (value: unknown) => {
  const auditStatus = String(value || "").toUpperCase();
  const labels: Record<string, string> = {
    APPROVED: "审核通过",
    AUDIT_APPROVED: "审核通过",
    REJECTED: "审核未通过",
    AUDIT_REJECTED: "审核未通过",
    PENDING: "未审核",
    PENDING_AUDIT: "未审核",
    审核通过: "审核通过",
    审核未通过: "审核未通过",
    未通过: "未审核",
    未审核: "未审核",
  };
  return labels[auditStatus] || "未审核";
};

const fetchRemoteRows = async () => {
  const apiByPath: Record<string, any> = {
    "/drug": drugApi,
    "/drug/stock-in": stockInOrderApi,
    "/drug/stock-out": stockOutOrderApi,
    "/drug/inventory": inventoryApi,
    "/drug/inventory-check": stockCheckOrderApi,
    "/drug/price-adjust": priceAdjustOrderApi,
  };
  const api = apiByPath[legacyPath.value];
  if (!api) return;

  try {
    const keyword = typeof filterState.keyword === "string"
      ? filterState.keyword.trim() || undefined
      : undefined;
    const dateRange = Array.isArray(filterState.date) ? filterState.date : [];
    const params: Record<string, unknown> = {
      page: currentPage.value,
      size: pageSize,
      keyword,
    };

    if (legacyPath.value === "/drug") {
      params.category = filterState.prescription === "全部"
        ? undefined
        : filterState.prescription;
      params.status = filterState.status === "启用"
        ? "ENABLED"
        : filterState.status === "停用"
          ? "DISABLED"
          : undefined;
      params.startDate = dateRange[0] || undefined;
      params.endDate = dateRange[1] || undefined;
    }
    if (legacyPath.value === "/drug/inventory") {
      params.category = filterState.prescription === "全部"
        ? undefined
        : filterState.prescription;
    }
    if (legacyPath.value === "/drug/price-adjust") {
      params.category = filterState.prescription === "全部"
        ? undefined
        : filterState.prescription;
    }
    if (legacyPath.value === "/drug/stock-in" || legacyPath.value === "/drug/stock-out") {
      const stockTypeValues: Record<string, string> = {
        采购入库: "PURCHASE_IN",
        退货入库: "RETURN_IN",
        其他入库: "OTHER_IN",
        科室领药: "DEPARTMENT_OUT",
        报损出库: "DAMAGE_OUT",
        采购退货: "PURCHASE_RETURN_OUT",
      };
      const auditStatusValues: Record<string, string> = {
        未审核: "PENDING",
        审核通过: "APPROVED",
        审核未通过: "REJECTED",
      };
      params.stockType = stockTypeValues[String(filterState.stockType)] || undefined;
      params.auditStatus = auditStatusValues[String(filterState.audit)] || undefined;
    }

    const [response, summaryResponse]: any[] = await Promise.all([
      legacyPath.value === "/drug/price-adjust"
        ? activePriceTab.value === "调价记录"
          ? priceAdjustOrderApi.records(params)
          : priceAdjustOrderApi.adjustableDrugs(params)
        : legacyPath.value === "/drug"
        ? drugApi.search(params)
        : legacyPath.value === "/drug/inventory"
          ? inventoryApi.search(params)
          : api.page(params),
      legacyPath.value === "/drug/inventory"
        ? inventoryApi.summary()
        : Promise.resolve(null),
    ]);
    total.value = Number(response.data.total || 0);
    if (summaryResponse?.data) {
      inventorySummary.purchaseAmount = Number(summaryResponse.data.purchaseAmount || 0);
      inventorySummary.retailAmount = Number(summaryResponse.data.retailAmount || 0);
    }
    remoteRows.value = (response.data.records || []).map((item: any, index: number) => ({
      ...item,
      index: (currentPage.value - 1) * pageSize + index + 1,
      id: legacyPath.value === "/drug/price-adjust"
        ? item.drugId || item.id
        : item.id || `${legacyPath.value}-${index}`,
      code: item.drugCode || item.code || item.orderNo || item.checkNo || "",
      name: item.name || item.drugName || "",
      orderNo: item.orderNo || "",
      checkNo: item.checkNo || "",
      type: formatStockType(item.stockType),
      supplier: item.supplierName || "",
      maker: item.maker || "",
      purchaseAmount: item.purchaseAmount || "",
      retailAmount: item.retailAmount || "",
      operator: item.operator || "",
      createTime: item.createdAt || "",
      auditStatus: formatAuditStatus(item.auditStatus),
      location: item.locationNo || "",
      spec: item.specification || "",
      form: item.dosageForm || "",
      stock: item.quantity || "",
      manufacturer: item.manufacturer || "",
      category: item.prescriptionCategory || item.category || "",
      purchasePrice: item.purchasePrice || "",
      sellPrice: item.sellPrice || "",
      adjustCount: item.adjustmentCount || 0,
      status: String(item.status || "").toUpperCase() === "DISABLED" ? "停用" : "启用",
      checkDate: item.checkDate || "",
      checkStatus: String(item.checkStatus || "").toUpperCase() === "CONFIRMED" ? "盘点完成" : "正在盘点",
      actions: legacyPath.value === "/drug"
        ? ["编辑", "复制", String(item.status || "").toUpperCase() === "DISABLED" ? "启用" : "停用"]
        : legacyPath.value === "/drug/stock-in"
          ? ["查看", "编辑", "删除", "再次入库"]
          : legacyPath.value === "/drug/stock-out"
            ? ["查看", "编辑", "删除", "再次出库"]
            : legacyPath.value === "/drug/inventory"
              ? ["查看"]
              : legacyPath.value === "/drug/inventory-check"
                ? ["查看"]
                : legacyPath.value === "/drug/price-adjust"
                  ? activePriceTab.value === "调价记录"
                    ? ["查看详情"]
                    : ["调价"]
                  : (page.value.rows?.[0] as any)?.actions || ["查看"],
    }));
  } catch {
    remoteRows.value = [];
    total.value = 0;
    ElMessage.error("查询失败，请稍后重试");
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchRemoteRows();
};

const handleFilterChange = () => {
  handleSearch();
};

const changePriceTab = (tab: string) => {
  if (activePriceTab.value === tab) return;
  activePriceTab.value = tab;
  currentPage.value = 1;
  fetchRemoteRows();
};

const changePage = (pageNumber: number) => {
  if (pageNumber < 1 || pageNumber > totalPages.value || pageNumber === currentPage.value) {
    return;
  }
  currentPage.value = pageNumber;
  fetchRemoteRows();
};

watch(
  () => legacyPath.value,
  () => {
    filterState.keyword = "";
    filterState.date = [];
    filterState.prescription = "全部";
    filterState.status = "全部";
    filterState.audit = "全部";
    filterState.stockType = "全部";
    currentPage.value = 1;
    total.value = 0;
    remoteRows.value = [];
    if (legacyPath.value === "/drug/price-adjust") {
      activePriceTab.value = "药品调价";
    }
    fetchRemoteRows();
  },
);

onMounted(fetchRemoteRows);

const handleAction = (target?: string) => {
  if (target) {
    router.push(target);
  }
};

const handleRowAction = async (action: string, row: RowData) => {
  if (legacyPath.value === "/drug" && action === "编辑") {
    router.push({
      path: "/drug/add",
      state: {
        mode: "edit",
        id: String(row.id),
      },
    });
    return;
  }

  if (legacyPath.value === "/drug" && action === "复制") {
    drugApi.copy(String(row.id)).then(async () => {
      ElMessage.success("药品复制成功");
      await fetchRemoteRows();
    });
    return;
  }

  if (legacyPath.value === "/drug" && (action === "停用" || action === "启用")) {
    const request = action === "停用"
      ? drugApi.disable(String(row.id))
      : drugApi.enable(String(row.id));
    request.then(async () => {
      ElMessage.success(`药品已${action}`);
      await fetchRemoteRows();
    });
    return;
  }

  if (legacyPath.value === "/drug/inventory-check" && action === "查看") {
    router.push({
      path: "/drugs/stock-check/detail",
      query: { id: String(row.id) },
    });
    return;
  }

  if (legacyPath.value === "/drug/price-adjust" && activePriceTab.value === "药品调价" && action === "调价") {
    router.push({
      path: "/drugs/price-adjust/create",
      query: { drugId: String(row.id) },
    });
    return;
  }

  if (legacyPath.value === "/drug/price-adjust" && activePriceTab.value === "调价记录" && action === "查看详情") {
    router.push({
      path: "/drugs/price-adjust/detail",
      query: { drugId: String(row.id) },
    });
    return;
  }

  if (
    legacyPath.value === "/drug/inventory"
    && (action === "查看" || action === "查看明细")
  ) {
    router.push({
      path: "/drug/inventory/detail",
      state: { id: String(row.id) },
    });
    return;
  }

  if (legacyPath.value !== "/drug/stock-in" && legacyPath.value !== "/drug/stock-out") {
    return;
  }

  const basePath = legacyPath.value === "/drug/stock-out" ? "/drug/stock-out" : "/drug/stock-in";

  if (action === "查看") {
    router.push({
      path: `${basePath}/detail`,
      state: { id: String(row.id) },
    });
    return;
  }

  if (action === "编辑") {
    router.push({
      path: `${basePath}/add`,
      state: {
        id: String(row.id),
        mode: "edit",
      },
    });
    return;
  }

  if (
    (legacyPath.value === "/drug/stock-in" && action === "再次入库")
    || (legacyPath.value === "/drug/stock-out" && action === "再次出库")
  ) {
    router.push({
      path: `${basePath}/add`,
      state: {
        id: String(row.id),
        mode: "restock",
      },
    });
    return;
  }

  if (action === "删除") {
    try {
      await ElMessageBox.confirm(
        `确定删除${legacyPath.value === "/drug/stock-out" ? "出库" : "入库"}单 ${String(row.orderNo || "")} 吗？`,
        "删除确认",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        },
      );
      const api = legacyPath.value === "/drug/stock-out"
        ? stockOutOrderApi
        : stockInOrderApi;
      await api.remove(String(row.id));
      ElMessage.success("删除成功");
      if (page.value.rows.length === 1 && currentPage.value > 1) {
        currentPage.value -= 1;
      }
      await fetchRemoteRows();
    } catch (error: any) {
      if (error !== "cancel" && error !== "close") {
        ElMessage.error("删除失败，请稍后重试");
      }
    }
    return;
  }
};
</script>

<style scoped>
.drug-page {
  min-height: 100%;
  padding: 27px 0 58px;
}

.drug-card {
  width: min(1614px, calc(100% - 96px));
  margin: 0 auto;
  padding: 31px 32px 43px;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 48px;
  gap: 18px;
}

.toolbar-actions {
  display: inline-flex;
  align-items: center;
  gap: 19px;
  margin-left: auto;
}

.top-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 11px;
  min-width: 145px;
  height: 46px;
  padding: 0 18px;
  border-radius: 5px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.top-btn.primary {
  background: #636be8;
  color: #fff;
}

.btn-icon {
  width: 24px;
  height: 24px;
}

.drug-tabs {
  display: inline-flex;
  overflow: hidden;
  border: 1px solid #c6c8cf;
  border-radius: 4px;
}

.drug-tab {
  width: 128px;
  height: 46px;
  border: 0;
  border-right: 1px solid #c6c8cf;
  background: #fff;
  color: #b8b8b8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.drug-tab:last-child {
  border-right: 0;
}

.drug-tab.is-active {
  background: #636be8;
  color: #fff;
}

.page-divider {
  height: 1px;
  margin: 28px 0 31px;
  background: #eeeeee;
}

.filters-row {
  display: flex;
  align-items: center;
  gap: 27px;
  margin-bottom: 32px;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  gap: 15px;
  color: #1e2533;
  font-size: 16px;
  white-space: nowrap;
}

.filter-item select,
.date-control,
.search-filter {
  height: 44px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  background: #fff;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.02);
}

.filter-item select {
  width: 196px;
  padding: 0 19px;
  color: #2f3541;
  font-size: 16px;
  outline: none;
}

.date-control {
  position: relative;
  width: 303px;
  display: flex;
  align-items: center;
}

.date-control input {
  width: 100%;
  height: 100%;
  padding: 0 48px 0 20px;
  border: 0;
  outline: none;
  color: #2f3541;
  font-size: 16px;
}

.date-icon {
  position: absolute;
  right: 14px;
  width: 22px;
  height: 22px;
  color: #c7c7c7;
  pointer-events: none;
}

.search-filter {
  display: grid;
  grid-template-columns: 377px 62px;
  overflow: hidden;
}

.search-filter input {
  height: 100%;
  padding: 0 14px;
  border: 0;
  outline: none;
  color: #2f3541;
  font-size: 16px;
}

.search-filter input::placeholder {
  color: #c5c5c5;
}

.search-filter button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-left: 1px solid #c9c9c9;
  background: #636be8;
  color: #fff;
  cursor: pointer;
}

.search-icon {
  width: 27px;
  height: 27px;
}

.check-filters {
  display: inline-flex;
  align-items: center;
  gap: 30px;
  margin-left: auto;
  color: #1e2533;
  font-size: 16px;
}

.check-filters label {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.check-filters input {
  width: 19px;
  height: 19px;
  accent-color: #636be8;
}

.table-wrapper {
  overflow-x: auto;
}

.drug-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.drug-table th,
.drug-table td {
  box-sizing: border-box;
}

.drug-table th {
  height: 73px;
  padding: 0 8px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
}

.drug-table td {
  height: 85px;
  padding: 0 8px;
  border-bottom: 1px solid #eeeeee;
  color: #000;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
}

.actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 25px;
}

.actions button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #6068f1;
  font-size: 16px;
  cursor: pointer;
}

.status-text {
  color: #22c97f;
}

.status-pending {
  color: #e6a23c;
}

.status-rejected {
  color: #f56c6c;
}

.accent-text {
  color: #6068f1;
  font-weight: 700;
}

.footer-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 28px;
}

.footer-row.has-summary {
  justify-content: space-between;
}

.summary-text {
  color: #000;
  font-size: 18px;
  white-space: nowrap;
}

.summary-text strong {
  color: #f00;
  font-size: 20px;
  letter-spacing: 1px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1e2533;
  font-size: 16px;
}

.pager-btn,
.pager-input {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: 2px solid #bfc0c4;
  border-radius: 50%;
  background: #fff;
  color: #777;
  font-size: 16px;
  line-height: 1;
}

.pager-btn {
  cursor: pointer;
}

.pager-btn.is-active {
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

.pager-ellipsis {
  margin: 0 8px 0 14px;
}

.pager-info {
  color: #222;
}

.table-drug th:nth-child(1),
.table-drug td:nth-child(1),
.table-stock th:nth-child(1),
.table-stock td:nth-child(1),
.table-inventory th:nth-child(1),
.table-inventory td:nth-child(1) {
  width: 78px;
}

.table-drug th:nth-child(2),
.table-drug td:nth-child(2) {
  width: 100px;
}

.table-drug th:nth-child(3),
.table-drug td:nth-child(3) {
  width: 198px;
}

.table-drug th:nth-child(4),
.table-drug td:nth-child(4),
.table-drug th:nth-child(5),
.table-drug td:nth-child(5),
.table-drug th:nth-child(9),
.table-drug td:nth-child(9) {
  width: 115px;
}

.table-drug th:nth-child(6),
.table-drug td:nth-child(6),
.table-drug th:nth-child(7),
.table-drug td:nth-child(7) {
  width: 93px;
}

.table-drug th:nth-child(8),
.table-drug td:nth-child(8) {
  width: 250px;
}

.table-drug th:nth-child(10),
.table-drug td:nth-child(10) {
  width: 188px;
}

.table-drug th:nth-child(11),
.table-drug td:nth-child(11) {
  width: 190px;
}

.table-stock th:nth-child(2),
.table-stock td:nth-child(2) {
  width: 158px;
}

.table-stock th:nth-child(3),
.table-stock td:nth-child(3),
.table-stock th:nth-child(5),
.table-stock td:nth-child(5),
.table-stock th:nth-child(8),
.table-stock td:nth-child(8) {
  width: 116px;
}

.table-stock th:nth-child(4),
.table-stock td:nth-child(4) {
  width: 150px;
}

.table-stock th:nth-child(6),
.table-stock td:nth-child(6),
.table-stock th:nth-child(7),
.table-stock td:nth-child(7),
.table-stock th:nth-child(10),
.table-stock td:nth-child(10) {
  width: 130px;
}

.table-stock th:nth-child(9),
.table-stock td:nth-child(9) {
  width: 190px;
}

.table-stock th:nth-child(11),
.table-stock td:nth-child(11) {
  width: 185px;
  position: sticky;
  right: 0;
  z-index: 2;
  background: #fff;
  box-shadow: -8px 0 12px -12px rgba(31, 41, 55, 0.45);
}

.table-stock {
  min-width: 1499px;
}

.table-stock th:nth-child(11) {
  z-index: 3;
  background: #b9bcf4;
}

.table-stock td:nth-child(11) {
  padding: 8px 6px;
}

.table-stock .actions {
  display: grid;
  grid-template-columns: repeat(2, max-content);
  justify-content: center;
  gap: 8px 14px;
  width: 100%;
}

.table-stock .actions button {
  font-size: 14px;
  line-height: 22px;
  white-space: nowrap;
}

.table-inventory th:nth-child(2),
.table-inventory td:nth-child(2),
.table-inventory th:nth-child(3),
.table-inventory td:nth-child(3) {
  width: 92px;
}

.table-inventory th:nth-child(4),
.table-inventory td:nth-child(4) {
  width: 180px;
}

.table-inventory th:nth-child(5),
.table-inventory td:nth-child(5),
.table-inventory th:nth-child(6),
.table-inventory td:nth-child(6),
.table-inventory th:nth-child(7),
.table-inventory td:nth-child(7) {
  width: 95px;
}

.table-inventory th:nth-child(8),
.table-inventory td:nth-child(8) {
  width: 258px;
}

.table-inventory th:nth-child(9),
.table-inventory td:nth-child(9) {
  width: 104px;
}

.table-inventory th:nth-child(10),
.table-inventory td:nth-child(10),
.table-inventory th:nth-child(11),
.table-inventory td:nth-child(11) {
  width: 143px;
}

.table-inventory th:nth-child(12),
.table-inventory td:nth-child(12) {
  width: 143px;
}

.table-check th:nth-child(1),
.table-check td:nth-child(1) {
  width: 150px;
}

.table-check th:nth-child(2),
.table-check td:nth-child(2),
.table-check th:nth-child(3),
.table-check td:nth-child(3),
.table-check th:nth-child(4),
.table-check td:nth-child(4),
.table-check th:nth-child(5),
.table-check td:nth-child(5),
.table-check th:nth-child(6),
.table-check td:nth-child(6) {
  width: auto;
}

.table-price th:nth-child(1),
.table-price td:nth-child(1) {
  width: 110px;
}

.table-price th:nth-child(2),
.table-price td:nth-child(2) {
  width: 160px;
}

.table-price th:nth-child(3),
.table-price td:nth-child(3) {
  width: 230px;
}

.table-price th:nth-child(4),
.table-price td:nth-child(4),
.table-price th:nth-child(5),
.table-price td:nth-child(5),
.table-price th:nth-child(6),
.table-price td:nth-child(6) {
  width: 150px;
}

.table-price th:nth-child(7),
.table-price td:nth-child(7) {
  width: 320px;
}

.table-price th:nth-child(8),
.table-price td:nth-child(8) {
  width: 170px;
}

.table-price-record th:nth-child(1),
.table-price-record td:nth-child(1) {
  width: 110px;
}

.table-price-record th:nth-child(2),
.table-price-record td:nth-child(2) {
  width: 130px;
}

.table-price-record th:nth-child(3),
.table-price-record td:nth-child(3) {
  width: 210px;
}

.table-price-record th:nth-child(4),
.table-price-record td:nth-child(4),
.table-price-record th:nth-child(5),
.table-price-record td:nth-child(5),
.table-price-record th:nth-child(6),
.table-price-record td:nth-child(6) {
  width: 150px;
}

.table-price-record th:nth-child(7),
.table-price-record td:nth-child(7) {
  width: 300px;
}

.table-price-record th:nth-child(8),
.table-price-record td:nth-child(8),
.table-price-record th:nth-child(9),
.table-price-record td:nth-child(9) {
  width: 130px;
}

@media (max-width: 1280px) {
  .drug-card {
    width: calc(100% - 32px);
    padding: 24px 20px 36px;
  }

  .filters-row,
  .page-toolbar,
  .footer-row {
    flex-wrap: wrap;
  }

  .check-filters,
  .toolbar-actions {
    margin-left: 0;
  }

  .table-wrapper {
    overflow-x: auto;
  }

  .drug-table {
    min-width: 1280px;
  }
}
</style>
