<template>
  <div class="inventory-detail-page">
    <section v-loading="loading" class="detail-card">
      <header class="detail-header">
        <h1>查看库存明细</h1>
        <button class="back-btn" type="button" @click="goBack">
          <Back class="back-icon" />
          <span>返回</span>
        </button>
      </header>

      <section class="info-panel">
        <div><span>药品编码：</span><strong>{{ displayValue(inventory?.drugCode) }}</strong></div>
        <div><span>药品名称：</span><strong>{{ displayValue(inventory?.drugName) }}</strong></div>
        <div><span>收费类型：</span><strong>{{ displayValue(inventory?.category || drug?.category) }}</strong></div>
        <div><span>药品规格：</span><strong>{{ displayValue(inventory?.specification || drug?.specification) }}</strong></div>
        <div><span>药品剂型：</span><strong>{{ displayValue(inventory?.dosageForm || drug?.dosageForm) }}</strong></div>
        <div><span>生产厂家：</span><strong>{{ displayValue(inventory?.manufacturer || drug?.manufacturer) }}</strong></div>
        <div><span>库存数量：</span><strong>{{ inventoryQuantity }}</strong></div>
      </section>

      <h2>库存明细</h2>

      <div class="table-wrap">
        <table class="detail-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>单号</th>
              <th>操作时间</th>
              <th>类型</th>
              <th>数量</th>
              <th>批号</th>
              <th>采购金额(元)</th>
              <th>零售金额（元）</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.index }}</td>
              <td>{{ row.orderNo }}</td>
              <td>{{ row.time }}</td>
              <td>{{ row.type }}</td>
              <td :class="{ 'negative-quantity': row.changeQuantity < 0 }">{{ row.quantity }}</td>
              <td>{{ row.batch }}</td>
              <td>{{ row.purchaseAmount }}</td>
              <td>{{ row.retailAmount }}</td>
            </tr>
            <tr v-if="!loading && rows.length === 0">
              <td class="empty-cell" colspan="8">暂无库存变动记录</td>
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
import { Back } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { drugApi, inventoryApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const inventoryId = readNavigationState(route);

const inventory = ref<any>(null);
const drug = ref<any>(null);
const logs = ref<any[]>([]);
const loading = ref(false);

const displayValue = (value: unknown) =>
  value === null || value === undefined || value === "" ? "-" : String(value);
const formatAmount = (value: unknown) => (Number(value) || 0).toFixed(2);
const inventoryQuantity = computed(() =>
  `${Number(inventory.value?.quantity || 0)}${inventory.value?.unit || drug.value?.unit || ""}`,
);

const typeLabels: Record<string, string> = {
  STOCK_IN: "采购入库",
  INCREASE: "入库调整",
  RETURN_IN: "退货入库",
  SALE: "处方发药",
  RETAIL: "零售",
  STOCK_OUT: "出库",
  DECREASE: "出库调整",
  CHECK_ADJUST: "盘点",
  PRESCRIPTION: "处方",
};

const rows = computed(() => {
  const purchasePrice = Number(drug.value?.purchasePrice || 0);
  const retailPrice = Number(drug.value?.sellPrice || 0);
  const unit = inventory.value?.unit || drug.value?.unit || "";
  return logs.value.map((item: any, index: number) => {
    const changeQuantity = Number(item.changeQuantity || 0);
    const amountQuantity = Math.abs(changeQuantity);
    const businessType = String(item.businessType || "").toUpperCase();
    return {
      id: item.id || index,
      index: index + 1,
      orderNo: displayValue(item.businessNo || item.id),
      time: displayValue(item.createdAt),
      type: typeLabels[businessType] || displayValue(item.businessType),
      changeQuantity,
      quantity: `${changeQuantity > 0 ? "+" : ""}${changeQuantity}${unit}`,
      batch: displayValue(inventory.value?.batchNo),
      purchaseAmount: formatAmount(amountQuantity * purchasePrice),
      retailAmount: formatAmount(amountQuantity * retailPrice),
    };
  });
});

const goBack = () => {
  router.push("/drugs/inventory");
};

onMounted(async () => {
  if (!inventoryId) {
    ElMessage.error("缺少库存 ID，无法加载库存明细");
    return;
  }
  loading.value = true;
  try {
    const [inventoryResponse, logResponse]: any[] = await Promise.all([
      inventoryApi.get(inventoryId),
      inventoryApi.logs(inventoryId),
    ]);
    inventory.value = inventoryResponse?.data || null;
    logs.value = Array.isArray(logResponse?.data) ? logResponse.data : [];
    if (!inventory.value) {
      ElMessage.error("未查询到库存数据");
      return;
    }
    if (inventory.value.drugId) {
      const drugResponse: any = await drugApi.get(inventory.value.drugId);
      drug.value = drugResponse?.data || null;
    }
  } catch {
    ElMessage.error("库存明细加载失败，请稍后重试");
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.inventory-detail-page {
  min-height: 100%;
  padding: 22px 0 58px;
}

.detail-card {
  width: min(1650px, calc(100% - 96px));
  min-height: 900px;
  margin: 0 auto;
  padding: 38px 32px 54px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.08);
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 43px;
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
  top: 0;
  width: 6px;
  height: 25px;
  content: "";
  background: #6570f2;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 145px;
  height: 48px;
  border: 1px solid #636be8;
  border-radius: 5px;
  background: #fff;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.back-icon {
  width: 25px;
  height: 25px;
}

.info-panel {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  column-gap: 48px;
  row-gap: 34px;
  min-height: 160px;
  padding: 34px 28px;
  box-sizing: border-box;
  background: #f0f1ff;
  color: #000;
  font-size: 18px;
}

.info-panel div {
  min-width: 0;
}

.info-panel strong {
  font-weight: 400;
}

h2 {
  margin: 36px 0 30px;
  color: #5f68f0;
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.table-wrap {
  overflow-x: auto;
}

.detail-table {
  width: 100%;
  min-width: 980px;
  border-collapse: collapse;
  table-layout: fixed;
}

.detail-table th {
  height: 72px;
  background: #bfc2f6;
  color: #03060b;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
}

.detail-table td {
  height: 84px;
  border-bottom: 1px solid #eee;
  color: #000;
  font-size: 18px;
  text-align: center;
  white-space: nowrap;
}

.detail-table .negative-quantity {
  color: #f04444;
}

.detail-table .empty-cell {
  height: 140px;
  color: #909399;
}

.detail-table th:nth-child(1),
.detail-table td:nth-child(1) { width: 55px; }
.detail-table th:nth-child(2),
.detail-table td:nth-child(2) { width: 145px; }
.detail-table th:nth-child(3),
.detail-table td:nth-child(3) { width: 170px; }
.detail-table th:nth-child(4),
.detail-table td:nth-child(4) { width: 100px; }
.detail-table th:nth-child(5),
.detail-table td:nth-child(5) { width: 95px; }
.detail-table th:nth-child(6),
.detail-table td:nth-child(6) { width: 125px; }
.detail-table th:nth-child(7),
.detail-table td:nth-child(7),
.detail-table th:nth-child(8),
.detail-table td:nth-child(8) { width: 145px; }

@media (max-width: 1280px) {
  .detail-card {
    width: calc(100% - 32px);
    padding: 28px 18px 42px;
  }

  .info-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 24px;
  }

  .detail-table th,
  .detail-table td {
    font-size: 16px;
  }
}
</style>
