<template>
  <div class="supplier-form-page">
    <section class="supplier-card">
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

      <section class="supplier-form">
        <div class="form-grid">
          <label class="field">
            <span>供应商编号</span>
            <input v-model="form.code" />
          </label>
          <label class="field">
            <span>供应商名称<i>*</i></span>
            <input v-model="form.name" />
          </label>
          <div class="radio-field">
            <span>供应商状态：</span>
            <label><input v-model="form.status" name="status" type="radio" value="启用" /> 启用</label>
            <label><input v-model="form.status" name="status" type="radio" value="禁用" /> 禁用</label>
          </div>

          <label class="field">
            <span>联系人</span>
            <input v-model="form.contact" />
          </label>
          <label class="field">
            <span>联系电话</span>
            <input v-model="form.phone" />
          </label>

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
import { computed, onMounted, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, FolderChecked } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { accountApi, supplierApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const supplierId = readNavigationState(route);

const pageTitle = computed(() => (supplierId ? "编辑供应商" : "新增供应商"));

const form = reactive({
  code: "",
  name: "",
  status: "启用",
  contact: "",
  phone: "",
  remark: "",
});

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
    ElMessage.warning("请输入供应商编号");
    return;
  }
  if (!form.name.trim()) {
    ElMessage.warning("请输入供应商名称");
    return;
  }
  const payload = {
    supplierCode: form.code.trim(),
    name: form.name.trim(),
    status: form.status === "启用" ? "ENABLED" : "DISABLED",
    contactName: form.contact.trim(),
    phone: form.phone.trim(),
    remark: form.remark.trim(),
    creator: await currentEmployeeName(),
  };
  if (supplierId) await supplierApi.update(supplierId, payload);
  else await supplierApi.create(payload);
  ElMessage.success("保存成功");
  router.push("/system/suppliers");
};

onMounted(async () => {
  if (!supplierId) return;
  const response: any = await supplierApi.get(supplierId);
  const item = response?.data;
  form.code = item?.supplierCode || "";
  form.name = item?.name || "";
  form.status = item?.status === "DISABLED" ? "禁用" : "启用";
  form.contact = item?.contactName || "";
  form.phone = item?.phone || "";
  form.remark = item?.remark || "";
});
</script>

<style scoped>
.supplier-form-page {
  min-height: 100%;
  padding: 14px 0 84px;
}

.supplier-card {
  width: min(1654px, calc(100% - 250px));
  min-height: 687px;
  margin: 0 auto;
  padding: 35px 32px 48px;
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
  font-size: 24px;
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
  padding-right: 2px;
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

.supplier-form {
  margin-top: 37px;
}

.form-grid {
  display: grid;
  grid-template-columns: 362px 362px minmax(320px, 1fr);
  column-gap: 47px;
  row-gap: 26px;
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

.field input {
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

.radio-field {
  display: flex;
  align-items: center;
  align-self: end;
  gap: 24px;
  min-height: 58px;
  padding-bottom: 5px;
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
  .supplier-card {
    width: calc(100% - 32px);
    padding: 28px 22px 42px;
  }

  .form-grid {
    grid-template-columns: repeat(2, minmax(240px, 1fr));
  }

  .radio-field {
    grid-column: 1 / -1;
  }
}
</style>
