<template>
  <div class="price-form-page">
    <section class="price-card">
      <header class="page-header">
        <h1>新增调价</h1>
        <div class="header-actions">
          <button class="primary-btn" type="button" :disabled="submitting" @click="saveAdjustment">
            <CircleCheckFilled class="btn-icon" />
            <span>{{ submitting ? "提交中..." : "完成调价" }}</span>
          </button>
          <button class="outline-btn" type="button" @click="goBack">
            <Back class="btn-icon" />
            <span>返回</span>
          </button>
        </div>
      </header>

      <div class="divider"></div>

      <div class="table-wrap">
        <table class="adjust-table">
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
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td class="empty-cell" colspan="13">正在加载库存数据...</td>
            </tr>
            <tr v-else-if="!rows.length">
              <td class="empty-cell" colspan="13">未查询到该药品的库存数据</td>
            </tr>
            <tr v-for="(row, index) in rows" v-else :key="row.inventoryId || index">
              <td>{{ index + 1 }}</td>
              <td>{{ row.drugCode || "-" }}</td>
              <td :title="row.drugName">{{ row.drugName || "-" }}</td>
              <td>-</td>
              <td :title="row.specification">{{ row.specification || "-" }}</td>
              <td>{{ formatStock(row) }}</td>
              <td>{{ row.batchNo || "-" }}</td>
              <td>{{ formatMoney(row.oldPurchasePrice) }}</td>
              <td>{{ formatMoney(row.oldSellPrice) }}</td>
              <td>
                <input
                  v-model="newSellPrice"
                  class="price-input"
                  type="number"
                  min="0.01"
                  step="0.01"
                />
              </td>
              <td class="danger-text">{{ difference }}</td>
              <td class="danger-text">{{ ratio }}</td>
              <td>
                <input v-model.trim="row.remark" class="remark-input" type="text" placeholder="备注" />
              </td>
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
import { Back, CircleCheckFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { priceAdjustOrderApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

type AdjustmentRow = {
  inventoryId?: number;
  drugId?: number;
  drugCode?: string;
  drugName?: string;
  specification?: string;
  quantity?: number;
  unit?: string;
  batchNo?: string;
  oldPurchasePrice?: number;
  oldSellPrice?: number;
  remark: string;
};

const route = useRoute();
const router = useRouter();
const drugId = computed(() => String(route.query.drugId || readNavigationState(route) || ""));
const rows = ref<AdjustmentRow[]>([]);
const newSellPrice = ref("");
const loading = ref(false);
const submitting = ref(false);

const currentSellPrice = computed(() => Number(rows.value[0]?.oldSellPrice || 0));
const difference = computed(() => {
  const value = Number(newSellPrice.value);
  return Number.isFinite(value) ? (value - currentSellPrice.value).toFixed(2) : "-";
});
const ratio = computed(() => {
  const value = Number(newSellPrice.value);
  if (!Number.isFinite(value) || currentSellPrice.value <= 0) return "-";
  return `${((value / currentSellPrice.value) * 100).toFixed(2)}%`;
});

const formatMoney = (value?: number) => Number(value || 0).toFixed(2);
const formatStock = (row: AdjustmentRow) => `${Number(row.quantity || 0)}${row.unit || ""}`;

const goBack = () => {
  router.push("/drugs/price-adjust");
};

const loadRows = async () => {
  if (!drugId.value) {
    ElMessage.error("缺少药品编号");
    return;
  }
  loading.value = true;
  try {
    const response: any = await priceAdjustOrderApi.form(drugId.value);
    rows.value = (response?.data || []).map((item: any) => ({ ...item, remark: "" }));
    newSellPrice.value = rows.value.length
      ? Number(rows.value[0].oldSellPrice || 0).toFixed(2)
      : "";
  } finally {
    loading.value = false;
  }
};

const saveAdjustment = async () => {
  if (!rows.value.length || !drugId.value) {
    ElMessage.warning("没有可提交的药品数据");
    return;
  }
  const newPrice = Number(newSellPrice.value);
  if (!Number.isFinite(newPrice) || newPrice <= 0) {
    ElMessage.warning("请输入大于0的现零售价");
    return;
  }
  if (Math.abs(newPrice - currentSellPrice.value) < 0.000001) {
    ElMessage.warning("现零售价不能与原零售价相同");
    return;
  }

  const remarks = [...new Set(rows.value.map((row) => row.remark).filter(Boolean))];
  submitting.value = true;
  try {
    await priceAdjustOrderApi.complete({
      drugId: Number(drugId.value),
      newPurchasePrice: Number(rows.value[0].oldPurchasePrice || 0),
      newSellPrice: newPrice,
      operator: localStorage.getItem("rememberedAccount") || "系统管理员",
      reason: remarks.join("；") || "药品调价",
    });
    ElMessage.success("调价已完成");
    router.replace({
      path: "/drugs/price-adjust/detail",
      query: { drugId: drugId.value },
    });
  } finally {
    submitting.value = false;
  }
};

onMounted(loadRows);
</script>

<style scoped>
.price-form-page {
  min-height: 100%;
  padding: 29px 0 58px;
}

.price-card {
  width: min(1660px, calc(100% - 96px));
  min-height: 801px;
  margin: 0 auto;
  padding: 36px 34px 40px;
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
  gap: 20px;
}

.primary-btn,
.outline-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 150px;
  height: 48px;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn {
  border: 1px solid #636be8;
  background: #636be8;
  color: #fff;
}

.primary-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.outline-btn {
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
}

.btn-icon {
  width: 24px;
  height: 24px;
}

.divider {
  height: 1px;
  margin: 28px 0 27px;
  background: #eee;
}

.table-wrap {
  overflow-x: auto;
}

.adjust-table {
  width: 100%;
  min-width: 1450px;
  border-collapse: collapse;
  table-layout: fixed;
}

.adjust-table th {
  height: 76px;
  padding: 0 8px;
  background: #bfc2f6;
  color: #03060b;
  font-size: 17px;
  line-height: 1.35;
  white-space: nowrap;
}

.adjust-table td {
  height: 88px;
  padding: 0 8px;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.adjust-table th:nth-child(1) { width: 4%; }
.adjust-table th:nth-child(2) { width: 7%; }
.adjust-table th:nth-child(3) { width: 13%; }
.adjust-table th:nth-child(4) { width: 10%; }
.adjust-table th:nth-child(5) { width: 9%; }
.adjust-table th:nth-child(6) { width: 6%; }
.adjust-table th:nth-child(7) { width: 8%; }
.adjust-table th:nth-child(8),
.adjust-table th:nth-child(9),
.adjust-table th:nth-child(10) { width: 7%; }
.adjust-table th:nth-child(11),
.adjust-table th:nth-child(12) { width: 5%; }
.adjust-table th:nth-child(13) { width: 12%; }

.price-input,
.remark-input {
  box-sizing: border-box;
  height: 40px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  font-size: 16px;
}

.price-input {
  width: 82px;
  text-align: center;
}

.remark-input {
  width: 100%;
  padding: 0 12px;
}

.danger-text {
  color: #ff4545;
}

.empty-cell {
  height: 150px !important;
  color: #8a8f9d;
}

@media (max-width: 1280px) {
  .price-card {
    width: calc(100% - 32px);
    padding: 28px 18px 36px;
  }
}
</style>
