<template>
  <el-config-provider :locale="zhCn">
    <div class="edit-registration-page">
      <section class="edit-card">
        <header class="page-header">
          <h2><span></span>编辑挂号信息</h2>
          <div class="header-actions">
            <button class="refund-btn" type="button" @click="saveRegistration">
              <span class="refund-icon"></span>
              <span>保存</span>
            </button>
            <button class="back-btn" type="button" @click="handleBack">
              <span class="back-icon"></span>
              <span>返回</span>
            </button>
          </div>
        </header>

        <section class="section-block order-section">
          <h3>订单信息</h3>
          <div class="order-table">
            <div>订单编号： {{ orderInfo.orderNo || "-" }}</div>
            <div>应收金额（元）： {{ orderInfo.receivable }}</div>
            <div>优惠金额（元）： {{ orderInfo.discount }}</div>
            <div>医保支付（元）： {{ orderInfo.medical }}</div>
            <div>实收金额（元）： <strong>{{ orderInfo.received }}</strong></div>
            <div>支付方式： {{ orderInfo.payMethod }}</div>
            <div>找零（元）： {{ orderInfo.change }}</div>
            <div>收费日期： {{ orderInfo.chargeTime || "-" }}</div>
            <div>收费员： {{ orderInfo.cashier || "-" }}</div>
            <div></div>
          </div>
        </section>

        <section class="section-block registration-section">
          <h3>挂号信息</h3>
          <div class="form-grid registration-grid">
            <label class="form-field">
              <span>科室<i>*</i></span>
              <select v-model="form.department">
                <option v-for="item in departmentOptions" :key="item.id" :value="item.name">{{ item.name }}</option>
              </select>
            </label>
            <label class="form-field">
              <span>接诊类型<i>*</i></span>
              <select v-model="form.visitType">
                <option>初诊</option>
                <option>复诊</option>
              </select>
            </label>
            <label class="form-field active-field">
              <span>接诊医生<i>*</i></span>
              <select v-model="form.doctor" :disabled="!form.department">
                <option value="">请选择</option>
                <option v-if="form.department && doctorOptions.length === 0" value="" disabled>
                  该科室暂无可用医生
                </option>
                <option v-for="item in doctorOptions" :key="item.id" :value="item.name">{{ item.name }}</option>
              </select>
            </label>
            <label class="form-field">
              <span>挂号费（元）<i>*</i></span>
              <input v-model="form.registrationFee" disabled />
            </label>
            <label class="form-field">
              <span>诊疗费（元）</span>
              <input v-model="form.treatmentFee" disabled />
            </label>
          </div>
        </section>

        <section class="section-block patient-section">
          <h3>患者信息</h3>
          <div class="form-grid patient-grid">
            <label class="form-field search-field">
              <span>患者姓名<i>*</i></span>
              <input v-model="form.patientName" />
              <b class="search-icon"></b>
            </label>
            <label class="form-field">
              <span>患者卡号</span>
              <input v-model="form.cardNo" disabled />
            </label>
            <label class="form-field age-field">
              <span>患者年龄<i>*</i></span>
              <div class="age-control">
                <input v-model="form.age" placeholder="请输入数字" />
                <em>岁</em>
              </div>
            </label>
            <div class="form-field date-field">
              <span>出生日期<i>*</i></span>
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder=""
              />
            </div>
            <label class="form-field">
              <span>性别<i>*</i></span>
              <select v-model="form.gender">
                <option>男</option>
                <option>女</option>
              </select>
            </label>
            <label class="form-field">
              <span>手机号码</span>
              <input v-model="form.phone" />
            </label>
            <label class="form-field">
              <span>证件号码</span>
              <input v-model="form.idNo" placeholder="请输入证件号码" />
            </label>
            <label class="form-field">
              <span>地址</span>
              <select v-model="form.city">
                <option v-for="city in cityOptions" :key="city" :value="city">{{ city }}</option>
              </select>
            </label>
            <label class="form-field detail-address">
              <span>&nbsp;</span>
              <input v-model="form.address" placeholder="请输入详细地址" />
            </label>
            <label class="form-field remark-field">
              <span>备注</span>
              <input v-model="form.remark" />
            </label>
          </div>
        </section>
      </section>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import { ElMessage } from "element-plus";
import { departmentApi, patientApi, registrationApi, staffApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const registrationId = readNavigationState(route);

const form = reactive({
  department: "全科",
  visitType: "初诊",
  doctor: "王冕",
  registrationFee: "挂号费（10.00元）",
  treatmentFee: "诊疗费（50.00）",
  patientName: "王蒙",
  cardNo: "",
  age: "",
  birthDate: "",
  gender: "男",
  phone: "18865178977",
  idNo: "",
  city: "广东省/深圳市",
  address: "",
  remark: "",
});
const patientId = ref<string | number | null>(null);
const departmentOptions = ref<any[]>([]);
const staffOptions = ref<Array<{
  id: string | number;
  name: string;
  departmentName: string;
  roleName: string;
  enabled: boolean;
}>>([]);
const doctorOptions = computed(() => staffOptions.value.filter((item) => (
  item.departmentName === form.department
  && item.enabled
  && (item.roleName === "医生" || item.roleName.toUpperCase() === "DOCTOR")
)));
const cityOptions = computed(() => Array.from(new Set([
  form.city,
  "北京市/北京市",
  "上海市/上海市",
  "广东省/深圳市",
  "广东省/广州市",
  "广西壮族自治区/南宁市",
].filter(Boolean))));
const orderInfo = reactive({
  orderNo: "",
  receivable: "0.00",
  discount: "0.00",
  medical: "0.00",
  received: "0.00",
  payMethod: "-",
  change: "0.00",
  chargeTime: "",
  cashier: "",
});

watch(() => form.department, () => {
  if (!doctorOptions.value.some((item) => item.name === form.doctor)) {
    form.doctor = "";
  }
});

const handleBack = () => {
  router.back();
};


onMounted(async () => {
  if (!registrationId) return;
  const [response, departmentResponse, staffResponse]: any[] = await Promise.all([
    registrationApi.get(registrationId),
    departmentApi.list(),
    staffApi.list(),
  ]);
  const item = response?.data || {};
  patientId.value = item.patientId || null;
  departmentOptions.value = (departmentResponse?.data || []).filter(
    (department: any) => department.enabled !== false && department.enabled !== 0
  );
  staffOptions.value = (staffResponse?.data || []).map((staff: any) => ({
    id: staff.id,
    name: staff.name || "",
    departmentName: staff.departmentName || staff.department || "",
    roleName: staff.roleName || staff.role || "",
    enabled: staff.enabled !== false && staff.enabled !== 0,
  }));
  const patientResponse: any = patientId.value ? await patientApi.get(patientId.value) : null;
  const patient = patientResponse?.data || {};
  const feeTotal = Number(item.registrationFee || 0) + Number(item.diagnosisFee || 0);
  const receivable = Number(item.receivableAmount ?? feeTotal);
  const discount = Number(item.discountAmount ?? 0);
  const rawPaidAmount = item.paidAmount ?? item.paidamount ?? item.paid_amount;
  const payMethod = item.payMethod || item.paymethod || item.pay_method || "";
  const paidAmount = rawPaidAmount == null
    ? (payMethod ? Math.max(receivable - discount, 0) : 0)
    : Number(rawPaidAmount);
  Object.assign(form, {
    department: item.departmentName || "",
    visitType: item.visitType || "",
    doctor: item.doctorName || "",
    registrationFee: String(item.registrationFee ?? 0),
    treatmentFee: String(item.diagnosisFee ?? 0),
    patientName: patient.name || item.patientName || "",
    cardNo: patient.cardNo || patient.patientCode || "",
    age: String(patient.age || item.age || ""),
    birthDate: patient.birthday || "",
    gender: patient.gender || item.gender || "",
    phone: patient.phone || item.phone || "",
    idNo: patient.idCard || "",
    city: patient.provinceCity || "",
    address: patient.address || "",
    remark: item.remark || "",
  });
  Object.assign(orderInfo, {
    orderNo: item.chargeOrderNo || item.registrationNo || String(item.id || ""),
    receivable: receivable.toFixed(2),
    discount: discount.toFixed(2),
    received: paidAmount.toFixed(2),
    payMethod: payMethod || "-",
    chargeTime: item.paidAt || item.createdAt || "",
    cashier: item.cashier || item.operator || "",
  });
});

const saveRegistration = async () => {
  await registrationApi.update(registrationId, {
    patientName: form.patientName,
    phone: form.phone,
    gender: form.gender,
    age: Number(form.age || 0),
    departmentName: form.department,
    doctorId: doctorOptions.value.find((item) => item.name === form.doctor)?.id || null,
    doctorName: form.doctor,
    visitType: form.visitType,
    registrationFee: Number(String(form.registrationFee).match(/[\d.]+/)?.[0] || 0),
    diagnosisFee: Number(String(form.treatmentFee).match(/[\d.]+/)?.[0] || 0),
    remark: form.remark,
  });
  ElMessage.success("挂号信息保存成功");
  router.push("/registration/list");
};
</script>

<style>
.edit-registration-page {
  min-height: 100%;
  padding: 28px 0 82px;
}

.edit-card {
  width: min(1710px, calc(100% - 64px));
  min-height: 1282px;
  margin: 0 auto;
  padding: 39px 32px 64px;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
  overflow: hidden;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h2 {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  color: #111722;
  font-size: 24px;
  font-weight: 700;
}

.page-header h2 span {
  width: 6px;
  height: 26px;
  display: inline-block;
  background: #636be8;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 17px;
}

.refund-btn,
.back-btn {
  height: 50px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-radius: 4px;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.refund-btn {
  width: 149px;
  border: 0;
  background: #7779ee;
  color: #fff;
}

.back-btn {
  width: 149px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
}

.refund-icon {
  width: 20px;
  height: 20px;
  position: relative;
  border: 3px solid #fff;
  border-radius: 3px;
  box-sizing: border-box;
}

.refund-icon::before {
  content: "";
  width: 6px;
  height: 6px;
  position: absolute;
  left: 5px;
  top: 5px;
  border: 2px solid #fff;
  border-radius: 50%;
}

.back-icon {
  width: 26px;
  height: 19px;
  position: relative;
}

.back-icon::before {
  content: "";
  width: 20px;
  height: 20px;
  position: absolute;
  left: 2px;
  top: 0;
  border-left: 6px solid #636be8;
  border-bottom: 6px solid #636be8;
  transform: rotate(45deg);
}

.back-icon::after {
  content: "";
  width: 21px;
  height: 6px;
  position: absolute;
  right: 0;
  top: 9px;
  background: #636be8;
}

.section-block h3 {
  margin: 0 0 28px;
  color: #636be8;
  font-size: 31px;
  font-weight: 700;
}

.order-section {
  margin-top: 30px;
}

.order-table {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  border-top: 1px solid #dfe2ff;
  border-left: 1px solid #dfe2ff;
}

.order-table div {
  min-height: 53px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  padding: 0 14px;
  border-right: 1px solid #dfe2ff;
  border-bottom: 1px solid #dfe2ff;
  color: #111722;
  font-size: 18px;
  white-space: nowrap;
  overflow: hidden;
}

.order-table strong {
  color: #ffb400;
  font-weight: 700;
}

.registration-section {
  margin-top: 62px;
}

.patient-section {
  margin-top: 57px;
}

.form-grid {
  display: grid;
  column-gap: 31px;
  row-gap: 27px;
  max-width: 100%;
}

.registration-grid {
  width: min(1290px, 100%);
  grid-template-columns: repeat(3, minmax(0, 368px));
}

.patient-grid {
  grid-template-columns: repeat(4, minmax(0, 368px));
  column-gap: min(44px, 2.3vw);
  row-gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
  color: #111722;
  font-size: 16px;
}

.form-field span i {
  color: #ff0000;
  font-style: normal;
}

.form-field input,
.form-field select,
.age-control {
  width: 100%;
  height: 58px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
  color: #111722;
  font-size: 20px;
  min-width: 0;
}

.form-field input,
.form-field select {
  padding: 0 18px;
}

.form-field input:disabled {
  background: #f5f5f5;
}

.form-field select {
  appearance: none;
  background-image: linear-gradient(45deg, transparent 50%, #cfcfcf 50%), linear-gradient(135deg, #cfcfcf 50%, transparent 50%);
  background-position: calc(100% - 28px) 24px, calc(100% - 20px) 24px;
  background-size: 9px 9px, 9px 9px;
  background-repeat: no-repeat;
}

.active-field select {
  border-color: #636be8;
  box-shadow: 0 2px 9px rgba(99, 107, 232, 0.25);
}

.search-field {
  position: relative;
}

.search-field input {
  padding-right: 52px;
  background: #f5f5f5;
}

.search-icon {
  width: 20px;
  height: 20px;
  position: absolute;
  right: 19px;
  bottom: 18px;
  box-sizing: border-box;
  border: 3px solid #d8d8d8;
  border-radius: 50%;
}

.search-icon::after {
  content: "";
  width: 9px;
  height: 3px;
  position: absolute;
  right: -7px;
  bottom: -4px;
  background: #d8d8d8;
  transform: rotate(45deg);
}

.age-control {
  display: grid;
  grid-template-columns: 1fr 103px;
  overflow: hidden;
}

.age-control input {
  height: 100%;
  border: 0;
  border-radius: 0;
}

.age-control em {
  display: flex;
  align-items: center;
  justify-content: center;
  border-left: 2px solid #e5e5e5;
  font-style: normal;
}

.date-field .el-date-editor.el-input {
  width: 100%;
  height: 58px;
}

.date-field .el-input__wrapper {
  width: 100%;
  height: 58px;
  box-sizing: border-box;
  position: relative;
  padding: 0 48px 0 18px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
  box-shadow: none;
}

.date-field .el-input__wrapper::before {
  content: "";
  width: 18px;
  height: 16px;
  position: absolute;
  right: 16px;
  top: 50%;
  box-sizing: border-box;
  border: 3px solid #cfcfcf;
  border-top-width: 5px;
  border-radius: 2px;
  transform: translateY(-50%);
  pointer-events: none;
}

.date-field .el-input__wrapper::after {
  content: "";
  width: 14px;
  height: 2px;
  position: absolute;
  right: 18px;
  top: calc(50% - 3px);
  background: #fff;
  pointer-events: none;
}

.date-field .el-input__wrapper.is-focus {
  box-shadow: none;
}

.date-field .el-input__inner {
  width: 100%;
  height: 54px;
  min-width: 0;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  color: #111722;
  font-size: 20px;
  line-height: 54px;
}

.date-field .el-input__prefix,
.date-field .el-input__suffix {
  display: none;
}

.detail-address {
  grid-column: span 2;
  min-width: 0;
}

.remark-field {
  grid-column: 1 / span 3;
  min-width: 0;
}

.remark-field input {
  width: 100%;
}

@media (max-width: 1500px) {
  .edit-card {
    width: calc(100% - 48px);
  }

  .registration-grid,
  .patient-grid {
    width: 100%;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .order-table {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1180px) {
  .page-header {
    align-items: flex-start;
    gap: 18px;
  }

  .registration-grid,
  .patient-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .detail-address,
  .remark-field {
    grid-column: 1 / -1;
  }

  .order-table {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
