<template>
  <div class="page-shell">
    <div class="page-card">
      <div class="tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          :class="['tab-btn', { active: activeTab === tab.key }]"
          @click="setActiveTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="notice-list">
        <div
          v-for="item in pagedNotices"
          :key="item.id"
          class="notice-item"
          @click="openNotice(item)"
        >
          <div class="notice-left">
            <div class="notice-icon-wrap">
              <el-icon class="notice-icon">
                <Bell />
              </el-icon>
            </div>
            <div class="notice-content">
              <div class="notice-title-row">
                <span class="notice-title">{{ item.title }}</span>
                <span class="notice-time">{{ item.time }}</span>
              </div>
              <div class="notice-desc">
                {{ item.content }}
              </div>
            </div>
          </div>
          <div class="notice-right">
            <span
              v-if="item.unread"
              class="notice-dot"
            />
            <span class="notice-arrow">›</span>
          </div>
        </div>

        <el-empty
          v-if="!filteredNotices.length"
          description="暂无消息"
        />
      </div>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, total, jumper"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="filteredNotices.length"
          @current-change="currentPage = $event"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { Bell } from "@element-plus/icons-vue";
import type { RouteLocationRaw } from "vue-router";
import { useRouter } from "vue-router";
import { messageApi } from "@/api";
import { getStoredCurrentUser, hasPermissionForPath, normalizeUserPermissions } from "@/utils/permissions";

type NoticeTab = "business" | "system";

type NoticeItem = {
  id: string | number;
  title: string;
  time: string;
  content: string;
  unread: boolean;
  messageType: string;
  receiverId: string;
  payload: Record<string, any>;
  raw: Record<string, any>;
};

const router = useRouter();
const activeTab = ref<NoticeTab>("business");
const currentPage = ref(1);
const pageSize = 10;

const tabs: Array<{ key: NoticeTab; label: string }> = [
  { key: "business", label: "业务消息" },
  { key: "system", label: "系统消息" },
];

const notices = ref<NoticeItem[]>([]);

const businessTypes = new Set([
  "BUSINESS",
  "REGISTRATION",
  "REGISTER",
  "VISIT",
  "CONSULTATION",
  "MEDICAL",
  "TREATMENT",
  "CHARGE",
  "PAYMENT",
  "ORDER",
  "PRESCRIPTION",
  "DRUG",
  "INVENTORY",
  "STOCK",
  "STOCK_IN",
  "STOCK_OUT",
  "PATIENT",
  "MEMBER",
  "RETAIL",
  "CHECK",
  "CHECK_PROJECT",
]);

const systemTypes = new Set([
  "SYSTEM",
  "SYS",
  "NOTICE",
  "ANNOUNCEMENT",
  "ACCOUNT",
  "AUTH",
  "SECURITY",
  "LOGIN",
  "PASSWORD",
  "PERMISSION",
  "ROLE",
  "SETTING",
  "DICT",
  "OPERATION",
  "OPERATION_LOG",
]);

const businessKeywords = [
  "业务",
  "就诊",
  "接诊",
  "挂号",
  "收费",
  "支付",
  "退费",
  "处方",
  "药品",
  "入库",
  "出库",
  "库存",
  "盘点",
  "调价",
  "患者",
  "会员",
  "检查",
  "订单",
  "零售",
];

const systemKeywords = ["系统", "账号", "密码", "权限", "登录", "角色", "字典", "基础设置", "公告", "安全", "员工"];

const toUpperValue = (value: unknown) => String(value ?? "").trim().toUpperCase();

const parseMaybeJson = (value: unknown): Record<string, any> => {
  if (!value) return {};
  if (typeof value === "object") return value as Record<string, any>;
  const text = String(value).trim();
  if (!text.startsWith("{") && !text.startsWith("[")) return {};
  try {
    const parsed = JSON.parse(text);
    return parsed && typeof parsed === "object" && !Array.isArray(parsed) ? parsed : {};
  } catch {
    return {};
  }
};

const getCurrentUser = () => getStoredCurrentUser() || {};

const currentUserIds = () => {
  const user = getCurrentUser();
  return new Set(
    [user.id, user.userId, user.employeeId, user.staffId]
      .map((value) => String(value ?? "").trim())
      .filter(Boolean),
  );
};

const isReceiverMatched = (notice: NoticeItem) => {
  if (!notice.receiverId) return true;
  const ids = currentUserIds();
  return ids.size === 0 || ids.has(String(notice.receiverId));
};

const getNoticeText = (notice: NoticeItem) => `${notice.title} ${notice.content}`.trim();

const getNoticeKind = (notice: NoticeItem): NoticeTab => {
  const type = toUpperValue(notice.messageType);
  const text = getNoticeText(notice);
  if (businessTypes.has(type) || businessKeywords.some((keyword) => text.includes(keyword))) return "business";
  if (systemTypes.has(type) || systemKeywords.some((keyword) => text.includes(keyword))) return "system";
  return "business";
};

const normalizeNotice = (item: Record<string, any>): NoticeItem => {
  const payload = {
    ...parseMaybeJson(item.payload),
    ...parseMaybeJson(item.extra),
    ...parseMaybeJson(item.data),
    ...parseMaybeJson(item.content),
  };
  const messageType = item.messageType || item.message_type || item.type || payload.messageType || "";
  const readStatus = item.readStatus ?? item.read_status ?? item.readFlag ?? item.read ?? false;
  const content = payload.content || payload.message || item.content || "";
  return {
    id: item.id,
    title: item.title || (systemTypes.has(toUpperValue(messageType)) ? "系统消息" : "业务消息"),
    time: item.createdAt || item.created_at || item.time || "",
    content: typeof content === "string" ? content : JSON.stringify(content),
    unread: !(readStatus === true || readStatus === 1 || toUpperValue(readStatus) === "READ"),
    messageType: String(messageType || ""),
    receiverId: String(item.receiverId ?? item.receiver_id ?? payload.receiverId ?? ""),
    payload,
    raw: item,
  };
};

const filteredNotices = computed(() =>
  notices.value.filter((notice) => isReceiverMatched(notice) && getNoticeKind(notice) === activeTab.value),
);

const pagedNotices = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredNotices.value.slice(start, start + pageSize);
});

const setActiveTab = (tab: NoticeTab) => {
  activeTab.value = tab;
  currentPage.value = 1;
};

onMounted(async () => {
  const response: any = await messageApi.page({ page: 1, size: 50 });
  const records = response?.data?.records || response?.data?.list || response?.records || [];
  notices.value = records.map(normalizeNotice);
});

const currentPermissions = () => normalizeUserPermissions(getCurrentUser());

const firstAllowedPath = (paths: string[]) =>
  paths.find((path) => hasPermissionForPath(path, currentPermissions())) || paths[0];

const findValue = (notice: NoticeItem, keys: string[]) => {
  for (const key of keys) {
    const value = notice.payload[key] ?? notice.raw[key];
    if (value !== undefined && value !== null && value !== "") return String(value);
  }
  const text = getNoticeText(notice);
  for (const key of keys) {
    const match = text.match(new RegExp(`${key}\\s*[:：=]\\s*(\\d+)`, "i"));
    if (match?.[1]) return match[1];
  }
  return "";
};

const buildState = (notice: NoticeItem) => {
  const registrationId = findValue(notice, ["registrationId", "registration_id", "registerId"]);
  const visitId = findValue(notice, ["visitId", "visit_id"]);
  const patientId = findValue(notice, ["patientId", "patient_id"]);
  const orderId = findValue(notice, ["orderId", "order_id", "chargeOrderId", "charge_order_id"]);
  return {
    ...(registrationId ? { registrationId } : {}),
    ...(visitId ? { visitId } : {}),
    ...(patientId ? { patientId, id: patientId } : {}),
    ...(orderId ? { orderId, id: orderId } : {}),
  };
};

const roleText = () => {
  const user = getCurrentUser();
  return [user.roleName, user.roleCode, user.role, user.positionName, user.jobTitle, user.permissions, user.name, user.username]
    .map((value) => String(value || ""))
    .join(" ")
    .toUpperCase();
};

const hasRole = (...keywords: string[]) => keywords.some((keyword) => roleText().includes(keyword.toUpperCase()));

const explicitRoute = (notice: NoticeItem): RouteLocationRaw | null => {
  const rawPath = notice.payload.path || notice.payload.route || notice.payload.url || notice.payload.targetPath || notice.raw.targetPath;
  const path = String(rawPath || "").trim();
  return path.startsWith("/") ? { path, state: buildState(notice) } : null;
};

const resolveBusinessRoute = (notice: NoticeItem): RouteLocationRaw | null => {
  const directRoute = explicitRoute(notice);
  if (directRoute) return directRoute;

  const type = toUpperValue(notice.messageType);
  const text = getNoticeText(notice);
  const state = buildState(notice);
  const hasVisitMeaning = ["VISIT", "CONSULTATION", "REGISTRATION", "REGISTER", "MEDICAL", "TREATMENT"].some((key) =>
    type.includes(key),
  ) || /就诊|接诊|挂号|病历/.test(text);

  if (hasVisitMeaning) {
    if (hasRole("DOCTOR", "医生", "医师")) return { path: firstAllowedPath(["/consultation/new", "/workbench"]), state };
    if (hasRole("RECEPTION", "前台", "挂号")) return { path: firstAllowedPath(["/registration/list", "/registration/create"]), state };
    return { path: firstAllowedPath(["/workbench", "/registration/list", "/consultation/new"]), state };
  }

  if (type.includes("CHARGE") || type.includes("PAYMENT") || /收费|支付|退费|订单/.test(text)) {
    return { path: firstAllowedPath(["/charge/orders", "/statistics/charge/"]), state };
  }

  if (type.includes("PRESCRIPTION") || type.includes("DRUG") || type.includes("STOCK") || type.includes("INVENTORY") || /处方|药品|入库|出库|库存|盘点|调价/.test(text)) {
    if (hasRole("PHARMACIST", "药师", "药房")) return { path: firstAllowedPath(["/retail", "/drugs/inventory"]), state };
    return { path: firstAllowedPath(["/drugs/inventory", "/drugs"]), state };
  }

  if (type.includes("PATIENT") || /患者/.test(text)) return { path: firstAllowedPath(["/patients"]), state };
  if (type.includes("MEMBER") || /会员/.test(text)) return { path: firstAllowedPath(["/members"]), state };
  return { path: firstAllowedPath(["/workbench", "/dashboard"]), state };
};

const markRead = async (notice: NoticeItem) => {
  if (!notice.unread) return;
  await messageApi.read(notice.id);
  notice.unread = false;
};

const openNotice = async (notice: NoticeItem) => {
  await markRead(notice);
  if (getNoticeKind(notice) !== "business") return;
  const target = resolveBusinessRoute(notice);
  if (target) router.push(target);
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
}

.tabs {
  display: flex;
  margin-bottom: 28px;
}

.tab-btn {
  min-width: 122px;
  height: 50px;
  border: 1px solid #d9ddea;
  background: #fff;
  color: #8b93a9;
  font-size: 16px;
  cursor: pointer;
}

.tab-btn:first-child {
  border-radius: 10px 0 0 10px;
}

.tab-btn:last-child {
  border-radius: 0 10px 10px 0;
}

.tab-btn.active {
  background: #6574f7;
  color: #fff;
  border-color: #6574f7;
}

.notice-list {
  border-top: 1px solid #eef1f7;
}

.notice-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 106px;
  border-bottom: 1px solid #eef1f7;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.notice-item:hover {
  background: #f8faff;
}

.notice-left {
  display: flex;
  align-items: center;
  gap: 22px;
  min-width: 0;
}

.notice-content {
  min-width: 0;
}

.notice-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #6574f7;
}

.notice-icon {
  color: #fff;
  font-size: 28px;
}

.notice-title-row {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 10px;
}

.notice-title {
  font-size: 18px;
  color: #1f2937;
}

.notice-time,
.notice-desc {
  color: #98a1b6;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-right {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #a7afc2;
  font-size: 44px;
}

.notice-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #ff6464;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}
</style>
