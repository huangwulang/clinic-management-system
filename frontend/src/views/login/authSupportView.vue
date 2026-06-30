<template>
  <main class="support-page">
    <section class="support-card">
      <div class="support-visual">
        <img src="@/assets/login.png" alt="诊所管理系统" />
        <h1>诊所管理系统</h1>
        <p>让接诊、收费、药品和患者管理更清晰。</p>
      </div>

      <div class="support-panel">
        <button class="back-link" type="button" @click="router.push('/login')">返回登录</button>

        <template v-if="isForgot">
          <h2>找回密码</h2>
          <p class="description">验证绑定手机号后设置新的登录密码。</p>
          <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" label-position="top">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="forgotForm.phone" maxlength="11" placeholder="请输入绑定手机号" />
            </el-form-item>
            <el-form-item label="短信验证码" prop="code">
              <div class="code-row">
                <el-input v-model.trim="forgotForm.code" maxlength="6" placeholder="请输入验证码" />
                <el-button :disabled="countdown > 0" @click="sendCode">
                  {{ countdown > 0 ? `${countdown}s 后重发` : "发送验证码" }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="新密码" prop="password">
              <el-input v-model="forgotForm.password" type="password" show-password placeholder="8-20 位密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="forgotForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
            </el-form-item>
            <el-button class="submit-btn" type="primary" :loading="submitting" @click="submitForgot">
              重置密码
            </el-button>
          </el-form>
        </template>

        <template v-else>
          <h2>申请试用</h2>
          <p class="description">提交诊所信息，我们会尽快联系您开通试用。</p>
          <el-result
            v-if="trialSubmitted"
            icon="success"
            title="申请已提交"
            sub-title="当前状态：审核中，请保持电话畅通。"
          >
            <template #extra>
              <el-button type="primary" @click="router.push('/login')">返回登录</el-button>
            </template>
          </el-result>
          <el-form v-else ref="trialFormRef" :model="trialForm" :rules="trialRules" label-position="top">
            <div class="form-grid">
              <el-form-item label="诊所名称" prop="clinicName">
                <el-input v-model.trim="trialForm.clinicName" placeholder="请输入诊所名称" />
              </el-form-item>
              <el-form-item label="联系人" prop="contactName">
                <el-input v-model.trim="trialForm.contactName" placeholder="请输入联系人" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model.trim="trialForm.phone" maxlength="11" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="诊所规模" prop="scale">
                <el-select v-model="trialForm.scale" placeholder="请选择规模">
                  <el-option label="1-5 人" value="1-5" />
                  <el-option label="6-20 人" value="6-20" />
                  <el-option label="21-50 人" value="21-50" />
                  <el-option label="50 人以上" value="50+" />
                </el-select>
              </el-form-item>
            </div>
            <el-form-item label="所在地区" prop="region">
              <el-input v-model.trim="trialForm.region" placeholder="例如：广东省深圳市南山区" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="trialForm.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
            </el-form-item>
            <el-button class="submit-btn" type="primary" :loading="submitting" @click="submitTrial">
              提交申请
            </el-button>
          </el-form>
        </template>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { authApi } from "@/api";

const route = useRoute();
const router = useRouter();
const isForgot = computed(() => route.path === "/forgot-password");
const submitting = ref(false);
const countdown = ref(0);
const trialSubmitted = ref(false);
const forgotFormRef = ref<FormInstance>();
const trialFormRef = ref<FormInstance>();
let timer: number | undefined;

const forgotForm = reactive({
  phone: "",
  code: "",
  password: "",
  confirmPassword: "",
});

const trialForm = reactive({
  clinicName: "",
  contactName: "",
  phone: "",
  region: "",
  scale: "",
  remark: "",
});

const phoneRule = { pattern: /^1\d{10}$/, message: "请输入正确的 11 位手机号", trigger: "blur" };
const forgotRules: FormRules = {
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }, phoneRule],
  code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
  password: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 8, max: 20, message: "密码长度应为 8-20 位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (_rule, value, callback) => {
        if (value !== forgotForm.password) callback(new Error("两次输入的密码不一致"));
        else callback();
      },
      trigger: "blur",
    },
  ],
};

const trialRules: FormRules = {
  clinicName: [{ required: true, message: "请输入诊所名称", trigger: "blur" }],
  contactName: [{ required: true, message: "请输入联系人", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }, phoneRule],
  region: [{ required: true, message: "请输入所在地区", trigger: "blur" }],
  scale: [{ required: true, message: "请选择诊所规模", trigger: "change" }],
};

const sendCode = async () => {
  if (!/^1\d{10}$/.test(forgotForm.phone)) {
    ElMessage.warning("请先输入正确的手机号");
    return;
  }
  try {
    await authApi.sendResetCode({ phone: forgotForm.phone });
  } catch {
    return;
  }
  countdown.value = 60;
  timer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0 && timer) {
      window.clearInterval(timer);
      timer = undefined;
    }
  }, 1000);
  ElMessage.success("验证码已发送");
};

const submitForgot = async () => {
  if (!(await forgotFormRef.value?.validate().catch(() => false))) return;
  submitting.value = true;
  try {
    await authApi.resetPassword({ ...forgotForm });
    ElMessage.success("密码重置成功，请重新登录");
    await router.push("/login");
  } finally {
    submitting.value = false;
  }
};

const submitTrial = async () => {
  if (!(await trialFormRef.value?.validate().catch(() => false))) return;
  submitting.value = true;
  try {
    await authApi.applyTrial({ ...trialForm });
    trialSubmitted.value = true;
  } finally {
    submitting.value = false;
  }
};

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer);
});
</script>

<style scoped>
.support-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  box-sizing: border-box;
  background: linear-gradient(135deg, #eef3ff 0%, #f7f9ff 48%, #eef8ff 100%);
}

.support-card {
  width: min(1060px, 100%);
  min-height: 620px;
  display: grid;
  grid-template-columns: 42% 58%;
  overflow: hidden;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 24px 70px rgba(44, 63, 120, 0.16);
}

.support-visual {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 56px;
  color: #fff;
  background: linear-gradient(150deg, #5662df, #7780ef);
}

.support-visual img {
  width: 100%;
  max-height: 280px;
  object-fit: contain;
  filter: drop-shadow(0 14px 24px rgba(31, 37, 112, 0.2));
}

.support-visual h1 {
  margin: 34px 0 10px;
  font-size: 30px;
}

.support-visual p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.8;
}

.support-panel {
  position: relative;
  padding: 54px 64px;
}

.back-link {
  position: absolute;
  top: 24px;
  right: 32px;
  border: 0;
  background: transparent;
  color: #636be8;
  cursor: pointer;
}

h2 {
  margin: 18px 0 8px;
  color: #172033;
  font-size: 28px;
}

.description {
  margin: 0 0 28px;
  color: #8a92a3;
}

.code-row {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 126px;
  gap: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 18px;
}

.form-grid :deep(.el-select) {
  width: 100%;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 8px;
}

@media (max-width: 800px) {
  .support-card {
    grid-template-columns: 1fr;
  }

  .support-visual {
    display: none;
  }

  .support-panel {
    padding: 58px 28px 36px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
