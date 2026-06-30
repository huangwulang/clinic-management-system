<template>
  <div class="check-project-form-page">
    <section class="project-card">
      <header class="form-header">
        <h1><span></span>{{ pageTitle }}</h1>
        <div class="header-actions">
          <button class="save-btn" type="button" @click="saveForm">
            <FolderChecked class="action-icon" />
            保存
          </button>
          <button class="back-btn" type="button" @click="router.back()">
            <Back class="action-icon" />
            返回
          </button>
        </div>
      </header>

      <section class="project-info">
        <h2>项目信息</h2>

        <div class="form-grid">
          <label class="field">
            <span>项目编码</span>
            <input v-model="form.code" />
          </label>
          <label class="field">
            <span>项目名称</span>
            <input v-model="form.name" />
          </label>
          <label class="field">
            <span>成本价（元）</span>
            <input v-model="form.cost" />
          </label>
          <label class="field">
            <span>零售价（元）</span>
            <input v-model="form.retail" />
          </label>

          <label class="field select-field">
            <span>单位<i>*</i></span>
            <div class="with-add">
              <select v-model="form.unit">
                <option value="">请选择</option>
                <option v-for="unit in unitOptions" :key="unit" :value="unit">{{ unit }}</option>
              </select>
              <button type="button" aria-label="新增单位">+</button>
            </div>
          </label>
          <label class="field select-field">
            <span>项目分类<i>*</i></span>
            <div class="with-add">
              <select v-model="form.category">
                <option value="">请选择</option>
                <option v-for="category in categoryOptions" :key="category" :value="category">{{ category }}</option>
              </select>
              <button type="button" aria-label="新增项目分类">+</button>
            </div>
          </label>
          <label class="field select-field">
            <span>发票项目<i>*</i></span>
            <div class="with-add">
              <select v-model="form.invoice">
                <option value="">请选择</option>
                <option v-for="invoice in invoiceOptions" :key="invoice" :value="invoice">{{ invoice }}</option>
              </select>
              <button type="button" aria-label="新增发票项目">+</button>
            </div>
          </label>
          <label class="field">
            <span>部位</span>
            <input v-model="form.part" />
          </label>

          <div class="radio-field">
            <span>允许会员折扣：</span>
            <label><input v-model="form.discount" name="discount" type="radio" value="是" /> 是</label>
            <label><input v-model="form.discount" name="discount" type="radio" value="否" /> 否</label>
          </div>
          <div class="radio-field">
            <span>项目状态：</span>
            <label><input v-model="form.status" name="status" type="radio" value="启用" /> 启用</label>
            <label><input v-model="form.status" name="status" type="radio" value="禁用" /> 禁用</label>
          </div>
          <label class="field remark-field">
            <span>备注</span>
            <input v-model="form.remark" />
          </label>
        </div>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, FolderChecked } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { accountApi, checkProjectApi, dictionaryApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const projectId = readNavigationState(route);

const pageTitle = computed(() => (projectId ? "编辑项目" : "新增项目"));

const form = reactive({
  code: "",
  name: "",
  cost: "",
  retail: "",
  unit: "",
  category: "",
  invoice: "",
  part: "",
  discount: "是",
  status: "启用",
  remark: "",
});

const unitOptions = ref(["次", "项"]);
const categoryOptions = ref(["脑电图", "血压测量", "超声检查", "针灸"]);
const invoiceOptions = ref(["检查费", "注射费"]);

const dictionaryNames = (records: any[]) =>
  records.map((item) => item.itemName || item.name || item.label || item.value || "").filter(Boolean);

const loadOptions = async () => {
  const [unitResponse, categoryResponse, invoiceResponse]: any[] = await Promise.all([
    dictionaryApi("project-项目单位").list(),
    dictionaryApi("project-项目分类").list(),
    dictionaryApi("drug-发票项目").list(),
  ]);
  const units = dictionaryNames(unitResponse?.data || []);
  const categories = dictionaryNames(categoryResponse?.data || []);
  const invoices = dictionaryNames(invoiceResponse?.data || []);
  if (units.length) unitOptions.value = units;
  if (categories.length) categoryOptions.value = categories;
  if (invoices.length) invoiceOptions.value = invoices;
};

const currentEmployeeName = async () => {
  try {
    const response: any = await accountApi.profile();
    const profile = response?.data || {};
    return profile.name || profile.realName || profile.employeeName || profile.username || "";
  } catch {
    return "";
  }
};

const saveForm = async () => {
  if (!form.code.trim()) {
    ElMessage.warning("请输入项目编码");
    return;
  }
  if (!form.name.trim()) {
    ElMessage.warning("请输入项目名称");
    return;
  }
  const payload = {
    projectCode: form.code.trim(),
    name: form.name.trim(),
    costPrice: Number(form.cost || 0),
    retailPrice: Number(form.retail || 0),
    price: Number(form.retail || 0),
    unit: form.unit,
    category: form.category,
    invoiceItem: form.invoice,
    bodyPart: form.part.trim(),
    allowMemberDiscount: form.discount === "是",
    status: form.status === "启用" ? "ENABLED" : "DISABLED",
    creator: await currentEmployeeName(),
    remark: form.remark.trim(),
  };
  if (projectId) await checkProjectApi.update(projectId, payload);
  else await checkProjectApi.create(payload);
  ElMessage.success("保存成功");
  router.push("/system/check-projects");
};

onMounted(async () => {
  await loadOptions();
  if (!projectId) return;
  const response: any = await checkProjectApi.get(projectId);
  const item = response?.data;
  form.code = item?.projectCode || "";
  form.name = item?.name || "";
  form.cost = String(item?.costPrice ?? "");
  form.retail = String(item?.retailPrice ?? item?.price ?? "");
  form.unit = item?.unit || "";
  form.category = item?.category || "";
  form.invoice = item?.invoiceItem || "";
  form.part = item?.bodyPart || "";
  form.discount = item?.allowMemberDiscount === false ? "否" : "是";
  form.status = item?.status === "DISABLED" ? "禁用" : "启用";
  form.remark = item?.remark || "";
});
</script>

<style scoped>
.check-project-form-page {
  min-height: 100%;
  padding: 24px 0 84px;
}

.project-card {
  width: min(1616px, calc(100% - 96px));
  min-height: 685px;
  margin: 0 auto;
  padding: 32px 32px 48px;
  background: #ffffff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
  box-sizing: border-box;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.form-header h1 {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #111111;
  font-size: 25px;
  line-height: 1;
  font-weight: 800;
}

.form-header h1 span {
  width: 6px;
  height: 25px;
  background: #7275f2;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-actions button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 48px;
  min-width: 146px;
  padding: 0 24px;
  border-radius: 5px;
  border: 1px solid #636be8;
  background: #ffffff;
  color: #636be8;
  font-size: 19px;
  font-weight: 800;
  cursor: pointer;
}

.header-actions .save-btn {
  background: #636be8;
  color: #ffffff;
}

.action-icon {
  width: 26px;
  height: 26px;
}

.project-info {
  margin-top: 42px;
}

.project-info h2 {
  margin: 0 0 28px;
  color: #636be8;
  font-size: 31px;
  line-height: 1;
  font-weight: 800;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  column-gap: 47px;
  row-gap: 38px;
  align-items: end;
}

.field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 13px;
  color: #000000;
  font-size: 17px;
}

.field i {
  color: #ff3333;
  font-style: normal;
}

.field input,
.field select {
  width: 100%;
  height: 58px;
  padding: 0 22px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  outline: none;
  color: #000000;
  font-size: 20px;
  box-shadow: 0 0 0 1px #d0d0d0 inset;
  box-sizing: border-box;
}

.field input.is-disabled {
  background: #f4f4f4;
}

.field select {
  appearance: none;
  background: #ffffff
    linear-gradient(45deg, transparent 50%, #d5d5d5 50%) right 22px center / 9px 9px no-repeat;
  color: #111111;
}

.with-add {
  display: grid;
  grid-template-columns: 1fr 28px;
  gap: 10px;
  align-items: center;
}

.with-add button {
  width: 26px;
  height: 26px;
  padding: 0;
  border: 3px solid #636be8;
  border-radius: 50%;
  background: #ffffff;
  color: #636be8;
  font-size: 26px;
  line-height: 18px;
  font-weight: 800;
  cursor: pointer;
}

.radio-field {
  display: flex;
  align-items: center;
  gap: 24px;
  min-height: 58px;
  color: #000000;
  font-size: 18px;
}

.radio-field label {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.radio-field input {
  width: 18px;
  height: 18px;
  margin: 0;
  accent-color: #1677ee;
}

.remark-field {
  grid-column: span 2;
}

@media (max-width: 1280px) {
  .project-card {
    width: calc(100% - 32px);
    padding: 28px 22px 42px;
  }

  .form-grid {
    grid-template-columns: repeat(2, minmax(240px, 1fr));
  }

  .remark-field {
    grid-column: span 2;
  }
}
</style>
