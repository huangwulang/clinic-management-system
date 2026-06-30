<template>
  <div class="refund-page">
    <header class="refund-head">
      <h1>订单详情-退费</h1>
      <div class="head-actions">
        <button class="head-btn primary" type="button" @click="refundDialogVisible = true">
          <CircleCheckFilled class="head-icon" />
          <span>退费</span>
        </button>
        <button class="head-btn outline" type="button" @click="goBack">
          <Back class="head-icon" />
          <span>返回</span>
        </button>
      </div>
    </header>

    <main class="refund-layout">
      <section class="refund-card">
        <h2><Tickets class="section-icon" />退费项目</h2>

        <section v-for="group in refundGroups" :key="group.title" class="refund-group">
          <div class="group-head">
            <div class="group-tab">{{ group.title }}</div>
          </div>

          <table class="refund-table">
            <colgroup>
              <col class="c-check" />
              <col class="c-index" />
              <col class="c-name" />
              <col class="c-price" />
              <col class="c-qty" />
              <col class="c-unit" />
              <col class="c-retail" />
              <col class="c-discount" />
              <col class="c-amount" />
              <col class="c-returnable" />
              <col class="c-returned" />
              <col class="c-refund-qty" />
              <col class="c-refund-amount" />
            </colgroup>
            <thead>
              <tr>
                <th v-for="(header, index) in refundHeaders" :key="header || index">
                  <input v-if="index === 0" type="checkbox" />
                  <span v-else>{{ header }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in group.items" :key="item.index">
                <td><input type="checkbox" /></td>
                <td>{{ item.index }}</td>
                <td>{{ item.name }}</td>
                <td>{{ item.price }}</td>
                <td>{{ item.quantity }}</td>
                <td>{{ item.unit }}</td>
                <td>{{ item.retail }}</td>
                <td>{{ item.discount }}</td>
                <td>{{ item.amount }}</td>
                <td>{{ item.refundable }}</td>
                <td>{{ item.refunded }}</td>
                <td><input class="mini-input" /></td>
                <td><input class="mini-input" /></td>
              </tr>
            </tbody>
          </table>

          <div v-if="refundFees.length" class="fee-line">
            <label v-for="fee in refundFees" :key="fee.name">
              <input type="checkbox" />
              <span>{{ fee.name }}： {{ fee.amount }}元</span>
              <input class="fee-input" :value="fee.amount" />
            </label>
          </div>
        </section>
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

    <div v-if="refundDialogVisible" class="refund-modal-mask">
      <section class="refund-dialog" role="dialog" aria-modal="true" aria-label="退费信息确认">
        <button class="dialog-close" type="button" aria-label="关闭" @click="refundDialogVisible = false">
          <Close class="dialog-close-icon" />
        </button>

        <h3>退费信息确认</h3>

        <section class="dialog-summary">
          <h4>收费信息</h4>
          <div class="summary-row">
            <span>应收金额： {{ dialogSummary.receivableAmount }} 元</span>
            <span>实收金额： {{ dialogSummary.paidAmount }} 元</span>
            <span>可退金额： {{ dialogSummary.refundableAmount }} 元</span>
            <span>支付方式： {{ dialogSummary.payMethod }}</span>
          </div>
        </section>

        <h4 class="dialog-section-title">退费明细</h4>
        <table class="dialog-table">
          <thead>
            <tr>
              <th>编码</th>
              <th>收费名称</th>
              <th>单价（元）</th>
              <th>退费数量</th>
              <th>单位</th>
              <th>退费金额（元）</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in confirmItems" :key="item.code">
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.price }}</td>
              <td>{{ item.quantity }}</td>
              <td>{{ item.unit }}</td>
              <td>{{ item.amount }}</td>
            </tr>
          </tbody>
        </table>

        <div v-if="refundFees.length" class="dialog-extra-fee">
          <span>附加费用</span>
          <span v-for="fee in refundFees" :key="fee.name">{{ fee.name }}： {{ fee.amount }}元</span>
        </div>

        <div class="dialog-divider"></div>

        <div class="dialog-form-row">
          <label class="actual-amount">
            <span>实退金额：</span>
            <input :value="detailRefundAmount" readonly />
            <em>元</em>
          </label>

          <div class="refund-methods">
            <span>退费方式：</span>
            <label v-for="method in refundMethods" :key="method">
              <input v-model="selectedRefundMethod" type="radio" name="refund-method" :value="method" />
              <span>{{ method }}</span>
            </label>
          </div>
        </div>

        <label class="refund-reason">
          <span>退费说明：</span>
          <input v-model="refundReason" />
        </label>

        <footer class="dialog-actions">
          <button class="dialog-btn cancel" type="button" @click="refundDialogVisible = false">取消</button>
          <button class="dialog-btn confirm" type="button" @click="confirmRefund">确定</button>
        </footer>
      </section>
    </div>

    <div v-if="successToastVisible" class="success-toast">退费成功</div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, type Component } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Back,
  CircleCheckFilled,
  Close,
  Document,
  Notebook,
  Tickets,
  UserFilled,
} from "@element-plus/icons-vue";
import { chargeOrderApi, patientApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

type RefundItem = {
  index: number;
  code: string;
  name: string;
  price: string;
  quantity: number;
  unit: string;
  retail: string;
  discount: string;
  amount: string;
  refundable: number;
  refunded: number;
};

type InfoCell = { label: string; value: string; accent?: "blue" | "gold" };
type InfoRow = InfoCell[];

const route = useRoute();
const router = useRouter();
const refundDialogVisible = ref(false);
const successToastVisible = ref(false);
const remoteOrder = ref<any>(null);
const remotePatient = ref<any>(null);
const selectedRefundMethod = ref("");
const refundReason = ref("");
const orderId = readNavigationState(route);
let successTimer: ReturnType<typeof window.setTimeout> | undefined;

const detail = computed(() => ({
  orderNo: String(remoteOrder.value?.orderNo || orderId || "-"),
  patientName: String(remotePatient.value?.name || remoteOrder.value?.patientName || "-"),
  age: String(remotePatient.value?.age ?? remoteOrder.value?.age ?? "-"),
  gender: String(remotePatient.value?.gender || remoteOrder.value?.gender || "-"),
  phone: String(remotePatient.value?.phone || remoteOrder.value?.phone || "-"),
  doctor: String(remoteOrder.value?.doctorName || "-"),
}));

const money = (value: unknown) => Number(value || 0).toFixed(2);

const toRefundItem = (item: any, index: number): RefundItem => {
  const quantity = Number(item.quantity || 1);
  const retail = Number(item.retailAmount ?? item.amount ?? item.price ?? 0);
  const amount = Number(item.discountAmount ?? item.amount ?? retail);
  return {
    index: index + 1,
    code: String(item.code || item.drugCode || item.projectCode || item.id || item.drugId || index + 1).padStart(6, "0"),
    name: String(item.name || item.chargeName || item.projectName || item.drugName || remoteOrder.value?.chargeType || "收费项目"),
    price: money(item.price ?? (quantity ? amount / quantity : amount)),
    quantity,
    unit: String(item.unit || ""),
    retail: money(retail),
    discount: money(item.discount ?? item.discountRate ?? 0),
    amount: money(amount),
    refundable: quantity,
    refunded: Number(remoteOrder.value?.refundAmount || 0) > 0 ? quantity : 0,
  };
};

const parseRefundGroups = () => {
  const order = remoteOrder.value;
  if (!order?.remark) {
    return [];
  }
  try {
    const payload = JSON.parse(order.remark);
    if (Array.isArray(payload?.tabs)) {
      return payload.tabs
        .map((tab: any) => ({
          title: String(tab.label || tab.name || "收费明细"),
          items: Array.isArray(tab.rows) ? tab.rows.map(toRefundItem) : [],
        }))
        .filter((group: any) => group.items.length);
    }
    if (Array.isArray(payload?.items)) {
      return [{ title: String(payload.title || order.chargeType || "收费明细"), items: payload.items.map(toRefundItem) }];
    }
  } catch {
    return [];
  }
  return [];
};

const refundHeaders = [
  "",
  "序号",
  "收费名称",
  "单价\n(元)",
  "数量",
  "单位",
  "零售金额\n(元)",
  "折扣",
  "折后金额\n(元)",
  "可退数量",
  "已退数量",
  "退费数量\n(元)",
  "退款金额\n(元)",
];

const fallbackRefundItem = computed<RefundItem>(() => ({
  index: 1,
  code: String(remoteOrder.value?.orderNo || orderId || "1"),
  name: String(remoteOrder.value?.chargeType || "收费项目"),
  price: money(remoteOrder.value?.paidAmount || remoteOrder.value?.receivableAmount),
  quantity: 1,
  unit: "项",
  retail: money(remoteOrder.value?.receivableAmount),
  discount: money(remoteOrder.value?.discountAmount),
  amount: money(remoteOrder.value?.paidAmount || remoteOrder.value?.receivableAmount),
  refundable: 1,
  refunded: Number(remoteOrder.value?.refundAmount || 0) > 0 ? 1 : 0,
}));

const refundGroups = computed(() => {
  const groups = parseRefundGroups();
  return groups.length ? groups : [{ title: String(remoteOrder.value?.chargeType || "收费明细"), items: [fallbackRefundItem.value] }];
});

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

const confirmItems = computed(() => refundGroups.value.flatMap((group) => group.items).map((item, index) => ({
  code: item.code || String(index + 1).padStart(6, "0"),
  name: item.name,
  price: item.price,
  quantity: item.quantity,
  unit: item.unit,
  amount: item.amount,
})));

const detailRefundAmount = computed(() => {
  const itemsTotal = confirmItems.value.reduce((total, item) => total + Number(item.amount || 0), 0);
  const feesTotal = refundFees.value.reduce((total, item) => total + Number(item.amount || 0), 0);
  const total = itemsTotal + feesTotal;
  const refundable = Number(refundableAmount.value || 0);
  return money(total > 0 ? Math.min(total, refundable) : refundable);
});

const refundableAmount = computed(() => money(
  Math.max(
    0,
    Number(remoteOrder.value?.paidAmount || remoteOrder.value?.receivableAmount || 0) - Number(remoteOrder.value?.refundAmount || 0),
  ),
));

const dialogSummary = computed(() => ({
  receivableAmount: money(remoteOrder.value?.receivableAmount),
  paidAmount: money(remoteOrder.value?.paidAmount || remoteOrder.value?.receivableAmount),
  refundableAmount: refundableAmount.value,
  payMethod: remoteOrder.value?.payMethod || "-",
}));

const refundMethods = ["现金", "支付宝", "微信", "银行卡", "会员卡"];

const orderRows = computed<InfoRow[]>(() => [
  [{ label: "订单编号：", value: detail.value.orderNo }],
  [{ label: "应收金额（元）：", value: Number(remoteOrder.value?.receivableAmount || 0).toFixed(2) }, { label: "实收金额（元）：", value: Number(remoteOrder.value?.paidAmount || 0).toFixed(2), accent: "gold" }],
  [{ label: "优惠金额（元）：", value: Number(remoteOrder.value?.discountAmount || 0).toFixed(2) }, { label: "医保支付（元）：", value: "0.00" }],
  [{ label: "支付方式：", value: remoteOrder.value?.payMethod || "-" }, { label: "找零（元）：", value: "0.00元" }],
  [{ label: "收费日期：", value: remoteOrder.value?.paidAt || remoteOrder.value?.createdAt || "-" }, { label: "收费员：", value: remoteOrder.value?.cashier || "-" }],
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
  [{ label: "接诊日期：", value: remoteOrder.value?.paidAt || remoteOrder.value?.createdAt || "-" }],
  [{ label: "接诊类型：", value: remoteOrder.value?.chargeType || "-" }],
]);

const infoBlocks = computed<Array<{ title: string; icon: Component; rows: InfoRow[]; single?: boolean }>>(() => [
  { title: "订单信息", icon: Document, rows: orderRows.value },
  { title: "人员信息", icon: UserFilled, rows: patientRows.value },
  { title: "接诊信息", icon: Notebook, rows: visitRows.value, single: true },
]);

const goBack = () => router.push("/charge");

const confirmRefund = async () => {
  await chargeOrderApi.refund(orderId, {
    refundAmount: Number(detailRefundAmount.value),
    refundMethod: selectedRefundMethod.value || remoteOrder.value?.payMethod || "现金",
    remark: refundReason.value,
  });
  refundDialogVisible.value = false;
  successToastVisible.value = true;

  if (successTimer) {
    window.clearTimeout(successTimer);
  }

  successTimer = window.setTimeout(() => {
    successToastVisible.value = false;
    router.push({
      name: "ChargeRefundDetail",
      state: { id: orderId },
    });
  }, 2000);
};


onMounted(async () => {
  if (!orderId) return;
  const response: any = await chargeOrderApi.get(orderId);
  remoteOrder.value = response?.data || null;
  selectedRefundMethod.value = remoteOrder.value?.payMethod || "现金";
  if (remoteOrder.value?.patientId) {
    const patientResponse: any = await patientApi.get(remoteOrder.value.patientId);
    remotePatient.value = patientResponse?.data || null;
  }
});
</script>

<style scoped>
.refund-page {
  min-height: 100%;
  padding: 18px 0 56px;
}

.refund-head,
.refund-layout {
  width: min(1778px, calc(100% - 88px));
  margin-left: 44px;
  margin-right: auto;
}

.refund-head,
.refund-card,
.side-card {
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.refund-head {
  height: 115px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 36px 0 33px;
}

.refund-head h1 {
  position: relative;
  margin: 0;
  padding-left: 18px;
  color: #111722;
  font-size: 25px;
  font-weight: 700;
}

.refund-head h1::before {
  position: absolute;
  left: 0;
  top: 1px;
  width: 6px;
  height: 25px;
  background: #636be8;
  content: "";
}

.head-actions {
  display: flex;
  gap: 21px;
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

.head-btn.primary {
  background: #636be8;
  color: #fff;
}

.head-icon {
  width: 25px;
  height: 25px;
}

.refund-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 574px;
  gap: 14px;
  margin-top: 15px;
  align-items: start;
}

.refund-card {
  min-height: 1120px;
  padding: 31px 34px 34px;
  min-width: 0;
}

.side-card {
  min-height: 1120px;
  padding: 30px 29px 32px;
  min-width: 0;
}

.refund-card h2,
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

.refund-group {
  margin-top: 43px;
}

.refund-group + .refund-group {
  margin-top: 39px;
}

.group-head {
  display: flex;
  align-items: flex-end;
  height: 45px;
  border-bottom: 3px solid #f2f2f2;
}

.group-tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 43px;
  min-width: 132px;
  padding: 0 17px;
  box-sizing: border-box;
  border-radius: 5px 5px 0 0;
  background: #636be8;
  color: #fff;
  font-size: 20px;
}

.refund-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.refund-table th,
.refund-table td {
  border: 1px solid #e2e5ff;
  text-align: center;
}

.refund-table th {
  height: 69px;
  background: #d8daf7;
  color: #111;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.22;
  white-space: pre-line;
}

.refund-table td {
  height: 54px;
  color: #111722;
  font-size: 18px;
}

.refund-table .c-check { width: 5.4%; }
.refund-table .c-index { width: 5.4%; }
.refund-table .c-name { width: 13.6%; }
.refund-table .c-price { width: 6.8%; }
.refund-table .c-qty { width: 5.1%; }
.refund-table .c-unit { width: 5.1%; }
.refund-table .c-retail { width: 8.9%; }
.refund-table .c-discount { width: 6.4%; }
.refund-table .c-amount { width: 8.8%; }
.refund-table .c-returnable { width: 6.4%; }
.refund-table .c-returned { width: 6.4%; }
.refund-table .c-refund-qty { width: 10.6%; }
.refund-table .c-refund-amount { width: 10.6%; }

.refund-table input[type="checkbox"],
.fee-line input[type="checkbox"] {
  width: 20px;
  height: 20px;
  accent-color: #636be8;
}

.mini-input,
.fee-input {
  width: min(69px, calc(100% - 18px));
  height: 39px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
  color: #000;
  font-size: 18px;
  text-align: center;
}

.fee-line {
  display: flex;
  justify-content: flex-end;
  gap: 34px;
  margin-top: 15px;
  color: #111722;
  font-size: 20px;
  flex-wrap: nowrap;
}

.fee-line label {
  display: inline-flex;
  align-items: center;
  gap: 10px;
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

.info-cell span,
.info-cell strong {
  min-width: 0;
}

.info-cell strong {
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
}

.accent-blue {
  color: #636be8;
  font-weight: 700 !important;
}

.accent-gold {
  color: #ffb800;
  font-weight: 700 !important;
}

.refund-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1200;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.42);
}

.refund-dialog {
  position: relative;
  width: 1042px;
  min-height: 700px;
  padding: 24px 26px 26px;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.22);
}

.dialog-close {
  position: absolute;
  top: 24px;
  right: 25px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #d0d0d0;
  cursor: pointer;
}

.dialog-close-icon {
  width: 26px;
  height: 26px;
}

.refund-dialog h3 {
  margin: 0 0 18px;
  color: #111722;
  font-size: 24px;
  font-weight: 700;
}

.dialog-summary {
  height: 104px;
  padding: 0 27px;
  box-sizing: border-box;
  border-radius: 4px;
  background: #f0f1ff;
}

.dialog-summary h4 {
  margin: 0;
  padding-top: 10px;
  color: #111722;
  font-size: 22px;
  font-weight: 700;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(4, max-content);
  gap: 54px;
  margin-top: 16px;
  color: #333a45;
  font-size: 18px;
  white-space: nowrap;
}

.dialog-section-title {
  margin: 20px 0 16px;
  color: #636be8;
  font-size: 20px;
  font-weight: 400;
}

.dialog-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.dialog-table th,
.dialog-table td {
  border: 1px solid #e2e5ff;
  text-align: center;
}

.dialog-table th {
  height: 52px;
  background: #d8daf7;
  color: #111722;
  font-size: 20px;
  font-weight: 700;
}

.dialog-table td {
  height: 55px;
  color: #111722;
  font-size: 20px;
}

.dialog-table th:nth-child(1) { width: 16.6%; }
.dialog-table th:nth-child(2) { width: 16.6%; }
.dialog-table th:nth-child(3) { width: 12.4%; }
.dialog-table th:nth-child(4) { width: 20.7%; }
.dialog-table th:nth-child(5) { width: 16.6%; }
.dialog-table th:nth-child(6) { width: 16.8%; }

.dialog-extra-fee {
  display: flex;
  align-items: center;
  gap: 31px;
  height: 55px;
  color: #111722;
  font-size: 20px;
}

.dialog-divider {
  height: 1px;
  background: #e2e5ff;
}

.dialog-form-row {
  display: flex;
  align-items: center;
  gap: 70px;
  margin-top: 20px;
}

.actual-amount,
.refund-methods,
.refund-reason {
  display: flex;
  align-items: center;
  color: #111722;
  font-size: 20px;
}

.actual-amount {
  gap: 10px;
}

.actual-amount > span,
.refund-methods > span {
  color: #ff0000;
  font-weight: 700;
}

.actual-amount input {
  width: 108px;
  height: 38px;
  padding: 0 12px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  color: #111722;
  font-size: 20px;
  outline: none;
}

.actual-amount em {
  color: #111722;
  font-style: normal;
  line-height: 1;
}

.refund-methods {
  gap: 16px;
}

.refund-methods label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.refund-methods input {
  width: 20px;
  height: 20px;
  accent-color: #1e80ff;
}

.refund-reason {
  margin-top: 23px;
}

.refund-reason input {
  flex: 1;
  height: 54px;
  min-width: 0;
  padding: 0 12px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  color: #111722;
  font-size: 20px;
  outline: none;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 30px;
}

.dialog-btn {
  width: 87px;
  height: 44px;
  border-radius: 5px;
  font-size: 18px;
  cursor: pointer;
}

.dialog-btn.cancel {
  border: 1px solid #d0d0d0;
  background: #fff;
  color: #333a45;
}

.dialog-btn.confirm {
  border: 1px solid #636be8;
  background: #636be8;
  color: #fff;
  font-weight: 700;
}

.success-toast {
  position: fixed;
  top: 28px;
  left: 50%;
  z-index: 1300;
  min-width: 140px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 28px;
  box-sizing: border-box;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  background: #f0f9eb;
  color: #67c23a;
  font-size: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  transform: translateX(-50%);
}

@media (max-width: 1280px) {
  .refund-head,
  .refund-layout {
    width: calc(100% - 32px);
    margin-left: 16px;
    margin-right: 16px;
  }

  .refund-layout {
    grid-template-columns: 1fr;
  }

  .refund-dialog {
    width: calc(100% - 48px);
  }

  .summary-row,
  .dialog-form-row {
    gap: 20px;
  }
}
</style>
