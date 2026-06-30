<template>
  <div class="charge-page">
    <section class="charge-card">
      <div class="charge-tabs" role="tablist" aria-label="收费状态">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          :class="['charge-tab', { 'is-active': activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="charge-divider"></div>

      <section class="filters-row" aria-label="筛选条件">
        <label class="filter-item order-filter">
          <span>订单类型</span>
          <select v-model="filters.orderType">
            <option value="">全部</option>
            <option value="处方开立">处方开立</option>
            <option value="药品零售">药品零售</option>
            <option value="挂号">挂号</option>
            <option value="诊疗收费">诊疗收费</option>
          </select>
        </label>

        <div class="filter-item date-filter">
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
            @clear="clearDateRange"
            @change="handleDateRangeChange"
          />
        </div>

        <label class="search-filter">
          <input v-model="filters.keyword" type="text" placeholder="输入订单编号/患者姓名" />
          <button type="button" aria-label="搜索">
            <Search class="search-icon" />
          </button>
        </label>
      </section>

      <div class="table-wrapper">
        <table :class="['charge-table', tableClass]">
          <thead>
            <tr>
              <th v-for="column in columns" :key="column.key" v-html="column.label"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageRows" :key="row.orderNo">
              <td v-for="column in columns" :key="column.key">
                <template v-if="column.key === 'actions'">
                  <div class="actions">
                    <button
                      v-for="action in row.actions"
                      :key="action"
                      type="button"
                      @click="handleAction(action, row)"
                    >
                      {{ action }}
                    </button>
                  </div>
                </template>
                <span v-else :class="statusClass(column.key)">
                  {{ row[column.key] }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="pagination" aria-label="分页">
        <button type="button" class="pager-btn" aria-label="上一页" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">&lt;</button>
        <button
          v-for="page in totalPages"
          :key="page"
          type="button"
          :class="['pager-btn', { 'is-active': page === currentPage }]"
          @click="changePage(page)"
        >
          {{ page }}
        </button>
        <button type="button" class="pager-btn" aria-label="下一页" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">&gt;</button>
        <span class="pager-info">每页{{ pageSize }}条，共{{ filteredRows.length }}条&nbsp;&nbsp;前往第</span>
        <span class="pager-input">{{ currentPage }}</span>
        <span class="pager-info">页</span>
      </footer>
    </section>

    <div v-if="deleteConfirmVisible" class="modal-mask">
      <section class="delete-dialog" role="dialog" aria-modal="true" aria-label="确认删除">
        <h2>确认删除</h2>
        <p>确定删除订单“{{ selectedRow?.orderNo }}”吗？</p>
        <footer class="dialog-actions">
          <button type="button" class="dialog-btn cancel" @click="deleteConfirmVisible = false">取消</button>
          <button type="button" class="dialog-btn danger" @click="confirmDelete">删除</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useClientPagination } from "@/composables/useClientPagination";
import { Search } from "@element-plus/icons-vue";
import { chargeOrderApi } from "@/api";

type TabKey = "pending" | "paid" | "refunded";

type ChargeRow = {
  id?: string | number;
  patientId?: string | number;
  index: number;
  orderNo: string;
  orderType: string;
  name: string;
  gender: string;
  age: string;
  phone: string;
  department: string;
  doctor: string;
  createdAt: string;
  amount?: number;
  paidAmount?: number;
  refundAmount?: number;
  payMethod?: string;
  refundMethod?: string;
  status: string;
  actions: string[];
};

type Column = {
  key: keyof ChargeRow | "actions";
  label: string;
};

const tabs = [
  { key: "pending" as const, label: "待收费" },
  { key: "paid" as const, label: "已收费" },
  { key: "refunded" as const, label: "已退费" },
];

const activeTab = ref<TabKey>("pending");
const router = useRouter();
const deleteConfirmVisible = ref(false);
const selectedRow = ref<ChargeRow | null>(null);

const filters = reactive({
  orderType: "",
  dateRange: [] as string[] | null,
  keyword: "",
});

const remoteRows = ref<ChargeRow[]>([]);

const clearDateRange = () => {
  filters.dateRange = [];
};

const handleDateRangeChange = (value: string[] | null) => {
  if (!Array.isArray(value)) {
    filters.dateRange = [];
  }
};

const currentDateRange = computed(() => Array.isArray(filters.dateRange) ? filters.dateRange : []);

const chargeTypeLabel = (value: string) => {
  const raw = String(value || "");
  const upper = raw.toUpperCase();
  if (/REGISTRATION|REGISTER/.test(upper) || /挂号/.test(raw)) return "挂号";
  if (/DRUG_RETAIL|RETAIL/.test(upper) || /药品零售|零售/.test(raw)) return "药品零售";
  if (/PRESCRIPTION|OUTPATIENT/.test(upper) || /处方|门诊/.test(raw)) return "处方开立";
  if (/DIAGNOSIS|TREATMENT|CONSULT/.test(upper) || /诊疗/.test(raw)) return "诊疗收费";
  return raw || "-";
};

const matchChargeType = (rowType: string, filterType: string) => {
  if (!filterType) return true;
  const raw = String(rowType || "");
  return chargeTypeLabel(raw) === filterType || raw === filterType || raw.toUpperCase() === filterType.toUpperCase();
};

const rowsByTab = computed<Record<TabKey, ChargeRow[]>>(() => ({
  pending: remoteRows.value
    .filter((row) => ["PENDING", "PARTIAL"].includes(String(row.status).toUpperCase()))
    .map((row) => ({
    ...row,
    status: "待收费",
    actions: ["收费", "编辑", "删除"],
  })),
  paid: remoteRows.value
    .filter((row) => String(row.status).toUpperCase() === "PAID")
    .map((row) => ({
    ...row,
    status: "已收费",
    actions: ["查看", "退费"],
  })),
  refunded: remoteRows.value
    .filter((row) => String(row.status).toUpperCase() === "REFUNDED" || Number(row.refundAmount || 0) > 0)
    .map((row) => ({
    ...row,
    status: "已退费",
    actions: ["查看"],
  })),
}));

onMounted(async () => {
  try {
    const response = await chargeOrderApi.page({ page: 1, size: 200 });
    remoteRows.value = (response.data.records || []).map((item: any, index: number) => ({
      id: item.id,
      patientId: item.patientId,
      index: index + 1,
      orderNo: item.orderNo || String(item.id || ""),
      orderType: chargeTypeLabel(item.chargeType || ""),
      name: item.patientName || "",
      gender: item.gender || item.patientGender || "",
      age: String(item.age ?? item.patientAge ?? ""),
      phone: item.phone || item.patientPhone || "",
      department: item.departmentName || "",
      doctor: item.doctorName || "",
      createdAt: item.createdAt || "",
      amount: Number(item.receivableAmount || 0),
      paidAmount: Number(item.paidAmount || 0),
      refundAmount: Number(item.refundAmount || 0),
      payMethod: item.payMethod || "",
      refundMethod: item.refundMethod || "",
      status: item.status || "PENDING",
      actions: [],
    }));
  } catch {
    remoteRows.value = [];
  }
});

const pendingColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "orderNo", label: "订单编号" },
  { key: "orderType", label: "订单类型" },
  { key: "name", label: "姓名" },
  { key: "gender", label: "性别" },
  { key: "age", label: "年龄" },
  { key: "phone", label: "手机号码" },
  { key: "department", label: "科室" },
  { key: "doctor", label: "接诊医生" },
  { key: "createdAt", label: "创建时间" },
  { key: "amount", label: "应收金额" },
  { key: "status", label: "收费状态" },
  { key: "actions", label: "操作" },
];

const paidColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "orderNo", label: "订单编号" },
  { key: "orderType", label: "订单类型" },
  { key: "name", label: "姓名" },
  { key: "gender", label: "性别" },
  { key: "age", label: "年龄" },
  { key: "phone", label: "手机号码" },
  { key: "department", label: "科室" },
  { key: "doctor", label: "接诊医生" },
  { key: "createdAt", label: "创建时间" },
  { key: "amount", label: "应收金额<br>（元）" },
  { key: "paidAmount", label: "实收金额<br>（元）" },
  { key: "payMethod", label: "支付方式" },
  { key: "status", label: "收费状态" },
  { key: "actions", label: "操作" },
];

const refundedColumns: Column[] = [
  { key: "index", label: "序号" },
  { key: "orderNo", label: "订单编号" },
  { key: "orderType", label: "订单类型" },
  { key: "name", label: "姓名" },
  { key: "gender", label: "性别" },
  { key: "age", label: "年龄" },
  { key: "phone", label: "手机号码" },
  { key: "department", label: "科室" },
  { key: "doctor", label: "接诊医生" },
  { key: "createdAt", label: "创建时间" },
  { key: "refundAmount", label: "退费金额<br>（元）" },
  { key: "refundMethod", label: "退费方式" },
  { key: "status", label: "收费状态" },
  { key: "actions", label: "操作" },
];

const columns = computed(() => {
  if (activeTab.value === "paid") {
    return paidColumns;
  }

  if (activeTab.value === "refunded") {
    return refundedColumns;
  }

  return pendingColumns;
});

const tableClass = computed(() => `table-${activeTab.value}`);

const filteredRows = computed(() => {
  const keyword = filters.keyword.trim();

  return rowsByTab.value[activeTab.value].filter((row) => {
    const matchesType = matchChargeType(row.orderType, filters.orderType);
    const rowDate = String(row.createdAt || "").slice(0, 10);
    const range = currentDateRange.value;
    const matchesDate = range.length !== 2
      || (rowDate >= range[0] && rowDate <= range[1]);
    const matchesKeyword =
      !keyword || row.orderNo.includes(keyword) || row.name.includes(keyword);

    return matchesType && matchesDate && matchesKeyword;
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
  () => [activeTab.value, filters.orderType, filters.keyword, ...currentDateRange.value],
  resetPage,
);

const statusClass = (key: string) => {
  if (key !== "status") {
    return "";
  }

  return {
    pending: "status-pending",
    paid: "status-paid",
    refunded: "status-refunded",
  }[activeTab.value];
};

const handleAction = (action: string, row: ChargeRow) => {
  if (activeTab.value === "pending" && action === "收费") {
    router.push({
      path: "/charge/payment",
      state: {
        id: row.id || row.orderNo,
      },
    });
    return;
  }

  if (activeTab.value === "pending" && action === "编辑") {
    router.push({
      path: "/consultation",
      state: { patientId: row.patientId },
    });
    return;
  }

  if (activeTab.value === "pending" && action === "删除") {
    selectedRow.value = row;
    deleteConfirmVisible.value = true;
    return;
  }

  if (activeTab.value === "paid" && action === "查看") {
    router.push({
      name: "StatisticsChargeDetail",
      state: { id: String(row.id || row.orderNo) },
    });
    return;
  }

  if (activeTab.value === "paid" && action === "退费") {
    router.push({
      name: "ChargeRefund",
      query: { id: String(row.id || row.orderNo) },
      state: { id: String(row.id || row.orderNo) },
    });
    return;
  }

  if (activeTab.value === "refunded" && action === "查看") {
    router.push({
      name: "ChargeRefundDetail",
      query: { id: String(row.id || row.orderNo) },
      state: { id: String(row.id || row.orderNo) },
    });
  }
};

const confirmDelete = async () => {
  const target = selectedRow.value;
  if (!target) {
    deleteConfirmVisible.value = false;
    return;
  }

  await chargeOrderApi.remove(target.id || target.orderNo);
  const remoteIndex = remoteRows.value.findIndex((row) => row.orderNo === target.orderNo);
  if (remoteIndex >= 0) {
    remoteRows.value.splice(remoteIndex, 1);
  }
  deleteConfirmVisible.value = false;
};
</script>

<style scoped>
.charge-page {
  min-height: 100%;
  padding: 21px 0 56px;
}

.charge-card {
  width: min(1548px, calc(100% - 96px));
  min-height: 1158px;
  margin: 0 auto;
  padding: 28px 30px 39px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.charge-tabs {
  display: inline-flex;
  overflow: hidden;
  border: 1px solid #c6c8cf;
  border-radius: 4px;
}

.charge-tab {
  width: 128px;
  height: 43px;
  border: 0;
  border-right: 1px solid #c6c8cf;
  background: #fff;
  color: #b8b8b8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.charge-tab:last-child {
  border-right: 0;
}

.charge-tab.is-active {
  background: #636be8;
  color: #fff;
}

.charge-divider {
  height: 1px;
  margin: 28px 0 30px;
  background: #eeeeee;
}

.filters-row {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 30px;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  color: #1e2533;
  font-size: 18px;
  white-space: nowrap;
}

.filter-item select,
.date-control,
.search-filter {
  height: 44px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
}

.filter-item select {
  width: 187px;
  padding: 0 49px 0 18px;
  appearance: none;
  background:
    linear-gradient(45deg, transparent 50%, #d4d4d4 50%) calc(100% - 24px) 18px / 9px 9px no-repeat,
    linear-gradient(135deg, #d4d4d4 50%, transparent 50%) calc(100% - 16px) 18px / 9px 9px no-repeat,
    #fff;
  color: #1d2430;
  font-size: 16px;
  outline: none;
}

.date-control {
  position: relative;
  width: 286px;
  display: flex;
  align-items: center;
}

.date-control input {
  width: 100%;
  height: 100%;
  padding: 0 48px 0 17px;
  border: 0;
  outline: none;
  color: #2f3541;
  font-size: 16px;
}

.date-icon {
  position: absolute;
  right: 13px;
  width: 22px;
  height: 22px;
  color: #c7c7c7;
  pointer-events: none;
}

.search-filter {
  display: grid;
  grid-template-columns: 402px 60px;
  overflow: hidden;
}

.search-filter input {
  min-width: 0;
  padding: 0 14px;
  border: 0;
  outline: none;
  color: #2f3541;
  font-size: 16px;
}

.search-filter input::placeholder {
  color: #c5c5c5;
}

.search-filter button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  background: #636be8;
  color: #fff;
  cursor: pointer;
}

.search-icon {
  width: 27px;
  height: 27px;
}

.table-wrapper {
  overflow: hidden;
}

.charge-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.charge-table th {
  height: 69px;
  padding: 0 6px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.25;
  text-align: center;
  white-space: nowrap;
}

.charge-table td {
  height: 82px;
  padding: 0 6px;
  border-bottom: 1px solid #eeeeee;
  color: #000;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.table-pending th:nth-child(1),
.table-pending td:nth-child(1),
.table-paid th:nth-child(1),
.table-paid td:nth-child(1),
.table-refunded th:nth-child(1),
.table-refunded td:nth-child(1) {
  width: 6%;
}

.table-pending th:nth-child(2),
.table-pending td:nth-child(2),
.table-paid th:nth-child(2),
.table-paid td:nth-child(2),
.table-refunded th:nth-child(2),
.table-refunded td:nth-child(2) {
  width: 10%;
}

.table-pending th:nth-child(3),
.table-pending td:nth-child(3),
.table-paid th:nth-child(3),
.table-paid td:nth-child(3),
.table-refunded th:nth-child(3),
.table-refunded td:nth-child(3) {
  width: 7.2%;
}

.table-pending th:nth-child(4),
.table-pending td:nth-child(4),
.table-paid th:nth-child(4),
.table-paid td:nth-child(4),
.table-refunded th:nth-child(4),
.table-refunded td:nth-child(4),
.table-pending th:nth-child(5),
.table-pending td:nth-child(5),
.table-paid th:nth-child(5),
.table-paid td:nth-child(5),
.table-refunded th:nth-child(5),
.table-refunded td:nth-child(5),
.table-pending th:nth-child(6),
.table-pending td:nth-child(6),
.table-paid th:nth-child(6),
.table-paid td:nth-child(6),
.table-refunded th:nth-child(6),
.table-refunded td:nth-child(6),
.table-pending th:nth-child(8),
.table-pending td:nth-child(8),
.table-paid th:nth-child(8),
.table-paid td:nth-child(8),
.table-refunded th:nth-child(8),
.table-refunded td:nth-child(8) {
  width: 5.5%;
}

.table-pending th:nth-child(7),
.table-pending td:nth-child(7),
.table-paid th:nth-child(7),
.table-paid td:nth-child(7),
.table-refunded th:nth-child(7),
.table-refunded td:nth-child(7) {
  width: 9%;
}

.table-pending th:nth-child(9),
.table-pending td:nth-child(9),
.table-paid th:nth-child(9),
.table-paid td:nth-child(9),
.table-refunded th:nth-child(9),
.table-refunded td:nth-child(9) {
  width: 6%;
}

.table-pending th:nth-child(10),
.table-pending td:nth-child(10),
.table-paid th:nth-child(10),
.table-paid td:nth-child(10),
.table-refunded th:nth-child(10),
.table-refunded td:nth-child(10) {
  width: 12%;
}

.table-pending th:nth-child(11),
.table-pending td:nth-child(11),
.table-pending th:nth-child(12),
.table-pending td:nth-child(12) {
  width: 8%;
}

.table-pending th:nth-child(13),
.table-pending td:nth-child(13) {
  width: 13%;
}

.table-paid th:nth-child(11),
.table-paid td:nth-child(11),
.table-paid th:nth-child(12),
.table-paid td:nth-child(12) {
  width: 5.5%;
}

.table-paid th:nth-child(13),
.table-paid td:nth-child(13),
.table-paid th:nth-child(14),
.table-paid td:nth-child(14) {
  width: 6.2%;
}

.table-paid th:nth-child(15),
.table-paid td:nth-child(15) {
  width: 8.4%;
}

.table-refunded th:nth-child(11),
.table-refunded td:nth-child(11),
.table-refunded th:nth-child(12),
.table-refunded td:nth-child(12),
.table-refunded th:nth-child(13),
.table-refunded td:nth-child(13) {
  width: 8%;
}

.table-refunded th:nth-child(14),
.table-refunded td:nth-child(14) {
  width: 6.3%;
}

.status-pending {
  color: #23c783;
}

.status-paid {
  color: #ffb800;
}

.status-refunded {
  color: #8f8f8f;
}

.actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 21px;
}

.actions button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #636be8;
  font-size: 16px;
  cursor: pointer;
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.42);
}

.delete-dialog {
  width: 420px;
  padding: 28px 30px 26px;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(18, 25, 48, 0.18);
}

.delete-dialog h2 {
  margin: 0;
  color: #111722;
  font-size: 24px;
  font-weight: 800;
}

.delete-dialog p {
  margin: 26px 0 0;
  color: #333a45;
  font-size: 18px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 26px;
}

.dialog-btn {
  min-width: 84px;
  height: 42px;
  border-radius: 5px;
  border: 1px solid #d6d6d6;
  background: #fff;
  color: #333a45;
  font-size: 16px;
  cursor: pointer;
}

.dialog-btn.danger {
  border-color: #f15b5b;
  background: #f15b5b;
  color: #fff;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 26px;
  color: #1e2533;
  font-size: 16px;
}

.pager-btn,
.pager-input {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
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

.pager-btn.is-active {
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

.pager-ellipsis {
  margin: 0 6px 0 12px;
}

.pager-info {
  color: #222;
}

@media (max-width: 1280px) {
  .charge-card {
    width: calc(100% - 32px);
    padding: 24px 18px 36px;
  }

  .filters-row,
  .pagination {
    flex-wrap: wrap;
  }
}
</style>
