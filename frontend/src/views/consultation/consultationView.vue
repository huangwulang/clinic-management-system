<template>
  <div class="consultation-page">
    <section class="consultation-card patient-card">
      <div class="visit-tabs" role="tablist">
        <button
          type="button"
          :class="{ active: activeTab === 'prescription' }"
          @click="activeTab = 'prescription'"
        >
          处方
        </button>
        <button
          type="button"
          :class="{ active: activeTab === 'medical' }"
          @click="activeTab = 'medical'"
        >
          病历
        </button>
      </div>

      <div class="patient-grid">
        <FormField label="患者姓名" required>
          <el-autocomplete
            v-model="patient.name"
            :fetch-suggestions="queryPatients"
            value-key="name"
            placeholder="患者姓名/手机号码/证件号码/卡号"
            @select="selectPatient"
          />
        </FormField>
        <FormField label="患者卡号">
          <el-input v-model="patient.cardNo" placeholder="请输入病人卡号" />
        </FormField>
        <FormField label="患者年龄" required>
          <el-input v-model="patient.age" placeholder="请输入数字">
            <template #append>岁</template>
          </el-input>
        </FormField>
        <FormField label="出生日期" required>
          <el-date-picker
            v-model="patient.birthday"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder=""
          />
        </FormField>

        <FormField label="性别" required>
          <el-select v-model="patient.gender">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </FormField>
        <FormField label="手机号码">
          <el-input v-model="patient.phone" placeholder="请输入手机号码" />
        </FormField>
        <FormField label="证件号码">
          <el-input v-model="patient.idNo" placeholder="请输入证件号码" />
        </FormField>
        <FormField label="接诊类型" required>
          <el-select v-model="patient.visitType" placeholder="请选择">
            <el-option label="初诊" value="初诊" />
            <el-option label="复诊" value="复诊" />
          </el-select>
        </FormField>

        <FormField label="地址">
          <el-cascader
            v-model="patient.region"
            :options="regionOptions"
            :show-all-levels="true"
            popper-class="address-cascader-popper"
          />
        </FormField>
        <FormField class="address-detail" label=" ">
          <el-input v-model="patient.address" placeholder="请输入详细地址" />
        </FormField>

        <FormField class="wide-field" label="诊断" required>
          <el-autocomplete v-model="patient.diagnosis" :fetch-suggestions="queryDiagnosis" value-key="value" />
        </FormField>
        <FormField class="wide-field" label="医嘱">
          <el-autocomplete v-model="patient.advice" :fetch-suggestions="queryAdvice" value-key="value" />
        </FormField>
      </div>
    </section>

    <section v-if="activeTab === 'prescription'" class="consultation-card prescription-card">
      <div class="prescription-tabs">
        <el-dropdown trigger="click" @command="addPrescription">
          <button class="outline-button" type="button">
            添加处方
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="西/成药处方">西/成药处方</el-dropdown-item>
              <el-dropdown-item command="中药处方">中药处方</el-dropdown-item>
              <el-dropdown-item command="检查项目">检查项目</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <button class="prescription-tag" type="button">
          {{ prescriptionName }} 1
          <el-icon><Close /></el-icon>
        </button>
      </div>

      <div class="prescription-workspace">
        <div class="rx-panel">
          <header class="rx-header">
            <strong>Rp</strong>
            <button type="button" class="text-action" @click="batchVisible = true">
              <el-icon><Setting /></el-icon>
              批量设置
            </button>
          </header>

          <div class="rx-table-wrap">
            <el-table
              :data="prescriptionItems"
              height="100%"
              empty-text=" "
              @selection-change="selectedPrescriptionItems = $event"
            >
              <el-table-column type="selection" width="38" />
              <el-table-column type="index" label="序号" width="42" />
              <el-table-column label="组号" width="66">
                <template #default="{ row }">
                  <el-select v-model="row.group" class="rx-group-select">
                    <el-option v-for="group in groupOptions" :key="group" :label="group" :value="group" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="名称" min-width="108">
                <template #default="{ row }">
                  <span class="rx-drug-name">{{ row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="单次用量" width="78">
                <template #default="{ row }">
                  <div class="rx-inline-field">
                    <el-input v-model="row.dosage" />
                    <span>{{ row.dosageUnit }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="用法" width="106">
                <template #default="{ row }">
                  <el-select v-model="row.usage" class="rx-cell-select">
                    <el-option v-for="item in usageOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="频度" width="106">
                <template #default="{ row }">
                  <el-select v-model="row.frequency" class="rx-cell-select">
                    <el-option v-for="item in frequencyOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="天数" width="62">
                <template #default="{ row }">
                  <el-input v-model.number="row.days" class="rx-small-input" />
                </template>
              </el-table-column>
              <el-table-column label="总量" width="108">
                <template #default="{ row }">
                  <div class="rx-quantity-field">
                    <el-input v-model="row.quantity" />
                    <el-select v-model="row.quantityUnit">
                      <el-option v-for="unit in unitOptions" :key="unit" :label="unit" :value="unit" />
                    </el-select>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="64">
                <template #default="{ row }">
                  <el-input v-model.number="row.price" class="rx-price-input" />
                </template>
              </el-table-column>
              <el-table-column label="编辑" width="44">
                <template #default="{ row }">
                  <el-button link type="danger" class="rx-delete-button" @click="removePrescriptionItem(row)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <footer class="rx-footer">
            <el-button class="fee-button" type="success" @click="feeDialogVisible = true">
              <el-icon><CirclePlusFilled /></el-icon>
              附加费用
            </el-button>
            <span>科室<i>*</i></span>
            <el-select v-model="prescription.department" class="footer-select">
              <el-option v-for="item in departments" :key="item.id || item.name" :label="item.name" :value="item.name" />
            </el-select>
            <span>接诊医生<i>*</i></span>
            <el-select v-model="prescription.doctor" class="footer-select doctor-select">
              <el-option v-for="item in filteredDoctors" :key="item.id || item.name" :label="item.name" :value="item.name" />
            </el-select>
            <p>
              此方合计：<b>{{ prescriptionTotal }}</b> 元；
              共 <b>{{ prescriptionItems.length }}</b> 个处方，附加费用 <b>{{ feeTotal }}</b> 元
            </p>
          </footer>
        </div>

        <aside class="drug-panel">
          <div class="drug-filter">
            <span>药品分类</span>
            <el-select v-model="drugCategory">
              <el-option label="全部分类" value="全部分类" />
              <el-option v-for="category in drugCategoryOptions" :key="category" :label="category" :value="category" />
            </el-select>
            <el-input v-model="drugKeyword" placeholder="输入药品中文或拼音名称">
              <template #suffix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>

          <el-table
            ref="drugTableRef"
            class="drug-table"
            :data="filteredDrugs"
            height="438"
            @selection-change="selectedDrugs = $event"
          >
            <el-table-column type="selection" width="38" />
            <el-table-column prop="name" label="名称" min-width="150" />
            <el-table-column prop="spec" label="规格" width="78" />
            <el-table-column prop="stockText" label="库存" width="82" />
            <el-table-column prop="price" label="价格" width="58" />
          </el-table>
          <el-button type="primary" class="add-drug-button" @click="addSelectedDrugs">
            添加药品
          </el-button>
        </aside>
      </div>
    </section>

    <section v-else class="consultation-card medical-card">
      <h2>体格信息</h2>
      <div class="vitals-grid">
        <label v-for="item in vitals" :key="item.key" class="vital-field">
          <span>{{ item.label }}</span>
          <el-input v-model="medical[item.key]" />
          <b>{{ item.unit }}</b>
        </label>
      </div>

      <h2 class="medical-heading">
        病历信息
        <small>（病历信息可在系统设置-基础设置中的自定义设置）</small>
      </h2>
      <div class="medical-grid">
        <FormField class="wide-field" label="发病日期">
          <el-date-picker
            v-model="medical.onsetDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder=""
          />
        </FormField>
        <FormField class="wide-field" label="主诉" required>
          <el-input v-model="medical.chiefComplaint">
            <template #suffix><el-icon><Search /></el-icon></template>
          </el-input>
        </FormField>
        <FormField
          v-for="field in medicalFields"
          :key="field.key"
          class="wide-field"
          :label="field.label"
        >
          <el-input
            v-model="medical[field.key]"
            type="textarea"
            :rows="3"
            resize="none"
          >
            <template v-if="field.search" #suffix><el-icon><Search /></el-icon></template>
          </el-input>
        </FormField>
      </div>
    </section>

    <footer class="consultation-actions">
      <div class="action-inner">
        <div class="action-links">
          <button type="button" @click="openTemplateDialog">
            <el-icon><Tickets /></el-icon>
            {{ activeTab === "prescription" ? "处方调用" : "病历调用" }}
          </button>
          <button type="button" @click="notify(activeTab === 'prescription' ? '正在打印处方' : '正在打印病历')">
            <el-icon><Printer /></el-icon>
            {{ activeTab === "prescription" ? "打印处方" : "打印病历" }}
          </button>
          <button type="button" @click="saveAsTemplate">
            <el-icon><Collection /></el-icon>
            存为模板
          </button>
        </div>
        <div class="primary-actions">
          <el-button type="primary" @click="saveConsultation">
            <el-icon><FolderChecked /></el-icon>
            保存
          </el-button>
          <el-button v-if="activeTab === 'prescription'" class="warning-button" @click="createChargeOrder">
            <span class="money-icon">￥</span>
            收费
          </el-button>
          <el-button class="warning-button" @click="finishConsultation">
            <el-icon><SwitchButton /></el-icon>
            结束接诊
          </el-button>
        </div>
      </div>
    </footer>

    <el-dialog v-model="batchVisible" title="批量设置" width="420px">
      <el-form label-width="90px">
        <el-form-item label="用法">
          <el-select v-model="batchForm.usage"><el-option label="口服" value="口服" /></el-select>
        </el-form-item>
        <el-form-item label="频度">
          <el-select v-model="batchForm.frequency"><el-option label="每日三次" value="每日三次" /></el-select>
        </el-form-item>
        <el-form-item label="天数"><el-input-number v-model="batchForm.days" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" @click="applyBatchSetting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="feeDialogVisible" title="附加费用" width="680px">
      <el-table :data="feeItems" @selection-change="selectedFeeItems = $event">
        <el-table-column type="selection" width="52" />
        <el-table-column prop="name" label="费用名称" />
        <el-table-column prop="category" label="费用分类" />
        <el-table-column prop="price" label="单价（元）" />
        <el-table-column prop="unit" label="单位" />
      </el-table>
      <template #footer>
        <el-button @click="feeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmFees">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="templateDialogVisible" :title="activeTab === 'prescription' ? '处方调用' : '病历调用'" width="720px">
      <el-table :data="templateRows" highlight-current-row @current-change="selectedTemplate = $event">
        <el-table-column prop="name" label="模板名称" />
        <el-table-column prop="departmentName" label="科室" />
        <el-table-column prop="creator" label="创建人" />
        <el-table-column prop="updatedAt" label="更新时间" />
      </el-table>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedTemplate" @click="applyTemplate">调用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  ArrowDown,
  CirclePlusFilled,
  Close,
  Collection,
  Delete,
  FolderChecked,
  Printer,
  Search,
  Setting,
  SwitchButton,
  Tickets,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, defineComponent, h, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  departmentApi,
  drugApi,
  feeItemApi,
  medicalTemplateApi,
  patientApi,
  prescriptionTemplateApi,
  registrationApi,
  staffApi,
  visitApi,
} from "@/api";
import { chinaCascaderOptions } from "@/utils/chinaArea";
import { readNavigationState } from "@/utils/navigation";

type TabName = "prescription" | "medical";
type VitalKey = "temperature" | "breathing" | "pulse" | "bloodPressure" | "height" | "weight" | "bloodSugar" | "bloodFat";
type MedicalTextKey = "presentHistory" | "pastHistory" | "allergyHistory" | "personalHistory" | "familyHistory" | "auxiliaryExam" | "treatment" | "remark";

interface Drug {
  id: number;
  category: string;
  name: string;
  spec: string;
  stock: number;
  stockText: string;
  price: number;
  unit: string;
}

interface PrescriptionItem extends Drug {
  group: number;
  dosage: string;
  dosageUnit: string;
  usage: string;
  frequency: string;
  days: number;
  quantity: string;
  quantityUnit: string;
}

const FormField = defineComponent({
  name: "FormField",
  props: {
    label: { type: String, required: true },
    required: { type: Boolean, default: false },
  },
  setup(props, { slots, attrs }) {
    return () => h("div", { ...attrs, class: ["form-field", attrs.class] }, [
      h("span", { class: "field-label" }, [
        props.label,
        props.required ? h("i", "*") : null,
      ]),
      h("div", { class: "field-control" }, slots.default?.()),
    ]);
  },
});

const activeTab = ref<TabName>(
  new URLSearchParams(window.location.search).get("tab") === "medical"
    ? "medical"
    : "prescription",
);
const prescriptionName = ref("西/成药处方");
const batchVisible = ref(false);
const drugCategory = ref("全部分类");
const drugKeyword = ref("");
const selectedDrugs = ref<Drug[]>([]);
const prescriptionItems = ref<PrescriptionItem[]>([]);
const selectedPrescriptionItems = ref<PrescriptionItem[]>([]);
const drugTableRef = ref();
const patientOptions = ref<any[]>([]);
const departments = ref<any[]>([]);
const staffRows = ref<any[]>([]);
const feeItems = ref<any[]>([]);
const selectedFeeItems = ref<any[]>([]);
const appliedFeeItems = ref<any[]>([]);
const feeDialogVisible = ref(false);
const templateDialogVisible = ref(false);
const templateRows = ref<any[]>([]);
const selectedTemplate = ref<any>(null);
const selectedPatientId = ref<string | number | null>(null);

const patient = reactive({
  name: "",
  cardNo: "",
  age: "",
  birthday: "",
  gender: "男",
  phone: "",
  idNo: "",
  visitType: "",
  region: ["广东省", "深圳市"],
  address: "",
  diagnosis: "",
  advice: "",
});

const prescription = reactive({ department: "全科", doctor: "王阳明" });
const batchForm = reactive({ usage: "口服", frequency: "每日三次", days: 3 });

const medical = reactive<Record<VitalKey | MedicalTextKey | "onsetDate" | "chiefComplaint", string>>({
  temperature: "",
  breathing: "",
  pulse: "",
  bloodPressure: "/",
  height: "",
  weight: "",
  bloodSugar: "",
  bloodFat: "",
  onsetDate: "",
  chiefComplaint: "",
  presentHistory: "",
  pastHistory: "",
  allergyHistory: "",
  personalHistory: "",
  familyHistory: "",
  auxiliaryExam: "",
  treatment: "",
  remark: "",
});

const regionOptions = chinaCascaderOptions;

const drugs = ref<Drug[]>([
  { id: 1, category: "西药", name: "止咳露瓶贴", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.08, unit: "盒" },
  { id: 2, category: "西药", name: "止咳露说明书", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.16, unit: "盒" },
  { id: 3, category: "中成药", name: "减肥药复合袋", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.15, unit: "盒" },
  { id: 4, category: "西药", name: "雷丁、舒必利瓶子", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.15, unit: "盒" },
  { id: 5, category: "西药", name: "雷丁、舒必利盖子", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.18, unit: "盒" },
  { id: 6, category: "西药", name: "盐酸雷尼替丁胶囊小盒", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.35, unit: "盒" },
  { id: 7, category: "中成药", name: "牡蛎碳酸钙颗粒中盒", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.08, unit: "盒" },
  { id: 8, category: "中成药", name: "牡蛎碳酸钙颗粒盒贴", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.16, unit: "盒" },
  { id: 9, category: "中成药", name: "牡蛎碳酸钙颗粒复合膜", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.15, unit: "盒" },
  { id: 10, category: "中成药", name: "牡蛎碳酸钙颗粒纸箱", spec: "1g*1", stock: 1000, stockText: "1000盒", price: 0.15, unit: "盒" },
  { id: 11, category: "西药", name: "阿莫西林胶囊", spec: "0.25g*24", stock: 368, stockText: "368盒", price: 12.8, unit: "盒" },
  { id: 12, category: "中成药", name: "感冒清热颗粒", spec: "12g*10", stock: 526, stockText: "526盒", price: 18.6, unit: "盒" },
]);

const vitals: Array<{ key: VitalKey; label: string; unit: string }> = [
  { key: "temperature", label: "体温", unit: "℃" },
  { key: "breathing", label: "呼吸", unit: "次/分" },
  { key: "pulse", label: "脉搏", unit: "次/分" },
  { key: "bloodPressure", label: "血压", unit: "mmHg" },
  { key: "height", label: "身高", unit: "cm" },
  { key: "weight", label: "体重", unit: "Kg" },
  { key: "bloodSugar", label: "血糖", unit: "mmol/l" },
  { key: "bloodFat", label: "血脂", unit: "mmol/l" },
];

const medicalFields: Array<{ key: MedicalTextKey; label: string; search: boolean }> = [
  { key: "presentHistory", label: "现病史", search: true },
  { key: "pastHistory", label: "既往史", search: true },
  { key: "allergyHistory", label: "过敏史", search: true },
  { key: "personalHistory", label: "个人史", search: true },
  { key: "familyHistory", label: "家族史", search: true },
  { key: "auxiliaryExam", label: "辅助检查", search: true },
  { key: "treatment", label: "治疗意见", search: true },
  { key: "remark", label: "备注", search: false },
];

const groupOptions = Array.from({ length: 10 }, (_, index) => index + 1);
const usageOptions = ["口服", "外用", "静滴", "肌注", "皮下注射", "雾化吸入"];
const frequencyOptions = ["一天1次", "一天2次", "一天3次", "每日三次", "必要时", "睡前"];
const unitOptions = computed(() => {
  const units = drugs.value.map((drug) => drug.unit).filter(Boolean);
  return Array.from(new Set(["盒", "瓶", "片", "袋", "支", ...units]));
});
const drugCategoryOptions = computed(() => Array.from(
  new Set(drugs.value.map((drug) => drug.category).filter(Boolean)),
));
const filteredDrugs = computed(() => drugs.value.filter((drug) => {
  const categoryMatched = drugCategory.value === "全部分类" || drug.category === drugCategory.value;
  const typeCategories = prescriptionName.value === "中药处方"
    ? ["中药", "中草药"]
    : prescriptionName.value === "检查项目"
      ? []
    : ["西药", "中成药"];
  const typeMatched = drugCategory.value === "全部分类" || typeCategories.includes(drug.category);
  const keywordMatched = !drugKeyword.value || drug.name.toLowerCase().includes(drugKeyword.value.trim().toLowerCase());
  return typeMatched && categoryMatched && keywordMatched;
}));

const drugTotal = computed(() => prescriptionItems.value.reduce(
  (total, item) => total + item.price * Math.max(1, Number.parseFloat(item.quantity) || 1),
  0,
));
const feeTotal = computed(() => appliedFeeItems.value.reduce(
  (total, item) => total + Number(item.price || 0),
  0,
).toFixed(2));
const prescriptionTotal = computed(() => (drugTotal.value + Number(feeTotal.value)).toFixed(2));
const filteredDoctors = computed(() => staffRows.value.filter((item) => {
  const enabled = item.enabled !== false && Number(item.enabled) !== 0;
  const doctor = !item.roleName || item.roleName.includes("医生") || item.positionName?.includes("医生");
  return enabled && doctor && (!prescription.department || item.departmentName === prescription.department);
}));

const notify = (message: string) => ElMessage.success(message);

const addPrescription = (command: string) => {
  prescriptionName.value = command;
  drugCategory.value = "全部分类";
  selectedDrugs.value = [];
  drugTableRef.value?.clearSelection();
  notify(`已添加${command}`);
};

watch(() => prescription.department, () => {
  if (!filteredDoctors.value.some((item) => item.name === prescription.doctor)) {
    prescription.doctor = filteredDoctors.value[0]?.name || "";
  }
});

const addSelectedDrugs = () => {
  if (!selectedDrugs.value.length) {
    ElMessage.warning("请先选择药品");
    return;
  }
  const existingIds = new Set(prescriptionItems.value.map((item) => item.id));
  const additions = selectedDrugs.value
    .filter((drug) => !existingIds.has(drug.id))
    .map((drug, index) => ({
      ...drug,
      group: prescriptionItems.value.length + index + 1,
      dosage: "1",
      dosageUnit: drug.unit || "片",
      usage: "口服",
      frequency: "一天1次",
      days: 1,
      quantity: "1",
      quantityUnit: drug.unit || "盒",
    }));
  prescriptionItems.value.push(...additions);
  drugTableRef.value?.clearSelection();
  notify(`已添加 ${additions.length} 种药品`);
};

const removePrescriptionItem = (row: PrescriptionItem) => {
  prescriptionItems.value = prescriptionItems.value.filter((item) => item.id !== row.id);
};

const applyBatchSetting = () => {
  const targets = selectedPrescriptionItems.value.length
    ? selectedPrescriptionItems.value
    : prescriptionItems.value;
  targets.forEach((item) => {
    item.usage = batchForm.usage;
    item.frequency = batchForm.frequency;
    item.days = batchForm.days;
  });
  batchVisible.value = false;
  notify("批量设置已应用");
};

const queryPatients = (keyword: string, callback: (items: any[]) => void) => {
  const value = keyword.trim().toLowerCase();
  callback(patientOptions.value.filter((item) => !value
    || [item.name, item.phone, item.idCard, item.cardNo].some((field) => String(field || "").toLowerCase().includes(value))));
};

const selectPatient = (item: any) => {
  selectedPatientId.value = item.id;
  Object.assign(patient, {
    name: item.name || "",
    cardNo: item.cardNo || "",
    age: String(item.age || ""),
    birthday: item.birthday || item.birthDate || "",
    gender: item.gender || "男",
    phone: item.phone || "",
    idNo: item.idCard || "",
    region: parseRegion(item.provinceCity).length ? parseRegion(item.provinceCity) : patient.region,
    address: item.address || "",
  });
};

const diagnosisOptions = ["上呼吸道感染", "急性支气管炎", "胃炎", "高血压", "糖尿病"];
const adviceOptions = ["按时服药，注意休息", "清淡饮食，多饮水", "三日后复诊", "如症状加重及时就医"];
const queryTextOptions = (source: string[], keyword: string, callback: (items: any[]) => void) => {
  callback(source.filter((item) => !keyword || item.includes(keyword)).map((value) => ({ value })));
};
const queryDiagnosis = (keyword: string, callback: (items: any[]) => void) => queryTextOptions(diagnosisOptions, keyword, callback);
const queryAdvice = (keyword: string, callback: (items: any[]) => void) => queryTextOptions(adviceOptions, keyword, callback);

const confirmFees = () => {
  appliedFeeItems.value = [...selectedFeeItems.value];
  feeDialogVisible.value = false;
  notify(`已选择 ${appliedFeeItems.value.length} 项附加费用`);
};

const openTemplateDialog = async () => {
  const response: any = activeTab.value === "prescription"
    ? await prescriptionTemplateApi.list()
    : await medicalTemplateApi.page({ page: 1, size: 100 });
  templateRows.value = activeTab.value === "prescription"
    ? (response.data || [])
    : (response.data?.records || []).filter((item: any) => item.templateType !== "PRESCRIPTION");
  selectedTemplate.value = null;
  templateDialogVisible.value = true;
};

const applyTemplate = () => {
  if (!selectedTemplate.value) return;
  try {
    const content = JSON.parse(selectedTemplate.value.content || "{}");
    if (activeTab.value === "prescription") {
      prescriptionName.value = content.name || prescriptionName.value;
      prescription.department = content.department || prescription.department;
      prescription.doctor = content.doctor || prescription.doctor;
      prescriptionItems.value = Array.isArray(content.items) ? content.items : [];
      appliedFeeItems.value = Array.isArray(content.fees) ? content.fees : [];
    } else {
      Object.assign(medical, content.medical || content);
      patient.diagnosis = content.diagnosis || patient.diagnosis;
      patient.advice = content.advice || patient.advice;
    }
    templateDialogVisible.value = false;
    notify("模板调用成功");
  } catch {
    ElMessage.error("模板内容格式不正确");
  }
};

const saveAsTemplate = async () => {
  const { value: name } = await ElMessageBox.prompt("请输入模板名称", "存为模板", {
    inputPattern: /\S+/,
    inputErrorMessage: "模板名称不能为空",
  });
  if (activeTab.value === "prescription") {
    await prescriptionTemplateApi.create({
      templateCode: `RX${Date.now()}`,
      name,
      departmentName: prescription.department,
      content: JSON.stringify({
        name: prescriptionName.value,
        department: prescription.department,
        doctor: prescription.doctor,
        items: prescriptionItems.value,
        fees: appliedFeeItems.value,
      }),
      status: "ENABLED",
    });
  } else {
    await medicalTemplateApi.create({
      templateCode: `MR${Date.now()}`,
      templateType: "MEDICAL",
      name,
      departmentName: prescription.department,
      content: JSON.stringify({
        medical: { ...medical },
        diagnosis: patient.diagnosis,
        advice: patient.advice,
      }),
      status: "ENABLED",
    });
  }
  notify("模板保存成功");
};

const validatePatient = () => {
  if (!patient.name || !patient.age || !patient.birthday || !patient.visitType || !patient.diagnosis) {
    ElMessage.warning("请完整填写患者姓名、年龄、出生日期、接诊类型和诊断");
    return false;
  }
  return true;
};

const parseContent = (value: any): Record<string, any> | null => {
  if (!value) return null;
  if (typeof value === "object") return value;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
};

const parseRegion = (value: any) => {
  if (Array.isArray(value)) return value.filter(Boolean);
  if (!value) return [];
  return String(value).split(/[,\s/]+/).filter(Boolean);
};

const saveConsultation = async (options: { silent?: boolean } = {}) => {
  if (!validatePatient()) return false;
  if (!visitId.value) {
    const created: any = await visitApi.create({
      registrationId: registrationId ? Number(registrationId) : null,
      patientId: selectedPatientId.value ? Number(selectedPatientId.value) : null,
      patientName: patient.name,
      doctorName: prescription.doctor,
      departmentName: prescription.department,
      visitType: patient.visitType,
      chiefComplaint: medical.chiefComplaint,
      diagnosis: patient.diagnosis,
      doctorAdvice: patient.advice,
      status: "IN_PROGRESS",
    });
    visitId.value = created?.data?.id;
  }
  await visitApi.updatePatient(visitId.value, {
    patientId: selectedPatientId.value ? Number(selectedPatientId.value) : null,
    patientName: patient.name,
    diagnosis: patient.diagnosis,
    doctorAdvice: patient.advice,
  });
  await visitApi.saveMedicalRecord(visitId.value, {
    diagnosis: patient.diagnosis,
    doctorAdvice: patient.advice,
    vitalSign: Object.fromEntries(vitals.map((item) => [item.key, medical[item.key]])),
    medicalRecord: { ...medical },
  });
  await visitApi.savePrescription(visitId.value, {
    prescription: {
      name: prescriptionName.value,
      department: prescription.department,
      doctor: prescription.doctor,
      patient: {
        code: selectedPatientId.value ? String(selectedPatientId.value) : "",
        name: patient.name,
        age: patient.age,
        birthday: patient.birthday,
        gender: patient.gender,
        phone: patient.phone,
        idNo: patient.idNo,
        visitType: patient.visitType,
        cardNo: patient.cardNo,
        address: patient.address,
        region: patient.region,
        level: "普通",
        balance: "0.00",
        points: "0",
      },
      items: prescriptionItems.value,
      fees: appliedFeeItems.value,
    },
  });
  if (!options.silent) {
    await ElMessageBox.alert("保存成功", "提示", {
      type: "success",
      confirmButtonText: "确定",
    });
  }
  return true;
};

const finishConsultation = async () => {
  if (!validatePatient()) return;
  await ElMessageBox.confirm("确定结束本次接诊吗？结束后将生成接诊记录。", "结束接诊", {
    type: "warning",
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  });
  const saved = await saveConsultation({ silent: true });
  if (!saved) return;
  await visitApi.finish(visitId.value);
  notify("本次接诊已结束");
  router.push("/workbench");
};


const route = useRoute();
const router = useRouter();
const registrationId = readNavigationState(route, "registrationId");
const sourcePatientId = readNavigationState(route, "patientId") || readNavigationState(route);
const visitId = ref<string | number | null>(readNavigationState(route, "visitId") || null);

const isEnabledFeeItem = (item: any) => {
  const status = String(item.status ?? "").toUpperCase();
  return status !== "DISABLED" && status !== "0" && status !== "FALSE" && item.status !== false;
};

const isAddonFeeItem = (item: any) => {
  const category = String(item.category || "");
  return category !== "挂号费" && category !== "诊疗费";
};

onMounted(async () => {
  const tasks: Promise<any>[] = [
    drugApi.saleable(),
    patientApi.page({ page: 1, size: 200 }),
    departmentApi.page({ page: 1, size: 100 }),
    staffApi.page({ page: 1, size: 200 }),
    feeItemApi.page({ page: 1, size: 100 }),
  ];
  const isVisitSource = Boolean(visitId.value);
  if (visitId.value) tasks.push(visitApi.get(visitId.value));
  else if (registrationId) tasks.push(registrationApi.get(registrationId));
  else if (sourcePatientId) tasks.push(patientApi.get(sourcePatientId));
  const [drugResponse, patientResponse, departmentResponse, staffResponse, feeResponse, sourceResponse]: any[] = await Promise.all(tasks);
  const drugRecords = drugResponse?.data?.records || drugResponse?.data || [];
  drugs.value = drugRecords.map((item: any) => ({
    id: Number(item.id),
    category: item.category || "",
    name: item.name || item.drugName || "",
    spec: item.specification || item.spec || "",
    stock: Number(item.stockQuantity || item.stock || item.quantity || 0),
    stockText: item.stockText || `${Number(item.stockQuantity || item.stock || item.quantity || 0)}${item.unit || ""}`,
    price: Number(item.sellPrice || item.salePrice || item.retailPrice || 0),
    unit: item.unit || item.totalUnit || "盒",
  }));
  patientOptions.value = patientResponse?.data?.records || [];
  departments.value = (departmentResponse?.data?.records || []).filter(
    (item: any) => item.enabled !== false && Number(item.enabled) !== 0,
  );
  staffRows.value = staffResponse?.data?.records || [];
  feeItems.value = (feeResponse?.data?.records || []).filter((item: any) => isEnabledFeeItem(item) && isAddonFeeItem(item));
  const item = sourceResponse?.data || {};
  const resolvedPatientId = item.patientId || sourcePatientId || (!isVisitSource && item.id ? item.id : null);
  if (resolvedPatientId) {
    selectedPatientId.value = resolvedPatientId;
    const visitItem = { ...item };
    const detailResponse: any = await patientApi.get(resolvedPatientId);
    Object.assign(item, detailResponse?.data || {}, visitItem);
  } else if (item.id) {
    selectedPatientId.value = item.id;
  }
  Object.assign(patient, {
    name: item.patientName || item.name || patient.name,
    age: String(item.age || patient.age),
    birthday: item.birthday || item.birthDate || patient.birthday,
    gender: item.gender || patient.gender,
    phone: item.phone || patient.phone,
    idNo: item.idCard || patient.idNo,
    visitType: item.visitType || patient.visitType,
    cardNo: item.cardNo || patient.cardNo,
    address: item.address || patient.address,
    region: parseRegion(item.provinceCity).length ? parseRegion(item.provinceCity) : patient.region,
  });
  const defaultDepartment = departments.value.find((department: any) => staffRows.value.some((staff: any) => {
    const enabled = staff.enabled !== false && Number(staff.enabled) !== 0;
    const doctor = !staff.roleName || staff.roleName.includes("医生") || staff.positionName?.includes("医生");
    return enabled && doctor && staff.departmentName === department.name;
  }));
  prescription.department = item.departmentName || defaultDepartment?.name || departments.value[0]?.name || "";
  prescription.doctor = item.doctorName || filteredDoctors.value[0]?.name || "";
  if (isVisitSource) {
    patient.diagnosis = item.diagnosis || patient.diagnosis;
    patient.advice = item.doctorAdvice || patient.advice;
    Object.assign(medical, parseContent(item.medicalRecord) || {});
    Object.assign(medical, parseContent(item.vitalSigns) || {});
    const savedPrescription = parseContent(item.prescription);
    if (savedPrescription) {
      prescriptionName.value = savedPrescription.name || prescriptionName.value;
      prescription.department = savedPrescription.department || prescription.department;
      prescription.doctor = savedPrescription.doctor || prescription.doctor;
      const savedPatient = savedPrescription.patient || {};
      const savedRegion = parseRegion(savedPatient.region || savedPatient.provinceCity);
      Object.assign(patient, {
        name: patient.name || savedPatient.name || "",
        age: patient.age || String(savedPatient.age || ""),
        birthday: patient.birthday || savedPatient.birthday || savedPatient.birthDate || "",
        gender: patient.gender || savedPatient.gender || "",
        phone: patient.phone || savedPatient.phone || "",
        idNo: patient.idNo || savedPatient.idNo || savedPatient.idCard || "",
        visitType: patient.visitType || savedPatient.visitType || "",
        cardNo: patient.cardNo || savedPatient.cardNo || "",
        address: patient.address || savedPatient.address || "",
      });
      if (savedRegion.length && !item.provinceCity) {
        patient.region = savedRegion;
      }
      prescriptionItems.value = Array.isArray(savedPrescription.items) ? savedPrescription.items : [];
      appliedFeeItems.value = Array.isArray(savedPrescription.fees) ? savedPrescription.fees : [];
    }
  }
});

const createChargeOrder = async () => {
  const saved = await saveConsultation({ silent: true });
  if (!saved) return;
  const response: any = await visitApi.createChargeOrder(visitId.value);
  router.push({
    path: "/charge/payment",
    state: {
      id: String(response?.data?.id || ""),
      returnPath: "/consultation/new",
      visitId: String(visitId.value || ""),
      registrationId: String(registrationId || ""),
      patientId: String(selectedPatientId.value || sourcePatientId || ""),
    },
  });
};
</script>

<style scoped>
.consultation-page {
  --primary: #6268e8;
  --warning: #ffc01c;
  --border: #d3d3d3;
  min-height: calc(100vh - 126px);
  padding: 0 0 88px;
  color: #111827;
  font-size: 14px;
}

.consultation-card {
  width: min(100%, 1340px);
  margin: 0 auto;
  box-sizing: border-box;
  border: 1px solid #e3e3e3;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 2px 8px rgb(15 23 42 / 8%);
}

.patient-card {
  padding: 24px 26px 22px;
}

.visit-tabs {
  display: flex;
  width: 204px;
  height: 38px;
  margin-bottom: 24px;
}

.visit-tabs button {
  width: 50%;
  border: 1px solid #c9c9c9;
  background: #fff;
  color: #c0c0c0;
  font-weight: 700;
  cursor: pointer;
}

.visit-tabs button:first-child { border-radius: 3px 0 0 3px; }
.visit-tabs button:last-child { border-radius: 0 3px 3px 0; }
.visit-tabs button.active { border-color: var(--primary); background: var(--primary); color: #fff; }

.patient-grid,
.medical-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px 36px;
}

.form-field {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  min-height: 17px;
  line-height: 17px;
}

.field-label i,
.rx-footer i {
  color: #f00;
  font-style: normal;
}

.field-control,
.field-control :deep(.el-select),
.field-control :deep(.el-cascader),
.field-control :deep(.el-date-editor) {
  width: 100%;
}

.field-control :deep(.el-input__wrapper),
.field-control :deep(.el-select__wrapper),
.vital-field :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 3px;
  box-shadow: 0 0 0 1px var(--border) inset;
}

.field-control :deep(.el-input-group__append) {
  width: 56px;
  padding: 0;
  justify-content: center;
  background: #fff;
}

.field-control :deep(.el-input__wrapper.is-focus),
.field-control :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--primary) inset;
}

:global(.address-cascader-popper .el-cascader-menu) {
  height: 216px;
}

:global(.address-cascader-popper .el-cascader-node) {
  height: 36px;
  line-height: 36px;
}

.address-detail {
  grid-column: span 2;
}

.wide-field {
  grid-column: span 2;
}

.prescription-card,
.medical-card {
  margin-top: 10px;
  padding: 28px 26px 72px;
}

.prescription-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.outline-button,
.prescription-tag {
  height: 33px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 14px;
  border-radius: 3px;
  background: #fff;
  cursor: pointer;
}

.outline-button { border: 1px solid #c9c9c9; color: #333; }
.prescription-tag { border: 1px solid var(--primary); color: var(--primary); }

.prescription-workspace {
  display: grid;
  grid-template-columns: minmax(760px, 2.2fr) minmax(320px, 1fr);
  gap: 14px;
}

.rx-panel {
  height: 574px;
  display: grid;
  grid-template-rows: 76px 1fr 62px;
  overflow: hidden;
  border-radius: 4px;
  box-shadow: 0 1px 12px rgb(98 104 232 / 24%);
}

.rx-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
}

.rx-header strong {
  color: var(--primary);
  font-size: 36px;
  line-height: 1;
}

.text-action {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border: 0;
  background: transparent;
  color: #222;
  font-weight: 700;
  cursor: pointer;
}

.rx-table-wrap {
  min-height: 0;
}

.rx-table-wrap :deep(.el-table) {
  --el-table-header-bg-color: #dfe1fa;
  --el-table-border-color: #eee;
  font-size: 13px;
}

.rx-table-wrap :deep(.el-table th.el-table__cell) {
  height: 52px;
  padding: 0;
  color: #111;
}

.rx-table-wrap :deep(.el-table td.el-table__cell) {
  padding: 6px 0;
}

.rx-table-wrap :deep(.el-table__body-wrapper) {
  overflow-x: auto;
}

.rx-table-wrap :deep(.el-table__body) {
  min-width: 818px;
}

.rx-table-wrap :deep(.cell) {
  padding: 0 4px;
}

.rx-drug-name {
  display: block;
  color: #111827;
  line-height: 20px;
  white-space: normal;
  word-break: break-all;
}

.rx-group-select,
.rx-cell-select,
.rx-small-input,
.rx-price-input,
.rx-inline-field :deep(.el-input),
.rx-quantity-field :deep(.el-input) {
  width: 100%;
}

.rx-inline-field,
.rx-quantity-field {
  display: grid;
  align-items: center;
  gap: 6px;
}

.rx-inline-field {
  grid-template-columns: minmax(34px, 1fr) auto;
}

.rx-quantity-field {
  grid-template-columns: minmax(34px, 1fr) 52px;
}

.rx-table-wrap :deep(.el-input__wrapper),
.rx-table-wrap :deep(.el-select__wrapper) {
  min-height: 38px;
  border-radius: 3px;
  box-shadow: 0 0 0 1px #cfcfcf inset;
}

.rx-table-wrap :deep(.el-input__inner) {
  text-align: center;
}

.rx-table-wrap :deep(.el-select__selected-item) {
  color: #111827;
}

.rx-delete-button {
  min-width: 0;
  padding: 0;
  color: #777dff;
  font-size: 18px;
}

.rx-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  border-top: 1px solid #eee;
  white-space: nowrap;
  font-size: 13px;
}

.fee-button {
  padding: 8px 10px;
}

.footer-select {
  width: 104px;
}

.doctor-select {
  width: 112px;
}

.footer-select :deep(.el-select__wrapper) {
  min-height: 32px;
  border-radius: 2px;
}

.rx-footer p {
  min-width: 0;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rx-footer b {
  color: #f00;
}

.drug-panel {
  min-width: 0;
  padding-top: 4px;
}

.drug-filter {
  display: grid;
  grid-template-columns: auto 130px minmax(150px, 1fr);
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.drug-filter :deep(.el-input__wrapper),
.drug-filter :deep(.el-select__wrapper) {
  min-height: 36px;
  border-radius: 2px;
}

.drug-table {
  width: 100%;
  font-size: 13px;
}

.drug-table :deep(.el-table th.el-table__cell) {
  height: 38px;
  padding: 0;
  color: #111;
}

.drug-table :deep(.el-table td.el-table__cell) {
  height: 40px;
  padding: 0;
}

.add-drug-button {
  margin-top: 12px;
}

.medical-card {
  padding-bottom: 66px;
}

.medical-card h2 {
  margin: 0 0 18px;
  color: #6870ef;
  font-size: 18px;
}

.vitals-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px 42px;
  margin-bottom: 26px;
}

.vital-field {
  min-width: 0;
  display: grid;
  grid-template-columns: 38px minmax(72px, 100px) auto;
  align-items: center;
  gap: 10px;
}

.vital-field b {
  font-weight: 400;
  white-space: nowrap;
}

.medical-heading {
  margin-top: 28px !important;
}

.medical-heading small {
  color: #c3c6cd;
  font-size: 12px;
  font-weight: 400;
}

.medical-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 34px;
}

.medical-grid .wide-field {
  grid-column: span 1;
}

.medical-grid :deep(.el-textarea__inner) {
  min-height: 88px !important;
  border-radius: 3px;
  box-shadow: 0 0 0 1px var(--border) inset;
}

.consultation-actions {
  position: fixed;
  left: 84px;
  right: 0;
  bottom: 0;
  z-index: 25;
  display: flex;
  justify-content: center;
  pointer-events: none;
}

.action-inner {
  width: min(calc(100% - 56px), 1280px);
  min-height: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 22px;
  padding: 0 22px;
  box-sizing: border-box;
  border-radius: 5px 5px 0 0;
  background: #fff;
  box-shadow: 0 -8px 30px rgb(15 23 42 / 10%);
  pointer-events: auto;
}

.action-links,
.primary-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-links button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #222;
  font-weight: 700;
  cursor: pointer;
}

.primary-actions :deep(.el-button) {
  min-width: 112px;
  height: 38px;
  margin-left: 0;
  font-weight: 700;
}

.warning-button {
  border-color: var(--warning) !important;
  background: var(--warning) !important;
  color: #fff !important;
}

.money-icon {
  width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff;
  color: var(--warning);
}

@media (max-width: 1280px) {
  .patient-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .address-detail, .wide-field { grid-column: span 1; }
  .prescription-workspace { grid-template-columns: 1fr; }
  .rx-panel { height: 560px; }
  .consultation-actions { left: 84px; }
}

@media (max-width: 820px) {
  .patient-grid,
  .medical-grid,
  .vitals-grid { grid-template-columns: 1fr; }
  .consultation-actions { left: 84px; }
  .action-inner { align-items: flex-end; flex-direction: column; gap: 8px; padding: 10px 14px; }
  .drug-filter { grid-template-columns: 1fr; }
}
</style>
