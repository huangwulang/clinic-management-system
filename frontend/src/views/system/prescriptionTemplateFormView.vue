<template>
  <div class="prescription-form-page">
    <section class="prescription-card">
      <header class="form-header">
        <h1><span></span>{{ pageTitle }}</h1>
        <div class="header-actions">
          <button v-if="isCheckTemplate" class="green-btn" type="button">
            <CirclePlusFilled class="action-icon" />
            添加项目
          </button>
          <template v-else>
            <button class="green-btn" type="button">
              <CirclePlusFilled class="action-icon" />
              添加药品
            </button>
            <button class="green-btn" type="button">
              <CirclePlusFilled class="action-icon" />
              添加附加费
            </button>
          </template>
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

      <section class="basic-panel">
        <div class="basic-grid">
          <label class="field">
            <span>模板编号</span>
            <input v-model="form.code" />
          </label>
          <label class="field">
            <span>模板类型</span>
            <select v-model="form.templateType">
              <option>西/成药处方</option>
              <option>中药处方</option>
              <option>检查项目</option>
            </select>
          </label>
          <label class="field">
            <span>模板名称<i>*</i></span>
            <input v-model="form.name" />
          </label>
          <div class="radio-field">
            <span>模板权限</span>
            <label><input v-model="form.permission" name="permission" type="radio" value="私人模板" /> 私人模板</label>
            <label><input v-model="form.permission" name="permission" type="radio" value="公共模板" /> 公共模板</label>
          </div>

          <label class="field diagnosis-field">
            <span>诊断<i>*</i></span>
            <input v-model="form.diagnosis" />
            <Search class="field-search" />
          </label>
          <label class="field desc-field">
            <span>模板说明</span>
            <input v-model="form.description" />
          </label>

          <label class="field small-field">
            <span>创建人员</span>
            <input v-model="form.creator" class="is-disabled" disabled />
          </label>
          <label class="field small-field">
            <span>创建时间</span>
            <input v-model="form.createTime" class="is-disabled" disabled />
          </label>
        </div>
      </section>

      <section class="content-section">
        <h2>{{ contentTitle }}</h2>
        <table class="content-table">
          <thead>
            <tr>
              <th v-for="column in tableColumns" :key="column">{{ column }}</th>
            </tr>
          </thead>
        </table>
      </section>

      <h2 v-if="!isCheckTemplate" class="extra-title">附加费用</h2>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, CirclePlusFilled, FolderChecked, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { accountApi, prescriptionTemplateApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";
import { getStoredCurrentUser } from "@/utils/permissions";

type PrescriptionTemplateType = "western" | "chinese" | "check";

const route = useRoute();
const router = useRouter();
const templateId = readNavigationState(route);
const templateType = readNavigationState(route, "type");

const templateRows = Array.from({ length: 10 }, (_, index) => ({
  id: `prescription-${index}`,
  code: `RZ${String(index + 5).padStart(5, "0")}`,
  name: `处方模板名称${index + 1}`,
  prescriptionType: ["西/成药处方", "中药处方", "检查项目"][index % 3],
  permission: index % 2 === 0 ? "私人模板" : "公共模板",
}));

const currentTemplate = computed(() => {
  return templateRows.find((item) => item.id === templateId);
});

const typeNameFromKey = (value: string) => {
  if (value === "chinese") return "中药处方";
  if (value === "check") return "检查项目";
  return "西/成药处方";
};

const initialTemplateType = () => currentTemplate.value?.prescriptionType || typeNameFromKey(templateType);

const form = reactive({
  code: currentTemplate.value?.code || "",
  templateType: initialTemplateType(),
  name: currentTemplate.value?.name || "",
  permission: currentTemplate.value?.permission || "私人模板",
  diagnosis: "",
  description: "",
  creator: "",
  createTime: "",
});

const type = computed<PrescriptionTemplateType>(() => {
  if (form.templateType === "中药处方") return "chinese";
  if (form.templateType === "检查项目") return "check";
  return "western";
});

const pageTitle = computed(() => {
  const action = currentTemplate.value ? "编辑" : "新增";
  if (type.value === "chinese") return `${action}中药处方模板`;
  if (type.value === "check") return `${action}检查项目模板`;
  return `${action}西/成药处方模板`;
});

const contentTitle = computed(() => {
  if (type.value === "chinese") return "中药模板内容";
  if (type.value === "check") return "检查项目模板内容";
  return "西/成药模板内容";
});

const isCheckTemplate = computed(() => type.value === "check");

const tableColumns = computed(() => {
  if (type.value === "chinese") return ["序号", "名称", "剂量", "用法", "编辑"];
  if (type.value === "check") return ["序号", "名称", "部位", "数量", "用法", "备注", "编辑"];
  return ["序号", "组号", "名称", "单次用量", "用法", "频度", "天数", "总量"];
});

const formatDateTime = (date = new Date()) => {
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

const currentEmployeeName = async () => {
  try {
    const response: any = await accountApi.profile();
    const profile = response?.data || {};
    return profile.name || profile.realName || profile.employeeName || profile.username || "";
  } catch {
    const user = getStoredCurrentUser() || {};
    return user.name || user.realName || user.employeeName || user.username || "";
  }
};

onMounted(async () => {
  if (!form.creator) form.creator = await currentEmployeeName();
  if (!form.createTime) form.createTime = formatDateTime();
});

const saveForm = async () => {
  const payload = { templateCode: form.code, name: form.name, templateType: "PRESCRIPTION", permission: form.permission, diagnosis: form.diagnosis, description: form.description, creator: form.creator, content: JSON.stringify(form) };
  if (templateId) await prescriptionTemplateApi.update(templateId, payload);
  else await prescriptionTemplateApi.create(payload);
  ElMessage.success("保存成功");
  router.push("/system/templates/prescription");
};
</script>

<style scoped>
.prescription-form-page {
  min-height: 100%;
  padding: 25px 0 92px;
}

.prescription-card {
  width: min(1654px, calc(100% - 96px));
  min-height: 1006px;
  margin: 0 auto;
  padding: 32px 34px 58px;
  background: #ffffff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.1);
  box-sizing: border-box;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
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
  flex: 0 1 auto;
}

.form-header h1 span {
  width: 6px;
  height: 25px;
  background: #636be8;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1 1 620px;
  justify-content: flex-end;
  flex-wrap: wrap;
  min-width: 0;
}

.header-actions button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 11px;
  height: 48px;
  min-width: 120px;
  padding: 0 22px;
  border-radius: 5px;
  border: 1px solid #636be8;
  background: #ffffff;
  color: #636be8;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
  white-space: nowrap;
  flex: 0 1 auto;
}

.header-actions .green-btn {
  min-width: 126px;
  border-color: #2acb92;
  background: #2acb92;
  color: #ffffff;
}

.header-actions .save-btn {
  background: #636be8;
  color: #ffffff;
}

.action-icon {
  width: 25px;
  height: 25px;
}

.basic-panel {
  margin-top: 34px;
  padding: 29px 25px 50px;
  background: #eef0ff;
  overflow: hidden;
}

.basic-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  column-gap: 30px;
  row-gap: 37px;
  align-items: end;
}

.field {
  position: relative;
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
  padding: 0 24px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  outline: none;
  background: #ffffff;
  color: #000000;
  font-size: 20px;
  box-shadow: 0 0 0 1px #d0d0d0 inset;
  box-sizing: border-box;
  min-width: 0;
}

.field select {
  appearance: none;
  background: #ffffff
    linear-gradient(45deg, transparent 50%, #d5d5d5 50%) right 24px center / 9px 9px no-repeat;
}

.field .is-disabled {
  background: #f4f4f4;
}

.radio-field {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 18px;
  min-height: 58px;
  padding-bottom: 4px;
  color: #000000;
  font-size: 19px;
}

.radio-field label {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.radio-field input {
  width: 18px;
  height: 18px;
  margin: 0;
  accent-color: #1677ee;
}

.diagnosis-field,
.desc-field {
  grid-column: span 2;
}

.field-search {
  position: absolute;
  right: 18px;
  bottom: 17px;
  width: 24px;
  height: 24px;
  color: #c6c8cf;
}

.small-field {
  width: auto;
}

.content-section {
  margin-top: 38px;
}

.content-section h2,
.extra-title {
  margin: 0 0 31px;
  color: #7275f2;
  font-size: 28px;
  line-height: 1;
  font-weight: 800;
}

.content-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.content-table th {
  height: 66px;
  padding: 0 8px;
  background: #dfe2ff;
  color: #05070c;
  font-size: 20px;
  font-weight: 800;
  text-align: center;
  white-space: normal;
  word-break: keep-all;
}

.extra-title {
  margin-top: 150px;
}

@media (max-width: 1280px) {
  .prescription-card {
    width: calc(100% - 32px);
  }

  .form-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 12px;
  }

  .header-actions button {
    min-width: 112px;
    height: 42px;
    padding: 0 16px;
    font-size: 16px;
  }

  .basic-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .radio-field,
  .small-field {
    width: auto;
  }
}

@media (max-width: 900px) {
  .prescription-card {
    padding: 24px 18px 48px;
  }

  .basic-panel {
    padding: 22px 18px 34px;
  }

  .basic-grid {
    grid-template-columns: 1fr;
  }

  .diagnosis-field,
  .desc-field {
    grid-column: span 1;
  }

  .content-table th {
    font-size: 16px;
    padding: 0 4px;
  }
}
</style>
