<template>
  <div class="add-patient-page">
    <section class="patient-card">
      <header class="page-header">
        <h2 v-if="!isEditPage">新增患者信息</h2>
        <div v-else class="patient-tabs" role="tablist">
          <button
            v-for="tab in patientTabs"
            :key="tab.key"
            :class="['patient-tab', { active: activeTab === tab.key }]"
            type="button"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>
        <div class="header-actions">
          <button v-if="!isEditPage || activeTab === 'info'" type="button" class="top-btn primary" @click="savePatient">
            <Document class="btn-icon" />
            <span>保存</span>
          </button>
          <button v-else-if="isEditPage && activeTab === 'record'" type="button" class="top-btn primary">
            <CirclePlusFilled class="btn-icon" />
            <span>新增病历</span>
          </button>
          <button type="button" class="top-btn outline" @click="router.back()">
            <Back class="btn-icon" />
            <span>返回</span>
          </button>
        </div>
      </header>

      <template v-if="!isEditPage || activeTab === 'info'">
        <h1 class="section-title">基本信息</h1>

        <section class="form-grid" aria-label="基本信息">
          <label v-for="field in formFields" :key="field.key" :class="['field', field.className]">
            <span v-html="field.label"></span>
            <div
              :class="[
                'control',
                field.type === 'age' ? 'age-control' : '',
                field.type === 'address' && isAddressPickerOpen ? 'active-control' : '',
                field.key === 'nation' && isNationSelectOpen ? 'active-control' : '',
                field.key in fieldErrors ? 'invalid-control' : '',
              ]"
            >
              <input
                v-if="field.type === 'input'"
                :placeholder="field.placeholder"
                v-model="patientForm[field.key]"
                :maxlength="field.key === 'phone' ? 11 : field.key === 'idCard' ? 18 : undefined"
                @input="handlePatientInput(field.key)"
              />
              <template v-else-if="field.type === 'select' && field.key === 'nation'">
                <button type="button" class="custom-select-trigger" @click.stop="toggleNationSelect">
                  <span>{{ patientForm.nation || field.placeholder || "请选择" }}</span>
                  <ArrowUp v-if="isNationSelectOpen" class="select-arrow" />
                  <ArrowDown v-else class="select-arrow" />
                </button>
                <div v-if="isNationSelectOpen" class="nation-select-panel" @click.stop>
                  <button
                    v-for="option in field.options || []"
                    :key="option"
                    type="button"
                    :class="['nation-option', { active: patientForm.nation === option }]"
                    @click="selectNation(option)"
                  >
                    {{ option }}
                  </button>
                </div>
              </template>
              <select v-else-if="field.type === 'select'" v-model="patientForm[field.key]">
                <option value="" disabled>{{ field.placeholder || "请选择" }}</option>
                <option v-for="option in field.options" :key="option" :value="option">{{ option }}</option>
              </select>
              <template v-else-if="field.type === 'date'">
                <input
                  type="date"
                  :placeholder="field.placeholder"
                  v-model="patientForm[field.key]"
                  @change="handlePatientDateChange(field.key)"
                />
                <Calendar class="control-icon" />
              </template>
              <template v-else-if="field.type === 'age'">
                <input type="number" min="0" placeholder="请输入数字" v-model="patientForm[field.key]" />
                <select><option>岁</option></select>
              </template>
              <template v-else-if="field.type === 'address'">
                <button type="button" class="address-trigger" @click="isAddressPickerOpen = !isAddressPickerOpen">
                  <span>{{ selectedAddressLabel }}</span>
                  <ArrowUp v-if="isAddressPickerOpen" class="select-arrow" />
                  <ArrowDown v-else class="select-arrow" />
                </button>
                <div v-if="isAddressPickerOpen" class="address-panel">
                  <div class="address-column province-column">
                    <button
                      v-for="province in areaOptions"
                      :key="province.name"
                      type="button"
                      :class="['address-option', { active: selectedProvince === province.name }]"
                      @click="selectProvince(province.name)"
                    >
                      <span>{{ province.name }}</span>
                      <ArrowRight class="option-arrow" />
                    </button>
                  </div>
                  <div class="address-column">
                    <button
                      v-for="city in currentCities"
                      :key="city"
                      type="button"
                      :class="['address-option', { active: patientForm.city === `${selectedProvince}/${city}` }]"
                      @click="selectCity(city)"
                    >
                      {{ city }}
                    </button>
                  </div>
                </div>
              </template>
            </div>
            <p v-if="field.key in fieldErrors" class="field-error">{{ fieldErrors[field.key] }}</p>
          </label>
        </section>

        <section class="family-section">
          <h1 class="section-title">关联家庭成员</h1>
          <table class="family-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>家庭关系</th>
                <th>姓名</th>
                <th>性别</th>
                <th>单位</th>
                <th>出生日期</th>
                <th>手机号码</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(member, index) in familyMembers" :key="member.id">
                <td>{{ index + 1 }}</td>
                <td>{{ member.relation }}</td>
                <td>{{ member.name || "-" }}</td>
                <td>{{ member.gender }}</td>
                <td>{{ member.company || "无" }}</td>
                <td>{{ member.birthday }}</td>
                <td>{{ member.phone }}</td>
                <td>{{ member.createdAt }}</td>
                <td class="family-actions">
                  <button type="button" class="link-btn" @click="openFamilyDialog('view', member)">查看</button>
                  <button type="button" class="link-btn" @click="openFamilyDialog('edit', member)">编辑</button>
                  <button type="button" class="link-btn" @click="removeFamilyMember(member)">取消关联</button>
                </td>
              </tr>
            </tbody>
          </table>

          <button type="button" class="family-add-btn" @click="openFamilyDialog('create')">
            <CirclePlusFilled class="btn-icon" />
            <span>添加家庭成员</span>
          </button>
        </section>
      </template>

      <section v-else-if="isEditPage && activeTab === 'record'" class="record-panel">
        <aside class="record-timeline">
          <article
            v-for="(record, index) in medicalRecords"
            :key="record.id"
            :class="['timeline-item', { active: index === activeRecordIndex }]"
            @click="activeRecordIndex = index"
          >
            <div class="time-tag">{{ record.time }}</div>
            <div class="record-card">
              <p>门诊编号： <span>{{ record.no }}</span>　诊断： <a>{{ record.diagnosis }}</a></p>
              <p>接诊科室： {{ record.departmentName || "-" }}　　　　接诊医生： {{ record.doctorName || "-" }}</p>
            </div>
          </article>
        </aside>

        <article v-if="activeMedicalRecord" class="record-detail">
          <div class="record-detail-actions">
            <span class="visit-badge">{{ activeMedicalRecord.visitType || "门诊" }}</span>
            <div>
              <button type="button" class="outline-action">
                <EditPen class="small-icon" />
                <span>编辑病历</span>
              </button>
              <button type="button" class="outline-action">
                <Printer class="small-icon" />
                <span>打印病历</span>
              </button>
            </div>
          </div>

          <h2>体格信息</h2>
          <div class="vital-grid">
            <span v-for="field in vitalFields" :key="field.key">
              {{ field.label }}： {{ activeMedicalRecord.vitalSigns[field.key] || "-" }} {{ field.unit }}
            </span>
          </div>

          <h2>病历信息</h2>
          <div class="medical-text">
            <p><a>发病日期：</a>{{ activeMedicalRecord.medicalRecord.onsetDate || "-" }}</p>
            <p><a>主诉：</a>{{ activeMedicalRecord.medicalRecord.chiefComplaint || activeMedicalRecord.chiefComplaint || "-" }}</p>
            <p v-for="field in medicalFields" :key="field.key">
              {{ field.label }}：{{ activeMedicalRecord.medicalRecord[field.key] || "-" }}
            </p>
            <p>初步诊断：{{ activeMedicalRecord.diagnosis || "-" }}</p>
            <p>医嘱： <span class="advice-tag">{{ activeMedicalRecord.doctorAdvice || "-" }}</span></p>
          </div>

          <div class="record-divider"></div>
          <h2>处方信息</h2>
          <div class="prescription-text">
            <a>{{ activeMedicalRecord.prescription.name || "处方" }}</a>
            <p v-for="(item, index) in activeMedicalRecord.prescription.items || []" :key="item.id || index">
              {{ index + 1 }}　{{ item.name || "-" }}　{{ item.dosage || "" }}　{{ item.quantity || "" }}
             　用法：{{ item.usage || "-" }}　{{ item.frequency || "" }}　{{ item.days ? `${item.days}天` : "" }}
            </p>
            <p v-if="!(activeMedicalRecord.prescription.items || []).length">暂无处方信息</p>
          </div>
        </article>
        <el-empty v-else description="暂无电子病历" />
      </section>

      <section v-else-if="isEditPage" class="charge-record-panel">
        <div class="charge-total">消费总计金额： <strong>{{ chargeTotal }}</strong>元</div>
        <h2>消费明细</h2>
        <table class="charge-record-table">
          <thead>
            <tr>
              <th>收费/退费时间</th>
              <th>收费状态</th>
              <th>收费类型</th>
              <th>应收（可退）金额</th>
              <th>实收（实退）金额</th>
              <th>支付方式</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in chargeRows" :key="row.id">
              <td>{{ row.time }}</td>
              <td :class="row.statusClass">{{ row.status }}</td>
              <td>{{ row.type }}</td>
              <td>{{ row.receivable }}</td>
              <td :class="{ refund: row.actual.startsWith('-') }">{{ row.actual }}</td>
              <td>{{ row.payMethod }}</td>
              <td>
                <button type="button" @click="viewChargeOrder(row.id)">{{ row.action }}</button>
              </td>
            </tr>
            <tr v-if="!chargeRows.length">
              <td colspan="7">暂无收费记录</td>
            </tr>
          </tbody>
        </table>
      </section>
    </section>

    <div v-if="showFamilyDialog" class="modal-mask">
      <section class="family-dialog" role="dialog" aria-modal="true" aria-label="添加家庭成员">
        <button type="button" class="dialog-close" aria-label="关闭" @click="closeFamilyDialog">
          <Close />
        </button>
        <h2>{{ familyDialogTitle }}</h2>

        <div class="dialog-grid">
          <label class="field">
            <span>家庭关系<i>*</i></span>
            <div class="control">
              <select v-model="familyForm.relation" :disabled="familyDialogReadonly">
                <option value="" disabled>请选择</option>
                <option v-if="familyDialogReadonly && !familyRelationOptions.includes(familyForm.relation)" :value="familyForm.relation">{{ familyForm.relation }}</option>
                <option v-for="relation in familyRelationOptions" :key="relation" :value="relation">
                  {{ relation }}
                </option>
              </select>
            </div>
          </label>
          <label class="field">
            <span>患者姓名<i>*</i></span>
            <div class="control search-inside">
              <input v-model.trim="familyForm.name" :disabled="familyDialogReadonly" placeholder="请输入家庭成员姓名" />
              <Search class="inside-icon" />
            </div>
          </label>
          <label class="field">
            <span>患者年龄<i>*</i></span>
            <div class="control age-control">
              <input v-model="familyForm.age" :disabled="familyDialogReadonly" type="number" min="0" placeholder="请输入数字" />
              <select><option>岁</option></select>
            </div>
          </label>
          <label class="field">
            <span>出生日期<i>*</i></span>
            <div class="control">
              <input
                v-model="familyForm.birthday"
                :disabled="familyDialogReadonly"
                type="date"
                @change="syncFamilyAgeFromBirthday"
              />
              <Calendar class="control-icon" />
            </div>
          </label>
          <label class="field">
            <span>性别<i>*</i></span>
            <div class="control">
              <select v-model="familyForm.gender" :disabled="familyDialogReadonly">
                <option v-for="gender in genderOptions" :key="gender" :value="gender">{{ gender }}</option>
              </select>
            </div>
          </label>
          <label class="field">
            <span>手机号码</span>
            <div class="control"><input v-model.trim="familyForm.phone" :disabled="familyDialogReadonly" placeholder="请输入手机号码" /></div>
          </label>
          <label class="field">
            <span>单位</span>
            <div class="control"><input v-model.trim="familyForm.company" :disabled="familyDialogReadonly" placeholder="无" /></div>
          </label>
        </div>

        <footer class="dialog-actions">
          <button type="button" class="dialog-btn cancel" @click="closeFamilyDialog">{{ familyDialogReadonly ? "关闭" : "取消" }}</button>
          <button v-if="!familyDialogReadonly" type="button" class="dialog-btn confirm" @click="saveFamilyMember">确定</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowDown,
  ArrowRight,
  ArrowUp,
  Back,
  Calendar,
  CirclePlusFilled,
  Close,
  Document,
  EditPen,
  Printer,
  Search,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { familyMemberApi, patientApi } from "@/api";
import { chinaAreaOptions } from "@/utils/chinaArea";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
type PatientTab = "info" | "record" | "charge";
const isEditPage = computed(() => route.path === "/patients/edit" || route.path === "/patient/edit");
const activeTab = ref<PatientTab>("info");
const showFamilyDialog = ref(false);
const isAddressPickerOpen = ref(false);
const isNationSelectOpen = ref(false);
const activeRecordIndex = ref(0);
const familyDialogMode = ref<"create" | "view" | "edit">("create");
const editingFamilyMemberId = ref<string | number | null>(null);

const patientTabs: Array<{ key: PatientTab; label: string }> = [
  { key: "info", label: "个人信息" },
  { key: "record", label: "电子病历" },
  { key: "charge", label: "收费记录" },
];

const required = (text: string) => `${text}<i>*</i>`;

type FieldType = "input" | "select" | "date" | "age" | "address";

type FormField = {
  key: string;
  label: string;
  type: FieldType;
  placeholder?: string;
  options?: string[];
  className?: string;
};

const genderOptions = ["男", "女"];
const patientSourceOptions = ["别人介绍", "广告", "自来"];
const memberTypeOptions = ["非会员", "初级会员", "高级会员", "白银会员", "黄金会员", "钻石会员"];
const marriageOptions = ["未婚", "已婚", "保密"];
const educationOptions = ["初中及以下", "高中", "大专", "本科", "硕士及以上"];
const jobOptions = ["工人", "公务员", "医生", "教师", "互联网从业者", "其他"];
const familyRelationOptions = ["父女", "母女", "兄弟", "姐妹", "其他"];
const nationOptions = [
  "汉族",
  "蒙古族",
  "回族",
  "藏族",
  "维吾尔族",
  "苗族",
  "彝族",
  "壮族",
  "布依族",
  "朝鲜族",
  "满族",
  "侗族",
  "瑶族",
  "白族",
  "土家族",
  "哈尼族",
  "哈萨克族",
  "傣族",
  "黎族",
  "傈僳族",
  "佤族",
  "畲族",
  "高山族",
  "拉祜族",
  "水族",
  "东乡族",
  "纳西族",
  "景颇族",
  "柯尔克孜族",
  "土族",
  "达斡尔族",
  "仫佬族",
  "羌族",
  "布朗族",
  "撒拉族",
  "毛南族",
  "仡佬族",
  "锡伯族",
  "阿昌族",
  "普米族",
  "塔吉克族",
  "怒族",
  "乌孜别克族",
  "俄罗斯族",
  "鄂温克族",
  "德昂族",
  "保安族",
  "裕固族",
  "京族",
  "塔塔尔族",
  "独龙族",
  "鄂伦春族",
  "赫哲族",
  "门巴族",
  "珞巴族",
  "基诺族",
];

const patientForm = ref<Record<string, string>>({
  name: "",
  card: "",
  age: "",
  birthday: "",
  gender: "男",
  phone: "",
  idCard: "",
  source: "",
  member: "非会员",
  expire: "",
  nation: "汉族",
  marriage: "",
  education: "",
  city: "广东省/深圳市",
  address: "",
  job: "",
  company: "",
  remark: "",
});

const createEmptyFamilyForm = () => ({
  relation: "",
  name: "",
  age: "",
  birthday: "",
  gender: "男",
  phone: "",
  company: "无",
});

const familyForm = ref(createEmptyFamilyForm());
const familyDialogReadonly = computed(() => familyDialogMode.value === "view");
const familyDialogTitle = computed(() => ({
  create: "添加家庭成员",
  view: "查看家庭成员",
  edit: "编辑家庭成员",
}[familyDialogMode.value]));

const phonePattern = /^1[3-9]\d{9}$/;
const idCardPattern = /(^\d{15}$)|(^\d{17}[\dXx]$)/;
const fieldErrors = computed<Record<string, string>>(() => {
  const errors: Record<string, string> = {};
  const phone = patientForm.value.phone.trim();
  const idCard = patientForm.value.idCard.trim();
  if (phone && !phonePattern.test(phone)) {
    errors.phone = "\u8bf7\u8f93\u5165\u6b63\u786e\u7684 11 \u4f4d\u624b\u673a\u53f7\u7801";
  }
  if (idCard && !idCardPattern.test(idCard)) {
    errors.idCard = "\u8bf7\u8f93\u5165\u6b63\u786e\u7684 15 \u4f4d\u6216 18 \u4f4d\u8bc1\u4ef6\u53f7\u7801";
  }
  return errors;
});

const areaOptions = chinaAreaOptions;

const selectedProvince = ref(patientForm.value.city.split("/")[0]);
const currentCities = computed(() => (
  areaOptions.find((province) => province.name === selectedProvince.value)?.cities || []
));
const selectedAddressLabel = computed(() => patientForm.value.city || "请选择");

const toggleNationSelect = () => {
  isAddressPickerOpen.value = false;
  isNationSelectOpen.value = !isNationSelectOpen.value;
};

const selectNation = (nation: string) => {
  patientForm.value.nation = nation;
  isNationSelectOpen.value = false;
};

const closeNationSelect = () => {
  isNationSelectOpen.value = false;
};

const calculateAge = (birthday: string) => {
  if (!birthday) return "";
  const parts = birthday.split("-").map(Number);
  if (parts.length !== 3 || parts.some((part) => Number.isNaN(part))) return "";
  const [year, month, day] = parts;
  const today = new Date();
  let age = today.getFullYear() - year;
  const birthdayThisYear = new Date(today.getFullYear(), month - 1, day);
  if (today < birthdayThisYear) age -= 1;
  return String(Math.max(age, 0));
};

const syncPatientAgeFromBirthday = () => {
  patientForm.value.age = calculateAge(patientForm.value.birthday);
};

const syncFamilyAgeFromBirthday = () => {
  familyForm.value.age = calculateAge(familyForm.value.birthday);
};

const handlePatientDateChange = (fieldKey: string) => {
  if (fieldKey === "birthday") syncPatientAgeFromBirthday();
};

const selectProvince = (province: string) => {
  selectedProvince.value = province;
};

const selectCity = (city: string) => {
  patientForm.value.city = `${selectedProvince.value}/${city}`;
  isAddressPickerOpen.value = false;
};

const handlePatientInput = (key: string) => {
  if (key === "phone") {
    patientForm.value.phone = patientForm.value.phone.replace(/\D/g, "").slice(0, 11);
  }
  if (key === "idCard") {
    patientForm.value.idCard = patientForm.value.idCard.replace(/[^\dXx]/g, "").slice(0, 18).toUpperCase();
  }
};

onMounted(() => {
  document.addEventListener("click", closeNationSelect);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", closeNationSelect);
});

const formFields = [
  { key: "name", label: required("患者姓名"), type: "input", placeholder: "请填写患者姓名" },
  { key: "card", label: "患者卡号", type: "input", placeholder: "请输入病人卡号" },
  { key: "age", label: required("患者年龄"), type: "age" },
  { key: "birthday", label: required("出生日期"), type: "date", placeholder: "请选择日期" },
  { key: "gender", label: required("性别"), type: "select", options: genderOptions },
  { key: "phone", label: "手机号码", type: "input", placeholder: "请输入手机号码" },
  { key: "idCard", label: "证件号码", type: "input", placeholder: "请输入证件号码" },
  { key: "source", label: "患者来源", type: "select", placeholder: "请选择", options: patientSourceOptions },
  { key: "member", label: required("会员类型"), type: "select", options: memberTypeOptions },
  { key: "expire", label: "到期时间", type: "date", placeholder: "请选择日期" },
  { key: "nation", label: "民族", type: "select", placeholder: "请选择", options: nationOptions },
  { key: "marriage", label: "婚姻状况", type: "select", placeholder: "请选择", options: marriageOptions },
  { key: "education", label: "学历", type: "select", placeholder: "请选择", options: educationOptions },
  { key: "city", label: "地址", type: "address" },
  { key: "address", label: "&nbsp;", type: "input", placeholder: "请输入详细地址", className: "wide" },
  { key: "job", label: "职业", type: "select", placeholder: "请选择", options: jobOptions },
  { key: "company", label: "工作单位", type: "input" },
  { key: "remark", label: "备注", type: "input", className: "wide" },
] satisfies FormField[];

const familyMembers = ref<Array<Record<string, string | number>>>([]);

const parseJsonObject = (value: unknown): Record<string, any> => {
  if (value && typeof value === "object") return value as Record<string, any>;
  if (typeof value !== "string" || !value.trim()) return {};
  try {
    const result = JSON.parse(value);
    return result && typeof result === "object" ? result : {};
  } catch {
    return {};
  }
};

const parsePrescription = (value: unknown): Record<string, any> => {
  const parsed = parseJsonObject(value);
  if (Object.keys(parsed).length) return parsed;
  if (typeof value === "string" && value.trim()) {
    return {
      name: "处方",
      items: [{ name: value.trim() }],
    };
  }
  return {};
};

const vitalFields = [
  { key: "temperature", label: "体温", unit: "℃" },
  { key: "breathing", label: "呼吸", unit: "次/分" },
  { key: "pulse", label: "脉搏", unit: "次/分" },
  { key: "bloodPressure", label: "血压", unit: "mmHg" },
  { key: "bloodSugar", label: "血糖", unit: "mmol/L" },
  { key: "bloodFat", label: "血脂", unit: "mmol/L" },
  { key: "height", label: "身高", unit: "cm" },
  { key: "weight", label: "体重", unit: "kg" },
];

const medicalFields = [
  { key: "presentHistory", label: "现病史" },
  { key: "pastHistory", label: "既往史" },
  { key: "allergyHistory", label: "过敏史" },
  { key: "personalHistory", label: "个人史" },
  { key: "familyHistory", label: "家族史" },
  { key: "auxiliaryExam", label: "辅助检查" },
  { key: "treatment", label: "治疗意见" },
  { key: "remark", label: "备注" },
];

const medicalRecords = ref<any[]>([]);
const activeMedicalRecord = computed(() => medicalRecords.value[activeRecordIndex.value] || null);
const chargeRows = ref<any[]>([]);
const chargeTotal = computed(() => chargeRows.value.reduce(
  (total, row) => total + Number(row.paidAmount || 0) - Number(row.refundAmount || 0),
  0,
).toFixed(2));

const money = (value: unknown) => Number(value || 0).toFixed(2);

const mapMedicalRecord = (item: any) => {
  const medicalRecord = parseJsonObject(item.medicalRecord);
  if (medicalRecord.history && !medicalRecord.pastHistory) {
    medicalRecord.pastHistory = medicalRecord.history;
  }
  return {
    ...item,
    time: item.createdAt || item.updatedAt || "",
    no: item.registrationId || item.id || "",
    diagnosis: item.diagnosis || "未填写",
    vitalSigns: parseJsonObject(item.vitalSigns),
    medicalRecord,
    prescription: parsePrescription(item.prescription),
  };
};

const mapChargeRow = (item: any) => {
  const statusKey = String(item.status || "PENDING").toUpperCase();
  const refunded = statusKey === "REFUNDED" || Number(item.refundAmount || 0) > 0;
  const statusMap: Record<string, { text: string; className: string }> = {
    PENDING: { text: "未收费", className: "pending" },
    PARTIAL: { text: "部分收费", className: "pending" },
    PAID: { text: "已收费", className: "paid" },
    REFUNDED: { text: "已退费", className: "refunded" },
    CANCELLED: { text: "已取消", className: "refunded" },
  };
  const status = statusMap[statusKey] || { text: item.status || "-", className: "" };
  return {
    id: item.id,
    time: refunded ? item.refundedAt || item.updatedAt : item.paidAt || item.createdAt,
    status: status.text,
    statusClass: status.className,
    type: item.chargeType || "-",
    receivable: money(item.receivableAmount),
    actual: refunded ? `-${money(item.refundAmount)}` : money(item.paidAmount),
    paidAmount: Number(item.paidAmount || 0),
    refundAmount: Number(item.refundAmount || 0),
    payMethod: refunded ? item.refundMethod || item.payMethod || "-" : item.payMethod || "-",
    action: "查看详情",
  };
};

const patientId = ref<string | number | null>(readNavigationState(route) || null);

onMounted(async () => {
  if (!patientId.value) return;
  const [patientResponse, familyResponse, medicalResponse, chargeResponse]: any[] = await Promise.all([
    patientApi.get(patientId.value),
    patientApi.familyMembers(patientId.value),
    patientApi.medicalRecords(patientId.value),
    patientApi.chargeOrders(patientId.value),
  ]);
  const item = patientResponse?.data || {};
  Object.assign(patientForm.value, {
    name: item.name || "",
    card: item.cardNo || item.patientCode || "",
    age: String(item.age || ""),
    birthday: item.birthday || item.birthDate || "",
    gender: item.gender || "男",
    phone: item.phone || "",
    idCard: item.idCard || "",
    source: item.source || "",
    member: item.memberLevel || "非会员",
    expire: item.memberExpireDate || "",
    nation: item.nation || "",
    marriage: item.marriage || "",
    education: item.education || "",
    city: item.provinceCity || "",
    address: item.address || "",
    job: item.job || "",
    company: item.company || "",
    remark: item.remark || "",
  });
  syncPatientAgeFromBirthday();
  familyMembers.value = familyResponse?.data || [];
  medicalRecords.value = (medicalResponse?.data || []).map(mapMedicalRecord);
  chargeRows.value = (chargeResponse?.data || []).map(mapChargeRow);
});

const patientPayload = () => ({
  name: patientForm.value.name,
  patientCode: patientForm.value.card,
  cardNo: patientForm.value.card,
  age: Number(patientForm.value.age || 0),
  birthday: patientForm.value.birthday || null,
  gender: patientForm.value.gender,
  phone: patientForm.value.phone,
  idCard: patientForm.value.idCard,
  source: patientForm.value.source,
  memberLevel: patientForm.value.member,
  memberExpireDate: patientForm.value.expire || null,
  nation: patientForm.value.nation,
  marriage: patientForm.value.marriage,
  education: patientForm.value.education,
  provinceCity: patientForm.value.city,
  address: patientForm.value.address,
  job: patientForm.value.job,
  company: patientForm.value.company,
  remark: patientForm.value.remark,
});

const savePatient = async () => {
  syncPatientAgeFromBirthday();
  if (!patientForm.value.name || !patientForm.value.age || !patientForm.value.birthday) {
    ElMessage.warning("请填写患者姓名、年龄和出生日期");
    return;
  }
  if (Object.keys(fieldErrors.value).length) {
    ElMessage.warning(Object.values(fieldErrors.value)[0]);
    return;
  }
  const response: any = patientId.value
    ? await patientApi.update(patientId.value, patientPayload())
    : await patientApi.create(patientPayload());
  patientId.value = response?.data?.id || patientId.value;
  ElMessage.success("患者信息保存成功");
  if (!isEditPage.value) router.replace({ path: "/patients/edit", state: { id: String(patientId.value) } });
};

const openFamilyDialog = (mode: "create" | "view" | "edit", member?: any) => {
  familyDialogMode.value = mode;
  editingFamilyMemberId.value = member?.id || null;
  familyForm.value = member ? {
    relation: String(member.relation || ""),
    name: String(member.name || ""),
    age: String(member.age ?? ""),
    birthday: String(member.birthday || ""),
    gender: String(member.gender || "男"),
    phone: String(member.phone || ""),
    company: String(member.company || "无"),
  } : createEmptyFamilyForm();
  showFamilyDialog.value = true;
};

const closeFamilyDialog = () => {
  showFamilyDialog.value = false;
  editingFamilyMemberId.value = null;
  familyForm.value = createEmptyFamilyForm();
};

const saveFamilyMember = async () => {
  syncFamilyAgeFromBirthday();
  if (!familyForm.value.relation || !familyForm.value.name || !familyForm.value.age
    || !familyForm.value.birthday || !familyForm.value.gender) {
    ElMessage.warning("请完整填写家庭关系、姓名、年龄、出生日期和性别");
    return;
  }
  if (!patientId.value) await savePatient();
  if (!patientId.value) return;
  const payload = {
    patientId: patientId.value,
    relation: familyForm.value.relation,
    name: familyForm.value.name,
    age: Number(familyForm.value.age || 0),
    birthday: familyForm.value.birthday || null,
    gender: familyForm.value.gender,
    phone: familyForm.value.phone,
    company: familyForm.value.company || "无",
  };

  if (editingFamilyMemberId.value) {
    const response: any = await familyMemberApi.update(editingFamilyMemberId.value, payload);
    familyMembers.value = familyMembers.value.map((member: any) => (
      String(member.id) === String(editingFamilyMemberId.value) ? response?.data || { ...member, ...payload } : member
    ));
    ElMessage.success("家庭成员修改成功");
  } else {
    const response: any = await familyMemberApi.create(payload);
    familyMembers.value.unshift(response?.data || { id: Date.now(), ...payload });
    ElMessage.success("家庭成员添加成功");
  }
  closeFamilyDialog();
};

const removeFamilyMember = async (member: any) => {
  if (member.id) await familyMemberApi.remove(member.id);
  familyMembers.value = familyMembers.value.filter((item: any) => item !== member);
  ElMessage.success("已取消家庭成员关联");
};

const viewChargeOrder = (id: string | number) => {
  router.push(`/statistics/charge/detail/${id}`);
};
</script>

<style scoped>
.add-patient-page {
  min-height: 100%;
  padding: 24px 0 58px;
}

.patient-card {
  width: min(1598px, calc(100% - 96px));
  min-height: 1224px;
  margin: 0 auto;
  padding: 32px 31px 72px;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h2 {
  position: relative;
  margin: 0;
  padding-left: 16px;
  color: #111722;
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.page-header h2::before {
  position: absolute;
  left: 0;
  top: 0;
  width: 6px;
  height: 25px;
  background: #636be8;
  content: "";
}

.patient-tabs {
  display: inline-flex;
  height: 49px;
  overflow: hidden;
  border: 2px solid #c9c9c9;
  border-radius: 5px;
}

.patient-tab {
  min-width: 137px;
  height: 49px;
  padding: 0 28px;
  border: 0;
  border-right: 2px solid #c9c9c9;
  background: #fff;
  color: #c9c9c9;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.patient-tab:last-child {
  border-right: 0;
}

.patient-tab.active {
  background: #636be8;
  color: #fff;
}

.header-actions {
  display: inline-flex;
  gap: 20px;
}

.top-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-width: 143px;
  height: 46px;
  border-radius: 5px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.top-btn.primary {
  background: #636be8;
  color: #fff;
}

.btn-icon {
  width: 24px;
  height: 24px;
}

.section-title {
  margin: 36px 0 34px;
  color: #636be8;
  font-size: 31px;
  line-height: 1;
  font-weight: 800;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 27px 48px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #111;
  font-size: 16px;
}

.field i {
  color: #e11d2e;
  font-style: normal;
}

.control {
  position: relative;
  display: flex;
  height: 58px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  background: #fff;
  overflow: hidden;
}

.control.invalid-control {
  border-color: #e11d2e;
}

.field-error {
  min-height: 18px;
  margin: -4px 0 0;
  color: #e11d2e;
  font-size: 13px;
  line-height: 18px;
}

.control.active-control {
  z-index: 20;
  border-color: #c9c9c9;
  box-shadow: 0 0 0 2px rgba(17, 24, 39, 0.08);
  overflow: visible;
}

.control input,
.control select {
  width: 100%;
  height: 100%;
  padding: 0 18px;
  border: 0;
  outline: none;
  background: transparent;
  color: #111;
  font-size: 18px;
}

.control input[type="date"] {
  padding-right: 52px;
}

.control input[type="date"]::-webkit-calendar-picker-indicator {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.control input::placeholder {
  color: #c4c4c4;
}

.age-control input {
  border-right: 1px solid #e5e5e5;
}

.age-control select {
  width: 100px;
  flex: none;
}

.control-icon {
  position: absolute;
  right: 17px;
  top: 17px;
  width: 22px;
  height: 22px;
  color: #c7c7c7;
  pointer-events: none;
}

.address-trigger,
.custom-select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
  padding: 0 18px;
  border: 0;
  background: transparent;
  color: #111;
  font-size: 18px;
  text-align: left;
  cursor: pointer;
}

.nation-select-panel {
  position: absolute;
  left: -1px;
  right: -1px;
  top: calc(100% + 6px);
  z-index: 30;
  max-height: 320px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(21, 28, 54, 0.1);
  overflow-y: auto;
}

.nation-option {
  display: flex;
  align-items: center;
  width: 100%;
  height: 40px;
  padding: 0 18px;
  border: 0;
  background: #fff;
  color: #111;
  font-size: 16px;
  text-align: left;
  cursor: pointer;
}

.nation-option:hover,
.nation-option.active {
  background: #f5f6ff;
  color: #6068f1;
}

.select-arrow {
  width: 20px;
  height: 20px;
  color: #c9c9c9;
}

.address-panel {
  position: absolute;
  left: 4px;
  top: calc(100% + 21px);
  z-index: 30;
  display: grid;
  grid-template-columns: 180px 180px;
  min-height: 0;
  max-height: 258px;
  padding: 9px 0;
  border-radius: 8px 8px 0 0;
  background: #fff;
  box-shadow: 0 4px 16px rgba(21, 28, 54, 0.1);
}

.address-panel::before {
  position: absolute;
  left: 43px;
  top: -13px;
  width: 26px;
  height: 26px;
  background: #fff;
  box-shadow: -4px -4px 10px rgba(21, 28, 54, 0.04);
  transform: rotate(45deg);
  content: "";
}

.address-column {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  max-height: 240px;
  overflow-y: auto;
}

.province-column {
  border-right: 1px solid #f1f1f1;
}

.address-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40px;
  height: 40px;
  padding: 0 20px;
  border: 0;
  background: #fff;
  color: #111;
  font-size: 16px;
  text-align: left;
  cursor: pointer;
}

.address-option:hover,
.address-option.active {
  color: #6068f1;
}

.option-arrow {
  width: 18px;
  height: 18px;
  color: #e7e7e7;
}

.address-option.active .option-arrow,
.address-option:hover .option-arrow {
  color: #c9c9c9;
}

.wide {
  grid-column: span 2;
}

.family-section {
  margin-top: 45px;
}

.family-section .section-title {
  margin-bottom: 28px;
}

.family-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.family-table th {
  height: 72px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
}

.family-table td {
  height: 72px;
  border-bottom: 1px solid #eee;
  text-align: center;
}

.family-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.link-btn {
  border: 0;
  background: transparent;
  color: #6068f1;
  cursor: pointer;
}

.family-add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 194px;
  height: 58px;
  margin: 91px auto 0;
  border: 0;
  border-radius: 5px;
  background: #22ce91;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.42);
}

.family-dialog {
  position: relative;
  width: 846px;
  padding: 25px 36px 30px;
  border-radius: 5px;
  background: #fff;
}

.family-dialog h2 {
  margin: 0 0 30px;
  color: #111;
  font-size: 24px;
  font-weight: 800;
}

.dialog-close {
  position: absolute;
  top: 24px;
  right: 34px;
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  color: #cfcfcf;
  cursor: pointer;
}

.dialog-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 27px 50px;
}

.search-inside input {
  padding-right: 44px;
}

.inside-icon {
  position: absolute;
  right: 17px;
  top: 18px;
  width: 22px;
  height: 22px;
  color: #c7c7c7;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
}

.dialog-btn {
  min-width: 84px;
  height: 42px;
  border-radius: 5px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
  font-size: 16px;
  cursor: pointer;
}

.dialog-btn.confirm {
  background: #636be8;
  color: #fff;
}

.record-panel {
  display: grid;
  grid-template-columns: 520px minmax(0, 1fr);
  gap: 34px;
  margin-top: 62px;
}

.record-timeline {
  position: relative;
  padding-right: 24px;
}

.record-timeline::after {
  position: absolute;
  top: 0;
  right: 0;
  width: 2px;
  height: 100%;
  background: #e6e8ff;
  content: "";
}

.timeline-item {
  position: relative;
  margin-bottom: 30px;
  cursor: pointer;
}

.timeline-item::after {
  position: absolute;
  top: 92px;
  right: -32px;
  z-index: 2;
  width: 14px;
  height: 14px;
  border: 2px solid #c9ceff;
  border-radius: 50%;
  background: #fff;
  content: "";
}

.timeline-item.active::after {
  border-color: #636be8;
  background: #636be8;
  box-shadow: 0 0 0 5px rgba(99, 107, 232, 0.16);
}

.time-tag {
  width: 188px;
  height: 42px;
  margin-left: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #636be8;
  border-radius: 4px;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
}

.timeline-item.active .time-tag {
  background: #636be8;
  color: #fff;
}

.record-card {
  position: relative;
  margin-top: 18px;
  padding: 18px 32px;
  border: 1px solid #e2e5ff;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(99, 107, 232, 0.14);
}

.record-card::after {
  position: absolute;
  right: -11px;
  top: 40px;
  width: 20px;
  height: 20px;
  border-top: 1px solid #e2e5ff;
  border-right: 1px solid #e2e5ff;
  background: #fff;
  transform: rotate(45deg);
  content: "";
}

.record-card p {
  margin: 0;
  color: #111722;
  font-size: 18px;
  line-height: 1.9;
}

.record-card a,
.medical-text a,
.prescription-text a {
  color: #636be8;
  text-decoration: none;
}

.record-detail {
  min-height: 930px;
  padding: 25px 32px 45px;
  border: 1px solid #e2e5ff;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 14px rgba(99, 107, 232, 0.16);
}

.record-detail-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.visit-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 104px;
  height: 39px;
  border-radius: 22px;
  background: #25ce91;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
}

.outline-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-width: 140px;
  height: 46px;
  margin-left: 20px;
  border: 1px solid #636be8;
  border-radius: 4px;
  background: #fff;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.small-icon {
  width: 22px;
  height: 22px;
}

.record-detail h2,
.charge-record-panel h2 {
  margin: 0 0 22px;
  color: #111722;
  font-size: 24px;
  line-height: 1;
  font-weight: 800;
}

.vital-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 42px;
  margin-bottom: 18px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e4e6ff;
  color: #111722;
  font-size: 18px;
}

.medical-text,
.prescription-text {
  color: #111722;
  font-size: 18px;
  line-height: 1.55;
}

.medical-text p,
.prescription-text p {
  margin: 0 0 8px;
}

.advice-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  padding: 0 20px;
  border-radius: 4px;
  background: #636be8;
  color: #fff;
}

.record-divider {
  height: 2px;
  margin: 28px 0 32px;
  background: #e4e6ff;
}

.charge-record-panel {
  margin-top: 63px;
}

.charge-total {
  display: flex;
  align-items: center;
  height: 78px;
  padding: 0 23px;
  border-radius: 4px;
  background: #eceeff;
  color: #111722;
  font-size: 24px;
  font-weight: 800;
}

.charge-total strong {
  margin-left: 8px;
  color: #f00;
  font-size: 26px;
}

.charge-record-panel h2 {
  margin-top: 33px;
}

.charge-record-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.charge-record-table th,
.charge-record-table td {
  height: 51px;
  border: 1px solid #e2e5ff;
  text-align: center;
}

.charge-record-table th {
  background: #d7d9f7;
  color: #111722;
  font-size: 18px;
  font-weight: 700;
}

.charge-record-table td {
  color: #111722;
  font-size: 17px;
}

.charge-record-table button {
  border: 0;
  background: transparent;
  color: #636be8;
  font-size: 17px;
  cursor: pointer;
}

.pending {
  color: #ffb800 !important;
}

.paid {
  color: #23c783 !important;
}

.refunded,
.refund {
  color: #f00 !important;
}

@media (max-width: 1280px) {
  .patient-card {
    width: calc(100% - 32px);
  }

  .form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .record-panel {
    grid-template-columns: 1fr;
  }

  .record-timeline {
    padding-right: 0;
  }

  .record-timeline::after,
  .timeline-item::after {
    display: none;
  }

  .vital-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
