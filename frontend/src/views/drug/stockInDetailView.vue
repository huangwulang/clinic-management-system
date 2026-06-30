<template>
  <div class="stock-detail-page">
    <section v-loading="loading" class="detail-card">
      <header class="detail-header">
        <h1>{{ title }}</h1>
        <div class="header-actions">
          <button v-if="showReview" class="action-btn primary" type="button" @click="auditOrder">
            <Tickets class="action-icon" />
            <span>审核</span>
          </button>
          <button v-if="showRestock" class="action-btn primary" type="button" @click="restockOrder">
            <Box class="action-icon" />
            <span>再次入库</span>
          </button>
          <button v-if="showEdit" class="action-btn outline" type="button" @click="editStockIn">
            <FolderChecked class="action-icon" />
            <span>编辑</span>
          </button>
          <button v-if="showDelete" class="action-btn outline" type="button" @click="deleteOrder">
            <Delete class="action-icon" />
            <span>删除</span>
          </button>
          <button class="action-btn outline" type="button" @click="goBack">
            <Back class="action-icon" />
            <span>返回</span>
          </button>
        </div>
      </header>

      <section class="info-panel">
        <div><span>入库订单：</span><strong>{{ displayValue(order?.orderNo) }}</strong></div>
        <div><span>入库日期：</span><strong>{{ stockDate }}</strong></div>
        <div><span>入库人员：</span><strong>{{ displayValue(order?.operator) }}</strong></div>
        <div><span>入库类型：</span><strong>{{ stockTypeLabel }}</strong></div>
        <div><span>供应商：</span><strong>{{ displayValue(order?.supplierName) }}</strong></div>
        <div><span>制单日期：</span><strong>{{ documentDate }}</strong></div>
        <div><span>制单人员：</span><strong>{{ displayValue(order?.maker) }}</strong></div>
        <div class="remark"><span>入库备注：</span><strong>{{ remark }}</strong></div>
        <div><span>审核日期：</span><strong>{{ formatDate(order?.auditAt) }}</strong></div>
        <div><span>审核人员：</span><strong>{{ auditOperator }}</strong></div>
      </section>

      <div class="table-wrap">
        <table class="detail-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>药品编码</th>
              <th>药品名称</th>
              <th>生产厂家</th>
              <th>数量</th>
              <th>单位</th>
              <th>采购价<br />（元）</th>
              <th>零售价<br />（元）</th>
              <th>批号</th>
              <th>药品有效期</th>
              <th>采购金额<br />（元）</th>
              <th>零售金额<br />（元）</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in medicines" :key="item.id">
              <td>{{ item.index }}</td>
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.factory }}</td>
              <td>{{ item.quantity }}</td>
              <td>{{ item.unit }}</td>
              <td>{{ item.purchasePrice }}</td>
              <td>{{ item.retailPrice }}</td>
              <td>{{ item.batchNo }}</td>
              <td>{{ item.expireDate }}</td>
              <td>{{ item.purchaseAmount }}</td>
              <td>{{ item.retailAmount }}</td>
            </tr>
            <tr v-if="!loading && medicines.length === 0">
              <td class="empty-cell" colspan="12">暂无药品明细</td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="totals">
        <span>采购金额合计：</span>
        <strong>{{ purchaseAmountTotal }}</strong>
        <span>元</span>
        <span class="retail-label">零售金额合计：</span>
        <strong>{{ retailAmountTotal }}</strong>
        <span>元</span>
      </footer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Back,
  Box,
  Delete,
  FolderChecked,
  Tickets,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { stockInOrderApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const orderId = readNavigationState(route);

const order = ref<any>(null);
const loading = ref(false);

const parseOrderRemark = (value: unknown): Record<string, any> => {
  if (typeof value !== "string" || !value) return {};
  try {
    const parsed = JSON.parse(value);
    return parsed && typeof parsed === "object" ? parsed : { remark: value };
  } catch {
    return { remark: value };
  }
};

const remarkData = computed(() => parseOrderRemark(order.value?.remark));
const statusCode = computed(() =>
  String(order.value?.auditStatus || order.value?.status || "PENDING").toUpperCase(),
);
const status = computed(() => ({
  PENDING: "未审核",
  PENDING_AUDIT: "未审核",
  SUBMITTED: "未审核",
  APPROVED: "审核通过",
  AUDIT_APPROVED: "审核通过",
  REJECTED: "审核未通过",
  AUDIT_REJECTED: "审核未通过",
}[statusCode.value] || statusCode.value));
const isRejected = computed(() =>
  ["REJECTED", "AUDIT_REJECTED"].includes(statusCode.value),
);
const isPending = computed(() =>
  ["PENDING", "PENDING_AUDIT", "SUBMITTED", "未审核", "未通过"].includes(statusCode.value),
);

const title = computed(() => `入库详情-${status.value}`);
const remark = computed(() =>
  remarkData.value.remark
    || (isRejected.value ? "入库信息审核未通过，请修改后重新提交。" : "无"),
);
const displayValue = (value: unknown) =>
  value === null || value === undefined || value === "" ? "-" : String(value);
const formatDate = (value: unknown) => {
  const text = displayValue(value);
  return text === "-" ? text : text.slice(0, 10);
};
const stockDate = computed(() =>
  formatDate(remarkData.value.stockDate || order.value?.createdAt),
);
const documentDate = computed(() =>
  formatDate(remarkData.value.documentDate || order.value?.createdAt),
);
const auditOperator = computed(() =>
  displayValue(remarkData.value.auditOperator || order.value?.auditOperator),
);
const stockTypeLabel = computed(() => ({
  PURCHASE_IN: "采购入库",
  RETURN_IN: "退货入库",
  OTHER_IN: "其他入库",
  采购入库: "采购入库",
  退货入库: "退货入库",
  其他入库: "其他入库",
  其它入库: "其他入库",
}[String(order.value?.stockType || "")] || displayValue(order.value?.stockType)));
const showReview = computed(() => isPending.value);
const showEdit = computed(() => isPending.value);
const showRestock = computed(() => !isPending.value);
const showDelete = computed(() => !status.value.includes("审核通过"));

const formatAmount = (value: unknown) => (Number(value) || 0).toFixed(2);
const medicines = computed(() => {
  const records = Array.isArray(remarkData.value.medicines)
    ? remarkData.value.medicines
    : [];
  return records.map((item: any, index: number) => {
    const quantity = Number(item.quantity) || 0;
    const purchasePrice = Number(item.purchasePrice) || 0;
    const retailPrice = Number(item.retailPrice ?? item.sellPrice) || 0;
    return {
      id: String(item.id ?? item.drugId ?? item.code ?? index),
      index: index + 1,
      code: displayValue(item.code || item.drugCode),
      name: displayValue(item.name || item.drugName),
      factory: displayValue(item.factory || item.manufacturer),
      quantity,
      unit: displayValue(item.unit),
      purchasePrice: formatAmount(purchasePrice),
      retailPrice: formatAmount(retailPrice),
      batchNo: displayValue(item.batchNo || item.batch),
      expireDate: formatDate(item.expireDate),
      purchaseAmount: formatAmount(quantity * purchasePrice),
      retailAmount: formatAmount(quantity * retailPrice),
    };
  });
});
const purchaseAmountTotal = computed(() =>
  formatAmount(order.value?.purchaseAmount
    ?? medicines.value.reduce((total, item) => total + Number(item.purchaseAmount), 0)),
);
const retailAmountTotal = computed(() =>
  formatAmount(order.value?.retailAmount
    ?? medicines.value.reduce((total, item) => total + Number(item.retailAmount), 0)),
);

const goBack = () => {
  router.push("/drug/stock-in");
};

const editStockIn = () => {
  router.push({
    path: "/drug/stock-in/add",
    state: { mode: "edit", id: orderId },
  });
};

const restockOrder = () => {
  router.push({
    path: "/drug/stock-in/add",
    state: { mode: "restock", id: orderId },
  });
};

onMounted(async () => {
  if (!orderId) {
    ElMessage.error("缺少入库单 ID，无法加载详情");
    return;
  }
  loading.value = true;
  try {
    const response: any = await stockInOrderApi.get(orderId);
    order.value = response?.data || null;
    if (!order.value) {
      ElMessage.error("未查询到入库单数据");
    }
  } catch {
    ElMessage.error("入库详情加载失败，请稍后重试");
  } finally {
    loading.value = false;
  }
});

const auditOrder = async () => {
  if (!orderId) return;
  await stockInOrderApi.audit(orderId, { approved: true, remark: "审核通过" });
  ElMessage.success("审核通过");
  router.push("/drugs/stock-in");
};

const deleteOrder = async () => {
  if (!orderId) return;
  await stockInOrderApi.remove(orderId);
  ElMessage.success("单据删除成功");
  router.push("/drugs/stock-in");
};
</script>

<style scoped>
.stock-detail-page {
  min-height: 100%;
  padding: 22px 0 58px;
}

.detail-card {
  width: min(1654px, calc(100% - 96px));
  min-height: 733px;
  margin: 0 auto;
  padding: 32px 32px 60px;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 33px;
}

.detail-header h1 {
  position: relative;
  margin: 0;
  padding-left: 16px;
  color: #1d2430;
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.detail-header h1::before {
  position: absolute;
  left: 0;
  top: 1px;
  width: 6px;
  height: 24px;
  content: "";
  background: #6570f2;
}

.header-actions {
  display: inline-flex;
  align-items: center;
  gap: 22px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-width: 147px;
  height: 48px;
  padding: 0 20px;
  border: 1px solid #636be8;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.action-btn.primary {
  background: #7b7fec;
  color: #fff;
}

.action-btn.outline {
  background: #fff;
  color: #636be8;
}

.action-icon {
  width: 25px;
  height: 25px;
}

.info-panel {
  display: grid;
  grid-template-columns: 250px 260px 210px 1fr;
  column-gap: 35px;
  row-gap: 32px;
  padding: 37px 28px 25px;
  min-height: 198px;
  box-sizing: border-box;
  background: #f0f1ff;
  color: #000;
  font-size: 18px;
}

.info-panel div {
  white-space: nowrap;
}

.info-panel strong {
  font-weight: 400;
}

.info-panel .remark {
  color: #f00;
}

.table-wrap {
  margin-top: 23px;
  overflow-x: auto;
}

.detail-table {
  width: 100%;
  min-width: 1534px;
  border-collapse: collapse;
  table-layout: fixed;
}

.detail-table th {
  height: 75px;
  background: #bfc2f6;
  color: #03060b;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
  text-align: center;
}

.detail-table td {
  height: 86px;
  border-bottom: 1px solid #eee;
  color: #000;
  font-size: 18px;
  text-align: center;
  white-space: nowrap;
}

.detail-table .empty-cell {
  color: #909399;
  text-align: center;
}

.detail-table th:nth-child(1),
.detail-table td:nth-child(1) {
  width: 80px;
}

.detail-table th:nth-child(2),
.detail-table td:nth-child(2) {
  width: 110px;
}

.detail-table th:nth-child(3),
.detail-table td:nth-child(3) {
  width: 190px;
}

.detail-table th:nth-child(4),
.detail-table td:nth-child(4) {
  width: 260px;
}

.detail-table th:nth-child(5),
.detail-table td:nth-child(5),
.detail-table th:nth-child(6),
.detail-table td:nth-child(6) {
  width: 78px;
}

.detail-table th:nth-child(7),
.detail-table td:nth-child(7),
.detail-table th:nth-child(8),
.detail-table td:nth-child(8) {
  width: 90px;
}

.detail-table th:nth-child(9),
.detail-table td:nth-child(9) {
  width: 138px;
}

.detail-table th:nth-child(10),
.detail-table td:nth-child(10) {
  width: 180px;
}

.detail-table th:nth-child(11),
.detail-table td:nth-child(11),
.detail-table th:nth-child(12),
.detail-table td:nth-child(12) {
  width: 120px;
}

.totals {
  display: flex;
  align-items: center;
  margin-top: 6px;
  color: #1d2430;
  font-size: 22px;
  line-height: 1;
}

.totals strong {
  margin: 0 18px 0 5px;
  color: #f00;
  font-size: 23px;
  letter-spacing: 1px;
}

.totals .retail-label {
  margin-left: 23px;
}

@media (max-width: 1280px) {
  .detail-card {
    width: calc(100% - 32px);
    padding: 24px 18px 42px;
  }

  .detail-header {
    flex-wrap: wrap;
  }

  .info-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .totals {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
