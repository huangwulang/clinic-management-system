<template>
  <div class="dashboard-container">
    <aside
      class="sidebar"
      @mouseleave="handleSidebarLeave"
    >
      <button
        type="button"
        class="sidebar-brand"
        @click="handleMenuSelect('/dashboard')"
      >
        <span class="brand-mark">
          <span class="brand-heart" />
          <span class="brand-line">
            <span class="brand-line-segment segment-left" />
            <span class="brand-line-segment segment-rise" />
            <span class="brand-line-segment segment-peak" />
            <span class="brand-line-segment segment-fall" />
            <span class="brand-line-segment segment-right" />
          </span>
        </span>
      </button>

      <button
        v-for="item in menuList"
        :key="item.index"
        type="button"
        :class="['menu-item', { 'is-active': activeIndex === item.path }]"
        @mouseenter="handleMouseEnter($event, item)"
        @click="handleMenuSelect(item.path)"
      >
        <span
          v-if="activeIndex === item.path"
          class="menu-active-bar"
        />
        <el-icon
          v-if="item.iconComponent"
          class="menu-icon menu-icon-component"
        >
          <component :is="item.iconComponent" />
        </el-icon>
        <img
          v-else
          :src="item.icon"
          :alt="item.title"
          class="menu-icon"
        >
      </button>

      <div
        v-if="hoveredMenu"
        class="submenu-popup"
        :style="submenuStyle"
        @mouseenter="handlePopupEnter"
        @mouseleave="handlePopupLeave"
      >
        <button
          v-for="sub in hoveredMenu.subMenu"
          :key="sub.path"
          type="button"
          class="submenu-item"
          @click="handleSubClick(sub)"
        >
          {{ sub.title }}
        </button>
      </div>
    </aside>

    <el-container class="layout-body">
      <el-header class="header">
        <div class="header-left">{{ currentTitle }}</div>
        <div
          class="header-right"
          @mouseenter="showDropdown = true"
          @mouseleave="showDropdown = false"
        >
          <img
            :src="avatarUrl"
            alt="avatar"
            class="avatar"
          >
          <span class="user-name">{{ userDisplayName }}</span>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            class="avatar-input"
            @change="handleAvatarChange"
          >
          <div
            v-if="showDropdown"
            class="dropdown-list"
          >
            <button
              v-for="item in dropdownItems"
              :key="item.label"
              type="button"
              class="dropdown-item"
              @click="handleDropdownClick(item)"
            >
              {{ item.label }}
            </button>
          </div>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="page-viewport">
          <router-view :key="$route.fullPath" />
        </div>
      </el-main>
    </el-container>

    <el-dialog
      v-model="showLogoutDialog"
      width="500px"
      align-center
      :show-close="false"
      class="logout-dialog"
    >
      <template #header>
        <div class="logout-dialog-header">
          <span>退出确认</span>
          <button
            type="button"
            class="logout-close"
            @click="showLogoutDialog = false"
          >
            ×
          </button>
        </div>
      </template>

      <div class="logout-dialog-body">
        <el-icon class="logout-warning-icon">
          <WarningFilled />
        </el-icon>
        <span>确定要退出系统吗?</span>
      </div>

      <template #footer>
        <div class="logout-dialog-footer">
          <el-button @click="showLogoutDialog = false">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="confirmLogout"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Calendar, WarningFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { authApi } from "@/api";
import {
  clearCurrentUser,
  getCurrentPermissions,
  getStoredCurrentUser,
  hasPermissionForPath,
  NO_PERMISSION_MESSAGE,
  storeCurrentUser,
} from "@/utils/permissions";
import defaultAvatar from "@/assets/avatar.png";
import icon2 from "@/assets/icon/icon2.png";
import icon3 from "@/assets/icon/icon3.png";
import icon4 from "@/assets/icon/icon4.png";
import icon5 from "@/assets/icon/icon5.png";
import icon6 from "@/assets/icon/icon6.png";
import icon7 from "@/assets/icon/icon7.png";
import icon8 from "@/assets/icon/icon8.png";
import icon9 from "@/assets/icon/icon9.png";
import icon10 from "@/assets/icon/icon10.png";
import icon11 from "@/assets/icon/icon11.png";

export default {
  name: "DashboardLayout",
  components: {
    WarningFilled,
  },
  data() {
    return {
      activeIndex: "",
      currentTitle: "经营概况",
      showDropdown: false,
      showLogoutDialog: false,
      currentUser: null,
      avatarUrl: defaultAvatar,
      hoveredMenu: null,
      submenuStyle: {
        top: "0px",
        left: "84px",
      },
      hoverTimer: null,
      dropdownItems: [
        { label: "账号资料", path: "/account/profile" },
        { label: "更换头像", action: "avatar" },
        { label: "修改密码", path: "/account/password" },
        { label: "消息通知", path: "/account/messages" },
        { label: "退出系统", action: "logout" },
      ],
      menuList: [
        {
          index: "1",
          title: "经营概况",
          iconComponent: Calendar,
          path: "/dashboard",
          subMenu: [{ title: "经营概况", path: "/dashboard" }],
        },
        {
          index: "2",
          title: "工作台",
          icon: icon2,
          path: "/workbench",
          subMenu: [{ title: "工作台", path: "/workbench" }],
        },
        {
          index: "3",
          title: "新开接诊",
          icon: icon3,
          path: "/consultation/new",
          subMenu: [{ title: "新开接诊", path: "/consultation/new" }],
        },
        {
          index: "4",
          title: "挂号管理",
          icon: icon4,
          path: "/registration/create",
          subMenu: [
            { title: "新增挂号", path: "/registration/create" },
            { title: "挂号记录", path: "/registration/list" },
          ],
        },
        {
          index: "5",
          title: "药品零售",
          icon: icon5,
          path: "/retail",
          subMenu: [{ title: "药品零售", path: "/retail" }],
        },
        {
          index: "6",
          title: "收费管理",
          icon: icon6,
          path: "/charge/orders",
          subMenu: [{ title: "收费管理", path: "/charge/orders" }],
        },
        {
          index: "7",
          title: "患者管理",
          icon: icon7,
          path: "/patients",
          subMenu: [{ title: "患者管理", path: "/patients" }],
        },
        {
          index: "8",
          title: "药品管理",
          icon: icon8,
          path: "/drugs",
          subMenu: [
            { title: "药品信息维护", path: "/drugs" },
            { title: "入库管理", path: "/drugs/stock-in" },
            { title: "出库管理", path: "/drugs/stock-out" },
            { title: "库存管理", path: "/drugs/inventory" },
            { title: "库存盘点", path: "/drugs/stock-check" },
            { title: "药品调价", path: "/drugs/price-adjust" },
          ],
        },
        {
          index: "9",
          title: "会员管理",
            icon: icon9,
            path: "/members",
            subMenu: [
              { title: "会员管理", path: "/members" },
            ],
          },
        {
          index: "10",
          title: "统计报表",
          icon: icon10,
          path: "/statistics",
          subMenu: [
            { title: "收费统计", path: "/statistics/charge/" },
            { title: "患者统计", path: "/statistics/patients/log" },
            { title: "检查项目统计", path: "/statistics/check-projects" },
            { title: "药品统计", path: "/statistics/drugs/stockIn" },
          ],
        },
        {
          index: "11",
          title: "系统设置",
          icon: icon11,
          path: "/system/clinic",
          subMenu: [
            { title: "诊所信息维护", path: "/system/clinic" },
            { title: "字典表维护", path: "/system/dictionaries/medical" },
            { title: "员工管理", path: "/system/staff" },
            { title: "检查项目设置", path: "/system/check-projects" },
            { title: "供应商管理", path: "/system/suppliers" },
            { title: "模板维护", path: "/system/templates/medical" },
            { title: "费用设置", path: "/system/fees/addon" },
            { title: "基础设置", path: "/system/basic" },
          ],
        },
      ],
    };
  },
  created() {
    this.syncCurrentUser();
    this.syncActiveMenu(this.$route.path);
  },
  computed: {
    userDisplayName() {
      const user = this.currentUser || {};
      return user.name || user.realName || user.employeeName || user.username || localStorage.getItem("rememberedAccount") || "当前用户";
    },
    avatarStorageKey() {
      const user = this.currentUser || {};
      const identity = user.id || user.userId || user.employeeId || user.staffId || user.username || "default";
      return `clinic:user-avatar:${identity}`;
    },
  },
  watch: {
    $route(to) {
      this.syncCurrentUser();
      this.syncActiveMenu(to.path);
      this.clearHoveredMenu();
    },
  },
  methods: {
    syncCurrentUser() {
      this.currentUser = getStoredCurrentUser() || {};
      this.avatarUrl = this.currentUser.avatar || localStorage.getItem(this.avatarStorageKey) || defaultAvatar;
    },
    async handleMenuSelect(path) {
      if (!path) return;

      this.clearHoveredMenu();

      if (!this.canOpenPath(path)) return;

      if (this.$route.path === path) {
        this.syncActiveMenu(path);
        return;
      }

      try {
        await this.$router.push(path);
      } catch (error) {
        // Ignore duplicated navigation errors from rapid clicks.
      }
    },
    handleSubClick(subItem) {
      if (!subItem?.path) return;
      this.handleMenuSelect(subItem.path);
    },
    canOpenPath(path) {
      if (hasPermissionForPath(path, getCurrentPermissions())) return true;
      ElMessage.warning(NO_PERMISSION_MESSAGE);
      return false;
    },
    async handleDropdownClick(item) {
      this.showDropdown = false;

      if (item.action === "logout") {
        this.showLogoutDialog = true;
        return;
      }

      if (item.action === "avatar") {
        this.$refs.avatarInput?.click();
        return;
      }

      if (item.path) {
        await this.handleMenuSelect(item.path);
      }
    },
    handleAvatarChange(event) {
      const file = event.target.files?.[0];
      event.target.value = "";
      if (!file) return;
      if (!file.type.startsWith("image/")) {
        ElMessage.warning("请选择图片文件");
        return;
      }

      this.readAvatarFile(file)
        .then((dataUrl) => {
          this.avatarUrl = dataUrl;
          localStorage.setItem(this.avatarStorageKey, dataUrl);
          const nextUser = { ...(this.currentUser || {}), avatar: dataUrl };
          this.currentUser = nextUser;
          storeCurrentUser(nextUser);
          ElMessage.success("头像已更换");
        })
        .catch(() => {
          ElMessage.error("头像更换失败，请重新选择图片");
        });
    },
    readAvatarFile(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onerror = reject;
        reader.onload = () => {
          const image = new Image();
          image.onerror = reject;
          image.onload = () => {
            const size = 160;
            const canvas = document.createElement("canvas");
            const context = canvas.getContext("2d");
            if (!context) {
              reject(new Error("canvas context unavailable"));
              return;
            }

            const minSide = Math.min(image.width, image.height);
            const sx = (image.width - minSide) / 2;
            const sy = (image.height - minSide) / 2;
            canvas.width = size;
            canvas.height = size;
            context.drawImage(image, sx, sy, minSide, minSide, 0, 0, size, size);
            resolve(canvas.toDataURL("image/jpeg", 0.88));
          };
          image.src = String(reader.result || "");
        };
        reader.readAsDataURL(file);
      });
    },
    async confirmLogout() {
      this.showLogoutDialog = false;
      await authApi.logout();
      this.activeIndex = "";
      this.currentTitle = "登录";
      localStorage.removeItem("token");
      sessionStorage.removeItem("token");
      clearCurrentUser();
      await this.$router.push("/login");
    },
    syncActiveMenu(path) {
      const matchesPath = (menuPath) => path === menuPath || path.startsWith(`${menuPath}/`);
      const parentItem = this.menuList.find(
        (item) => matchesPath(item.path) || item.subMenu.some((sub) => matchesPath(sub.path)),
      );

      if (!parentItem) {
        this.currentTitle = this.$route.meta?.title || this.currentTitle;
        return;
      }

      const matchedSub = [...parentItem.subMenu]
        .sort((a, b) => b.path.length - a.path.length)
        .find((sub) => matchesPath(sub.path));

      this.activeIndex = parentItem.path;
      this.currentTitle = this.$route.meta?.title || matchedSub?.title || parentItem.title;
    },
    handleMouseEnter(event, item) {
      if (this.hoverTimer) {
        clearTimeout(this.hoverTimer);
        this.hoverTimer = null;
      }

      const currentTarget = event.currentTarget;
      if (!currentTarget) return;

      this.hoveredMenu = item;
      this.submenuStyle = {
        top: `${currentTarget.offsetTop}px`,
        left: "84px",
      };
    },
    handleSidebarLeave() {
      this.startHideTimer();
    },
    handlePopupEnter() {
      if (this.hoverTimer) {
        clearTimeout(this.hoverTimer);
        this.hoverTimer = null;
      }
    },
    handlePopupLeave() {
      this.startHideTimer();
    },
    startHideTimer() {
      if (this.hoverTimer) {
        clearTimeout(this.hoverTimer);
      }

      this.hoverTimer = setTimeout(() => {
        this.hoveredMenu = null;
        this.hoverTimer = null;
      }, 120);
    },
    clearHoveredMenu() {
      if (this.hoverTimer) {
        clearTimeout(this.hoverTimer);
        this.hoverTimer = null;
      }
      this.hoveredMenu = null;
    },
  },
};
</script>

<style scoped>
.dashboard-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fb;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 84px;
  height: 100vh;
  background: #08294a;
  z-index: 40;
  overflow: visible;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 84px;
  height: 80px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.brand-mark {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.14);
}

.brand-heart {
  position: absolute;
  width: 18px;
  height: 18px;
  background: #6a6cf6;
  transform: rotate(45deg);
  border-radius: 4px 4px 2px 2px;
}

.brand-heart::before,
.brand-heart::after {
  content: "";
  position: absolute;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #6a6cf6;
}

.brand-heart::before {
  top: -9px;
  left: 0;
}

.brand-heart::after {
  top: 0;
  left: -9px;
}

.brand-line {
  position: absolute;
  left: 9px;
  top: 22px;
  display: flex;
  align-items: center;
  gap: 1px;
  width: 34px;
  height: 10px;
}

.brand-line-segment {
  display: block;
  background: #ffffff;
  border-radius: 999px;
}

.segment-left,
.segment-right {
  width: 8px;
  height: 2px;
}

.segment-rise,
.segment-fall {
  width: 4px;
  height: 2px;
  transform-origin: center;
}

.segment-rise {
  transform: rotate(-58deg);
}

.segment-peak {
  width: 4px;
  height: 8px;
}

.segment-fall {
  transform: rotate(58deg);
}

.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 84px;
  height: 66px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.menu-item:hover,
.menu-item.is-active {
  background: #6a6cf6;
}

.menu-item:hover {
  transform: translateX(2px);
}

.menu-active-bar {
  position: absolute;
  left: 0;
  top: 14px;
  width: 3px;
  height: 38px;
  border-radius: 0 999px 999px 0;
  background: #ffffff;
}

.menu-icon {
  width: 28px;
  height: 28px;
  object-fit: contain;
  transition: transform 0.2s ease;
}

.menu-icon-component {
  color: #ffffff;
  font-size: 24px;
}

.menu-item:hover .menu-icon,
.menu-item.is-active .menu-icon {
  transform: scale(1.08);
}

.submenu-popup {
  position: absolute;
  min-width: 168px;
  padding: 10px 0;
  background: #08294a;
  border-radius: 0 12px 12px 0;
  box-shadow: 0 16px 30px rgb(8 41 74 / 24%);
  z-index: 60;
  animation: submenuFadeIn 0.16s ease;
}

.submenu-item {
  display: block;
  width: 100%;
  padding: 11px 18px;
  border: 0;
  background: transparent;
  color: #ffffff;
  text-align: left;
  font-size: 14px;
  line-height: 1.2;
  white-space: nowrap;
  cursor: pointer;
  transition: background-color 0.18s ease, padding-left 0.18s ease;
}

.submenu-item:hover {
  background: #6a6cf6;
  padding-left: 22px;
}

.layout-body {
  width: calc(100% - 84px);
  margin-left: 84px;
}

.header {
  position: fixed;
  top: 0;
  left: 84px;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: calc(100% - 84px);
  height: 70px;
  padding: 0 32px;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
  box-sizing: border-box;
}

.header-left {
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
}

.header-right {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 70px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background: #eef2ff;
}

.avatar-input {
  display: none;
}

.user-name {
  color: #1f2937;
  font-size: 15px;
}

.dropdown-list {
  position: absolute;
  top: 58px;
  right: 0;
  width: 148px;
  padding: 8px 0;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.12);
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  border: 0;
  background: transparent;
  text-align: left;
  color: #374151;
  cursor: pointer;
  transition: background-color 0.18s ease, color 0.18s ease;
}

.dropdown-item:hover {
  background: #f3f5ff;
  color: #6574f7;
}

.main-content {
  margin-top: 70px;
  min-height: calc(100vh - 70px);
  padding: 28px 28px 36px;
  box-sizing: border-box;
}

.page-viewport {
  width: min(100%, 1640px);
  margin: 0 auto;
}

.logout-dialog :deep(.el-dialog) {
  border-radius: 14px;
  padding: 6px 0 8px;
}

.logout-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 6px;
  font-size: 16px;
  color: #1f2937;
}

.logout-close {
  border: 0;
  background: transparent;
  color: #c5cad5;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.logout-dialog-body {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 4px 6px;
  font-size: 16px;
  color: #1f2937;
}

.logout-warning-icon {
  color: #f7b500;
  font-size: 28px;
}

.logout-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-right: 6px;
}

@keyframes submenuFadeIn {
  from {
    opacity: 0;
    transform: translateX(-6px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
