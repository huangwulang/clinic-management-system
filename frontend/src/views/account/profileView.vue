<template>
  <div class="staff-page">
    <section class="form-card staff-form-card">
      <header class="form-header">
        <h2>账号资料</h2>
        <div class="form-actions">
          <button
            type="button"
            class="save-btn"
            :disabled="saving"
            @click="saveProfile"
          >
            <el-icon><Document /></el-icon>
            <span>{{ saving ? "保存中" : "保存" }}</span>
          </button>
        </div>
      </header>

      <form class="staff-form" @submit.prevent="saveProfile">
        <label class="form-field required">
          <span>员工编号</span>
          <input v-model.trim="form.username" placeholder="请输入员工编号" />
        </label>
        <label class="form-field required">
          <span>员工姓名</span>
          <input v-model.trim="form.name" placeholder="请输入员工姓名" />
        </label>
        <label class="form-field age-field required">
          <span>员工年龄</span>
          <div class="input-with-unit">
            <input v-model="form.age" readonly />
            <select disabled>
              <option>岁</option>
            </select>
          </div>
        </label>

        <label class="form-field required">
          <span>性别</span>
          <select v-model="form.gender" disabled>
            <option value="">请选择</option>
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </label>
        <label class="form-field">
          <span>手机号码</span>
          <input v-model.trim="form.phone" placeholder="请输入手机号码" />
        </label>
        <label class="form-field">
          <span>电子邮箱</span>
          <input v-model="form.email" readonly />
        </label>
        <label class="form-field">
          <span>证件号码</span>
          <input v-model="form.idCard" readonly />
        </label>

        <label class="form-field">
          <span>职位</span>
          <input v-model="form.positionName" readonly />
        </label>
        <label class="form-field address-select">
          <span>地址</span>
          <select v-model="form.region" disabled>
            <option value="">请选择</option>
            <option>广东省/深圳市</option>
          </select>
        </label>
        <label class="form-field address-detail">
          <span>&nbsp;</span>
          <input v-model="form.address" readonly />
        </label>

        <label class="form-field required">
          <span>所属科室</span>
          <select v-model="form.departmentName" disabled>
            <option value="">请选择</option>
            <option
              v-for="department in departmentOptions"
              :key="department"
              :value="department"
            >
              {{ department }}
            </option>
          </select>
        </label>
        <label class="form-field required">
          <span>角色</span>
          <select v-model="form.roleName">
            <option value="">请选择</option>
            <option
              v-for="role in roleOptions"
              :key="role"
              :value="role"
            >
              {{ role }}
            </option>
          </select>
        </label>
        <label class="form-field">
          <span>密码</span>
          <input value="请在修改密码页面维护" readonly />
        </label>
        <div class="status-line">
          <span>员工状态:</span>
          <button
            type="button"
            :class="['switch', { off: !form.enabled }]"
            @click="form.enabled = !form.enabled"
          >
            <span></span>
          </button>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { Document } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { accountApi, departmentApi, employeeApi, roleApi } from "@/api";
import { storeCurrentUser } from "@/utils/permissions";

type AnyRecord = Record<string, any>;

const saving = ref(false);
const staffDetail = ref<AnyRecord | null>(null);
const departmentRows = ref<AnyRecord[]>([]);
const roleRows = ref<AnyRecord[]>([]);

const form = reactive({
  id: "",
  username: "",
  name: "",
  phone: "",
  roleName: "",
  enabled: true,
  gender: "",
  age: "",
  email: "",
  idCard: "",
  positionName: "",
  region: "",
  address: "",
  departmentName: "",
});

const departmentOptions = computed(() =>
  Array.from(new Set(departmentRows.value.map((item) => item.name || item.departmentName).filter(Boolean))),
);

const roleOptions = computed(() => {
  const names = roleRows.value.map((item) => item.name || item.roleName).filter(Boolean);
  return Array.from(new Set([form.roleName, ...names].filter(Boolean)));
});

const firstString = (...values: unknown[]) => values.map((value) => String(value ?? "").trim()).find(Boolean) || "";

const toEnabled = (value: unknown) => value !== false && value !== 0 && String(value).toUpperCase() !== "FALSE";

const findMatchedStaff = async (profile: AnyRecord) => {
  const response: any = await employeeApi.page({ page: 1, size: 200 });
  const records: AnyRecord[] = response?.data?.records || [];
  const profilePhone = firstString(profile.phone);
  const profileName = firstString(profile.name, profile.realName, profile.employeeName);
  const profileRole = firstString(profile.roleName, profile.role);
  return records.find((item) => profilePhone && String(item.phone || "") === profilePhone)
    || records.find((item) => profileName && String(item.name || item.employeeName || "") === profileName)
    || records.find((item) => profileRole && String(item.roleName || item.role || "") === profileRole)
    || null;
};

const loadOptions = async () => {
  const [departmentResponse, roleResponse]: any[] = await Promise.all([
    departmentApi.page({ page: 1, size: 100 }),
    roleApi.page({ page: 1, size: 100 }),
  ]);
  departmentRows.value = departmentResponse?.data?.records || [];
  roleRows.value = roleResponse?.data?.records || [];
};

const fillForm = (profile: AnyRecord, staff: AnyRecord | null) => {
  staffDetail.value = staff;
  form.id = String(profile.id || "");
  form.username = firstString(profile.username);
  form.name = firstString(profile.name, profile.realName, profile.employeeName, staff?.name);
  form.phone = firstString(profile.phone, staff?.phone);
  form.roleName = firstString(profile.roleName, profile.role, staff?.roleName, staff?.role);
  form.enabled = toEnabled(profile.enabled);
  form.gender = firstString(staff?.gender);
  form.age = firstString(staff?.age);
  form.email = firstString(staff?.email);
  form.idCard = firstString(staff?.idCard);
  form.positionName = firstString(staff?.positionName, staff?.position);
  form.region = firstString(staff?.region, staff?.city, "广东省/深圳市");
  form.address = firstString(staff?.address);
  form.departmentName = firstString(staff?.departmentName, staff?.department);
};

const loadProfile = async () => {
  const profileResponse: any = await accountApi.profile();
  const profile = profileResponse?.data || {};
  const staff = await findMatchedStaff(profile);
  fillForm(profile, staff);
};

onMounted(async () => {
  await Promise.all([loadOptions(), loadProfile()]);
});

const saveProfile = async () => {
  if (!form.username.trim()) {
    ElMessage.warning("请输入员工编号");
    return;
  }
  if (!form.name.trim()) {
    ElMessage.warning("请输入员工姓名");
    return;
  }
  if (!form.roleName.trim()) {
    ElMessage.warning("请选择角色");
    return;
  }

  saving.value = true;
  try {
    const payload = {
      username: form.username.trim(),
      name: form.name.trim(),
      phone: form.phone.trim(),
      roleName: form.roleName,
      enabled: form.enabled,
    };
    const response: any = await accountApi.updateProfile(payload);
    const savedUser = { ...(response?.data || {}), ...payload };
    storeCurrentUser(savedUser);
    fillForm(savedUser, staffDetail.value);
    ElMessage.success("账号资料保存成功");
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.staff-page {
  min-height: 100%;
  padding: 16px 0 58px;
  overflow-x: hidden;
}

.form-card {
  width: min(1660px, calc(100% - 64px));
  min-height: 798px;
  margin: 0 auto;
  padding: 31px 34px 54px;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
  box-sizing: border-box;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-header h2 {
  position: relative;
  margin: 0;
  padding-left: 15px;
  color: #101828;
  font-size: 24px;
  font-weight: 700;
  line-height: 46px;
}

.form-header h2::before {
  content: "";
  position: absolute;
  left: 0;
  top: 15px;
  width: 6px;
  height: 25px;
  background: #636be8;
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 22px;
}

.save-btn {
  display: inline-flex;
  width: 148px;
  height: 46px;
  align-items: center;
  justify-content: center;
  gap: 11px;
  border: 1px solid #636be8;
  border-radius: 5px;
  background: #636be8;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.save-btn:disabled {
  cursor: not-allowed;
  opacity: 0.68;
}

.save-btn :deep(.el-icon) {
  font-size: 23px;
}

.staff-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 363px));
  column-gap: 46px;
  row-gap: 29px;
  margin-top: 35px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 11px;
  color: #101828;
  font-size: 16px;
}

.form-field.required span::after {
  content: "*";
  color: #ff1d1d;
}

.form-field input,
.form-field select {
  width: 100%;
  height: 58px;
  padding: 0 18px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  background: #fff;
  color: #000;
  font-size: 18px;
  box-sizing: border-box;
}

.form-field input[readonly],
.form-field select:disabled {
  background: #f3f3f3;
  color: #667085;
}

.form-field input::placeholder {
  color: #c5c5c5;
}

.age-field .input-with-unit {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 105px;
}

.age-field input,
.age-field select {
  border-radius: 4px 0 0 4px;
}

.age-field select {
  border-left: 0;
  border-radius: 0 4px 4px 0;
}

.address-select {
  grid-column: auto;
}

.address-detail {
  grid-column: span 2;
}

.status-line {
  display: flex;
  align-items: center;
  gap: 26px;
  padding-top: 38px;
  color: #101828;
  font-size: 18px;
}

.switch {
  display: inline-flex;
  width: 51px;
  height: 30px;
  padding: 2px;
  align-items: center;
  border: 0;
  border-radius: 999px;
  background: #35d07f;
  box-sizing: border-box;
  cursor: pointer;
}

.switch span {
  display: block;
  width: 26px;
  height: 26px;
  margin-left: auto;
  border-radius: 50%;
  background: #fff;
}

.switch.off {
  background: #b8beca;
}

.switch.off span {
  margin-left: 0;
}

@media (max-width: 1280px) {
  .staff-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .address-select,
  .address-detail {
    grid-column: auto;
  }
}
</style>
