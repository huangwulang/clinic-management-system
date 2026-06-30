<template>
  <div class="payment-page">
    <section class="top-card">
      <div class="amount-line">
        <span>应收金额：</span>
        <strong>{{ payableAmount }}</strong>
        <span>元</span>
      </div>
      <div class="top-actions">
        <button class="charge-btn" type="button" @click="showChargeDialog = true">
          <Money class="btn-icon" />
          <span>收费</span>
        </button>
        <button class="back-btn" type="button" @click="goBack">
          <Back class="btn-icon" />
          <span>返回</span>
        </button>
      </div>
    </section>

    <section class="content-grid">
      <div class="detail-card project-card">
        <h1>
          <Document class="title-icon" />
          <span>项目明细</span>
        </h1>

        <div class="project-tabs">
          <button
            v-for="tab in projectTabs"
            :key="tab.key"
            :class="{ 'is-active': activeProjectTab === tab.key || (retailPayload && tab.key === 'retail') }"
            type="button"
            @click="activeProjectTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <table class="project-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>收费名称</th>
              <th>单价（元）</th>
              <th>总量</th>
              <th>单位</th>
              <th>零售金额（元）</th>
              <th>折扣</th>
              <th>折后金额（元）</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in activeProjects" :key="row.index">
              <td>{{ row.index }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.price }}</td>
              <td>{{ row.quantity }}</td>
              <td>{{ row.unit }}</td>
              <td>{{ row.retailAmount }}</td>
              <td>{{ row.discount }}</td>
              <td>{{ row.discountAmount }}</td>
            </tr>
          </tbody>
        </table>

        <div class="fee-row">
          <span v-for="fee in feeSummary" :key="fee">{{ fee }}</span>
        </div>
        <div class="total-row">合计：<strong>{{ activeTotal }}</strong></div>
      </div>

      <aside class="detail-card side-card">
        <h2>
          <UserFilled class="title-icon" />
          <span>人员信息</span>
        </h2>
        <div class="person-grid">
          <div>患者编号： {{ patientInfo.code }}</div>
          <div>患者姓名： <strong class="blue">{{ patientInfo.name }}</strong></div>
          <div>年龄： {{ patientInfo.age }}</div>
          <div>性别： {{ patientInfo.gender }}</div>
          <div>手机： {{ patientInfo.phone }}</div>
          <div>会员等级： <strong class="orange">{{ patientInfo.level }}</strong></div>
          <div>账户余额： {{ patientInfo.balance }}元</div>
          <div>积分： {{ patientInfo.points }}</div>
        </div>

        <h2 v-if="paymentSource !== 'retail'" class="visit-title">
          <Notebook class="title-icon" />
          <span>接诊信息</span>
        </h2>
        <div v-if="paymentSource !== 'retail'" class="visit-list">
          <div>门诊编号： {{ visitInfo.registrationNo }}</div>
          <div>科室： {{ visitInfo.department }}</div>
          <div>接诊医生： {{ visitInfo.doctor }}</div>
          <div>接诊日期： {{ visitInfo.visitDate }}</div>
          <div>接诊类型： {{ visitInfo.visitType }}</div>
        </div>
      </aside>
    </section>

    <div v-if="showChargeDialog" class="dialog-mask">
      <section class="charge-dialog">
        <header class="dialog-header">
          <h3>收费</h3>
          <button type="button" aria-label="关闭" @click="showChargeDialog = false">
            <Close class="close-icon" />
          </button>
        </header>

        <div class="dialog-amount">
          <span>应收金额</span>
          <strong>{{ payableAmount }}</strong>
          <span>元</span>
        </div>

        <section class="pay-form">
          <label class="form-row">
            <span>优惠金额</span>
            <input type="text" />
            <em>元</em>
            <span class="discount-label">折扣</span>
            <input class="discount-input" type="text" />
            <em>折</em>
          </label>
          <label class="form-row">
            <span>医保支付</span>
            <input type="text" />
            <em>元</em>
          </label>
          <label class="form-row">
            <span>实收金额</span>
            <input class="amount-input" type="text" :value="payableAmount" />
            <em>元</em>
          </label>
          <label class="form-row">
            <span>找零</span>
            <input class="change-input" type="text" value="0" />
            <em>元</em>
          </label>
          <div class="pay-methods">
            <span>支付方式</span>
            <label v-for="method in payMethods" :key="method">
              <input v-model="selectedPayMethod" type="radio" name="payMethod" :value="method" />
              {{ method }}
            </label>
          </div>
          <label class="form-row">
            <span>收款备注</span>
            <input type="text" />
          </label>
        </section>

        <footer class="dialog-footer">
          <button class="cancel-btn" type="button" @click="showChargeDialog = false">取消</button>
          <button class="confirm-btn" type="button" @click="confirmPayment">确定收款</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, Close, Document, Money, Notebook, UserFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { chargeOrderApi, retailOrderApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";
import { loadEnabledPaymentMethods } from "@/utils/paymentMethods";

type PaymentRow = {
  index: number;
  name: string;
  price: string;
  quantity: number;
  unit: string;
  retailAmount: string;
  discount: string;
  discountAmount: string;
};

type RetailPaymentPayload = {
  amount: string;
  patient: {
    code: string;
    name: string;
    age: string;
    gender: string;
    phone: string;
    level: string;
    balance: string;
    points: string;
  };
  items: PaymentRow[];
  fees?: Array<{ name: string; amount: string }>;
};

type ConsultationPaymentPayload = {
  source: "consultation";
  amount: string;
  patient: Partial<RetailPaymentPayload["patient"]>;
  visit: {
    registrationNo?: string;
    department?: string;
    doctor?: string;
    visitDate?: string;
    visitType?: string;
  };
  tabs: Array<{
    key: string;
    label: string;
    total: string;
    rows: PaymentRow[];
  }>;
  fees?: Array<{ name: string; amount: string }>;
};

const route = useRoute();
const router = useRouter();
const showChargeDialog = ref(false);
const remoteOrder = ref<any>(null);
const activeProjectTab = ref("western");
const payMethods = ref<string[]>(["现金"]);
const selectedPayMethod = ref("现金");
const paymentId = readNavigationState(route);
const paymentSource = readNavigationState(route, "source");
const returnPath = readNavigationState(route, "returnPath");
const returnVisitId = readNavigationState(route, "visitId");
const returnRegistrationId = readNavigationState(route, "registrationId");
const returnPatientId = readNavigationState(route, "patientId");

const defaultProjectTabs = [
  { key: "western", label: "西/成药处方1" },
  { key: "chinese", label: "中药处方2" },
  { key: "check", label: "检查项目3" },
];

const projectGroups = {
  western: {
    total: "103.50",
    rows: [
      { index: 1, name: "肾宝糖浆", price: "15.00", quantity: 2, unit: "瓶", retailAmount: "30.00", discount: "9.00", discountAmount: "27.00" },
      { index: 2, name: "阿莫西林分散片", price: "25.00", quantity: 1, unit: "盒", retailAmount: "25.00", discount: "9.00", discountAmount: "22.50" },
      { index: 3, name: "肾宝糖浆", price: "20.00", quantity: 1, unit: "盒", retailAmount: "20.00", discount: "9.00", discountAmount: "18.00" },
    ],
  },
  chinese: {
    total: "84.50",
    rows: [
      { index: 1, name: "豨签草", price: "15.00", quantity: 2, unit: "g", retailAmount: "30.00", discount: "9.00", discountAmount: "27.00" },
      { index: 2, name: "陈皮", price: "25.00", quantity: 1, unit: "g", retailAmount: "25.00", discount: "9.00", discountAmount: "22.50" },
    ],
  },
  check: {
    total: "84.50",
    rows: [
      { index: 1, name: "脑电波检查", price: "15.00", quantity: 2, unit: "次", retailAmount: "30.00", discount: "9.00", discountAmount: "27.00" },
      { index: 2, name: "胃镜检查", price: "25.00", quantity: 1, unit: "次", retailAmount: "25.00", discount: "9.00", discountAmount: "22.50" },
    ],
  },
};

const retailPayload = computed<RetailPaymentPayload | null>(() => {
  if (paymentSource !== "retail" || !remoteOrder.value?.remark) return null;
  try {
    const payload = JSON.parse(remoteOrder.value.remark) as RetailPaymentPayload;
    return Array.isArray(payload?.items) ? payload : null;
  } catch {
    return null;
  }
});

const consultationPayload = computed<ConsultationPaymentPayload | null>(() => {
  if (paymentSource === "retail" || !remoteOrder.value?.remark) return null;
  try {
    const payload = JSON.parse(remoteOrder.value.remark) as ConsultationPaymentPayload;
    return Array.isArray(payload?.tabs) ? payload : null;
  } catch {
    return null;
  }
});

const projectTabs = computed(() => {
  if (retailPayload.value) return [{ key: "retail", label: "药品零售" }];
  if (consultationPayload.value?.tabs?.length) {
    return consultationPayload.value.tabs.map((tab) => ({ key: tab.key, label: tab.label }));
  }
  return paymentSource === "retail" ? defaultProjectTabs : [{ key: "charge", label: "收费明细" }];
});

const payableAmount = computed(() => String(
  remoteOrder.value?.receivableAmount || retailPayload.value?.amount || consultationPayload.value?.amount || "0.00",
));

const patientInfo = computed(() => ({
  code: retailPayload.value?.patient?.code || consultationPayload.value?.patient?.code || String(remoteOrder.value?.patientId || ""),
  name: retailPayload.value?.patient?.name || consultationPayload.value?.patient?.name || remoteOrder.value?.patientName || "",
  age: retailPayload.value?.patient?.age || consultationPayload.value?.patient?.age || "-",
  gender: retailPayload.value?.patient?.gender || consultationPayload.value?.patient?.gender || "-",
  phone: retailPayload.value?.patient?.phone || consultationPayload.value?.patient?.phone || "-",
  level: retailPayload.value?.patient?.level || consultationPayload.value?.patient?.level || "普通",
  balance: retailPayload.value?.patient?.balance || consultationPayload.value?.patient?.balance || "0.00",
  points: retailPayload.value?.patient?.points || consultationPayload.value?.patient?.points || "0",
}));

const visitInfo = computed(() => ({
  registrationNo: consultationPayload.value?.visit?.registrationNo || String(remoteOrder.value?.registrationId || ""),
  department: consultationPayload.value?.visit?.department || remoteOrder.value?.departmentName || "",
  doctor: consultationPayload.value?.visit?.doctor || remoteOrder.value?.doctorName || "",
  visitDate: consultationPayload.value?.visit?.visitDate || remoteOrder.value?.createdAt || "",
  visitType: consultationPayload.value?.visit?.visitType || "",
}));

const activeProjects = computed(() => {
  if (retailPayload.value) return retailPayload.value.items;
  const tab = consultationPayload.value?.tabs?.find((item) => item.key === activeProjectTab.value)
    || consultationPayload.value?.tabs?.[0];
  if (tab) return tab.rows || [];
  if (paymentSource !== "retail") return [];
  return projectGroups[activeProjectTab.value as keyof typeof projectGroups].rows;
});

const activeTotal = computed(() => {
  if (retailPayload.value) return payableAmount.value;
  const tab = consultationPayload.value?.tabs?.find((item) => item.key === activeProjectTab.value)
    || consultationPayload.value?.tabs?.[0];
  if (tab) return tab.total || "0.00";
  if (paymentSource !== "retail") return payableAmount.value;
  return projectGroups[activeProjectTab.value as keyof typeof projectGroups].total;
});

const feeSummary = computed(() => {
  if (retailPayload.value) {
    const fees = retailPayload.value.fees || [];
    return fees.length ? fees.map((fee) => `${fee.name}： ${fee.amount}元`) : ["附加费用： 0.00元"];
  }
  if (consultationPayload.value) {
    const fees = consultationPayload.value.fees || [];
    return fees.length ? fees.map((fee) => `${fee.name}： ${fee.amount}元`) : ["附加费用： 0.00元"];
  }
  return ["附加费用： 0.00元"];
});

const goBack = () => {
  if (paymentSource === "retail") {
    router.push("/retail");
    return;
  }
  const visitId = returnVisitId || String(consultationPayload.value?.consultationId || "");
  const registrationId = returnRegistrationId || String(remoteOrder.value?.registrationId || "");
  const patientId = returnPatientId || String(remoteOrder.value?.patientId || "");
  router.push({
    path: returnPath || "/consultation/new",
    query: {
      ...(visitId ? { visitId } : {}),
      ...(registrationId ? { registrationId } : {}),
      ...(patientId ? { patientId } : {}),
    },
    state: {
      visitId,
      registrationId,
      patientId,
    },
  });
};


onMounted(async () => {
  payMethods.value = await loadEnabledPaymentMethods();
  selectedPayMethod.value = payMethods.value.includes("现金") ? "现金" : payMethods.value[0] || "";
  if (!paymentId) return;
  const api = paymentSource === "retail" ? retailOrderApi : chargeOrderApi;
  const response: any = await api.get(paymentId);
  remoteOrder.value = response?.data || null;
  if (paymentSource === "retail") {
    activeProjectTab.value = "retail";
    return;
  }
  try {
    const payload = JSON.parse(remoteOrder.value?.remark || "{}") as ConsultationPaymentPayload;
    if (payload?.tabs?.[0]?.key) {
      activeProjectTab.value = payload.tabs[0].key;
    }
  } catch {
    activeProjectTab.value = "western";
  }
});

const confirmPayment = async () => {
  const id = String(paymentId || remoteOrder.value?.id || "");
  if (!id) {
    ElMessage.warning("缺少收费单编号");
    return;
  }
  if (!selectedPayMethod.value) {
    ElMessage.warning("请先启用至少一种支付方式");
    return;
  }
  const api = paymentSource === "retail" ? retailOrderApi : chargeOrderApi;
  await api.pay(id, {
    paidAmount: Number(payableAmount.value),
    payMethod: selectedPayMethod.value,
    cashier: remoteOrder.value?.cashier || "当前操作员",
  });
  showChargeDialog.value = false;
  ElMessage.success("收款成功");
  router.push("/charge/orders");
};
</script>

<style scoped>
.payment-page {
  min-height: 100%;
  padding: 28px 0 58px;
}

.top-card,
.detail-card {
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.top-card {
  width: min(1660px, calc(100% - 96px));
  height: 107px;
  margin: 0 auto 13px;
  padding: 0 33px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.amount-line {
  display: flex;
  align-items: baseline;
  gap: 20px;
  color: #444;
  font-size: 24px;
  font-weight: 700;
}

.amount-line strong {
  color: #f7b719;
  font-size: 44px;
  letter-spacing: 1px;
}

.top-actions {
  display: inline-flex;
  align-items: center;
  gap: 22px;
}

.charge-btn,
.back-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 48px;
  border-radius: 5px;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.charge-btn {
  width: 147px;
  border: 0;
  background: #ffbd17;
  color: #fff;
}

.back-btn {
  width: 148px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
}

.btn-icon {
  width: 25px;
  height: 25px;
}

.content-grid {
  width: min(1660px, calc(100% - 96px));
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 2.05fr) minmax(430px, 0.98fr);
  gap: 13px;
}

.project-card {
  min-height: 714px;
  padding: 30px 32px;
  box-sizing: border-box;
}

.side-card {
  min-height: 714px;
  padding: 30px 28px;
  box-sizing: border-box;
}

h1,
h2 {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  color: #636be8;
  font-size: 28px;
  font-weight: 700;
}

.title-icon {
  width: 28px;
  height: 28px;
}

.project-tabs {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-top: 41px;
  border-bottom: 2px solid #eeeeee;
}

.project-tabs button {
  height: 42px;
  padding: 0 18px;
  border: 0;
  background: #f5f5f5;
  color: #1d2430;
  font-size: 18px;
  cursor: pointer;
}

.project-tabs button.is-active {
  background: #636be8;
  color: #fff;
}

.project-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  margin-top: 24px;
  border-left: 1px solid #dfe2ff;
  border-right: 1px solid #dfe2ff;
}

.project-table th {
  height: 51px;
  background: #dfe2ff;
  color: #1d2430;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
}

.project-table td {
  height: 53px;
  border-right: 1px solid #dfe2ff;
  border-bottom: 1px solid #dfe2ff;
  color: #000;
  font-size: 18px;
  text-align: center;
}

.project-table th:nth-child(1),
.project-table td:nth-child(1) {
  width: 6.5%;
}

.project-table th:nth-child(2),
.project-table td:nth-child(2) {
  width: 18%;
}

.project-table th:nth-child(3),
.project-table td:nth-child(3),
.project-table th:nth-child(4),
.project-table td:nth-child(4),
.project-table th:nth-child(7),
.project-table td:nth-child(7) {
  width: 12%;
}

.project-table th:nth-child(5),
.project-table td:nth-child(5) {
  width: 13.5%;
}

.project-table th:nth-child(6),
.project-table td:nth-child(6),
.project-table th:nth-child(8),
.project-table td:nth-child(8) {
  width: 14.5%;
}

.fee-row {
  display: flex;
  justify-content: flex-end;
  gap: 40px;
  margin-top: 21px;
  color: #000;
  font-size: 18px;
}

.total-row {
  margin-top: 16px;
  padding-top: 17px;
  border-top: 2px solid #eeeeee;
  text-align: right;
  color: #000;
  font-size: 20px;
}

.total-row strong {
  font-size: 24px;
}

.person-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin-top: 29px;
  border: 1px solid #dfe2ff;
  color: #000;
  font-size: 18px;
}

.person-grid div {
  height: 52px;
  padding: 0 21px;
  display: flex;
  align-items: center;
  border-right: 1px solid #dfe2ff;
  border-bottom: 1px solid #dfe2ff;
}

.person-grid div:nth-child(2n) {
  border-right: 0;
}

.person-grid div:nth-last-child(-n + 2) {
  border-bottom: 0;
}

.blue {
  color: #636be8;
}

.orange {
  color: #ffb000;
}

.visit-title {
  margin-top: 35px;
}

.visit-list {
  margin-top: 30px;
  border: 1px solid #dfe2ff;
  color: #000;
  font-size: 18px;
}

.visit-list div {
  height: 52px;
  padding: 0 21px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #dfe2ff;
}

.visit-list div:last-child {
  border-bottom: 0;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.39);
}

.charge-dialog {
  width: 900px;
  min-height: 650px;
  box-sizing: border-box;
  padding: 24px 26px 40px;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.24);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dialog-header h3 {
  margin: 0;
  color: #1d2430;
  font-size: 24px;
  font-weight: 700;
}

.dialog-header button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #cfcfcf;
  cursor: pointer;
}

.close-icon {
  width: 28px;
  height: 28px;
}

.dialog-amount {
  display: flex;
  align-items: baseline;
  gap: 25px;
  height: 58px;
  margin-top: 33px;
  padding: 0 14px;
  background: #dfe2ff;
  color: #000;
  font-size: 18px;
}

.dialog-amount strong {
  color: #000;
  font-size: 24px;
}

.pay-form {
  padding: 39px 14px 0;
}

.form-row {
  display: flex;
  align-items: center;
  min-height: 56px;
  color: #000;
  font-size: 18px;
}

.form-row > span:first-child,
.pay-methods > span {
  width: 97px;
  flex: 0 0 97px;
}

.form-row input {
  width: 292px;
  height: 44px;
  box-sizing: border-box;
  padding: 0 13px;
  border: 3px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  color: #000;
  font-size: 20px;
}

.form-row em {
  margin-left: 11px;
  margin-right: 29px;
  color: #000;
  font-style: normal;
  font-size: 18px;
}

.form-row .discount-label {
  width: auto;
  flex: 0 0 auto;
  margin-right: 12px;
}

.form-row .discount-input {
  width: 106px;
}

.form-row .amount-input {
  color: #ffb800;
  font-weight: 700;
}

.form-row .change-input {
  border-color: #636be8;
}

.pay-methods {
  display: flex;
  align-items: center;
  height: 43px;
  color: #000;
  font-size: 18px;
}

.pay-methods label {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-right: 17px;
  white-space: nowrap;
}

.pay-methods input {
  width: 16px;
  height: 16px;
  accent-color: #1a73e8;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
  margin-top: 17px;
}

.dialog-footer button {
  height: 42px;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.cancel-btn {
  width: 82px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
}

.confirm-btn {
  width: 96px;
  border: 0;
  background: #636be8;
  color: #fff;
}

@media (max-width: 1280px) {
  .top-card,
  .content-grid {
    width: calc(100% - 32px);
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .top-card {
    flex-wrap: wrap;
    height: auto;
    min-height: 107px;
    gap: 18px;
    padding: 20px;
  }

  .charge-dialog {
    width: calc(100% - 48px);
  }
}
</style>
