<template>
  <div class="member-page">
    <section class="member-card">
      <header class="member-toolbar">
        <div class="member-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="['member-tab', { active: activeTab === tab.key }]"
            @click="selectTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="toolbar-actions">
          <template v-if="activeTab === 'setting'">
            <button type="button" class="primary-action" @click="openMemberTypeEditor()">
              <el-icon><CirclePlusFilled /></el-icon>
              新增会员类型
            </button>
            <button type="button" class="warning-action" @click="showConditionModal = true">
              <el-icon><Setting /></el-icon>
              会员条件设置
            </button>
          </template>
          <button v-else type="button" class="export-btn">
            <el-icon><Upload /></el-icon>
            导出
          </button>
        </div>
      </header>

      <div class="member-divider"></div>

      <section v-if="activeTab !== 'setting'" class="filters-row">
        <label class="filter-item level-filter">
          <span>会员等级</span>
          <el-select v-model="filters.level" placeholder="全部">
            <el-option label="全部" value="" />
            <el-option v-for="level in levels" :key="level" :label="level" :value="level" />
          </el-select>
        </label>

        <div v-if="activeTab === 'list'" class="filter-item date-filter">
          <span>创建时间</span>
          <el-date-picker
            v-model="filters.dateRange"
            class="date-control"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
          />
        </div>

        <label class="search-filter">
          <input v-model="filters.keyword" type="text" placeholder="患者姓名/手机号码/会员卡号" />
          <button type="button" aria-label="搜索">
            <el-icon><Search /></el-icon>
          </button>
        </label>

        <label v-if="activeTab === 'list'" class="warning-check">
          <input type="checkbox" />
          <span>会员到期预警</span>
        </label>
      </section>

      <div class="table-wrapper">
        <table v-if="activeTab === 'list'" class="member-table list-table">
          <thead>
            <tr>
              <th>序号</th><th>卡号</th><th>会员类型</th><th>会员等级</th><th>会员姓名</th><th>手机号码</th>
              <th>累积消费<br />（元）</th><th>储值余额<br />（元）</th><th>累计储值<br />（元）</th>
              <th>积分</th><th>开卡时间</th><th>到期时间</th><th>会员状态</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageRows" :key="row.cardNo">
              <td>{{ row.index }}</td><td>{{ row.cardNo }}</td><td>{{ row.type }}</td>
              <td><span class="level-text">{{ row.level }}</span></td>
              <td><button type="button" class="text-link">{{ row.name }}</button></td>
              <td>{{ row.phone }}</td><td>{{ row.totalConsume }}</td><td>{{ row.balance }}</td>
              <td>{{ row.totalStored }}</td><td>{{ row.points }}</td><td>{{ row.openTime }}</td><td>{{ row.expireTime }}</td>
              <td>
                <button
                  type="button"
                  :class="['switch', { off: row.status === 'DISABLED' }]"
                  @click="toggleMemberStatus(row)"
                ><span></span></button>
              </td>
              <td class="operation-cell">
                <div v-if="activeActionRow === row.cardNo" class="action-popover">
                  <button type="button" @click="handleMemberAction('充值', row)">充值</button>
                  <button type="button" @click="handleMemberAction('设置等级', row)">设置等级</button>
                  <button type="button" @click="handleMemberAction('积分变动', row)">积分变动</button>
                  <button type="button" @click="handleMemberAction('退款', row)">退款</button>
                  <button type="button" @click="handleMemberAction('等级变更记录', row)">等级变更记录</button>
                </div>
                <button type="button" class="text-link" @click="toggleActionRow(row.cardNo)">操作</button>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else-if="activeTab === 'stored'" class="member-table stored-table">
          <thead>
            <tr>
              <th>序号</th><th>卡号</th><th>会员类型</th><th>会员等级</th><th>会员姓名</th><th>手机号码</th>
              <th>累计储值<br />（元）</th><th>累计赠送<br />（元）</th><th>余额<br />（元）</th><th>储值累计消费<br />（元）</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageRows" :key="row.cardNo">
              <td>{{ row.index }}</td><td>{{ row.cardNo }}</td><td>{{ row.type }}</td>
              <td><span class="level-text">{{ row.level }}</span></td>
              <td><button type="button" class="text-link">{{ row.name }}</button></td>
              <td>{{ row.phone }}</td><td>{{ row.totalStored }}</td><td>{{ row.totalStored }}</td><td>{{ row.balance }}</td><td>{{ row.points }}</td>
              <td class="actions-cell">
                <button type="button" class="text-link" @click="handleMemberAction('充值', row)">充值</button>
                <button type="button" class="text-link" @click="handleMemberAction('退款', row)">退款</button>
                <button type="button" class="text-link" @click="handleMemberAction('充值/退款记录', row)">充值/退款记录</button>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else-if="activeTab === 'points'" class="member-table points-table">
          <thead>
            <tr>
              <th>序号</th><th>卡号</th><th>会员类型</th><th>会员等级</th><th>会员姓名</th><th>手机号码</th><th>积分</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageRows" :key="row.cardNo">
              <td>{{ row.index }}</td><td>{{ row.cardNo }}</td><td>{{ row.type }}</td>
              <td><span class="level-text">{{ row.level }}</span></td>
              <td><button type="button" class="text-link">{{ row.name }}</button></td>
              <td>{{ row.phone }}</td><td>{{ row.points }}</td>
              <td class="actions-cell">
                <button type="button" class="text-link" @click="handleMemberAction('积分变动', row)">积分变动</button>
                <button type="button" class="text-link" @click="handleMemberAction('积分变动记录', row)">积分变动记录</button>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else class="member-table setting-table">
          <thead>
            <tr>
              <th>序号</th><th>会员等级</th><th>会员名称</th><th>折扣</th><th>最低积分</th>
              <th>升级所需积分</th><th>备注</th><th>状态</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageRows" :key="row.id || row.level">
              <td>{{ row.index }}</td><td><span class="level-text">{{ row.level }}</span></td><td>{{ row.name }}</td>
              <td>{{ row.discount }}</td><td>{{ row.minPoints }}</td><td>{{ row.upgradePoints }}</td><td>{{ row.remark }}</td>
              <td>
                <button
                  type="button"
                  :class="['switch', { off: !row.enabled }]"
                  @click="toggleLevelStatus(row)"
                ><span></span></button>
              </td>
              <td><button type="button" class="text-link" @click="openMemberTypeEditor(row)">编辑</button></td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="pagination" :class="{ compact: activeTab === 'setting' }">
        <button type="button" class="pager-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">&lt;</button>
        <button
          v-for="page in totalPages"
          :key="page"
          type="button"
          :class="['pager-btn', { active: page === currentPage }]"
          @click="changePage(page)"
        >{{ page }}</button>
        <button type="button" class="pager-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">&gt;</button>
        <span>每页{{ pageSize }}条，共{{ paginationRows.length }}条&nbsp;&nbsp;前往第</span><span class="pager-input">{{ currentPage }}</span><span>页</span>
      </footer>
    </section>

    <div v-if="showMemberTypeModal || showConditionModal" class="modal-mask">
      <section v-if="showMemberTypeModal" class="modal-card member-type-modal">
        <button type="button" class="modal-close" @click="showMemberTypeModal = false">×</button>
        <h2>{{ memberLevelForm.id ? "编辑会员类型" : "新增会员类型" }}</h2>
        <div class="member-type-grid">
          <label><span>会员等级</span><input v-model.trim="memberLevelForm.level" type="text" /></label>
          <label><span>会员名称</span><input v-model.trim="memberLevelForm.name" type="text" /></label>
          <label><span>会员折扣</span><input v-model.number="memberLevelForm.discount" type="number" min="0" max="10" step="0.1" /></label>
          <label><span>最低消费积分</span><input v-model.number="memberLevelForm.minPoints" type="number" min="0" /></label>
          <label><span>升级所需积分</span><input v-model.number="memberLevelForm.upgradePoints" type="number" min="0" /></label>
          <label class="status-row">
            <span>状态:</span>
            <button type="button" :class="['switch', { off: !memberLevelForm.enabled }]" @click="memberLevelForm.enabled = !memberLevelForm.enabled"><span></span></button>
          </label>
          <label class="remark-field"><span>备注</span><input v-model.trim="memberLevelForm.remark" type="text" /></label>
        </div>
        <div class="modal-actions">
          <button type="button" class="cancel-btn" @click="showMemberTypeModal = false">取消</button>
          <button type="button" class="confirm-btn" @click="saveMemberLevel">确定</button>
        </div>
      </section>

      <section v-if="showConditionModal" class="modal-card condition-modal">
        <button type="button" class="modal-close" @click="showConditionModal = false">×</button>
        <h2>会员条件设置</h2>
        <div class="condition-body">
          <div class="condition-line">
            <span>会员折扣功能:</span>
            <button type="button" :class="['switch', { off: !conditionForm.discountEnabled }]" @click="conditionForm.discountEnabled = !conditionForm.discountEnabled"><span></span></button>
          </div>
          <div class="condition-line">
            <span>会员升级功能:</span>
            <button type="button" :class="['switch', { off: !conditionForm.upgradeEnabled }]" @click="conditionForm.upgradeEnabled = !conditionForm.upgradeEnabled"><span></span></button>
          </div>
          <div class="ratio-line">
            <span>消费积分比例</span><span class="muted">人民币：积分=1:</span><input v-model.number="conditionForm.consumeRatio" /><label><input v-model="conditionForm.consumePointsEnabled" type="checkbox" />开启</label>
          </div>
          <div class="ratio-line">
            <span>充值积分比例</span><span class="muted">人民币：积分=1:</span><input v-model.number="conditionForm.rechargeRatio" /><label><input v-model="conditionForm.rechargePointsEnabled" type="checkbox" />开启</label>
          </div>
          <div class="limit-line">
            <span>单次积分上限</span><input v-model.number="conditionForm.pointsLimit" />
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="cancel-btn" @click="showConditionModal = false">取消</button>
          <button type="button" class="confirm-btn" @click="saveMemberConditions">确定</button>
        </div>
      </section>
    </div>

    <el-dialog v-model="actionDialogVisible" :title="actionDialogTitle" width="520px">
      <div v-if="selectedMember" class="action-member-meta">
        <span>会员卡号：{{ selectedMember.cardNo }}</span>
        <span>会员姓名：{{ selectedMember.name }}</span>
        <span v-if="actionMode !== 'points'">余额：{{ selectedMember.balance }}元</span>
        <span v-else>当前积分：{{ selectedMember.points }}</span>
      </div>

      <el-form v-if="actionMode === 'recharge'" label-width="110px">
        <el-form-item label="充值金额（元）" required><el-input-number v-model="actionForm.amount" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="赠送金额（元）"><el-input-number v-model="actionForm.gift" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="支付方式"><el-radio-group v-model="actionForm.payment"><el-radio v-for="item in paymentOptions" :key="item" :label="item">{{ item }}</el-radio></el-radio-group></el-form-item>
      </el-form>

      <el-form v-else-if="actionMode === 'refund'" label-width="110px">
        <el-form-item label="退款金额（元）" required><el-input-number v-model="actionForm.amount" :min="0.01" :max="Number(selectedMember?.balance || 0)" :precision="2" /></el-form-item>
        <el-form-item label="退款方式"><el-radio-group v-model="actionForm.payment"><el-radio v-for="item in paymentOptions" :key="item" :label="item">{{ item }}</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="actionForm.remark" type="textarea" /></el-form-item>
      </el-form>

      <el-form v-else-if="actionMode === 'points'" label-width="100px">
        <el-form-item label="变动操作"><el-radio-group v-model="actionForm.pointsMode"><el-radio label="add">增加积分</el-radio><el-radio label="subtract">扣减积分</el-radio><el-radio label="clear">积分清零</el-radio></el-radio-group></el-form-item>
        <el-form-item v-if="actionForm.pointsMode !== 'clear'" label="变动数量" required><el-input-number v-model="actionForm.points" :min="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="actionForm.remark" type="textarea" /></el-form-item>
      </el-form>

      <div v-else-if="actionMode === 'level'">
        <el-date-picker v-model="actionForm.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="到期时间" :disabled="actionForm.neverExpire" />
        <el-checkbox v-model="actionForm.neverExpire">永不过期</el-checkbox>
        <el-table :data="settingRows" class="level-select-table">
          <el-table-column width="55">
            <template #default="{ row }"><el-radio v-model="actionForm.level" :label="row.level"><span /></el-radio></template>
          </el-table-column>
          <el-table-column prop="level" label="会员等级" />
          <el-table-column prop="name" label="会员名称" />
          <el-table-column prop="discount" label="折扣" />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMemberAction">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" :title="recordDialogTitle" width="860px">
      <div v-if="selectedMember" class="action-member-meta">
        <span>会员卡号：{{ selectedMember.cardNo }}</span>
        <span>会员姓名：{{ selectedMember.name }}</span>
      </div>
      <el-table :data="recordRows" empty-text="暂无记录">
        <el-table-column prop="createdAt" label="时间" min-width="170" />
        <el-table-column prop="transactionType" label="变动操作" min-width="130" />
        <el-table-column prop="changeValue" label="数量/金额" min-width="120" />
        <el-table-column prop="operator" label="操作员" min-width="120" />
        <el-table-column prop="remark" label="备注" min-width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { CirclePlusFilled, Search, Setting, Upload } from "@element-plus/icons-vue";
import { memberApi, memberLevelApi, settingsApi } from "@/api";
import { ElMessage } from "element-plus";
import { useClientPagination } from "@/composables/useClientPagination";

type TabKey = "list" | "stored" | "points" | "setting";

type MemberRow = {
  id?: string | number;
  index: number;
  cardNo: string;
  type: string;
  level: string;
  name: string;
  phone: string;
  totalConsume: string;
  balance: string;
  totalStored: string;
  points: number;
  openTime: string;
  expireTime: string;
  status: string;
};

const tabs: { key: TabKey; label: string }[] = [
  { key: "list", label: "会员列表" },
  { key: "stored", label: "储值管理" },
  { key: "points", label: "积分管理" },
  { key: "setting", label: "会员设置" },
];

const route = useRoute();
const router = useRouter();
const routeTabMap: Record<string, TabKey> = {
  recharge: "stored",
  points: "points",
  settings: "setting",
};
const tabRouteMap: Record<TabKey, string> = {
  list: "/members",
  stored: "/members/recharge",
  points: "/members/points",
  setting: "/members/settings",
};
const activeTab = ref<TabKey>("list");
const activeActionRow = ref("");
const showMemberTypeModal = ref(false);
const showConditionModal = ref(false);
const actionDialogVisible = ref(false);
const recordDialogVisible = ref(false);
const actionMode = ref<"recharge" | "refund" | "points" | "level">("recharge");
const selectedMember = ref<MemberRow | null>(null);
const recordRows = ref<any[]>([]);
const recordDialogTitle = ref("");
const paymentOptions = ["现金", "微信", "支付宝", "银行卡"];

const actionForm = reactive({
  amount: 0,
  gift: 0,
  payment: "现金",
  remark: "",
  pointsMode: "add",
  points: 1,
  level: "",
  expireDate: "",
  neverExpire: true,
});

const memberLevelForm = reactive({
  id: undefined as string | number | undefined,
  level: "",
  name: "",
  discount: 10,
  minPoints: 0,
  upgradePoints: 0,
  enabled: true,
  remark: "",
});

const conditionForm = reactive({
  discountEnabled: true,
  upgradeEnabled: true,
  consumeRatio: 1,
  consumePointsEnabled: true,
  rechargeRatio: 1,
  rechargePointsEnabled: true,
  pointsLimit: 0,
});

const filters = reactive({
  level: "",
  dateRange: [] as string[],
  keyword: "",
});

const levels = ["VIP1", "VIP2", "VIP3", "VIP4", "VIP5"];
const memberTypes = ["普通会员", "初级会员", "高级会员", "白银会员", "黄金会员", "钻石会员"];
const rowLevels = ["VIP1", "VIP2", "VIP3", "VIP4", "VIP5", "VIP7"];

const rows: MemberRow[] = Array.from({ length: 10 }, (_, index) => {
  const nameNo = index * 2 + 1;
  const seconds = index < 3 ? "12" : String(12 + (index - 2) * 2).padStart(2, "0");
  const amount = index === 9 ? "1200.90" : `${1200 + index * 1000}.90`;
  return {
    index: index + 1,
    cardNo: String(28100159 - index).padStart(8, "0"),
    type: memberTypes[index % memberTypes.length],
    level: rowLevels[index % rowLevels.length],
    name: `姓名${nameNo}`,
    phone: `177541387${String(69 + index * 2).padStart(2, "0")}`,
    totalConsume: amount,
    balance: amount,
    totalStored: amount,
    points: 1000 + index * 100,
    openTime: `2019-11-12 12:08:${seconds}`,
    expireTime: `2019-11-12 12:08:${seconds}`,
    status: "ENABLED",
  };
});

const remoteRows = ref<MemberRow[]>([]);

const settingRows = reactive<any[]>([]);
const actionDialogTitle = computed(() => ({
  recharge: "会员充值",
  refund: "会员退款",
  points: "积分变动",
  level: "设置会员等级",
}[actionMode.value]));

const filteredRows = computed(() => {
  const keyword = filters.keyword.trim();
  const sourceRows = remoteRows.value;
  return sourceRows.filter((row) => {
    const matchesLevel = !filters.level || row.level === filters.level;
    const rowDate = String(row.openTime || "").slice(0, 10);
    const matchesDate = filters.dateRange.length !== 2
      || (rowDate >= filters.dateRange[0] && rowDate <= filters.dateRange[1]);
    const matchesKeyword =
      !keyword || row.name.includes(keyword) || row.phone.includes(keyword) || row.cardNo.includes(keyword);
    return matchesLevel && matchesDate && matchesKeyword;
  });
});

const paginationRows = computed<any[]>(() =>
  activeTab.value === "setting" ? settingRows : filteredRows.value);
const {
  pageSize,
  currentPage,
  totalPages,
  pageRows,
  changePage,
  resetPage,
} = useClientPagination(paginationRows, 10);

watch(
  () => [activeTab.value, filters.level, filters.keyword, ...filters.dateRange],
  resetPage,
);

const loadMembers = async () => {
  try {
    const response = await memberApi.page({ page: 1, size: 50 });
    remoteRows.value = (response.data.records || []).map((item: any, index: number) => ({
      id: item.id,
      index: index + 1,
      cardNo: item.cardNo || String(item.id || ""),
      type: item.memberType || "",
      level: item.levelName || "",
      name: item.patientName || "",
      phone: item.phone || "",
      totalConsume: String(item.totalConsume || "0.00"),
      balance: String(item.balance || "0.00"),
      totalStored: String(item.totalStored || "0.00"),
      points: Number(item.points || 0),
      openTime: item.openDate || item.createdAt || "",
      expireTime: item.expireDate || "",
      status: item.status || "ENABLED",
    }));
  } catch {
    remoteRows.value = [];
  }
};

const parseLevelMeta = (remark: string) => {
  try {
    const value = JSON.parse(remark || "{}");
    return typeof value === "object" && value ? value : {};
  } catch {
    const discount = String(remark || "").match(/(\d+(?:\.\d+)?)\s*折/);
    return {
      discount: discount ? Number(discount[1]) : 10,
      note: remark || "",
    };
  }
};

const loadMemberLevels = async () => {
  try {
    const response = await memberLevelApi.list();
    settingRows.splice(0, settingRows.length, ...(response.data || []).map((item: any, index: number) => {
      const meta = parseLevelMeta(item.remark);
      return {
        id: item.id,
        index: index + 1,
        level: item.itemCode || `VIP${index + 1}`,
        name: item.itemName || "",
        discount: String(meta.discount ?? 10),
        minPoints: Number(meta.minPoints || 0),
        upgradePoints: Number(meta.upgradePoints || 0),
        remark: meta.note || "",
        enabled: item.enabled !== false && Number(item.enabled) !== 0,
      };
    }));
  } catch {
    settingRows.splice(0);
  }
};

const loadMemberConditions = async () => {
  try {
    const response = await settingsApi.get();
    const settings = response.data || {};
    conditionForm.discountEnabled = String(settings["member.discount.enabled"] ?? "true") === "true";
    conditionForm.upgradeEnabled = String(settings["member.upgrade.enabled"] ?? "true") === "true";
    conditionForm.consumeRatio = Number(settings["member.points.consumeRatio"] || 1);
    conditionForm.consumePointsEnabled = String(settings["member.points.consumeEnabled"] ?? "true") === "true";
    conditionForm.rechargeRatio = Number(settings["member.points.rechargeRatio"] || 1);
    conditionForm.rechargePointsEnabled = String(settings["member.points.rechargeEnabled"] ?? "true") === "true";
    conditionForm.pointsLimit = Number(settings["member.points.singleLimit"] || 0);
  } catch {
    // Keep the form defaults when settings have not been initialized.
  }
};

onMounted(() => {
  void Promise.all([loadMembers(), loadMemberLevels(), loadMemberConditions()]);
});

const selectTab = (tab: TabKey) => {
  activeTab.value = tab;
  router.replace(tabRouteMap[tab]);
};

watch(
  () => route.params.type,
  (type) => {
    activeTab.value = routeTabMap[String(type || "")] || "list";
  },
  { immediate: true },
);

const toggleActionRow = (cardNo: string) => {
  activeActionRow.value = activeActionRow.value === cardNo ? "" : cardNo;
};

const handleMemberAction = async (action: string, row: MemberRow) => {
  selectedMember.value = row;
  activeActionRow.value = "";
  const id = row.id || row.cardNo;

  if (action.includes("记录")) {
    const response = action === "充值/退款记录"
      ? await memberApi.balanceLogs(id)
      : action === "等级变更记录"
        ? await memberApi.levelLogs(id)
        : await memberApi.pointsLogs(id);
    recordRows.value = (response.data || []).map((item: any) => ({
      ...item,
      changeValue: item.amount ?? item.points ?? "-",
      transactionType: ({
        RECHARGE: "充值",
        REFUND: "退款",
        BALANCE_ADJUST: "余额调整",
        POINTS_ADJUST: "积分调整",
        LEVEL_CHANGE: "等级变更",
      } as Record<string, string>)[item.transactionType] || item.transactionType,
    }));
    recordDialogTitle.value = action;
    recordDialogVisible.value = true;
    return;
  }

  Object.assign(actionForm, {
    amount: 0,
    gift: 0,
    payment: "现金",
    remark: "",
    pointsMode: "add",
    points: 1,
    level: row.level,
    expireDate: row.expireTime || "",
    neverExpire: !row.expireTime,
  });
  actionMode.value = action === "充值" ? "recharge"
    : action === "退款" ? "refund"
      : action === "积分变动" ? "points" : "level";
  actionDialogVisible.value = true;
};

const submitMemberAction = async () => {
  const member = selectedMember.value;
  if (!member) return;
  const id = member.id || member.cardNo;
  if (actionMode.value === "recharge") {
    if (actionForm.amount <= 0) return ElMessage.warning("请输入充值金额");
    await memberApi.recharge(id, {
      amount: Number(actionForm.amount) + Number(actionForm.gift || 0),
      transactionType: "RECHARGE",
      remark: `${actionForm.payment}充值，充值${actionForm.amount}元，赠送${actionForm.gift || 0}元`,
    });
  } else if (actionMode.value === "refund") {
    if (actionForm.amount <= 0) return ElMessage.warning("请输入退款金额");
    await memberApi.adjustBalance(id, {
      amount: -Number(actionForm.amount),
      transactionType: "REFUND",
      remark: `${actionForm.payment}退款${actionForm.amount}元${actionForm.remark ? `，${actionForm.remark}` : ""}`,
    });
  } else if (actionMode.value === "points") {
    let points = Number(actionForm.points || 0);
    if (actionForm.pointsMode === "subtract") points = -points;
    if (actionForm.pointsMode === "clear") points = -Number(member.points || 0);
    if (!points) return ElMessage.warning("请输入积分变动数量");
    await memberApi.adjustPoints(id, {
      points,
      transactionType: "POINTS_ADJUST",
      remark: actionForm.remark || "页面积分调整",
    });
  } else {
    if (!actionForm.level) return ElMessage.warning("请选择会员等级");
    await memberApi.changeLevel(id, {
      levelName: actionForm.level,
      expireDate: actionForm.neverExpire ? "" : actionForm.expireDate,
    });
  }
  actionDialogVisible.value = false;
  await loadMembers();
  ElMessage.success("操作成功");
};

const toggleMemberStatus = async (row: MemberRow) => {
  const next = row.status === "DISABLED" ? "ENABLED" : "DISABLED";
  await memberApi.update(row.id || row.cardNo, { status: next });
  row.status = next;
  ElMessage.success(next === "ENABLED" ? "会员已启用" : "会员已停用");
};

const openMemberTypeEditor = (row?: any) => {
  Object.assign(memberLevelForm, {
    id: row?.id,
    level: row?.level || "",
    name: row?.name || "",
    discount: Number(row?.discount ?? 10),
    minPoints: Number(row?.minPoints || 0),
    upgradePoints: Number(row?.upgradePoints || 0),
    enabled: row?.enabled ?? true,
    remark: row?.remark || "",
  });
  showMemberTypeModal.value = true;
};

const saveMemberLevel = async () => {
  if (!memberLevelForm.level || !memberLevelForm.name) {
    return ElMessage.warning("请输入会员等级和会员名称");
  }
  const payload = {
    itemCode: memberLevelForm.level,
    itemName: memberLevelForm.name,
    sortNo: memberLevelForm.id
      ? settingRows.findIndex((item) => item.id === memberLevelForm.id) + 1
      : settingRows.length + 1,
    enabled: memberLevelForm.enabled,
    remark: JSON.stringify({
      discount: memberLevelForm.discount,
      minPoints: memberLevelForm.minPoints,
      upgradePoints: memberLevelForm.upgradePoints,
      note: memberLevelForm.remark,
    }),
  };
  if (memberLevelForm.id) await memberLevelApi.update(memberLevelForm.id, payload);
  else await memberLevelApi.create(payload);
  showMemberTypeModal.value = false;
  await loadMemberLevels();
  ElMessage.success("会员类型已保存");
};

const toggleLevelStatus = async (row: any) => {
  const payload = {
    itemCode: row.level,
    itemName: row.name,
    enabled: !row.enabled,
    remark: JSON.stringify({
      discount: Number(row.discount),
      minPoints: row.minPoints,
      upgradePoints: row.upgradePoints,
      note: row.remark,
    }),
  };
  await memberLevelApi.update(row.id, payload);
  row.enabled = !row.enabled;
  ElMessage.success(row.enabled ? "会员类型已启用" : "会员类型已停用");
};

const saveMemberConditions = async () => {
  await settingsApi.update({
    "member.discount.enabled": String(conditionForm.discountEnabled),
    "member.upgrade.enabled": String(conditionForm.upgradeEnabled),
    "member.points.consumeRatio": String(conditionForm.consumeRatio),
    "member.points.consumeEnabled": String(conditionForm.consumePointsEnabled),
    "member.points.rechargeRatio": String(conditionForm.rechargeRatio),
    "member.points.rechargeEnabled": String(conditionForm.rechargePointsEnabled),
    "member.points.singleLimit": String(conditionForm.pointsLimit),
  });
  showConditionModal.value = false;
  ElMessage.success("会员条件已保存");
};
</script>

<style scoped>
.member-page {
  min-height: 100%;
  padding: 37px 0 58px;
  overflow-x: auto;
}

.member-card {
  width: 1640px;
  min-height: 1216px;
  margin: 0 auto;
  padding: 31px 31px 41px;
  background: #fff;
  border-radius: 5px;
  box-sizing: border-box;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.member-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 46px;
}

.member-tabs {
  display: inline-flex;
  overflow: hidden;
  border: 1px solid #c5c7cd;
  border-radius: 4px;
}

.member-tab {
  width: 128px;
  height: 44px;
  border: 0;
  border-right: 1px solid #c5c7cd;
  background: #fff;
  color: #b9b9b9;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.member-tab:last-child {
  border-right: 0;
}

.member-tab.active {
  background: #636be8;
  color: #fff;
}

.toolbar-actions {
  display: flex;
  gap: 26px;
  align-items: center;
}

.export-btn,
.primary-action,
.warning-action {
  display: inline-flex;
  height: 46px;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.export-btn {
  width: 141px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
}

.primary-action {
  width: 174px;
  border: 1px solid #636be8;
  background: #636be8;
  color: #fff;
}

.warning-action {
  width: 174px;
  border: 1px solid #ffbc17;
  background: #ffbc17;
  color: #fff;
}

.toolbar-actions :deep(.el-icon) {
  font-size: 24px;
}

.member-divider {
  height: 1px;
  margin: 27px 0 32px;
  background: #eee;
}

.filters-row {
  display: grid;
  grid-template-columns: 302px 385px 468px 190px;
  align-items: center;
  column-gap: 30px;
  margin-bottom: 32px;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  gap: 18px;
  color: #1e2533;
  font-size: 16px;
  white-space: nowrap;
}

.filter-item :deep(.el-select) {
  width: 193px;
}

.filter-item :deep(.el-select__wrapper) {
  min-height: 44px;
  border-radius: 4px;
  box-shadow: 0 0 0 1px #c8c8c8 inset;
  font-size: 16px;
  padding: 0 16px 0 20px;
}

.level-filter {
  width: 302px;
}

.date-filter {
  width: 385px;
}

.date-control,
.search-filter {
  height: 44px;
  border: 1px solid #c8c8c8;
  border-radius: 4px;
  background: #fff;
  box-sizing: border-box;
}

.date-control {
  display: flex;
  width: 298px;
  align-items: center;
}

.date-control input,
.search-filter input {
  width: 100%;
  height: 100%;
  padding: 0 14px;
  border: 0;
  outline: none;
  color: #2f3541;
  font-size: 16px;
}

.date-control input {
  padding-left: 20px;
}

.date-control :deep(.el-icon) {
  margin-right: 13px;
  color: #c7c7c7;
  font-size: 22px;
}

.search-filter {
  display: grid;
  grid-template-columns: 406px 62px;
  width: 468px;
  overflow: hidden;
}

.search-filter input::placeholder {
  color: #c5c5c5;
}

.search-filter button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-left: 1px solid #c8c8c8;
  background: #636be8;
  color: #fff;
  cursor: pointer;
}

.search-filter :deep(.el-icon) {
  font-size: 27px;
}

.warning-check {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: #1e2533;
  font-size: 16px;
  white-space: nowrap;
}

.warning-check input {
  width: 19px;
  height: 19px;
}

.table-wrapper {
  overflow: visible;
}

.member-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.list-table {
  width: 1578px;
  min-width: 1578px;
}

.member-table th,
.member-table td {
  box-sizing: border-box;
}

.member-table th {
  height: 72px;
  padding: 0 6px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.25;
  text-align: center;
}

.member-table td {
  height: 84px;
  padding: 0 6px;
  border-bottom: 1px solid #eee;
  color: #000;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
}

.list-table th:nth-child(1), .list-table td:nth-child(1) { width: 65px; }
.list-table th:nth-child(2), .list-table td:nth-child(2) { width: 125px; }
.list-table th:nth-child(3), .list-table td:nth-child(3) { width: 120px; }
.list-table th:nth-child(4), .list-table td:nth-child(4) { width: 90px; }
.list-table th:nth-child(5), .list-table td:nth-child(5) { width: 95px; }
.list-table th:nth-child(6), .list-table td:nth-child(6) { width: 130px; }
.list-table th:nth-child(7), .list-table td:nth-child(7),
.list-table th:nth-child(8), .list-table td:nth-child(8),
.list-table th:nth-child(9), .list-table td:nth-child(9) { width: 90px; }
.list-table th:nth-child(10), .list-table td:nth-child(10) { width: 70px; }
.list-table th:nth-child(11), .list-table td:nth-child(11),
.list-table th:nth-child(12), .list-table td:nth-child(12) { width: 220px; }
.list-table th:nth-child(13), .list-table td:nth-child(13) { width: 110px; }
.list-table th:nth-child(14), .list-table td:nth-child(14) { width: 63px; }

.stored-table th:nth-child(1), .stored-table td:nth-child(1) { width: 72px; }
.stored-table th:nth-child(2), .stored-table td:nth-child(2) { width: 165px; }
.stored-table th:nth-child(3), .stored-table td:nth-child(3) { width: 125px; }
.stored-table th:nth-child(4), .stored-table td:nth-child(4) { width: 118px; }
.stored-table th:nth-child(5), .stored-table td:nth-child(5) { width: 118px; }
.stored-table th:nth-child(6), .stored-table td:nth-child(6) { width: 155px; }
.stored-table th:nth-child(7), .stored-table td:nth-child(7),
.stored-table th:nth-child(8), .stored-table td:nth-child(8),
.stored-table th:nth-child(9), .stored-table td:nth-child(9),
.stored-table th:nth-child(10), .stored-table td:nth-child(10) { width: 118px; }
.stored-table th:nth-child(11), .stored-table td:nth-child(11) { width: 260px; }

.points-table th:nth-child(1), .points-table td:nth-child(1) { width: 88px; }
.points-table th:nth-child(2), .points-table td:nth-child(2) { width: 190px; }
.points-table th:nth-child(3), .points-table td:nth-child(3) { width: 160px; }
.points-table th:nth-child(4), .points-table td:nth-child(4) { width: 140px; }
.points-table th:nth-child(5), .points-table td:nth-child(5) { width: 160px; }
.points-table th:nth-child(6), .points-table td:nth-child(6) { width: 190px; }
.points-table th:nth-child(7), .points-table td:nth-child(7) { width: 150px; }
.points-table th:nth-child(8), .points-table td:nth-child(8) { width: 240px; }

.setting-table th:nth-child(1), .setting-table td:nth-child(1) { width: 90px; }
.setting-table th:nth-child(2), .setting-table td:nth-child(2) { width: 160px; }
.setting-table th:nth-child(3), .setting-table td:nth-child(3) { width: 160px; }
.setting-table th:nth-child(4), .setting-table td:nth-child(4) { width: 110px; }
.setting-table th:nth-child(5), .setting-table td:nth-child(5) { width: 150px; }
.setting-table th:nth-child(6), .setting-table td:nth-child(6) { width: 190px; }
.setting-table th:nth-child(7), .setting-table td:nth-child(7) { width: 170px; }
.setting-table th:nth-child(8), .setting-table td:nth-child(8) { width: 120px; }
.setting-table th:nth-child(9), .setting-table td:nth-child(9) { width: 90px; }

.level-text {
  color: #ffb300;
  font-weight: 700;
  font-style: italic;
}

.text-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: #6068f1;
  font-size: 16px;
  cursor: pointer;
}

.actions-cell .text-link {
  margin: 0 8px;
}

.switch {
  display: inline-flex;
  width: 51px;
  height: 30px;
  padding: 2px;
  align-items: center;
  border-radius: 999px;
  background: #35d07f;
  border: 0;
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
  background: #c4c8cf;
}

.switch.off span {
  margin-left: 0;
}

.operation-cell {
  position: relative;
  overflow: visible;
}

.action-popover {
  position: absolute;
  right: 48px;
  top: 50%;
  z-index: 5;
  display: inline-flex;
  align-items: center;
  height: 50px;
  padding: 0 15px;
  transform: translateY(-50%);
  border: 2px solid #e1e4ff;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 0 7px rgba(99, 107, 232, 0.18);
  white-space: nowrap;
}

.action-popover::after {
  content: "";
  position: absolute;
  top: 50%;
  right: -13px;
  width: 22px;
  height: 22px;
  transform: translateY(-50%) rotate(45deg);
  border-top: 2px solid #e1e4ff;
  border-right: 2px solid #e1e4ff;
  background: #fff;
}

.action-popover button {
  position: relative;
  z-index: 1;
  padding: 0 12px;
  border: 0;
  border-right: 1px solid #6068f1;
  background: transparent;
  color: #6068f1;
  font-size: 16px;
  line-height: 20px;
  cursor: pointer;
}

.action-popover button:first-child {
  padding-left: 0;
}

.action-popover button:last-child {
  padding-right: 0;
  border-right: 0;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 27px;
  color: #1e2533;
  font-size: 16px;
}

.pager-btn,
.pager-input {
  display: inline-flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  border: 2px solid #bfc0c4;
  border-radius: 50%;
  background: #fff;
  color: #777;
  font-size: 16px;
  line-height: 1;
}

.pager-btn {
  cursor: pointer;
}

.pager-btn.active {
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

.pager-ellipsis {
  margin: 0 8px 0 14px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.42);
}

.modal-card {
  position: relative;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.18);
}

.modal-card h2 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 700;
}

.modal-close {
  position: absolute;
  top: 18px;
  right: 34px;
  border: 0;
  background: transparent;
  color: #c7c7c7;
  font-size: 34px;
  line-height: 1;
  cursor: pointer;
}

.member-type-modal {
  width: 820px;
  padding: 27px 30px 20px;
}

.member-type-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 45px;
  row-gap: 21px;
  margin-top: 28px;
}

.member-type-grid label {
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #111827;
  font-size: 16px;
}

.member-type-grid input {
  height: 52px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  padding: 0 12px;
  font-size: 18px;
  box-sizing: border-box;
}

.member-type-grid .status-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 36px;
  padding-top: 35px;
}

.remark-field {
  grid-column: 1 / -1;
}

.condition-modal {
  width: 470px;
  padding: 27px 30px 27px;
}

.condition-body {
  margin-top: 35px;
}

.condition-line,
.ratio-line,
.limit-line {
  display: grid;
  align-items: center;
  min-height: 48px;
  color: #111827;
  font-size: 16px;
}

.condition-line {
  grid-template-columns: 1fr 60px;
}

.ratio-line {
  grid-template-columns: 125px 145px 80px 70px;
  column-gap: 6px;
}

.ratio-line input[type="text"],
.ratio-line input:not([type]),
.limit-line input {
  height: 41px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  text-align: center;
  font-size: 20px;
}

.ratio-line label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.ratio-line label input {
  width: 16px;
  height: 16px;
  accent-color: #1677ee;
}

.muted {
  color: #8c8c8c;
}

.limit-line {
  grid-template-columns: 1fr 80px;
  margin-top: 10px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 31px;
}

.cancel-btn,
.confirm-btn {
  width: 82px;
  height: 42px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.cancel-btn {
  border: 1px solid #d0d0d0;
  background: #fff;
  color: #111827;
}

.confirm-btn {
  border: 1px solid #636be8;
  background: #636be8;
  color: #fff;
}
</style>
