<template>
  <div class="medical-template-form-page">
    <section class="template-form-card">
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

      <section class="basic-panel">
        <div class="basic-grid">
          <label class="field">
            <span>模板编号</span>
            <input v-model="form.code" />
          </label>
          <label class="field">
            <span>模板名称<i>*</i></span>
            <input v-model="form.name" placeholder="请输入模板名称" />
          </label>
          <div class="radio-field">
            <span>模板类型</span>
            <label><input v-model="form.type" name="type" type="radio" value="西药病历" /> 西药病历</label>
            <label><input v-model="form.type" name="type" type="radio" value="中药病历" /> 中药病历</label>
          </div>
          <div class="radio-field permission-field">
            <span>模板权限</span>
            <label><input v-model="form.permission" name="permission" type="radio" value="私人模板" /> 私人模板</label>
            <label><input v-model="form.permission" name="permission" type="radio" value="公共模板" /> 公共模板</label>
          </div>

          <label class="field desc-field">
            <span>模板说明</span>
            <input v-model="form.description" />
          </label>
          <label class="field">
            <span>创建人员</span>
            <input v-model="form.creator" class="is-disabled" disabled />
          </label>
          <label class="field">
            <span>创建时间</span>
            <input v-model="form.createDate" class="is-disabled" disabled />
          </label>
        </div>
      </section>

      <section class="medical-info">
        <h2>病历信息 <small>（病历信息可在系统设置-基础设置中的自定义设置）</small></h2>
        <div class="medical-grid">
          <label
            v-for="item in medicalFields"
            :key="item.key"
            :class="['field', 'medical-field', { remark: item.key === 'remark' }]"
          >
            <span>{{ item.label }}<i v-if="item.required">*</i></span>
            <textarea v-model="form[item.key]" :class="{ compact: item.compact }"></textarea>
            <Search class="field-search" />
          </label>
        </div>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, FolderChecked, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { accountApi, medicalTemplateApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";
import { getStoredCurrentUser } from "@/utils/permissions";

const route = useRoute();
const router = useRouter();
const templateId = readNavigationState(route);

const templateRows = Array.from({ length: 10 }, (_, index) => ({
  id: `medical-${index}`,
  code: `RZ${String(index + 5).padStart(5, "0")}`,
  name: `病历名称${index + 1}`,
  creator: index % 2 === 0 ? "王冕" : "林鹤",
  createDate: "2019-10-09",
  permission: index % 2 === 0 ? "私人模板" : "公共模板",
}));

const currentTemplate = computed(() => {
  return templateRows.find((item) => item.id === templateId);
});

const pageTitle = computed(() => (currentTemplate.value ? "编辑病历模板" : "新建病历模板"));

const form = reactive<Record<string, string>>({
  code: currentTemplate.value?.code || "",
  name: currentTemplate.value?.name || "",
  type: "西药病历",
  permission: currentTemplate.value?.permission || "私人模板",
  description: "",
  creator: currentTemplate.value?.creator || "",
  createDate: currentTemplate.value?.createDate || "",
  diagnosis: "",
  chiefComplaint: "",
  presentHistory: "",
  pastHistory: "",
  allergyHistory: "",
  personalHistory: "",
  familyHistory: "",
  auxiliaryExam: "",
  treatmentAdvice: "",
  doctorAdvice: "",
  remark: "",
});

const medicalFields = [
  { key: "diagnosis", label: "诊断", required: true, compact: true },
  { key: "chiefComplaint", label: "主诉", required: true, compact: true },
  { key: "presentHistory", label: "现病史" },
  { key: "pastHistory", label: "既往史" },
  { key: "allergyHistory", label: "过敏史" },
  { key: "personalHistory", label: "个人史" },
  { key: "familyHistory", label: "家族史" },
  { key: "auxiliaryExam", label: "辅助检查" },
  { key: "treatmentAdvice", label: "治疗意见" },
  { key: "doctorAdvice", label: "医嘱" },
  { key: "remark", label: "备注" },
];

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
  if (!form.createDate) form.createDate = formatDateTime();
});

const saveForm = async () => {
  const payload = { templateCode: form.code, name: form.name, templateType: form.type, permission: form.permission, diagnosis: form.diagnosis, description: form.description, creator: form.creator, content: JSON.stringify(form) };
  if (templateId) await medicalTemplateApi.update(templateId, payload);
  else await medicalTemplateApi.create(payload);
  ElMessage.success("保存成功");
  router.push("/system/templates/medical");
};
</script>

<style scoped>
.medical-template-form-page {
  min-height: 100%;
  padding: 15px 0 86px;
}

.template-form-card {
  width: 1242px;
  min-height: 1158px;
  margin: 0 auto;
  padding: 28px 25px 45px;
  background: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.1);
  box-sizing: border-box;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-header h1 {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  margin: 0;
  color: #111111;
  font-size: 20px;
  line-height: 1;
  font-weight: 800;
}

.form-header h1 span {
  width: 5px;
  height: 19px;
  background: #636be8;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.header-actions button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 110px;
  height: 36px;
  border-radius: 4px;
  border: 1px solid #636be8;
  background: #ffffff;
  color: #636be8;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.header-actions .save-btn {
  background: #636be8;
  color: #ffffff;
}

.action-icon {
  width: 20px;
  height: 20px;
}

.basic-panel {
  margin-top: 27px;
  padding: 20px 20px 21px;
  background: #eef0ff;
}

.basic-grid {
  display: grid;
  grid-template-columns: 271px 271px 270px 270px;
  gap: 31px 22px;
  align-items: end;
}

.field {
  position: relative;
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 9px;
  color: #000000;
  font-size: 14px;
}

.field i {
  color: #ff3333;
  font-style: normal;
}

.field input,
.field textarea {
  width: 100%;
  height: 43px;
  padding: 0 16px;
  border: 1px solid #c9c9c9;
  border-radius: 3px;
  outline: none;
  background: #ffffff;
  color: #000000;
  font-size: 14px;
  box-shadow: 0 0 0 1px #d0d0d0 inset;
  box-sizing: border-box;
}

.field input::placeholder {
  color: #bfc2cc;
}

.field .is-disabled {
  background: #f4f4f4;
}

.radio-field {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 43px;
  padding-bottom: 2px;
  color: #000000;
  font-size: 14px;
}

.radio-field label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.radio-field input {
  width: 14px;
  height: 14px;
  margin: 0;
  accent-color: #1677ee;
}

.permission-field {
  gap: 15px;
}

.desc-field {
  grid-column: span 2;
}

.medical-info {
  margin-top: 31px;
}

.medical-info h2 {
  margin: 0 0 24px;
  color: #7275f2;
  font-size: 18px;
  line-height: 1;
  font-weight: 800;
}

.medical-info h2 small {
  color: #c4c6d0;
  font-size: 12px;
  font-weight: 700;
}

.medical-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  column-gap: 33px;
  row-gap: 18px;
}

.medical-field textarea {
  height: 87px;
  padding: 12px 36px 12px 14px;
  resize: none;
}

.medical-field textarea.compact {
  height: 44px;
}

.field-search {
  position: absolute;
  right: 12px;
  bottom: 13px;
  width: 18px;
  height: 18px;
  color: #c6c8cf;
}

.medical-field:has(textarea.compact) .field-search {
  bottom: 12px;
}

.medical-field.remark {
  grid-column: span 1;
}

@media (max-width: 1280px) {
  .template-form-card {
    width: calc(100% - 32px);
  }

  .basic-grid,
  .medical-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .radio-field,
  .permission-field {
    justify-content: flex-start;
  }
}
</style>
