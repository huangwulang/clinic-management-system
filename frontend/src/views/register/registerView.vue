<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-form-wrap">
        <h2 class="register-title">&#27880;&#20876;&#35786;&#25152;&#31649;&#29702;&#31995;&#32479;&#36134;&#21495;</h2>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          class="register-form"
          @keyup.enter.native="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model.trim="registerForm.username"
              placeholder="&#35831;&#36755;&#20837;&#29992;&#25143;&#21517;"
              prefix-icon="el-icon-user"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              placeholder="&#35831;&#36755;&#20837;&#23494;&#30721;"
              prefix-icon="el-icon-lock"
              type="password"
              show-password
              clearable
            />
          </el-form-item>

          <el-button
            type="primary"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            &#27880;&#20876;
          </el-button>

          <div class="login-link">
            <span>&#24050;&#26377;&#36134;&#21495;&#65292;</span>
            <a href="#" @click.prevent="handleLogin">&#21435;&#30331;&#24405;</a>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { authApi } from "@/api";

export default {
  name: "RegisterView",
  data() {
    return {
      loading: false,
      registerForm: {
        username: "",
        password: "",
      },
      rules: {
        username: [{ required: true, message: "\u8bf7\u8f93\u5165\u7528\u6237\u540d", trigger: "blur" }],
        password: [
          { required: true, message: "\u8bf7\u8f93\u5165\u5bc6\u7801", trigger: "blur" },
          { min: 6, message: "\u5bc6\u7801\u81f3\u5c11 6 \u4f4d", trigger: "blur" },
        ],
      },
    };
  },
  methods: {
    handleRegister() {
      this.$refs.registerFormRef.validate(async (valid) => {
        if (!valid) {
          return;
        }

        this.loading = true;
        try {
          await authApi.register({ ...this.registerForm });
          this.$message.success("\u6ce8\u518c\u6210\u529f\uff0c\u8bf7\u767b\u5f55");
          this.$router.push("/login");
        } catch (error) {
          this.$message.error("\u6ce8\u518c\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5");
        } finally {
          this.loading = false;
        }
      });
    },
    handleLogin() {
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #6870e8;
}

.register-card {
  width: min(980px, 100%);
  min-height: 440px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 10px 28px rgba(18, 24, 72, 0.24);
}

.register-form-wrap {
  width: 390px;
  max-width: 100%;
}

.register-title {
  margin: 0 0 32px;
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.3;
  color: #262b35;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.register-form :deep(.el-input__wrapper) {
  min-height: 46px;
  background: #f1f3ff;
  box-shadow: none;
}

.register-btn {
  width: 100%;
  height: 46px;
  margin-top: 2px;
  background: #6870e8;
  border-color: #6870e8;
}

.login-link {
  margin-top: 16px;
  text-align: center;
  color: #6870e8;
}

.login-link a {
  color: #6870e8;
  text-decoration: none;
}

@media (max-width: 640px) {
  .register-card {
    min-height: auto;
    padding: 40px 24px;
  }
}
</style>
