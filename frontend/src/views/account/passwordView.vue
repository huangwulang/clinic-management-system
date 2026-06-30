<template>
  <div class="page-shell">
    <div class="page-card">
      <div class="toolbar">
        <el-button type="primary" :loading="saving" @click="savePassword">
          确定修改
        </el-button>
      </div>

      <div class="password-form-wrap">
        <el-form
          :model="form"
          label-position="top"
          class="password-form"
        >
          <el-form-item label="原密码*">
            <el-input
              v-model="form.oldPassword"
              type="password"
              show-password
              placeholder="请输入原密码"
            />
          </el-form-item>

          <el-form-item label="新密码*">
            <el-input
              v-model="form.newPassword"
              type="password"
              show-password
              placeholder="请输入 6-12 位新密码，包含数字和字母"
            />
          </el-form-item>

          <el-form-item label="再次输入*">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { accountApi } from "@/api";

const saving = ref(false);
const form = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});


const savePassword = async () => {
  if (!form.oldPassword || !form.newPassword || form.newPassword !== form.confirmPassword) {
    ElMessage.warning("请填写原密码，并确保两次新密码一致");
    return;
  }
  saving.value = true;
  try {
    await accountApi.updatePassword({ ...form });
    form.oldPassword = "";
    form.newPassword = "";
    form.confirmPassword = "";
    ElMessage.success("密码修改成功");
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.page-shell {
  padding: 24px;
}

.page-card {
  background: #fff;
  border-radius: 18px;
  padding: 28px 32px 34px;
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.06);
  min-height: 640px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 44px;
}

.password-form-wrap {
  width: 370px;
  margin: 80px auto 0;
}

.password-form :deep(.el-form-item) {
  margin-bottom: 26px;
}

.password-form :deep(.el-form-item__label) {
  color: #1f2937;
  font-size: 16px;
}

.password-form :deep(.el-input__wrapper) {
  min-height: 58px;
}
</style>
