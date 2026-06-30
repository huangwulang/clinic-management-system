<template>
  <div class="fee-form-page">
    <section class="fee-form-card">
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

      <section class="fee-form">
        <div :class="['form-grid', { addon: isAddon }]">
          <label class="field">
            <span>费用名称<i>*</i></span>
            <input v-model="form.name" />
          </label>

          <label v-if="isAddon" class="field select-field">
            <span>处方类型</span>
            <select v-model="form.prescriptionType">
              <option>西/成药处方</option>
              <option>中药方</option>
            </select>
          </label>

          <label class="field">
            <span>金额<i>*</i>（元）</span>
            <input v-model="form.amount" />
          </label>
          <label class="field">
            <span>成本价（元）</span>
            <input v-model="form.cost" />
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
import { accountApi, feeItemApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";
import { getStoredCurrentUser } from "@/utils/permissions";

type FeeType = "addon" | "treatment" | "registration";

const route = useRoute();
const router = useRouter();
const feeId = readNavigationState(route);
const feeType = readNavigationState(route, "type");

const type = ref<FeeType>("addon");
const isEdit = ref(false);

const normalizeType = (value: unknown): FeeType => {
  if (value === "treatment" || value === "registration") return value;
  return "addon";
};

type.value = normalizeType(feeType);

const isAddon = computed(() => type.value === "addon");

const pageTitle = computed(() => {
  const action = isEdit.value ? "编辑" : "新增";
  if (type.value === "addon") return `${action}附加费`;
  if (type.value === "registration") return `${action}挂号费`;
  return `${action}诊疗费`;
});

const form = reactive({
  name: "",
  prescriptionType: "西/成药处方",
  amount: "",
  cost: "0.00",
  discount: "是",
  status: "启用",
  creator: "",
});

const parseFeeRemark = (remark: unknown) => {
  if (typeof remark !== "string" || !remark.trim()) return {};
  try {
    const parsed = JSON.parse(remark);
    return parsed && typeof parsed === "object" ? parsed as Record<string, any> : {};
  } catch {
    return {};
  }
};

const typeFromCategory = (category: unknown): FeeType => {
  const value = String(category || "");
  if (value === "诊疗费" || value.toUpperCase() === "TREATMENT") return "treatment";
  if (value === "挂号费" || value.toUpperCase() === "REGISTRATION") return "registration";
  return "addon";
};

const categoryFromType = () => {
  if (type.value === "treatment") return "诊疗费";
  if (type.value === "registration") return "挂号费";
  return "附加费";
};

const normalizeStatus = (status: unknown) => {
  const value = String(status ?? "").toUpperCase();
  if (value === "ENABLED" || value === "1" || value === "TRUE" || status === true) return "启用";
  if (value === "DISABLED" || value === "0" || value === "FALSE" || status === false) return "禁用";
  return status === "停用" ? "禁用" : "启用";
};

const statusPayload = () => (form.status === "启用" ? "ENABLED" : "DISABLED");

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

const loadForm = async () => {
  form.creator = await currentEmployeeName();
  if (!feeId) return;

  const response: any = await feeItemApi.get(feeId);
  const item = response?.data || {};
  const meta = parseFeeRemark(item.remark);
  isEdit.value = true;
  type.value = typeFromCategory(item.category || feeType);
  form.name = item.name || "";
  form.prescriptionType = meta.prescriptionType || "西/成药处方";
  form.amount = Number(item.price || 0).toFixed(2);
  form.cost = Number(meta.cost || 0).toFixed(2);
  form.discount = meta.allowDiscount === false ? "否" : "是";
  form.status = normalizeStatus(item.status);
  form.creator = meta.creator || form.creator;
};

const saveForm = async () => {
  if (!form.name.trim()) {
    ElMessage.warning("请填写费用名称");
    return;
  }
  const payload = {
    name: form.name,
    category: categoryFromType(),
    price: Number(form.amount || 0),
    unit: "次",
    status: statusPayload(),
    remark: JSON.stringify({
      prescriptionType: isAddon.value ? form.prescriptionType : "",
      cost: Number(form.cost || 0),
      allowDiscount: form.discount === "是",
      creator: form.creator,
    }),
  };
  if (feeId) await feeItemApi.update(feeId, payload);
  else await feeItemApi.create(payload);
  ElMessage.success("保存成功");
  router.push(`/system/fees/${type.value}`);
};

onMounted(loadForm);
</script>

<style scoped>
.fee-form-page {
  min-height: 100%;
  padding: 32px 0 84px;
}

.fee-form-card {
  width: min(1654px, calc(100% - 96px));
  min-height: 688px;
  margin: 0 auto;
  padding: 35px 34px 48px;
  background: #ffffff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.08);
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
  flex-wrap: wrap;
  justify-content: flex-end;
  min-width: 0;
}

.header-actions button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 48px;
  min-width: 120px;
  padding: 0 24px;
  border-radius: 5px;
  border: 1px solid #636be8;
  background: #ffffff;
  color: #636be8;
  font-size: 19px;
  font-weight: 800;
  cursor: pointer;
  white-space: nowrap;
}

.header-actions .save-btn {
  background: #636be8;
  color: #ffffff;
}

.action-icon {
  width: 26px;
  height: 26px;
}

.fee-form {
  margin-top: 42px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(240px, 362px));
  column-gap: 46px;
  row-gap: 38px;
  align-items: end;
}

.form-grid.addon {
  grid-template-columns: repeat(4, minmax(220px, 1fr));
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
  min-width: 0;
}

.field select {
  appearance: none;
  background: #ffffff
    linear-gradient(45deg, transparent 50%, #d5d5d5 50%) right 22px center / 9px 9px no-repeat;
}

.radio-field {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 28px;
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

@media (max-width: 1500px) {
  .fee-form-card {
    width: calc(100% - 96px);
  }

  .form-grid,
  .form-grid.addon {
    grid-template-columns: repeat(3, minmax(220px, 1fr));
  }
}

@media (max-width: 1100px) {
  .fee-form-card {
    width: calc(100% - 32px);
    padding: 28px 22px 42px;
  }

  .form-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .header-actions button {
    height: 42px;
    padding: 0 18px;
    font-size: 16px;
  }

  .form-grid,
  .form-grid.addon {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}

@media (max-width: 720px) {
  .form-grid,
  .form-grid.addon {
    grid-template-columns: 1fr;
  }
}
</style>
