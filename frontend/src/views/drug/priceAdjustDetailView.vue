<template>
  <div class="price-detail-page">
    <section class="detail-card">
      <header class="page-header">
        <h1>查看调价详情</h1>
        <div class="header-actions">
          <button class="outline-btn" type="button" :disabled="!rows.length" @click="exportRows">
            <Upload class="btn-icon" />
            <span>导出</span>
          </button>
          <button class="outline-btn" type="button" @click="goBack">
            <Back class="btn-icon" />
            <span>返回</span>
          </button>
        </div>
      </header>

      <div class="divider"></div>

      <div class="table-wrap">
        <table class="detail-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>药品编码</th>
              <th>药品名称</th>
              <th>采购编号</th>
              <th>规格</th>
              <th>库存量</th>
              <th>批号</th>
              <th>采购价<br />（元）</th>
              <th>原零售价<br />（元）</th>
              <th>现零售价<br />（元）</th>
              <th>差价</th>
              <th>比例</th>
              <th>操作时间</th>
              <th>操作员</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td class="empty-cell" colspan="14">正在加载调价记录...</td>
            </tr>
            <tr v-else-if="!rows.length">
              <td class="empty-cell" colspan="14">暂无调价记录</td>
            </tr>
            <tr v-for="(row, index) in rows" v-else :key="row.id">
              <td>{{ index + 1 }}</td>
              <td>{{ row.drugCode || "-" }}</td>
              <td :title="row.drugName">{{ row.drugName || "-" }}</td>
              <td>-</td>
              <td :title="row.specification">{{ row.specification || "-" }}</td>
              <td>{{ formatStock(row) }}</td>
              <td>{{ row.batchNo || "-" }}</td>
              <td>{{ formatMoney(row.newPurchasePrice) }}</td>
              <td>{{ formatMoney(row.oldSellPrice) }}</td>
              <td>{{ formatMoney(row.newSellPrice) }}</td>
              <td class="danger-text">{{ priceDifference(row) }}</td>
              <td class="danger-text">{{ priceRatio(row) }}</td>
              <td>{{ row.createdAt || "-" }}</td>
              <td>{{ row.operator || "-" }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, Upload } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { priceAdjustOrderApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

type HistoryRow = {
  id: number;
  drugCode?: string;
  drugName?: string;
  specification?: string;
  quantity?: number;
  unit?: string;
  batchNo?: string;
  newPurchasePrice?: number;
  oldSellPrice?: number;
  newSellPrice?: number;
  createdAt?: string;
  operator?: string;
};

const route = useRoute();
const router = useRouter();
const drugId = computed(() => String(route.query.drugId || readNavigationState(route) || ""));
const rows = ref<HistoryRow[]>([]);
const loading = ref(false);

const formatMoney = (value?: number) => Number(value || 0).toFixed(2);
const formatStock = (row: HistoryRow) => `${Number(row.quantity || 0)}${row.unit || ""}`;
const priceDifference = (row: HistoryRow) =>
  (Number(row.newSellPrice || 0) - Number(row.oldSellPrice || 0)).toFixed(2);
const priceRatio = (row: HistoryRow) => {
  const oldPrice = Number(row.oldSellPrice || 0);
  if (oldPrice <= 0) return "-";
  return `${((Number(row.newSellPrice || 0) / oldPrice) * 100).toFixed(2)}%`;
};

const goBack = () => {
  router.push("/drugs/price-adjust");
};

const loadHistory = async () => {
  if (!drugId.value) {
    ElMessage.error("缺少药品编号");
    return;
  }
  loading.value = true;
  try {
    const response: any = await priceAdjustOrderApi.history(drugId.value);
    rows.value = response?.data || [];
  } finally {
    loading.value = false;
  }
};

const csvCell = (value: unknown) => `"${String(value ?? "").replaceAll('"', '""')}"`;
const exportRows = () => {
  if (!rows.value.length) return;
  const header = [
    "序号", "药品编码", "药品名称", "采购编号", "规格", "库存量", "批号",
    "采购价(元)", "原零售价(元)", "现零售价(元)", "差价", "比例", "操作时间", "操作员",
  ];
  const data = rows.value.map((row, index) => [
    index + 1,
    row.drugCode,
    row.drugName,
    "-",
    row.specification,
    formatStock(row),
    row.batchNo,
    formatMoney(row.newPurchasePrice),
    formatMoney(row.oldSellPrice),
    formatMoney(row.newSellPrice),
    priceDifference(row),
    priceRatio(row),
    row.createdAt,
    row.operator,
  ]);
  const csv = "\uFEFF" + [header, ...data].map((line) => line.map(csvCell).join(",")).join("\r\n");
  const link = document.createElement("a");
  link.href = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
  link.download = `药品调价详情-${rows.value[0]?.drugCode || drugId.value}.csv`;
  link.click();
  URL.revokeObjectURL(link.href);
};

onMounted(loadHistory);
</script>

<style scoped>
.price-detail-page {
  min-height: 100%;
  padding: 10px 0 58px;
}

.detail-card {
  width: min(1660px, calc(100% - 96px));
  min-height: 812px;
  margin: 0 auto;
  padding: 43px 34px 40px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h1 {
  position: relative;
  margin: 0;
  padding-left: 16px;
  color: #1d2430;
  font-size: 24px;
  line-height: 1;
}

.page-header h1::before {
  position: absolute;
  left: 0;
  top: 1px;
  width: 6px;
  height: 24px;
  content: "";
  background: #6570f2;
}

.header-actions {
  display: flex;
  gap: 19px;
}

.outline-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 148px;
  height: 48px;
  border: 1px solid #636be8;
  border-radius: 5px;
  background: #fff;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.outline-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.btn-icon {
  width: 24px;
  height: 24px;
}

.divider {
  height: 1px;
  margin: 28px 0;
  background: #eee;
}

.table-wrap {
  overflow-x: auto;
}

.detail-table {
  width: 100%;
  min-width: 1500px;
  border-collapse: collapse;
  table-layout: fixed;
}

.detail-table th {
  height: 76px;
  padding: 0 7px;
  background: #bfc2f6;
  color: #03060b;
  font-size: 17px;
  line-height: 1.35;
  white-space: nowrap;
}

.detail-table td {
  height: 87px;
  padding: 0 7px;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-table th:nth-child(1) { width: 4%; }
.detail-table th:nth-child(2) { width: 7%; }
.detail-table th:nth-child(3) { width: 12%; }
.detail-table th:nth-child(4) { width: 9%; }
.detail-table th:nth-child(5) { width: 8%; }
.detail-table th:nth-child(6) { width: 6%; }
.detail-table th:nth-child(7) { width: 8%; }
.detail-table th:nth-child(8),
.detail-table th:nth-child(9),
.detail-table th:nth-child(10) { width: 6%; }
.detail-table th:nth-child(11),
.detail-table th:nth-child(12) { width: 5%; }
.detail-table th:nth-child(13) { width: 12%; }
.detail-table th:nth-child(14) { width: 6%; }

.danger-text {
  color: #ff4545;
}

.empty-cell {
  height: 150px !important;
  color: #8a8f9d;
}

@media (max-width: 1280px) {
  .detail-card {
    width: calc(100% - 32px);
    padding: 32px 18px 36px;
  }
}
</style>
