<template>
  <div class="order-detail-page">
    <header class="page-head">
      <h1>订单详情-{{ detail.status }}</h1>
      <div class="head-actions">
        <button class="head-btn primary" type="button">
          <Wallet class="head-icon" />
          <span>退费</span>
        </button>
        <button class="head-btn outline" type="button">
          <Printer class="head-icon" />
          <span>打印收费单</span>
        </button>
        <button class="head-btn outline" type="button" @click="goBack">
          <Back class="head-icon" />
          <span>返回</span>
        </button>
      </div>
    </header>

    <main class="page-main">
      <section class="project-card">
        <h2>
          <Tickets class="title-icon" />
          <span>项目明细</span>
        </h2>

        <div class="tabs-line">
          <button
            v-for="tab in projectTabs"
            :key="tab.key"
            :class="['project-tab', { active: activeProject.key === tab.key }]"
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
            <tr v-for="item in activeProject.items" :key="`${activeProject.key}-${item.index}`">
              <td>{{ item.index }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.price }}</td>
              <td>{{ item.count }}</td>
              <td>{{ item.unit }}</td>
              <td>{{ item.retailAmount }}</td>
              <td>{{ item.discount }}</td>
              <td>{{ item.finalAmount }}</td>
            </tr>
          </tbody>
        </table>

        <div class="extra-fees">
          <span v-for="fee in extraFees" :key="fee.label">{{ fee.label }}： <strong>{{ fee.value }}元</strong></span>
        </div>
        <div class="project-total">
          <span>合计：</span>
          <strong>{{ activeProject.total }}</strong>
        </div>
      </section>

      <aside class="info-card">
        <section class="info-section">
          <h2>
            <Document class="title-icon" />
            <span>订单信息</span>
          </h2>
          <div class="info-table order-info">
            <div class="info-row one">
              <div>订单编号： <span>{{ detail.orderNo }}</span></div>
            </div>
            <div class="info-row two">
              <div>应收金额（元）： <span>{{ detail.receivable }}</span></div>
              <div>实收金额（元）： <strong class="gold">{{ detail.received }}</strong></div>
            </div>
            <div class="info-row two">
              <div>优惠金额（元）： <span>{{ detail.discount }}</span></div>
              <div>医保支付（元）： <span>{{ detail.medical }}</span></div>
            </div>
            <div class="info-row two">
              <div>支付方式： <span>{{ detail.payMethod }}</span></div>
              <div>退款金额（元）： <span>{{ detail.refund }}</span></div>
            </div>
            <div class="info-row two bottom">
              <div>收费日期： <span>{{ detail.chargeTime }}</span></div>
              <div>收费员： <span>{{ detail.cashier }}</span></div>
            </div>
          </div>
        </section>

        <section class="info-section people-section">
          <h2>
            <UserFilled class="title-icon" />
            <span>人员信息</span>
          </h2>
          <div class="info-table people-info">
            <div class="info-row two">
              <div>患者编号： <span>{{ detail.patientNo }}</span></div>
              <div>患者姓名： <strong class="blue">{{ detail.patientName }}</strong></div>
            </div>
            <div class="info-row two">
              <div>年龄： <span>{{ detail.age }}</span></div>
              <div>性别： <span>{{ detail.gender }}</span></div>
            </div>
            <div class="info-row two">
              <div>手机： <span>{{ detail.phone }}</span></div>
              <div>会员等级： <strong class="gold vip">{{ detail.memberLevel }}</strong></div>
            </div>
            <div class="info-row two bottom">
              <div>账户余额： <span>{{ detail.memberBalance }}元</span></div>
              <div>积分： <span>{{ detail.memberPoints }}</span></div>
            </div>
          </div>
        </section>

        <section class="info-section visit-section">
          <h2>
            <Notebook class="title-icon" />
            <span>接诊信息</span>
          </h2>
          <div class="visit-table">
            <div>门诊编号： <span>{{ detail.registrationNo }}</span></div>
            <div>科室： <span>{{ detail.department }}</span></div>
            <div>接诊医生： <span>{{ detail.doctor }}</span></div>
            <div>接诊日期： <span>{{ detail.visitTime }}</span></div>
            <div>接诊类型： <span>{{ detail.visitType }}</span></div>
          </div>
        </section>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Back,
  Document,
  Notebook,
  Printer,
  Tickets,
  UserFilled,
  Wallet,
} from "@element-plus/icons-vue";
import { statisticsApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const remoteDetail = ref<Record<string, any>>({});
const orderId = readNavigationState(route);

const statusText: Record<string, string> = {
  PAID: "已收费",
  REFUNDED: "已退费",
  PENDING: "待收费",
  PARTIAL: "部分收费",
  CANCELLED: "已取消",
};

const payMethodAliases: Record<string, string[]> = {
  医保: ["医保", "医保卡"],
  会员卡: ["会员", "会员卡", "储值卡"],
  现金: ["现金"],
  支付宝: ["支付宝"],
  微信: ["微信"],
  银行卡: ["银行卡", "银行"],
};

const numberValue = (value: unknown) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

const money = (value: unknown) => numberValue(value).toFixed(2);

const textValue = (value: unknown, fallback = "-") => {
  const text = value == null ? "" : String(value).trim();
  return text || fallback;
};

const matchedPayAmount = (methodName: string) => {
  const data = remoteDetail.value;
  const payMethod = textValue(data.payMethod, "");
  const refundMethod = textValue(data.refundMethod, "");
  const aliases = payMethodAliases[methodName] || [methodName];
  const paidAmount = numberValue(data.paidAmount);
  const refundAmount = numberValue(data.refundAmount);
  const matchesPay = aliases.some((name) => payMethod.includes(name));
  const matchesRefund = aliases.some((name) => refundMethod.includes(name));
  return (matchesPay ? paidAmount : 0) - (matchesRefund ? refundAmount : 0);
};

const detail = computed(() => {
  const data = remoteDetail.value;
  const remarkPayload: any = parseJsonValue(data.remark);
  const remarkPatient = remarkPayload?.patient || {};
  const rawStatus = textValue(data.status, "").toUpperCase();
  const paidAmount = numberValue(data.paidAmount);
  const refundAmount = numberValue(data.refundAmount);
  const receivedAmount = paidAmount - refundAmount;
  return {
    status: statusText[rawStatus] || rawStatus || "-",
    orderNo: textValue(data.orderNo, orderId || "-"),
    patientNo: textValue(data.patientCode || remarkPatient.code || data.patientId),
    patientName: textValue(data.patientName),
    doctor: textValue(data.registrationDoctor || data.doctorName),
    age: textValue(data.age || remarkPatient.age),
    gender: textValue(data.gender || remarkPatient.gender),
    phone: textValue(data.phone || remarkPatient.phone),
    memberLevel: textValue(data.memberLevel || data.memberType || remarkPatient.level, "非会员"),
    memberBalance: money(data.memberBalance ?? remarkPatient.balance),
    memberPoints: textValue(data.memberPoints ?? remarkPatient.points, "0"),
    registrationNo: textValue(data.registrationNo),
    department: textValue(data.registrationDepartment || data.departmentName),
    visitTime: textValue(data.visitTime),
    visitType: textValue(data.visitType),
    receivable: money(data.receivableAmount),
    discount: money(data.discountAmount),
    received: money(receivedAmount),
    refund: money(refundAmount),
    medical: money(matchedPayAmount("医保")),
    payMethod: textValue(data.payMethod),
    chargeTime: textValue(data.paidAt || data.createdAt),
    cashier: textValue(data.cashier),
    chargeType: textValue(data.chargeType, "收费项目"),
    remark: textValue(data.remark, ""),
  };
});

type ProjectTabKey = string;

type ProjectItem = {
  index: number;
  name: string;
  price: string;
  count: number;
  unit: string;
  retailAmount: string;
  discount: string;
  finalAmount: string;
};

type ProjectConfig = {
  key: ProjectTabKey;
  label: string;
  total: string;
  items: ProjectItem[];
};

const activeProjectTab = ref<ProjectTabKey>("order");

const parseJsonValue = (value: unknown) => {
  if (typeof value !== "string") return value;
  const text = value.trim();
  if (!text || !/^[\[{]/.test(text)) return value;
  try {
    return JSON.parse(text);
  } catch {
    return value;
  }
};

const toArray = (value: unknown): any[] => {
  if (Array.isArray(value)) return value;
  if (value && typeof value === "object") return [value];
  if (typeof value === "string") {
    return value
      .split(/[,\n，、]/)
      .map((item) => item.trim())
      .filter(Boolean)
      .map((name) => ({ name }));
  }
  return [];
};

const itemAmount = (item: any) => {
  const explicitAmount = numberValue(item.finalAmount ?? item.amount ?? item.retailAmount ?? item.discountAmount);
  if (explicitAmount > 0) return explicitAmount;
  const quantity = numberValue(item.quantity ?? item.count) || 1;
  return numberValue(item.price ?? item.retailPrice ?? item.sellPrice) * quantity;
};

const createProjectItem = (
  index: number,
  name: string,
  amount: number,
  discountAmount = 0,
): ProjectItem => {
  const finalAmount = Math.max(amount - discountAmount, 0);
  return {
    index,
    name,
    price: money(amount),
    count: 1,
    unit: "项",
    retailAmount: money(amount),
    discount: discountAmount > 0 ? money(discountAmount) : "-",
    finalAmount: money(finalAmount),
  };
};

const createProjectItemFromData = (item: any, index: number, fallbackAmount = 0): ProjectItem => {
  const quantity = numberValue(item.quantity ?? item.count) || 1;
  const amount = itemAmount(item) || fallbackAmount;
  const price = numberValue(item.price ?? item.retailPrice ?? item.sellPrice) || (quantity ? amount / quantity : amount);
  const discountAmount = numberValue(item.discountAmount);
  const discount = item.discount != null && String(item.discount).trim() !== ""
    ? String(item.discount)
    : discountAmount > 0
      ? money(discountAmount)
      : "-";
  return {
    index,
    name: textValue(item.name || item.drugName || item.projectName || item.itemName || item.chargeName, "收费项目"),
    price: money(price),
    count: quantity,
    unit: textValue(item.unit, "项"),
    retailAmount: money(amount),
    discount,
    finalAmount: money(discountAmount > 0 ? discountAmount : amount),
  };
};

const feeItems = computed<ProjectItem[]>(() => {
  const data = remoteDetail.value;
  const registrationFee = numberValue(data.registrationFee);
  const diagnosisFee = numberValue(data.diagnosisFee);
  const items: ProjectItem[] = [];

  if (registrationFee > 0) items.push(createProjectItem(items.length + 1, "挂号费", registrationFee));
  if (diagnosisFee > 0) items.push(createProjectItem(items.length + 1, "诊疗费", diagnosisFee));
  return items;
});

const projectConfigs = computed<ProjectConfig[]>(() => {
  const data = remoteDetail.value;
  const configs: ProjectConfig[] = [];
  const receivableAmount = numberValue(data.receivableAmount);
  const restAmount = Math.max(receivableAmount - feeItems.value.reduce((sum, item) => sum + numberValue(item.finalAmount), 0), 0);
  const remarkPayload: any = parseJsonValue(data.remark);
  const prescriptionPayload: any = parseJsonValue(data.consultationPrescription);
  const checkPayload: any = parseJsonValue(data.consultationCheckItems);

  if (feeItems.value.length) {
    configs.push({
      key: "fees",
      label: "挂号诊疗",
      total: money(feeItems.value.reduce((sum, item) => sum + numberValue(item.finalAmount), 0)),
      items: feeItems.value,
    });
  }

  const retailItems = toArray(remarkPayload?.items);
  const retailFees = toArray(remarkPayload?.fees);
  if (retailItems.length || retailFees.length) {
    const items = [...retailItems, ...retailFees].map((item, index) => createProjectItemFromData(item, index + 1));
    configs.push({
      key: "retail",
      label: "药品零售",
      total: money(items.reduce((sum, item) => sum + numberValue(item.finalAmount), 0)),
      items,
    });
  }

  const prescriptionItems = toArray(prescriptionPayload?.items ?? prescriptionPayload);
  const prescriptionFees = toArray(prescriptionPayload?.fees);
  if (prescriptionItems.length || prescriptionFees.length) {
    const hasStructuredAmount = prescriptionItems.some((item) =>
      numberValue(item.price ?? item.retailPrice ?? item.sellPrice ?? item.amount ?? item.retailAmount ?? item.discountAmount) > 0,
    );
    const fallbackAmount = !hasStructuredAmount && prescriptionItems.length === 1 ? restAmount : 0;
    const items = [...prescriptionItems, ...prescriptionFees].map((item, index) =>
      createProjectItemFromData(item, index + 1, index === 0 ? fallbackAmount : 0),
    );
    configs.push({
      key: "prescription",
      label: textValue(prescriptionPayload?.name, "处方项目"),
      total: money(items.reduce((sum, item) => sum + numberValue(item.finalAmount), 0)),
      items,
    });
  }

  const checkItems = toArray(checkPayload);
  if (checkItems.length) {
    const items = checkItems.map((item, index) => createProjectItemFromData(item, index + 1));
    configs.push({
      key: "check",
      label: "检查项目",
      total: money(items.reduce((sum, item) => sum + numberValue(item.finalAmount), 0)),
      items,
    });
  }

  if (!configs.length) {
    const fallbackName = typeof remarkPayload === "string" ? remarkPayload : detail.value.chargeType;
    configs.push({
      key: "order",
      label: detail.value.chargeType,
      total: detail.value.received,
      items: [createProjectItem(1, textValue(fallbackName, "收费项目"), receivableAmount, numberValue(data.discountAmount))],
    });
  }

  return configs;
});

const projectTabs = computed<Array<{ key: ProjectTabKey; label: string }>>(() => [
  ...projectConfigs.value.map((item) => ({ key: item.key, label: item.label })),
]);

const activeProject = computed<ProjectConfig>(() =>
  projectConfigs.value.find((item) => item.key === activeProjectTab.value) || projectConfigs.value[0] || {
    key: "order",
    label: "收费项目",
    total: "0.00",
    items: [createProjectItem(1, "收费项目", 0)],
  },
);

const extraFees = computed(() => {
  const data = remoteDetail.value;
  return [
    { label: "挂号费", value: money(data.registrationFee) },
    { label: "诊疗费", value: money(data.diagnosisFee) },
  ].filter((fee) => numberValue(fee.value) > 0);
});

const goBack = () => {
  router.push("/statistics/charge");
};


onMounted(async () => {
  if (!orderId) return;
  const response: any = await statisticsApi.chargeDetail(orderId);
  remoteDetail.value = response?.data || {};
});
</script>

<style scoped>
.order-detail-page {
  min-height: 100%;
  padding: 28px 0 80px;
  box-sizing: border-box;
  color: #111722;
}

.page-head,
.page-main {
  width: min(1720px, calc(100% - 64px));
  margin: 0 auto;
}

.page-head,
.project-card,
.info-card {
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.page-head {
  height: 112px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 36px 0 34px;
}

.page-head h1 {
  position: relative;
  margin: 0;
  padding-left: 18px;
  color: #1b2430;
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.page-head h1::before {
  position: absolute;
  left: 0;
  top: 0;
  width: 6px;
  height: 25px;
  content: "";
  background: #6770f1;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 21px;
  flex: 0 0 auto;
}

.head-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 13px;
  height: 51px;
  min-width: 156px;
  padding: 0 22px;
  box-sizing: border-box;
  border: 1px solid #636be8;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.head-btn.primary {
  color: #fff;
  background: #636be8;
}

.head-btn.outline {
  color: #636be8;
  background: #fff;
}

.head-icon {
  width: 25px;
  height: 25px;
}

.page-main {
  display: grid;
  grid-template-columns: minmax(0, 2.08fr) minmax(420px, 1fr);
  gap: 14px;
  margin-top: 14px;
  align-items: start;
}

.project-card {
  min-height: 560px;
  min-width: 0;
  padding: 31px 32px 54px;
}

.info-card {
  min-width: 0;
  padding: 31px 28px 32px;
}

.project-card h2,
.info-section h2 {
  display: flex;
  align-items: center;
  margin: 0;
  color: #636be8;
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
}

.title-icon {
  width: 27px;
  height: 27px;
  margin-right: 12px;
  flex: 0 0 auto;
  color: #636be8;
}

.tabs-line {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  margin-top: 47px;
  border-bottom: 3px solid #f0f0f0;
}

.project-tab {
  height: 44px;
  min-width: 136px;
  padding: 0 10px;
  border: 0;
  border-radius: 5px 5px 0 0;
  background: #f4f4f4;
  color: #111722;
  font-size: 18px;
  cursor: pointer;
}

.project-tab.active {
  color: #fff;
  background: #636be8;
}

.project-table {
  width: 100%;
  margin-top: 24px;
  border-collapse: collapse;
  table-layout: fixed;
}

.project-table th,
.project-table td {
  border: 1px solid #e2e5ff;
  text-align: center;
}

.project-table th {
  height: 51px;
  background: #d8daf7;
  color: #111;
  font-size: 16px;
  font-weight: 700;
}

.project-table td {
  height: 54px;
  color: #111722;
  font-size: 16px;
}

.project-table th:nth-child(1),
.project-table td:nth-child(1) {
  width: 6.6%;
}

.project-table th:nth-child(2),
.project-table td:nth-child(2) {
  width: 18.4%;
}

.project-table th:nth-child(3),
.project-table td:nth-child(3),
.project-table th:nth-child(4),
.project-table td:nth-child(4) {
  width: 12.4%;
}

.project-table th:nth-child(5),
.project-table td:nth-child(5) {
  width: 13.8%;
}

.project-table th:nth-child(6),
.project-table td:nth-child(6) {
  width: 14.6%;
}

.project-table th:nth-child(7),
.project-table td:nth-child(7) {
  width: 8.6%;
}

.extra-fees {
  display: flex;
  justify-content: flex-end;
  gap: 34px;
  margin-top: 24px;
  padding: 0 1px 18px 0;
  border-bottom: 2px solid #eee;
  color: #111722;
  font-size: 18px;
  line-height: 1;
}

.extra-fees strong,
.project-total strong {
  font-weight: 700;
}

.project-total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 10px;
  margin-top: 18px;
  color: #111722;
  font-size: 20px;
  line-height: 1;
}

.project-total strong {
  font-size: 23px;
}

.info-section + .info-section {
  margin-top: 35px;
}

.people-section {
  margin-top: 34px !important;
}

.visit-section {
  margin-top: 36px !important;
}

.info-table,
.visit-table {
  margin-top: 29px;
  border-top: 1px solid #e2e5ff;
  border-left: 1px solid #e2e5ff;
}

.info-row {
  display: grid;
}

.info-row.one {
  grid-template-columns: 1fr;
}

.info-row.two {
  grid-template-columns: 1fr 1fr;
}

.info-row > div,
.visit-table > div {
  min-height: 56px;
  display: flex;
  align-items: center;
  min-width: 0;
  box-sizing: border-box;
  border-right: 1px solid #e2e5ff;
  border-bottom: 1px solid #e2e5ff;
  color: #111722;
  font-size: 18px;
  white-space: nowrap;
  overflow: hidden;
}

.info-row > div {
  padding: 0 18px;
}

.order-info .info-row.one > div {
  padding-left: 18px;
}

.order-info .info-row.bottom > div {
  min-height: 53px;
}

.people-info .info-row > div {
  min-height: 55px;
  padding: 0 24px;
}

.visit-table > div {
  min-height: 55px;
  padding: 0 24px;
}

.info-row span,
.visit-table span,
.info-row strong {
  min-width: 0;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
}

.blue {
  color: #636be8;
  font-weight: 700;
}

.gold {
  color: #ffb800;
  font-weight: 700;
}

.vip {
  font-style: italic;
}

@media (max-width: 1280px) {
  .page-head,
  .page-main {
    width: calc(100% - 32px);
  }

  .page-head {
    height: auto;
    min-height: 112px;
    flex-wrap: wrap;
    gap: 20px;
    padding: 24px;
  }

  .head-actions {
    flex-wrap: wrap;
  }

  .page-main {
    grid-template-columns: 1fr;
  }

  .project-card,
  .info-card {
    padding: 26px 20px 36px;
  }
}
</style>
