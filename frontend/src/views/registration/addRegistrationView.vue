<template>
  <el-config-provider :locale="zhCn">
  <div class="registration-page">
    <section class="registration-panel">
      <div class="panel-actions">
        <button type="button" class="charge-button" @click="openChargeDialog">
          <span class="money-mark">￥</span>
          <span>收费</span>
        </button>
      </div>

      <section class="registration-box">
        <div class="registration-grid">
          <label class="field">
            <span>挂号单号</span>
            <input v-model="form.orderNo" class="control readonly" placeholder="保存后自动生成" readonly />
          </label>

          <label class="field">
            <span>科室<i>*</i></span>
            <div class="select-wrap">
              <select v-model="form.department">
                <option value="">请选择</option>
                <option v-for="item in departmentOptions" :key="item.id" :value="item.name">
                  {{ item.name }}
                </option>
              </select>
            </div>
          </label>

          <label class="field">
            <span>接诊类型<i>*</i></span>
            <div class="select-wrap">
              <select v-model="form.visitType">
                <option value="">请选择</option>
                <option>初诊</option>
                <option>复诊</option>
              </select>
            </div>
          </label>

          <label class="field">
            <span>接诊医生<i>*</i></span>
            <div class="select-wrap">
              <select v-model="form.doctor" :disabled="!form.department">
                <option value="">请选择</option>
                <option v-if="form.department && doctorOptions.length === 0" value="" disabled>
                  该科室暂无可用医生
                </option>
                <option v-for="item in doctorOptions" :key="item.id" :value="item.name">
                  {{ item.name }}
                </option>
              </select>
            </div>
          </label>

          <label class="field">
            <span>挂号费<i>*</i></span>
            <div class="select-wrap">
              <select v-model="form.registrationFee">
                <option value="">请选择</option>
                <option>免费（0.00元）</option>
                <option>普通挂号费（10.00元）</option>
                <option>专家挂号费（20.00元）</option>
              </select>
            </div>
          </label>

          <label class="field">
            <span>诊疗费</span>
            <div class="select-wrap">
              <select v-model="form.treatmentFee">
                <option value="">请选择</option>
                <option>免诊疗费（0.00元）</option>
                <option>诊疗费（15.00元）</option>
              </select>
            </div>
          </label>

          <div class="field">
            <span>挂号日期</span>
            <el-date-picker
              v-model="form.registrationDate"
              class="control"
              type="date"
              placeholder="请选择挂号日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
            />
          </div>

          <label class="field">
            <span>挂号员</span>
            <input v-model="form.registrar" class="control readonly" readonly />
          </label>
        </div>
      </section>

      <h1 class="patient-title">患者信息</h1>

      <section class="patient-grid">
        <label class="field">
          <span>患者姓名<i>*</i></span>
          <div class="search-wrap">
            <input
              v-model="patient.name"
              placeholder="患者姓名/手机号码/证件号码/卡号"
              @focus="showPatientDropdown = true"
              @click="showPatientDropdown = true"
              @input="showPatientDropdown = true"
              @blur="hidePatientDropdown"
            />
            <span class="search-icon"></span>
          </div>
          <div v-if="showPatientDropdown" class="patient-dropdown" @mousedown.prevent>
            <button
              v-for="item in filteredPatients"
              :key="item.cardNo"
              class="patient-option"
              type="button"
              @mousedown.prevent="selectPatient(item)"
            >
              <span class="patient-avatar"></span>
              <span class="patient-main">
                <strong>{{ item.name }}</strong>
                <em>{{ item.phone }}</em>
              </span>
              <span class="patient-meta">{{ item.gender }}</span>
              <span class="patient-meta">{{ item.age }}岁</span>
            </button>
          </div>
        </label>

        <label class="field">
          <span>患者卡号</span>
          <input v-model="patient.cardNo" class="control" placeholder="请输入病人卡号" />
        </label>

        <label class="field">
          <span>患者年龄<i>*</i></span>
          <div class="age-wrap">
            <input v-model="patient.age" placeholder="请输入数字" />
            <button type="button">岁<span class="chevron"></span></button>
          </div>
        </label>

        <div class="field">
          <span>出生日期<i>*</i></span>
          <div class="date-wrap date-picker-wrap">
            <el-date-picker
              v-model="patient.birth"
              type="date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              placeholder=""
              :editable="false"
              clearable
            />
          </div>
        </div>

        <label class="field">
          <span>性别<i>*</i></span>
          <div class="select-wrap">
            <select v-model="patient.gender">
              <option>男</option>
              <option>女</option>
            </select>
          </div>
        </label>

        <label class="field">
          <span>手机号码</span>
          <input v-model="patient.phone" class="control" placeholder="请输入手机号码" />
        </label>

        <label class="field">
          <span>证件号码</span>
          <input v-model="patient.idCard" class="control" placeholder="请输入证件号码" />
        </label>

        <label class="field address-region">
          <span>地址</span>
          <el-cascader
            v-model="patientRegionValue"
            class="address-cascader"
            :options="chinaCascaderOptions"
            :show-all-levels="true"
            popper-class="address-cascader-popper"
            @change="syncPatientRegion"
          />
        </label>

        <label class="field address-detail">
          <span>&nbsp;</span>
          <input v-model="patient.address" class="control" placeholder="请输入详细地址" />
        </label>

        <label class="field remark-field">
          <span>备注</span>
          <textarea v-model="patient.remark" class="remark-control"></textarea>
        </label>
      </section>
    </section>

    <div v-if="showChargeDialog" class="dialog-mask">
      <section class="charge-dialog">
        <button class="dialog-close" type="button" @click="showChargeDialog = false"></button>
        <h2>收费</h2>

        <div class="dialog-amount">
          <span>应收金额</span>
          <strong>{{ receivableAmount.toFixed(2) }}</strong>
          <em>元</em>
        </div>

        <div class="pay-form">
          <label class="pay-row">
            <span>优惠金额</span>
            <input v-model.number="payForm.discountAmount" type="number" min="0" step="0.01" @input="syncPayment" />
            <em>元</em>
            <span class="discount-text">折扣</span>
            <input v-model.number="payForm.discountRate" class="small-input" type="number" min="0" step="0.1" @input="syncPayment" />
            <em>折</em>
          </label>

          <label class="pay-row">
            <span>医保支付</span>
            <input v-model.number="payForm.medicalAmount" type="number" min="0" step="0.01" @input="syncPayment" />
            <em>元</em>
          </label>

          <label class="pay-row">
            <span>实收金额</span>
            <input v-model.number="payForm.actualAmount" class="highlight-input" type="number" min="0" step="0.01" />
            <em>元</em>
          </label>

          <label class="pay-row">
            <span>找零</span>
            <input v-model.number="payForm.changeAmount" class="calculated-input" type="number" />
            <em>元</em>
          </label>

          <div class="pay-methods">
            <span>支付方式</span>
            <label v-for="method in payMethods" :key="method">
              <input v-model="payForm.method" type="radio" :value="method" />
              {{ method }}
            </label>
          </div>

          <label class="pay-row">
            <span>收款备注</span>
            <input v-model="payForm.remark" type="text" />
          </label>
        </div>

        <footer class="dialog-actions">
          <button class="cancel-pay" type="button" @click="showChargeDialog = false">取消</button>
          <button class="confirm-pay" type="button" @click="confirmRegistration">确定收款</button>
        </footer>
      </section>
    </div>
  </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { authApi, chargeOrderApi, departmentApi, patientApi, registrationApi, staffApi } from "@/api";
import { chinaAreaOptions, chinaCascaderOptions } from "@/utils/chinaArea";
import { loadEnabledPaymentMethods } from "@/utils/paymentMethods";

type PatientOption = {
  id?: string | number;
  name: string;
  cardNo: string;
  age: string;
  birth: string;
  gender: string;
  phone: string;
  idCard: string;
  region: string;
  address: string;
};

type StaffOption = {
  id: string | number;
  name: string;
  departmentName: string;
  roleName: string;
  enabled: boolean;
};

const router = useRouter();
const selectedPatientId = ref<string | number | null>(null);

const form = reactive({
  orderNo: "",
  department: "",
  visitType: "",
  doctor: "",
  registrationFee: "",
  treatmentFee: "",
  registrationDate: "",
  registrar: "",
});

const patient = reactive({
  name: "",
  cardNo: "",
  age: "",
  birth: "",
  gender: "男",
  phone: "",
  idCard: "",
  region: "上海市",
  address: "",
  remark: "",
});

const patientRegionValue = ref<string[]>(["上海市", "上海市"]);

const syncPatientRegion = () => {
  patient.region = patientRegionValue.value.filter(Boolean).join("/");
};

const syncRegionPickerFromValue = (value: string) => {
  const normalized = String(value || "").replace(/\s+/g, "/");
  const [province, city] = normalized.split("/").filter(Boolean);
  const area = chinaAreaOptions.find((item) => item.name === province)
    || chinaAreaOptions.find((item) => item.name === normalized)
    || chinaAreaOptions[0];
  patientRegionValue.value = [area.name, area.cities.includes(city) ? city : area.cities[0] || ""];
  syncPatientRegion();
};

syncPatientRegion();

const showPatientDropdown = ref(false);
const showChargeDialog = ref(false);
const departmentOptions = ref<any[]>([]);
const staffOptions = ref<StaffOption[]>([]);
const doctorOptions = computed(() => staffOptions.value.filter((item) => (
  item.departmentName === form.department
  && item.enabled
  && (item.roleName === "医生" || item.roleName.toUpperCase() === "DOCTOR")
)));

watch(() => form.department, () => {
  if (!doctorOptions.value.some((item) => item.name === form.doctor)) {
    form.doctor = "";
  }
});

const payMethods = ref<string[]>(["现金"]);

const payForm = reactive({
  discountAmount: 0,
  discountRate: "",
  medicalAmount: 0,
  actualAmount: 0,
  changeAmount: 0,
  method: "现金",
  remark: "",
});

const patientOptions = ref<PatientOption[]>([
  { name: "王猛", cardNo: "028100159", age: "25", birth: "2001-06-12", gender: "男", phone: "18854138766", idCard: "370102200106121234", region: "上海市", address: "浦东新区" },
  { name: "王宽", cardNo: "1000100", age: "15", birth: "2011-03-18", gender: "女", phone: "18878909000", idCard: "440305201103182222", region: "广东省/深圳市", address: "南山区科技园" },
  { name: "李明", cardNo: "028100160", age: "32", birth: "1994-09-09", gender: "男", phone: "13800138000", idCard: "310101199409091111", region: "上海市", address: "黄浦区" },
  { name: "林鹤", cardNo: "028100161", age: "28", birth: "1998-11-21", gender: "男", phone: "18854138767", idCard: "370102199811211234", region: "广东省/广州市", address: "天河区" },
  { name: "张敏", cardNo: "028100162", age: "22", birth: "2004-05-16", gender: "女", phone: "18854138768", idCard: "370102200405161234", region: "上海市", address: "静安区" },
]);

const filteredPatients = computed(() => {
  const keyword = patient.name.trim().replace(/\s+/g, "");
  if (!keyword) return patientOptions.value;
  return patientOptions.value.filter((item) => (
    item.name.includes(keyword)
    || item.phone.includes(keyword)
    || item.cardNo.includes(keyword)
    || item.idCard.includes(keyword)
  ));
});

const hidePatientDropdown = () => {
  window.setTimeout(() => {
    showPatientDropdown.value = false;
  }, 120);
};

const selectPatient = (item: PatientOption) => {
  selectedPatientId.value = item.id || null;
  patient.name = item.name;
  patient.cardNo = item.cardNo;
  patient.age = item.age;
  patient.birth = item.birth;
  patient.gender = item.gender;
  patient.phone = item.phone;
  patient.idCard = item.idCard;
  patient.region = item.region;
  syncRegionPickerFromValue(item.region);
  patient.address = item.address;
  showPatientDropdown.value = false;
};

const feeAmount = (value: string) => {
  const match = value.match(/(\d+(?:\.\d+)?)元/);
  return match ? Number(match[1]) : 0;
};

const receivableAmount = computed(() => (
  feeAmount(form.registrationFee) + feeAmount(form.treatmentFee)
));

const syncPayment = () => {
  const actual = receivableAmount.value - Number(payForm.discountAmount || 0) - Number(payForm.medicalAmount || 0);
  payForm.actualAmount = Number(Math.max(actual, 0).toFixed(2));
  payForm.changeAmount = 0;
};

const padDatePart = (value: number) => String(value).padStart(2, "0");
const nowLocalDateTime = () => {
  const now = new Date();
  return `${now.getFullYear()}-${padDatePart(now.getMonth() + 1)}-${padDatePart(now.getDate())}`
    + `T${padDatePart(now.getHours())}:${padDatePart(now.getMinutes())}:${padDatePart(now.getSeconds())}`;
};

const openChargeDialog = () => {
  syncPayment();
  showChargeDialog.value = true;
};


onMounted(async () => {
  const [patientResponse, departmentResponse, staffResponse, meResponse, enabledPayMethods]: any[] = await Promise.all([
    patientApi.list(),
    departmentApi.list(),
    staffApi.list(),
    authApi.me(),
    loadEnabledPaymentMethods(),
  ]);
  payMethods.value = enabledPayMethods;
  payForm.method = payMethods.value.includes("现金") ? "现金" : payMethods.value[0] || "";
  patientOptions.value = (patientResponse?.data || []).map((item: any) => ({
    id: item.id,
    name: item.name || "",
    cardNo: item.cardNo || item.patientCode || "",
    age: String(item.age || ""),
    birth: item.birthday || item.birthDate || "",
    gender: item.gender || "",
    phone: item.phone || "",
    idCard: item.idCard || "",
    region: item.provinceCity || "",
    address: item.address || "",
  }));
  departmentOptions.value = (departmentResponse?.data || []).filter(
    (item: any) => item.enabled !== false && item.enabled !== 0
  );
  staffOptions.value = (staffResponse?.data || []).map((item: any) => ({
    id: item.id,
    name: item.name || "",
    departmentName: item.departmentName || item.department || "",
    roleName: item.roleName || item.role || "",
    enabled: item.enabled !== false && item.enabled !== 0,
  }));
  form.registrar = meResponse?.data?.name || "";
});

const confirmRegistration = async () => {
  if (!patient.name || !form.department || !form.visitType || !form.doctor) {
    ElMessage.warning("请选择患者并填写科室、接诊类型和接诊医生");
    return;
  }
  if (!payForm.method) {
    ElMessage.warning("请先启用至少一种支付方式");
    return;
  }
  const response: any = await registrationApi.create({
    patientId: selectedPatientId.value,
    patientName: patient.name,
    phone: patient.phone,
    gender: patient.gender,
    age: Number(patient.age || 0),
    departmentName: form.department,
    doctorId: doctorOptions.value.find((item) => item.name === form.doctor)?.id || null,
    doctorName: form.doctor,
    visitType: form.visitType,
    visitTime: form.registrationDate
      ? `${form.registrationDate}T00:00:00`
      : new Date().toISOString(),
    registrationFee: feeAmount(form.registrationFee),
    diagnosisFee: feeAmount(form.treatmentFee),
    status: "PENDING",
    remark: patient.remark,
  });
  form.orderNo = response?.data?.registrationNo || "";
  await chargeOrderApi.create({
    orderNo: `CHG${Date.now()}`,
    registrationId: response?.data?.id,
    patientId: selectedPatientId.value,
    patientName: patient.name,
    chargeType: "挂号收费",
    departmentName: form.department,
    doctorName: form.doctor,
    receivableAmount: receivableAmount.value,
    discountAmount: Number(payForm.discountAmount || 0),
    paidAmount: Number(payForm.actualAmount || 0),
    refundAmount: 0,
    payMethod: payForm.method,
    status: "PAID",
    cashier: form.registrar,
    paidAt: nowLocalDateTime(),
    remark: payForm.remark,
  });
  showChargeDialog.value = false;
  ElMessage.success("挂号成功，挂号单号：" + (response?.data?.registrationNo || form.orderNo));
  router.push("/registration/list");
};
</script>

<style scoped>
.registration-page {
  min-height: 100%;
  padding: 28px 0 64px;
}

.registration-panel {
  width: min(1812px, calc(100% - 96px));
  min-height: 1174px;
  margin: 0 auto;
  padding: 36px 36px 60px;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.panel-actions {
  display: flex;
  justify-content: flex-end;
  height: 90px;
}

.charge-button {
  width: 163px;
  height: 52px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border: 0;
  border-radius: 5px;
  background: #ffc21a;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.money-mark {
  width: 23px;
  height: 23px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff;
  color: #ffc21a;
  font-size: 16px;
  line-height: 1;
}

.registration-box {
  margin-top: 0;
  padding: 39px 16px 38px;
  box-sizing: border-box;
  background: #eef0ff;
}

.registration-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 30px 42px;
}

.patient-title {
  margin: 86px 0 37px;
  color: #636be8;
  font-size: 36px;
  line-height: 1;
  font-weight: 700;
}

.patient-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 23px 52px;
  max-width: none;
}

.field {
  display: block;
  position: relative;
  min-width: 0;
}

.field > span {
  display: block;
  margin-bottom: 13px;
  color: #111722;
  font-size: 18px;
}

.field i {
  color: #ff0000;
  font-style: normal;
}

.control,
.select-wrap,
.search-wrap,
.age-wrap,
.date-wrap {
  width: 100%;
  height: 64px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
}

.control {
  padding: 0 20px;
  outline: none;
  color: #000;
  font-size: 20px;
}

.readonly {
  background: #f7f7f7;
}

.control::placeholder,
.search-wrap input::placeholder,
.age-wrap input::placeholder {
  color: #c7c7c7;
}

.select-wrap {
  position: relative;
}

.select-wrap::after {
  content: "";
  position: absolute;
  top: 23px;
  right: 18px;
  width: 11px;
  height: 11px;
  border-right: 3px solid #cfcfcf;
  border-bottom: 3px solid #cfcfcf;
  transform: rotate(45deg);
  pointer-events: none;
}

.select-wrap select {
  width: 100%;
  height: 100%;
  padding: 0 50px 0 20px;
  border: 0;
  outline: none;
  appearance: none;
  background: transparent;
  color: #111722;
  font-size: 20px;
}

.address-cascader {
  width: 100%;
  height: 64px;
}

.address-cascader :deep(.el-input),
.address-cascader :deep(.el-input__wrapper) {
  width: 100%;
  height: 100%;
}

.address-cascader :deep(.el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 0 0 2px #d0d0d0 inset;
}

:global(.address-cascader-popper .el-cascader-menu) {
  height: 216px;
}

:global(.address-cascader-popper .el-cascader-node) {
  height: 36px;
  line-height: 36px;
}

.search-wrap,
.date-wrap {
  display: flex;
  align-items: center;
  position: relative;
}

.search-wrap input,
.date-wrap input {
  width: 100%;
  height: 100%;
  padding: 0 50px 0 20px;
  box-sizing: border-box;
  border: 0;
  outline: none;
  background: transparent;
  color: #111722;
  font-size: 20px;
}

.search-icon {
  position: absolute;
  right: 17px;
  width: 21px;
  height: 21px;
  border: 3px solid #d5d5d5;
  border-radius: 50%;
  box-sizing: border-box;
}

.search-icon::after {
  content: "";
  position: absolute;
  right: -7px;
  bottom: -5px;
  width: 9px;
  height: 3px;
  border-radius: 2px;
  background: #d5d5d5;
  transform: rotate(45deg);
}

.patient-dropdown {
  position: absolute;
  top: 96px;
  left: 0;
  z-index: 40;
  width: 100%;
  max-height: 417px;
  box-sizing: border-box;
  border: 1px solid #e6e8ff;
  border-radius: 3px;
  background: #fff;
  box-shadow: 0 2px 13px rgba(99, 107, 232, 0.14);
  overflow-y: auto;
}

.patient-dropdown::before {
  content: "";
  position: absolute;
  top: -12px;
  left: 96px;
  width: 0;
  height: 0;
  border-left: 13px solid transparent;
  border-right: 13px solid transparent;
  border-bottom: 13px solid #fff;
  filter: drop-shadow(0 -1px 1px rgba(99, 107, 232, 0.08));
}

.patient-dropdown::-webkit-scrollbar {
  width: 18px;
}

.patient-dropdown::-webkit-scrollbar-track {
  background: #fff;
}

.patient-dropdown::-webkit-scrollbar-thumb {
  border: 5px solid #fff;
  border-radius: 12px;
  background: #8b8b8b;
}

.patient-option {
  width: 100%;
  height: 83px;
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) 45px 58px;
  align-items: center;
  padding: 0 14px 0 19px;
  box-sizing: border-box;
  border: 0;
  border-bottom: 1px solid #eeeeee;
  background: #fff;
  color: #111722;
  text-align: left;
  cursor: pointer;
}

.patient-option:hover {
  background: #f7f8ff;
}

.patient-avatar {
  width: 48px;
  height: 48px;
  position: relative;
  border-radius: 50%;
  background: #f7f7fb;
  box-shadow: 0 1px 8px rgba(56, 61, 120, 0.16);
}

.patient-avatar::before {
  content: "";
  position: absolute;
  top: 9px;
  left: 16px;
  width: 17px;
  height: 18px;
  border-radius: 50% 50% 44% 44%;
  background: #ffb5b5;
  box-shadow: 0 -6px 0 2px #32385b;
}

.patient-avatar::after {
  content: "";
  position: absolute;
  left: 10px;
  bottom: 7px;
  width: 29px;
  height: 18px;
  border-radius: 18px 18px 9px 9px;
  background: #4a4d82;
}

.patient-main {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 5px;
}

.patient-main strong {
  color: #111722;
  font-size: 19px;
  font-weight: 700;
}

.patient-main em,
.patient-meta {
  color: #8c94a3;
  font-size: 16px;
  font-style: normal;
}

.age-wrap {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 104px;
}

.age-wrap input {
  min-width: 0;
  padding: 0 20px;
  border: 0;
  outline: none;
  color: #111722;
  font-size: 20px;
}

.age-wrap button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 17px;
  border: 0;
  border-left: 2px solid #e6e6e6;
  background: #fff;
  color: #111722;
  font-size: 20px;
}

.chevron {
  width: 11px;
  height: 11px;
  border-right: 3px solid #cfcfcf;
  border-bottom: 3px solid #cfcfcf;
  transform: rotate(45deg);
}

.calendar-icon {
  position: absolute;
  right: 18px;
  width: 20px;
  height: 18px;
  box-sizing: border-box;
  border: 3px solid #cfcfcf;
  border-top-width: 5px;
  border-radius: 2px;
}

.calendar-icon::before,
.calendar-icon::after {
  content: "";
  position: absolute;
  top: -8px;
  width: 3px;
  height: 7px;
  background: #cfcfcf;
}

.calendar-icon::before {
  left: 3px;
}

.calendar-icon::after {
  right: 3px;
}

.date-picker-wrap :deep(.el-date-editor.el-input) {
  width: 100%;
  height: 100%;
}

.date-picker-wrap :deep(.el-input__wrapper) {
  height: 100%;
  padding: 0 17px 0 20px;
  border-radius: 4px;
  background: transparent;
  box-shadow: none;
}

.date-picker-wrap :deep(.el-input__inner) {
  color: #111722;
  font-size: 20px;
}

.date-picker-wrap :deep(.el-input__prefix) {
  order: 2;
  margin-left: auto;
  margin-right: 0;
  color: #cfcfcf;
}

.address-region {
  grid-column: 1 / 2;
}

.address-detail {
  grid-column: 2 / 4;
}

.remark-field {
  grid-column: 1 / 4;
}

.remark-control {
  width: 100%;
  height: 64px;
  padding: 10px 20px;
  box-sizing: border-box;
  resize: none;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  color: #111722;
  font-size: 20px;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.38);
}

.charge-dialog {
  width: 972px;
  min-height: 704px;
  position: relative;
  box-sizing: border-box;
  padding: 29px 31px 29px;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.24);
}

.charge-dialog h2 {
  margin: 0 0 31px;
  color: #111722;
  font-size: 28px;
  font-weight: 700;
}

.dialog-close {
  position: absolute;
  top: 31px;
  right: 31px;
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.dialog-close::before,
.dialog-close::after {
  content: "";
  position: absolute;
  top: 13px;
  left: 1px;
  width: 27px;
  height: 3px;
  border-radius: 3px;
  background: #d3d3d3;
}

.dialog-close::before {
  transform: rotate(45deg);
}

.dialog-close::after {
  transform: rotate(-45deg);
}

.dialog-amount {
  min-height: 64px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  box-sizing: border-box;
  background: #dfe2ff;
  color: #111722;
  font-size: 22px;
  font-weight: 700;
}

.dialog-amount span {
  margin-right: 24px;
}

.dialog-amount strong {
  margin-right: 6px;
  color: #111722;
  font-size: 28px;
}

.dialog-amount em {
  display: block;
  margin-left: -80px;
  padding-top: 56px;
  font-style: normal;
}

.pay-form {
  padding: 36px 14px 0;
}

.pay-row {
  min-height: 76px;
  display: flex;
  align-items: center;
  color: #111722;
  font-size: 22px;
}

.pay-row > span:first-child,
.pay-methods > span {
  width: 111px;
  flex: 0 0 111px;
}

.pay-row input {
  width: 315px;
  height: 50px;
  box-sizing: border-box;
  padding: 0 14px;
  border: 3px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  color: #111722;
  font-size: 24px;
}

.pay-row em {
  margin-left: 12px;
  margin-right: 33px;
  color: #111722;
  font-size: 22px;
  font-style: normal;
}

.pay-row .discount-text {
  width: auto;
  flex: 0 0 auto;
  margin-right: 12px;
}

.pay-row .small-input {
  width: 119px;
}

.pay-row .highlight-input {
  color: #ffc21a;
  font-weight: 700;
}

.pay-row .calculated-input {
  background: #f7f7f7;
}

.pay-methods {
  min-height: 56px;
  display: flex;
  align-items: center;
  color: #111722;
  font-size: 22px;
}

.pay-methods label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-right: 22px;
  white-space: nowrap;
}

.pay-methods input {
  width: 17px;
  height: 17px;
  accent-color: #1a73e8;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 19px;
  margin-top: 40px;
}

.dialog-actions button {
  height: 48px;
  border-radius: 5px;
  font-size: 18px;
  cursor: pointer;
}

.cancel-pay {
  width: 91px;
  border: 1px solid #8b91ff;
  background: #fff;
  color: #8b91ff;
}

.confirm-pay {
  width: 108px;
  border: 0;
  background: #636be8;
  color: #fff;
  font-weight: 700;
}

@media (max-width: 1280px) {
  .registration-panel {
    width: calc(100% - 32px);
  }

  .registration-grid,
  .patient-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .address-detail,
  .remark-field {
    grid-column: 1 / -1;
  }
}
</style>
