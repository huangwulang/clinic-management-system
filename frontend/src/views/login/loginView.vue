<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-media">
        <img src="@/assets/login.png" alt="诊所管理系统登录">
      </div>

      <div class="auth-panel">
        <h2 class="auth-title">登录诊所管理系统</h2>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          class="auth-form"
          @keyup.enter.native="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model.trim="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              type="password"
              show-password
              clearable
            />
          </el-form-item>

          <el-button
            type="primary"
            class="auth-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>

          <div class="auth-options">
            <el-checkbox v-model="rememberAccount">记住账号</el-checkbox>
            <router-link to="/forgot-password">忘记密码？</router-link>
          </div>

          <div class="auth-link">
            <span>还没有账号？</span>
            <router-link to="/trial/apply">申请试用</router-link>
            <router-link to="/register">注册账号</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { authApi } from "@/api";
import { clearCurrentUser, storeCurrentUser } from "@/utils/permissions";

export default {
  name: "LoginView",
  data() {
    return {
      loading: false,
      rememberAccount: true,
      loginForm: {
        username: "",
        password: "",
      },
      rules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
    };
  },
  created() {
    const remembered = localStorage.getItem("rememberedAccount");
    if (remembered) {
      this.loginForm.username = remembered;
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginFormRef.validate(async (valid) => {
        if (!valid) {
          return;
        }

        this.loading = true;
        try {
          const response = await authApi.login({ ...this.loginForm });
          const token = response?.data?.token || response?.data?.data?.token;
          if (!token) {
            throw new Error("登录接口未返回令牌");
          }
          localStorage.setItem("token", token);
          const meResponse = await authApi.me();
          const currentUser = meResponse?.data || response?.data?.user || response?.data?.data?.user;
          storeCurrentUser(currentUser);
          if (this.rememberAccount) {
            localStorage.setItem("rememberedAccount", this.loginForm.username);
          } else {
            localStorage.removeItem("rememberedAccount");
          }
          this.$message.success("登录成功");
          const redirect = typeof this.$route.query.redirect === "string"
            ? this.$route.query.redirect
            : "/dashboard";
          this.$router.push(redirect);
        } catch (error) {
          localStorage.removeItem("token");
          sessionStorage.removeItem("token");
          clearCurrentUser();
          this.$message.error("登录失败，请检查用户名或密码");
        } finally {
          this.loading = false;
        }
      });
    },
    handleRegister() {
      this.$router.push("/register");
    },
  },
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f2f6ff;
}

.auth-card {
  width: min(920px, 100%);
  display: grid;
  grid-template-columns: 1fr 420px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 12px 36px rgba(35, 55, 110, 0.12);
  overflow: hidden;
}

.auth-media {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: #eef5ff;
}

.auth-media img {
  max-width: 100%;
  max-height: 360px;
  object-fit: contain;
}

.auth-panel {
  padding: 56px 44px;
}

.auth-title {
  margin: 0 0 32px;
  font-size: 24px;
  line-height: 1.3;
  color: #1f2d3d;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.auth-btn {
  width: 100%;
  height: 40px;
}

.auth-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -4px 0 18px;
}

.auth-options a,
.auth-link a {
  color: #636be8;
  text-decoration: none;
}

.auth-link {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 18px;
  text-align: center;
  color: #606266;
}

@media (max-width: 760px) {
  .auth-card {
    grid-template-columns: 1fr;
  }

  .auth-media {
    display: none;
  }

  .auth-panel {
    padding: 36px 24px;
  }
}
</style>
