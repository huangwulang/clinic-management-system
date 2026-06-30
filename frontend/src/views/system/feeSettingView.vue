<template>
  <div class="fee-page">
    <section class="fee-card" :class="{ payment: activeTab === 'payment' }">
      <header class="fee-toolbar">
        <nav class="fee-tabs" aria-label="费用设置">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="['fee-tab', { active: activeTab === tab.key }]"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </nav>

        <button v-if="activeTab !== 'payment'" type="button" class="add-btn" @click="handleAdd">
          <el-icon><CirclePlusFilled /></el-icon>
          <span>新增</span>
        </button>
      </header>

      <template v-if="activeTab !== 'payment'">
        <div class="fee-divider"></div>

        <section class="filters" :class="{ addon: activeTab === 'addon' }">
          <label v-if="activeTab === 'addon'" class="select-filter">
            <span>处方类别</span>
            <select v-model="prescriptionType">
              <option value="">全部</option>
              <option>西/成药处方</option>
              <option>中药方</option>
            </select>
          </label>

          <label class="search-filter">
            <input v-model="keyword" type="text" placeholder="费用名称" />
            <button type="button" aria-label="搜索">
              <el-icon><Search /></el-icon>
            </button>
          </label>
        </section>

        <table class="fee-table" :class="`${activeTab}-table`">
          <thead>
            <tr>
              <th v-for="column in currentColumns" :key="column.key">{{ column.label }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageRows" :key="`${activeTab}-${row.id}`">
              <td v-for="column in currentColumns" :key="column.key">
                <template v-if="column.key === 'actions'">
                  <button type="button" class="text-link" @click="handleEdit(row)">编辑</button>
                  <button type="button" class="text-link" @click="handleDelete(row)">删除</button>
                </template>
                <template v-else>{{ row[column.key] }}</template>
              </td>
            </tr>
          </tbody>
        </table>

        <footer class="pagination compact">
          <button type="button" class="pager-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">&lt;</button>
          <button
            v-for="page in totalPages"
            :key="page"
            type="button"
            :class="['pager-btn', { active: page === currentPage }]"
            @click="changePage(page)"
          >{{ page }}</button>
          <button type="button" class="pager-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">&gt;</button>
          <span class="pager-summary">每页{{ pageSize }}条，共{{ filteredRows.length }}条</span>
          <span>前往第</span>
          <span class="pager-input">{{ currentPage }}</span>
          <span>页</span>
        </footer>
      </template>

      <section v-else class="payment-list">
        <article v-for="item in paymentMethods" :key="item.itemCode" class="payment-item">
          <div class="pay-icon" :class="item.iconClass">{{ item.iconText }}</div>
          <div class="pay-copy">
            <h3>{{ item.title }}</h3>
            <p>这是一段支付方式介绍这是一段支付方式介绍这是一段支付方式介绍。</p>
          </div>
          <div class="payment-actions">
            <button
              type="button"
              :class="['switch', { 'is-on': item.enabled }]"
              :aria-label="`${item.title}${item.enabled ? '停用' : '启用'}`"
              @click="togglePaymentMethod(item)"
            >
              <span></span>
            </button>
            <button v-if="item.configurable" type="button">配置</button>
          </div>
        </article>
      </section>
    </section>

    <div v-if="deleteDialogVisible" class="modal-mask">
      <div class="delete-dialog">
        <button class="dialog-close" type="button" @click="closeDeleteDialog">×</button>
        <h3>删除确认</h3>
        <div class="dialog-content">
          <span class="warning-icon">!</span>
          <span>确定要删除此费用信息吗?</span>
        </div>
        <div class="dialog-actions">
          <button class="cancel-btn" type="button" @click="closeDeleteDialog">取消</button>
          <button class="confirm-btn" type="button" @click="confirmDeleteFee">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { CirclePlusFilled, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { dictionaryApi, feeItemApi } from "@/api";
import { useClientPagination } from "@/composables/useClientPagination";

type TabKey = "addon" | "treatment" | "registration" | "payment";
type Column = { key: string; label: string };
type Row = Record<string, string | number> & {
  id: string | number;
  category: string;
};
type PaymentMethod = {
  id?: string | number;
  itemCode: string;
  itemName: string;
  title: string;
  iconText: string;
  iconClass: string;
  configurable: boolean;
  enabled: boolean;
};

const route = useRoute();
const router = useRouter();

const tabs: { key: TabKey; label: string }[] = [
  { key: "addon", label: "附加费用设置" },
  { key: "treatment", label: "诊疗费设置" },
  { key: "registration", label: "挂号费设置" },
  { key: "payment", label: "支付方式设置" },
];

const activeTab = ref<TabKey>("addon");
const keyword = ref("");
const prescriptionType = ref("");

const commonColumns = (nameLabel: string): Column[] => [
  { key: "index", label: "序号" },
  { key: "name", label: nameLabel },
  { key: "amount", label: "金额（元）" },
  { key: "cost", label: "成本价（元）" },
  { key: "createTime", label: "创建时间" },
  { key: "creator", label: "创建人员" },
  { key: "discount", label: "允许会员折扣" },
  { key: "status", label: "费用状态" },
  { key: "actions", label: "操作" },
];

const addonColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "name", label: "附加费名称" },
  { key: "type", label: "处方类型" },
  { key: "amount", label: "金额（元）" },
  { key: "cost", label: "成本价（元）" },
  { key: "createTime", label: "创建时间" },
  { key: "creator", label: "创建人员" },
  { key: "discount", label: "允许会员折扣" },
  { key: "status", label: "费用状态" },
  { key: "actions", label: "操作" },
];

const rows: Record<Exclude<TabKey, "payment">, Row[]> = reactive({
  addon: [],
  treatment: [],
  registration: [],
});

const defaultPaymentMethods: PaymentMethod[] = [
  { itemCode: "CASH", itemName: "现金", title: "现金支付", iconText: "", iconClass: "cash", configurable: false, enabled: true },
  { itemCode: "ALIPAY", itemName: "支付宝", title: "支付宝支付", iconText: "支", iconClass: "alipay", configurable: true, enabled: true },
  { itemCode: "WECHAT", itemName: "微信", title: "微信支付", iconText: "✓", iconClass: "wechat", configurable: true, enabled: true },
  { itemCode: "BANK_CARD", itemName: "银行卡", title: "银行卡支付", iconText: "UnionPay", iconClass: "unionpay", configurable: true, enabled: true },
  { itemCode: "MEMBER_CARD", itemName: "会员卡", title: "会员卡支付", iconText: "", iconClass: "member", configurable: true, enabled: true },
  { itemCode: "CREDIT", itemName: "挂账", title: "挂账", iconText: "▤", iconClass: "credit", configurable: false, enabled: true },
];

const paymentMethods = ref<PaymentMethod[]>(defaultPaymentMethods.map((item) => ({ ...item })));
const paymentDict = dictionaryApi("PAYMENT_METHOD");

const currentColumns = computed(() => {
  if (activeTab.value === "addon") return addonColumns;
  if (activeTab.value === "treatment") return commonColumns("诊疗费名称");
  return commonColumns("挂号费名称");
});

const filteredRows = computed(() => {
  if (activeTab.value === "payment") return [];
  const word = keyword.value.trim();

  return rows[activeTab.value].filter((row) => {
    const matchesType = activeTab.value !== "addon" || !prescriptionType.value || row.type === prescriptionType.value;
    const matchesKeyword = !word || String(row.name).includes(word);
    return matchesType && matchesKeyword;
  });
});

const {
  pageSize,
  currentPage,
  totalPages,
  pageRows,
  changePage,
  resetPage,
} = useClientPagination(filteredRows, 10);

watch(
  () => [activeTab.value, keyword.value, prescriptionType.value],
  resetPage,
);

const switchTab = (tab: TabKey) => {
  activeTab.value = tab;
  router.replace({ name: "SystemFeeSetting", params: { type: tab } });
  keyword.value = "";
  prescriptionType.value = "";
  closeDeleteDialog();
};

const deleteDialogVisible = ref(false);
const deleteFeeId = ref<string | number | null>(null);
const deleteFeeTab = ref<Exclude<TabKey, "payment">>("addon");

watch(
  () => route.params.type,
  (type) => {
    const requested = String(type || "addon") as TabKey;
    if (tabs.some((tab) => tab.key === requested)) activeTab.value = requested;
  },
  { immediate: true },
);

const handleAdd = () => {
  if (activeTab.value === "payment") return;
  router.push({ name: "SystemFeeForm", state: { type: activeTab.value } });
};

const handleEdit = (row: Row) => {
  if (activeTab.value === "payment") return;
  router.push({ name: "SystemFeeForm", state: { type: activeTab.value, id: row.id } });
};

const handleDelete = (row: Row) => {
  if (activeTab.value === "payment") return;
  deleteFeeId.value = row.id;
  deleteFeeTab.value = activeTab.value;
  deleteDialogVisible.value = true;
};

const closeDeleteDialog = () => {
  deleteDialogVisible.value = false;
  deleteFeeId.value = null;
};

const confirmDeleteFee = async () => {
  if (deleteFeeId.value) await feeItemApi.remove(deleteFeeId.value);
  const targetRows = rows[deleteFeeTab.value];
  const targetIndex = targetRows.findIndex((row) => row.id === deleteFeeId.value);

  if (targetIndex > -1) {
    targetRows.splice(targetIndex, 1);
    targetRows.forEach((row, index) => {
      row.index = index + 1;
    });
  }

  closeDeleteDialog();
};

const moneyText = (value: unknown) => Number(value || 0).toFixed(2);

const parseFeeRemark = (remark: unknown) => {
  if (typeof remark !== "string" || !remark.trim()) return {};
  try {
    const parsed = JSON.parse(remark);
    return parsed && typeof parsed === "object" ? parsed as Record<string, any> : {};
  } catch {
    return {};
  }
};

const statusLabel = (status: unknown) => {
  const value = String(status ?? "").toUpperCase();
  if (value === "ENABLED" || value === "1" || value === "TRUE" || status === true) return "启用";
  if (value === "DISABLED" || value === "0" || value === "FALSE" || status === false) return "停用";
  return value || "-";
};

const tabFromCategory = (category: unknown): Exclude<TabKey, "payment"> => {
  const value = String(category || "");
  if (value === "诊疗费" || value.toUpperCase() === "TREATMENT") return "treatment";
  if (value === "挂号费" || value.toUpperCase() === "REGISTRATION") return "registration";
  return "addon";
};

const mapFeeRow = (item: any): Row => {
  const meta = parseFeeRemark(item.remark);
  return {
    id: item.id,
    index: 0,
    name: item.name || "",
    category: item.category || "",
    type: meta.prescriptionType || (tabFromCategory(item.category) === "addon" ? item.category || "" : ""),
    amount: moneyText(item.price),
    cost: moneyText(meta.cost),
    createTime: item.createdAt || "",
    creator: meta.creator || "",
    discount: meta.allowDiscount === false ? "否" : "是",
    status: statusLabel(item.status),
    actions: "",
  };
};

const loadFees = async () => {
  const response: any = await feeItemApi.page({ page: 1, size: 200 });
  const grouped: Record<Exclude<TabKey, "payment">, Row[]> = {
    addon: [],
    treatment: [],
    registration: [],
  };
  (response?.data?.records || []).forEach((item: any) => {
    grouped[tabFromCategory(item.category)].push(mapFeeRow(item));
  });
  (Object.keys(grouped) as Exclude<TabKey, "payment">[]).forEach((key) => {
    const nextRows = grouped[key].map((row, index) => ({ ...row, index: index + 1 }));
    rows[key].splice(0, rows[key].length, ...nextRows);
  });
};

const isEnabledValue = (value: unknown) => value === true || value === 1 || String(value).toUpperCase() === "TRUE";

const loadPaymentMethods = async () => {
  const response: any = await paymentDict.list();
  const records = response?.data || [];
  const recordMap = new Map(records.map((item: any) => [String(item.itemCode), item]));
  const nextMethods: PaymentMethod[] = [];
  for (const config of defaultPaymentMethods) {
    const record: any = recordMap.get(config.itemCode);
    if (!record) {
      const created: any = await paymentDict.create({
        itemCode: config.itemCode,
        itemName: config.itemName,
        sortNo: nextMethods.length + 1,
        enabled: true,
        remark: config.title,
      });
      nextMethods.push({ ...config, id: created?.data?.id, enabled: true });
    } else {
      nextMethods.push({
        ...config,
        id: record.id,
        itemName: record.itemName || config.itemName,
        title: record.remark || config.title,
        enabled: isEnabledValue(record.enabled),
      });
    }
  }
  paymentMethods.value = nextMethods;
};

const togglePaymentMethod = async (item: PaymentMethod) => {
  const nextEnabled = !item.enabled;
  if (!item.id) return;
  await paymentDict.update(item.id, {
    itemCode: item.itemCode,
    itemName: item.itemName,
    sortNo: defaultPaymentMethods.findIndex((method) => method.itemCode === item.itemCode) + 1,
    enabled: nextEnabled,
    remark: item.title,
  });
  item.enabled = nextEnabled;
  ElMessage.success(`${item.title}已${nextEnabled ? "启用" : "停用"}`);
};

onMounted(async () => {
  await Promise.all([loadFees(), loadPaymentMethods()]);
});
</script>

<style scoped>
.fee-page {
  min-height: 100%;
  padding: 25px 0 58px;
  overflow-x: hidden;
  box-sizing: border-box;
}

.fee-card {
  width: min(1620px, calc(100% - 96px));
  min-height: 810px;
  margin: 0 auto;
  padding: 31px 31px 40px;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.08);
  box-sizing: border-box;
}

.fee-card.payment {
  min-height: 1068px;
}

.fee-toolbar {
  display: flex;
  min-height: 48px;
  align-items: center;
  justify-content: space-between;
}

.fee-tabs {
  display: inline-flex;
  overflow: hidden;
  border: 1px solid #c5c7cd;
  border-radius: 4px;
}

.fee-tab {
  width: 158px;
  height: 46px;
  border: 0;
  border-right: 1px solid #c5c7cd;
  background: #fff;
  color: #c5c5c5;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.fee-tab:last-child {
  border-right: 0;
}

.fee-tab.active {
  background: #6269e8;
  color: #fff;
}

.add-btn {
  display: inline-flex;
  width: 142px;
  height: 48px;
  align-items: center;
  justify-content: center;
  gap: 11px;
  border: 1px solid #6269e8;
  border-radius: 5px;
  background: #6269e8;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.add-btn :deep(.el-icon) {
  font-size: 23px;
}

.fee-divider {
  height: 1px;
  margin: 28px 0 31px;
  background: #eee;
}

.filters {
  display: flex;
  min-height: 48px;
  align-items: center;
  gap: 35px;
  margin-bottom: 31px;
}

.filters.addon {
  gap: 36px;
}

.select-filter {
  display: inline-flex;
  align-items: center;
  gap: 18px;
  color: #111;
  font-size: 18px;
  white-space: nowrap;
}

.select-filter select {
  width: 204px;
  height: 48px;
  padding: 0 21px;
  border: 1px solid #c8c8c8;
  border-radius: 4px;
  outline: none;
  background: #fff;
  color: #111;
  font-size: 16px;
}

.search-filter {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 64px;
  width: 492px;
  height: 48px;
  overflow: hidden;
  border: 1px solid #c8c8c8;
  border-radius: 4px;
  background: #fff;
  box-sizing: border-box;
}

.search-filter input {
  width: 100%;
  height: 100%;
  padding: 0 15px;
  border: 0;
  outline: none;
  color: #2f3541;
  font-size: 16px;
  box-sizing: border-box;
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
  background: #6269e8;
  color: #fff;
  cursor: pointer;
}

.search-filter :deep(.el-icon) {
  font-size: 27px;
}

.fee-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.fee-table th {
  height: 74px;
  padding: 0 6px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
}

.fee-table td {
  height: 88px;
  padding: 0 6px;
  border-bottom: 1px solid #eee;
  color: #000;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
}

.addon-table th:nth-child(1),
.addon-table td:nth-child(1) {
  width: 7%;
}

.addon-table th:nth-child(2),
.addon-table td:nth-child(2),
.addon-table th:nth-child(3),
.addon-table td:nth-child(3) {
  width: 12%;
}

.addon-table th:nth-child(4),
.addon-table td:nth-child(4),
.addon-table th:nth-child(5),
.addon-table td:nth-child(5) {
  width: 9%;
}

.addon-table th:nth-child(6),
.addon-table td:nth-child(6) {
  width: 16%;
}

.treatment-table th:nth-child(1),
.treatment-table td:nth-child(1),
.registration-table th:nth-child(1),
.registration-table td:nth-child(1) {
  width: 7%;
}

.treatment-table th:nth-child(2),
.treatment-table td:nth-child(2),
.registration-table th:nth-child(2),
.registration-table td:nth-child(2) {
  width: 17%;
}

.treatment-table th:nth-child(3),
.treatment-table td:nth-child(3),
.treatment-table th:nth-child(4),
.treatment-table td:nth-child(4),
.registration-table th:nth-child(3),
.registration-table td:nth-child(3),
.registration-table th:nth-child(4),
.registration-table td:nth-child(4) {
  width: 10%;
}

.treatment-table th:nth-child(5),
.treatment-table td:nth-child(5),
.registration-table th:nth-child(5),
.registration-table td:nth-child(5) {
  width: 18%;
}

.fee-table th:last-child,
.fee-table td:last-child {
  width: 10%;
}

.text-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: #6068f1;
  font-size: 16px;
  cursor: pointer;
}

.text-link + .text-link {
  margin-left: 24px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 43px;
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
  box-sizing: border-box;
}

.pager-btn {
  cursor: pointer;
}

.pager-btn.active {
  border-color: #6269e8;
  background: #6269e8;
  color: #fff;
}

.pager-summary {
  margin-left: 2px;
}

.payment-list {
  margin-top: 34px;
}

.payment-item {
  display: grid;
  grid-template-columns: 70px 1fr 170px;
  min-height: 134px;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 28px;
  border-radius: 4px;
  background: #f8f8ff;
  box-sizing: border-box;
}

.pay-icon {
  position: relative;
  width: 46px;
  height: 46px;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  line-height: 46px;
  text-align: center;
}

.pay-icon.cash {
  width: 44px;
  height: 38px;
  border-radius: 5px;
  background: #6269e8;
}

.pay-icon.cash::before {
  position: absolute;
  top: 8px;
  right: -7px;
  width: 23px;
  height: 20px;
  border: 4px solid #fff;
  border-radius: 3px;
  content: "";
}

.pay-icon.cash::after {
  position: absolute;
  top: 17px;
  right: 1px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #fff;
  content: "";
}

.pay-icon.alipay {
  border-radius: 7px;
  background: #13a8e8;
}

.pay-icon.wechat {
  border-radius: 50%;
  background: #35b536;
}

.pay-icon.unionpay {
  width: 42px;
  height: 30px;
  border-radius: 3px;
  background: linear-gradient(90deg, #e62f38 0 33%, #1556aa 33% 66%, #18a977 66%);
  font-size: 8px;
  line-height: 30px;
}

.pay-icon.member {
  width: 44px;
  height: 36px;
  background: #ffc821;
  transform: rotate(45deg) scale(0.72);
}

.pay-icon.credit {
  border-radius: 4px;
  background: #40d47e;
}

.pay-copy h3 {
  margin: 0 0 12px;
  color: #111;
  font-size: 22px;
  font-weight: 700;
}

.pay-copy p {
  margin: 0;
  color: #999;
  font-size: 16px;
}

.payment-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 31px;
}

.payment-actions button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #6269e8;
  font-size: 16px;
  cursor: pointer;
}

.switch {
  position: relative;
  display: inline-flex;
  width: 52px;
  height: 30px;
  align-items: center;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: #c8cbd3;
  cursor: pointer;
}

.switch span {
  position: absolute;
  left: 2px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.18);
  transition: left 0.18s ease;
}

.switch.is-on {
  background: #43d383;
}

.switch.is-on span {
  left: 24px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.38);
}

.delete-dialog {
  position: relative;
  width: 536px;
  min-height: 226px;
  padding: 25px 26px 24px;
  background: #ffffff;
  border-radius: 5px;
  box-shadow: 0 2px 9px rgba(0, 0, 0, 0.28);
  box-sizing: border-box;
}

.delete-dialog h3 {
  margin: 0 0 28px;
  color: #111111;
  font-size: 20px;
  line-height: 1;
  font-weight: 500;
}

.dialog-close {
  position: absolute;
  top: 20px;
  right: 24px;
  border: 0;
  background: transparent;
  color: #cfcfcf;
  font-size: 34px;
  line-height: 1;
  cursor: pointer;
}

.dialog-content {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #1f2937;
  font-size: 18px;
}

.warning-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #ffc94d;
  color: #ffffff;
  font-size: 18px;
  font-weight: 800;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 60px;
}

.dialog-actions button {
  width: 84px;
  height: 43px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.cancel-btn {
  border: 1px solid #d2d2d2;
  background: #ffffff;
  color: #111111;
}

.confirm-btn {
  border: 1px solid #8b8df0;
  background: #8b8df0;
  color: #ffffff;
}

@media (max-width: 1200px) {
  .fee-card {
    width: calc(100% - 32px);
  }

  .filters {
    flex-wrap: wrap;
  }

  .search-filter {
    width: min(492px, 100%);
  }
}
</style>
