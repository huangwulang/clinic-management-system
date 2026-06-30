<template>
  <div class="refund-detail-page">
    <header class="detail-head">
      <div></div>
      <div class="head-actions">
        <button class="head-btn outline" type="button">
          <Printer class="head-icon" />
          <span>打印退费单</span>
        </button>
        <button class="head-btn outline" type="button" @click="goBack">
          <Back class="head-icon" />
          <span>返回</span>
        </button>
      </div>
    </header>

    <main class="detail-layout">
      <section class="refund-items-card">
        <h2><Tickets class="section-icon" />退费项目</h2>

        <table class="items-table">
          <colgroup>
            <col class="d-index" />
            <col class="d-name" />
            <col class="d-price" />
            <col class="d-qty" />
            <col class="d-unit" />
            <col class="d-amount" />
          </colgroup>
          <thead>
            <tr>
              <th>序号</th>
              <th>收费名称</th>
              <th>单价（元）</th>
              <th>退货数量</th>
              <th>单位</th>
              <th>退款金额（元）</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in refundItems" :key="item.index">
              <td>{{ item.index }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.price }}</td>
              <td>{{ item.quantity }}</td>
              <td>{{ item.unit }}</td>
              <td>{{ item.amount }}</td>
            </tr>
          </tbody>
        </table>

        <div v-if="refundFees.length" class="fee-line">
          <span v-for="fee in refundFees" :key="fee.name">{{ fee.name }}： <strong>{{ fee.amount }}元</strong></span>
        </div>
      </section>

      <aside class="side-card">
        <section v-for="block in infoBlocks" :key="block.title" :class="['info-block', { single: block.single }]">
          <h2><component :is="block.icon" class="section-icon" />{{ block.title }}</h2>
          <div class="info-grid">
            <template v-for="(row, rowIndex) in block.rows" :key="rowIndex">
              <div
                v-for="(cell, cellIndex) in row"
                :key="`${rowIndex}-${cellIndex}`"
                :class="['info-cell', { wide: block.single || row.length === 1 }]"
              >
                <span>{{ cell.label }}</span>
                <strong :class="cell.accent ? `accent-${cell.accent}` : ''">{{ cell.value }}</strong>
              </div>
            </template>
          </div>
        </section>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, type Component } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, Document, Notebook, Printer, Tickets, UserFilled } from "@element-plus/icons-vue";
import { chargeOrderApi, patientApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

type InfoCell = { label: string; value: string; accent?: "blue" | "gold" };
type InfoRow = InfoCell[];

const route = useRoute();
const router = useRouter();
const refundRecords = ref<any[]>([]);
const remoteOrder = ref<any>(null);
const remotePatient = ref<any>(null);
const orderId = readNavigationState(route);

const detail = computed(() => ({
  orderNo: String(remoteOrder.value?.orderNo || orderId || "-"),
  patientName: String(remotePatient.value?.name || remoteOrder.value?.patientName || "-"),
  age: String(remotePatient.value?.age ?? remoteOrder.value?.age ?? "-"),
  gender: String(remotePatient.value?.gender || remoteOrder.value?.gender || "-"),
  phone: String(remotePatient.value?.phone || remoteOrder.value?.phone || "-"),
  doctor: String(remoteOrder.value?.doctorName || "-"),
}));

const money = (value: unknown) => Number(value || 0).toFixed(2);

const refundRecord = computed(() => refundRecords.value?.[0] || remoteOrder.value || {});

const isJsonText = (value: unknown) => {
  if (typeof value !== "string") return false;
  const text = value.trim();
  return (text.startsWith("{") && text.endsWith("}")) || (text.startsWith("[") && text.endsWith("]"));
};

const refundRemark = computed(() => {
  const value = refundRecord.value?.refundRemark || refundRecord.value?.refundReason || "";
  if (value) return String(value);
  const remark = refundRecord.value?.remark;
  return isJsonText(remark) ? "-" : String(remark || "-");
});

const refundableAmount = computed(() => money(refundRecord.value?.paidAmount || refundRecord.value?.receivableAmount));

const toRefundItem = (item: any, index: number) => {
  const quantity = Number(item.quantity || 1);
  const amount = Number(item.discountAmount ?? item.amount ?? item.retailAmount ?? item.price ?? 0);
  return {
    index: index + 1,
    name: String(item.name || item.chargeName || item.projectName || item.drugName || remoteOrder.value?.chargeType || "收费项目"),
    price: money(item.price ?? (quantity ? amount / quantity : amount)),
    quantity,
    unit: String(item.unit || ""),
    amount: money(amount),
  };
};

const parsedRefundItems = computed(() => {
  const order = remoteOrder.value;
  if (!order?.remark) {
    return [];
  }
  try {
    const payload = JSON.parse(order.remark);
    if (Array.isArray(payload?.tabs)) {
      return payload.tabs.flatMap((tab: any) => Array.isArray(tab.rows) ? tab.rows : []).map(toRefundItem);
    }
    if (Array.isArray(payload?.items)) {
      return payload.items.map(toRefundItem);
    }
  } catch {
    return [];
  }
  return [];
});

const refundItems = computed(() => parsedRefundItems.value.length ? parsedRefundItems.value : [{
  index: 1,
  name: String(remoteOrder.value?.chargeType || "收费项目"),
  price: money(remoteOrder.value?.refundAmount || remoteOrder.value?.paidAmount || remoteOrder.value?.receivableAmount),
  quantity: 1,
  unit: "项",
  amount: money(remoteOrder.value?.refundAmount || remoteOrder.value?.paidAmount || remoteOrder.value?.receivableAmount),
}]);

const refundFees = computed(() => {
  if (!remoteOrder.value?.remark) return [];
  try {
    const payload = JSON.parse(remoteOrder.value.remark);
    return Array.isArray(payload?.fees)
      ? payload.fees.map((fee: any) => ({ name: String(fee.name || "附加费"), amount: money(fee.amount) }))
      : [];
  } catch {
    return [];
  }
});

const refundRows = computed<InfoRow[]>(() => [
  [{ label: "退费编号：", value: String(refundRecord.value?.orderNo || detail.value.orderNo) }],
  [{ label: "可退金额（元）：", value: refundableAmount.value }, { label: "实退金额（元）：", value: money(refundRecord.value?.refundAmount), accent: "gold" }],
  [{ label: "退款方式：", value: refundRecord.value?.refundMethod || "-" }, { label: "退费说明：", value: refundRemark.value }],
  [{ label: "退费日期：", value: refundRecord.value?.refundedAt || refundRecord.value?.updatedAt || "-" }, { label: "退费员：", value: refundRecord.value?.cashier || "-" }],
]);

const patientRows = computed<InfoRow[]>(() => [
  [{ label: "患者编号：", value: String(remotePatient.value?.patientCode || remotePatient.value?.cardNo || remoteOrder.value?.patientId || "-") }, { label: "患者姓名：", value: detail.value.patientName, accent: "blue" }],
  [{ label: "年龄：", value: detail.value.age }, { label: "性别：", value: detail.value.gender }],
  [{ label: "手机：", value: detail.value.phone }, { label: "会员等级：", value: String(remotePatient.value?.memberLevel || "-"), accent: "gold" }],
  [{ label: "账户余额：", value: "-" }, { label: "积分：", value: "-" }],
]);

const visitRows = computed<InfoRow[]>(() => [
  [{ label: "门诊编号：", value: String(remoteOrder.value?.registrationId || remoteOrder.value?.orderNo || "-") }],
  [{ label: "科室：", value: remoteOrder.value?.departmentName || "-" }],
  [{ label: "接诊医生：", value: detail.value.doctor }],
  [{ label: "接诊日期：", value: remoteOrder.value?.refundedAt || remoteOrder.value?.paidAt || remoteOrder.value?.createdAt || "-" }],
  [{ label: "接诊类型：", value: remoteOrder.value?.chargeType || "-" }],
]);

const infoBlocks = computed<Array<{ title: string; icon: Component; rows: InfoRow[]; single?: boolean }>>(() => [
  { title: "项目信息", icon: Document, rows: refundRows.value },
  { title: "人员信息", icon: UserFilled, rows: patientRows.value },
  { title: "接诊信息", icon: Notebook, rows: visitRows.value, single: true },
]);

const goBack = () => router.push("/charge");


onMounted(async () => {
  if (!orderId) return;
  const [orderResponse, refundResponse]: any[] = await Promise.all([
    chargeOrderApi.get(orderId),
    chargeOrderApi.refunds(orderId),
  ]);
  remoteOrder.value = orderResponse?.data || null;
  refundRecords.value = refundResponse?.data || [];
  if (remoteOrder.value?.patientId) {
    const patientResponse: any = await patientApi.get(remoteOrder.value.patientId);
    remotePatient.value = patientResponse?.data || null;
  }
});
</script>

<style scoped>
.refund-detail-page {
  min-height: 100%;
  padding: 24px 0 56px;
}

.detail-head,
.detail-layout {
  width: min(1760px, calc(100% - 104px));
  margin-left: 52px;
  margin-right: auto;
}

.detail-head,
.refund-items-card,
.side-card {
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.detail-head {
  height: 113px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 36px;
}

.head-actions {
  display: flex;
  gap: 23px;
}

.head-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-width: 156px;
  height: 51px;
  border-radius: 5px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.head-icon {
  width: 25px;
  height: 25px;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 566px;
  gap: 14px;
  margin-top: 15px;
  align-items: start;
}

.refund-items-card {
  min-height: 451px;
  padding: 32px 34px 32px;
  min-width: 0;
}

.side-card {
  min-height: 1070px;
  padding: 30px 29px 32px;
  min-width: 0;
}

.refund-items-card h2,
.info-block h2 {
  display: flex;
  align-items: center;
  margin: 0;
  color: #636be8;
  font-size: 29px;
  font-weight: 700;
}

.section-icon {
  width: 28px;
  height: 28px;
  margin-right: 12px;
  color: #636be8;
}

.items-table {
  width: 100%;
  margin-top: 42px;
  border-collapse: collapse;
  table-layout: fixed;
}

.items-table th,
.items-table td {
  border: 1px solid #e2e5ff;
  text-align: center;
}

.items-table th {
  height: 70px;
  background: #d8daf7;
  color: #111;
  font-size: 20px;
  font-weight: 700;
}

.items-table td {
  height: 54px;
  color: #111722;
  font-size: 20px;
}

.items-table .d-index { width: 12.6%; }
.items-table .d-name { width: 30.2%; }
.items-table .d-price { width: 15%; }
.items-table .d-qty { width: 11.2%; }
.items-table .d-unit { width: 11.2%; }
.items-table .d-amount { width: 19.8%; }

.fee-line {
  display: flex;
  justify-content: flex-end;
  gap: 33px;
  margin-top: 23px;
  color: #111722;
  font-size: 20px;
}

.fee-line strong {
  font-weight: 400;
}

.info-block + .info-block {
  margin-top: 32px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin-top: 30px;
  border-top: 1px solid #e2e5ff;
  border-left: 1px solid #e2e5ff;
}

.info-block.single .info-grid {
  grid-template-columns: 1fr;
}

.info-cell {
  min-height: 55px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  box-sizing: border-box;
  border-right: 1px solid #e2e5ff;
  border-bottom: 1px solid #e2e5ff;
  color: #111722;
  font-size: 18px;
  white-space: nowrap;
  overflow: hidden;
}

.info-cell.wide {
  grid-column: 1 / -1;
}

.info-cell strong {
  font-weight: 400;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-cell span {
  min-width: 0;
}

.accent-blue {
  color: #636be8;
  font-weight: 700 !important;
}

.accent-gold {
  color: #ffb800;
  font-weight: 700 !important;
}

@media (max-width: 1280px) {
  .detail-head,
  .detail-layout {
    width: calc(100% - 32px);
    margin-left: 16px;
    margin-right: 16px;
  }

  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
