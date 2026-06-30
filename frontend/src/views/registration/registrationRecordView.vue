<template>
  <el-config-provider :locale="zhCn">
    <div class="registration-record-page">
    <section class="record-card">
      <header class="record-top">
        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            :class="['tab-btn', { active: activeTab === tab.value }]"
            type="button"
            @click="activeTab = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="top-actions">
          <button class="add-btn" type="button" @click="goAdd">
            <span class="plus-icon">+</span>
            <span>新增挂号</span>
          </button>
          <button class="export-btn" type="button">
            <span class="export-icon"></span>
            <span>导出</span>
          </button>
        </div>
      </header>

      <div class="divider"></div>

      <section class="filters">
        <label class="filter-item dept-filter">
          <span>挂号科室</span>
          <div class="select-wrap">
            <select v-model="filters.department">
              <option>全部</option>
              <option v-for="item in departmentOptions" :key="item" :value="item">{{ item }}</option>
            </select>
          </div>
        </label>

        <label class="filter-item doctor-filter">
          <span>挂号医生</span>
          <div class="select-wrap">
            <select v-model="filters.doctor">
              <option>全部</option>
              <option v-for="item in doctorOptions" :key="item" :value="item">{{ item }}</option>
            </select>
          </div>
        </label>

        <div class="filter-item date-filter">
          <span>就诊时间</span>
          <div class="date-box">
            <el-date-picker
              v-model="filters.visitRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
            />
          </div>
        </div>

        <label class="search-box">
          <input v-model="filters.keyword" placeholder="输入患者姓名" />
          <button type="button"><span class="search-icon"></span></button>
        </label>
      </section>

      <table class="record-table">
        <colgroup>
          <col class="c-index" />
          <col class="c-order" />
          <col class="c-name" />
          <col class="c-gender" />
          <col class="c-age" />
          <col class="c-phone" />
          <col class="c-dept" />
          <col class="c-doctor" />
          <col class="c-time" />
          <col class="c-money" />
          <col class="c-money" />
          <col class="c-status" />
          <col class="c-action" />
        </colgroup>
        <thead>
          <tr>
            <th>序号</th>
            <th>挂号单号</th>
            <th>患者姓名</th>
            <th>性别</th>
            <th>年龄</th>
            <th>手机号码</th>
            <th>科室</th>
            <th>接诊医生</th>
            <th>接诊时间</th>
            <th v-if="activeTab !== 'cancelled'">应收金额<br />（元）</th>
            <th v-else>实收金额<br />（元）</th>
            <th v-if="activeTab !== 'cancelled'">实收金额<br />（元）</th>
            <th v-else>退费金额<br />（元）</th>
            <th>就诊状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in pageRows" :key="`${activeTab}-${row.index}`">
            <td>{{ row.index }}</td>
            <td>{{ row.orderNo }}</td>
            <td>{{ row.name }}</td>
            <td>{{ row.gender }}</td>
            <td>{{ row.age }}</td>
            <td>{{ row.phone }}</td>
            <td>{{ row.department }}</td>
            <td>{{ row.doctor }}</td>
            <td>{{ row.time }}</td>
            <td>{{ row.receivable }}</td>
            <td>{{ row.received }}</td>
            <td><span class="status-text">{{ statusLabel }}</span></td>
            <td class="action-cell" @click="activeTab !== 'pending' && goDetail(row)">
              <template v-if="activeTab === 'pending'">
                <button type="button" @click="goUpdate(row)">编辑</button>
                <button type="button" @click="goConsultation(row)">就诊</button>
                <button type="button" @click="openCancelDialog(row)">退号</button>
              </template>
              <template v-else>
                <button type="button">查看详情</button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>

      <footer class="pagination-row">
        <button class="pager" type="button" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">&lt;</button>
        <button
          v-for="page in totalPages"
          :key="page"
          :class="['pager', { active: page === currentPage }]"
          type="button"
          @click="changePage(page)"
        >{{ page }}</button>
        <button class="pager" type="button" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">&gt;</button>
        <span class="page-text">每页{{ pageSize }}条，共{{ filteredRows.length }}条&nbsp;&nbsp;前往第</span>
        <span class="page-input">{{ currentPage }}</span>
        <span class="page-text">页</span>
      </footer>
    </section>

    <div v-if="cancelDialogVisible" class="cancel-mask">
      <section class="cancel-dialog">
        <button class="dialog-close" type="button" @click="closeCancelDialog"></button>
        <h3>确认退号</h3>
        <p>
          确认将挂号单号
          <strong>{{ selectedRow?.orderNo }}</strong>
          进行退号？
        </p>
        <div class="dialog-info">
          <span>患者姓名：{{ selectedRow?.name }}</span>
          <span>科室：{{ selectedRow?.department }}</span>
          <span>接诊医生：{{ selectedRow?.doctor }}</span>
          <span>实收金额：{{ selectedRow?.received }}元</span>
        </div>
        <div class="dialog-actions">
          <button class="dialog-cancel" type="button" @click="closeCancelDialog">取消</button>
          <button class="dialog-confirm" type="button" @click="confirmCancelRegistration">确认</button>
        </div>
      </section>
    </div>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import { registrationApi } from "@/api";
import { useClientPagination } from "@/composables/useClientPagination";

type TabValue = "pending" | "visited" | "cancelled";

type RowItem = {
  id: string;
  index: number;
  orderNo: string;
  name: string;
  gender: string;
  age: number;
  phone: string;
  department: string;
  doctor: string;
  time: string;
  receivable: string;
  received: string;
};

const router = useRouter();

const tabs: Array<{ label: string; value: TabValue }> = [
  { label: "未就诊", value: "pending" },
  { label: "已就诊", value: "visited" },
  { label: "已退号", value: "cancelled" },
];

const activeTab = ref<TabValue>("pending");
const cancelDialogVisible = ref(false);
const selectedRow = ref<RowItem | null>(null);

const filters = reactive({
  department: "全部",
  doctor: "全部",
  visitRange: [] as string[],
  keyword: "",
});

const rows: RowItem[] = Array.from({ length: 10 }, (_, index) => {
  const rowIndex = index + 1;
  const departments = ["全科", "内科", "儿科", "全科", "内科", "儿科", "全科", "内科", "儿科", "全科"];
  const doctors = ["王冕", "林鹤", "王冕", "林鹤", "王冕", "林鹤", "王冕", "林鹤", "王冕", "林鹤"];
  const genders = ["男", "女", "男", "女", "男", "女", "男", "女", "男", "女"];
  const ages = [12, 13, 14, 15, 16, 12, 13, 14, 15, 16];
  const amounts = ["20.00", "0.00", "30.00", "30.00", "30.00", "30.00", "30.00", "30.00", "30.00", "20.00"];

  return {
    id: String(10000 + rowIndex),
    index: rowIndex,
    orderNo: String(201912090031 - rowIndex),
    name: `姓名${rowIndex}`,
    gender: genders[index],
    age: ages[index],
    phone: String(17754138768 + rowIndex),
    department: departments[index],
    doctor: doctors[index],
    time: `2019-11-12 12:08:${rowIndex <= 5 ? "12" : String(7 + rowIndex).padStart(2, "0")}`,
    receivable: amounts[index],
    received: amounts[index],
  };
});

const statusLabel = computed(() => {
  if (activeTab.value === "pending") return "未就诊";
  if (activeTab.value === "visited") return "已就诊";
  return "已退号";
});

const remoteRows = ref<RowItem[]>([]);
const departmentOptions = computed(() => [...new Set(remoteRows.value.map((row) => row.department).filter(Boolean))]);
const doctorOptions = computed(() => [...new Set(remoteRows.value.map((row) => row.doctor).filter(Boolean))]);

const filteredRows = computed(() => remoteRows.value.filter((row) => {
  const matchDepartment = filters.department === "全部" || row.department === filters.department;
  const matchDoctor = filters.doctor === "全部" || row.doctor === filters.doctor;
  const rowDate = String(row.time || "").slice(0, 10);
  const matchDate = filters.visitRange.length !== 2
    || (rowDate >= filters.visitRange[0] && rowDate <= filters.visitRange[1]);
  const matchKeyword = !filters.keyword.trim() || row.name.includes(filters.keyword.trim());
  return matchDepartment && matchDoctor && matchDate && matchKeyword;
}));

const {
  pageSize,
  currentPage,
  totalPages,
  pageRows,
  changePage,
  resetPage,
} = useClientPagination(filteredRows, 10);

watch(
  () => [
    activeTab.value,
    filters.department,
    filters.doctor,
    filters.keyword,
    ...filters.visitRange,
  ],
  resetPage,
);

const goAdd = () => {
  router.push("/registration/add");
};

const goUpdate = (row: RowItem) => {
  router.push({ path: "/registration/update", state: { id: String(row.id) } });
};

const goConsultation = async (row: RowItem) => {
  await registrationApi.startVisit(row.id);
  router.push({ path: "/consultation", state: { registrationId: String(row.id) } });
};

const goDetail = (row: RowItem) => {
  router.push({
    path: "/registration/detail",
    state: { id: String(row.id) },
  });
};

const openCancelDialog = (row: RowItem) => {
  selectedRow.value = row;
  cancelDialogVisible.value = true;
};

const closeCancelDialog = () => {
  cancelDialogVisible.value = false;
};

const confirmCancelRegistration = async () => {
  if (selectedRow.value) await registrationApi.cancel(selectedRow.value.id, { remark: "前台退号" });
  closeCancelDialog();
  await loadRegistrations();
  ElMessage({
    message: "退号成功",
    type: "success",
    duration: 2000,
    showClose: false,
  });
};


const loadRegistrations = async () => {
  const response: any = await registrationApi.page({
    page: 1,
    size: 100,
    keyword: filters.keyword || undefined,
  });
  const statusMap: Record<TabValue, string[]> = {
    pending: ["PENDING", "WAITING"],
    visited: ["PROCESSING", "DONE", "COMPLETED", "FINISHED", "VISITED"],
    cancelled: ["CANCELLED"],
  };
  remoteRows.value = (response?.data?.records || [])
    .filter((item: any) => !item.status || statusMap[activeTab.value].includes(String(item.status).toUpperCase()))
    .map((item: any, index: number) => ({
      id: String(item.id),
      index: index + 1,
      orderNo: item.registrationNo || String(item.id),
      name: item.patientName || "",
      gender: item.gender || "",
      age: Number(item.age || 0),
      phone: item.phone || "",
      department: item.departmentName || "",
      doctor: item.doctorName || "",
      time: item.visitTime || item.createdAt || "",
      receivable: Number(
        item.receivableAmount
          ?? (Number(item.registrationFee || 0) + Number(item.diagnosisFee || 0))
      ).toFixed(2),
      received: Number(item.paidAmount ?? item.paidamount ?? item.paid_amount ?? 0).toFixed(2),
    }));
};

onMounted(loadRegistrations);
watch(activeTab, loadRegistrations);
</script>

<style scoped>
.registration-record-page {
  min-height: 100%;
  padding: 26px 0 58px;
}

.record-card {
  width: min(1710px, calc(100% - 80px));
  min-height: 1292px;
  margin: 0 auto;
  padding: 32px 33px 35px;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
  overflow: hidden;
}

.record-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tabs {
  display: flex;
  align-items: center;
}

.tab-btn {
  width: 130px;
  height: 46px;
  border: 2px solid #d0d0d0;
  background: #fff;
  color: #c7c7c7;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.tab-btn + .tab-btn {
  margin-left: -2px;
}

.tab-btn:first-child {
  border-radius: 4px 0 0 4px;
}

.tab-btn:last-child {
  border-radius: 0 4px 4px 0;
}

.tab-btn.active {
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

.top-actions {
  display: flex;
  gap: 13px;
}

.add-btn,
.export-btn {
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 11px;
  border-radius: 4px;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.add-btn {
  width: 139px;
  border: 0;
  background: #636be8;
  color: #fff;
}

.export-btn {
  width: 139px;
  border: 1px solid #636be8;
  background: #fff;
}

.plus-icon {
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff;
  color: #636be8;
  font-size: 20px;
  line-height: 1;
}

.export-icon {
  width: 20px;
  height: 20px;
  position: relative;
  box-sizing: border-box;
  border: 3px solid currentColor;
}

.export-icon::before {
  content: "";
  position: absolute;
  top: 5px;
  left: 4px;
  width: 10px;
  height: 3px;
  background: currentColor;
}

.export-icon::after {
  content: "";
  position: absolute;
  top: 2px;
  right: 2px;
  width: 7px;
  height: 7px;
  border-top: 3px solid currentColor;
  border-right: 3px solid currentColor;
  transform: rotate(45deg);
}

.divider {
  height: 2px;
  margin: 28px 0 32px;
  background: #eeeeee;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  column-gap: 34px;
  row-gap: 18px;
  margin-bottom: 31px;
  min-width: 0;
}

.filter-item {
  display: flex;
  align-items: center;
  min-width: 0;
}

.filter-item > span {
  margin-right: 16px;
  color: #111722;
  font-size: 19px;
  white-space: nowrap;
}

.dept-filter {
  flex: 0 0 284px;
}

.doctor-filter {
  flex: 0 0 286px;
}

.date-filter {
  flex: 0 0 398px;
}

.select-wrap,
.date-box,
.search-box {
  height: 44px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
}

.select-wrap {
  width: 200px;
  position: relative;
}

.select-wrap::after {
  content: "";
  position: absolute;
  top: 14px;
  right: 14px;
  width: 11px;
  height: 11px;
  border-right: 3px solid #cfcfcf;
  border-bottom: 3px solid #cfcfcf;
  transform: rotate(45deg);
  pointer-events: none;
}

.select-wrap select {
  width: 100%;
  height: 100%;
  padding: 0 42px 0 19px;
  border: 0;
  outline: none;
  appearance: none;
  background: transparent;
  color: #111722;
  font-size: 18px;
}

.date-box {
  width: 300px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
  border-color: #636be8;
  color: #111722;
  font-size: 18px;
  overflow: hidden;
}

.date-box :deep(.el-date-editor.el-input__wrapper) {
  width: 100%;
  height: 100%;
  padding: 0 10px 0 18px;
  box-sizing: border-box;
  border: 0;
  border-radius: 0;
  box-shadow: none;
  background: transparent;
}

.date-box :deep(.el-range-input) {
  color: #111722;
  font-size: 18px;
}

.date-box :deep(.el-range-separator) {
  flex: 0 0 22px;
  color: #111722;
  font-size: 18px;
}

.date-box :deep(.el-range__icon) {
  color: #cfcfcf;
  font-size: 18px;
}

.search-box {
  flex: 1 1 390px;
  min-width: 300px;
  max-width: 494px;
  margin-left: auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 61px;
  overflow: hidden;
}

.search-box input {
  min-width: 0;
  padding: 0 16px;
  border: 0;
  outline: none;
  color: #111722;
  font-size: 18px;
}

.search-box input::placeholder {
  color: #c7c7c7;
}

.search-box button {
  border: 0;
  background: #636be8;
  cursor: pointer;
}

.search-icon {
  width: 20px;
  height: 20px;
  display: inline-block;
  position: relative;
  box-sizing: border-box;
  border: 3px solid #fff;
  border-radius: 50%;
}

.search-icon::after {
  content: "";
  position: absolute;
  right: -8px;
  bottom: -5px;
  width: 10px;
  height: 3px;
  border-radius: 2px;
  background: #fff;
  transform: rotate(45deg);
}

.record-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.record-table th {
  height: 76px;
  background: #bbbef5;
  color: #000;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
}

.record-table td {
  height: 88px;
  border-bottom: 1px solid #eeeeee;
  color: #000;
  font-size: 18px;
  text-align: center;
}

.c-index { width: 6.1%; }
.c-order { width: 11.7%; }
.c-name { width: 9.4%; }
.c-gender { width: 5.1%; }
.c-age { width: 5.1%; }
.c-phone { width: 11.3%; }
.c-dept { width: 7%; }
.c-doctor { width: 7.1%; }
.c-time { width: 12.4%; }
.c-money { width: 6.3%; }
.c-status { width: 7.8%; }
.c-action { width: 10.6%; }

.status-text {
  color: #24cf95;
}

.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 13px;
  min-width: 0;
  white-space: normal;
}

.action-cell button {
  flex: 0 0 auto;
  margin: 0;
  border: 0;
  background: transparent;
  color: #636be8;
  font-size: 17px;
  line-height: 1;
  cursor: pointer;
}

.action-cell button:last-child {
  margin-right: 0;
}

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  padding-right: 5px;
  color: #111722;
  font-size: 17px;
}

.pager,
.page-input {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  border: 2px solid #b7b7b7;
  border-radius: 50%;
  background: #fff;
  color: #8b8b8b;
  font-size: 18px;
}

.pager.active {
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

.ellipsis {
  margin: 0 12px;
}

.page-text {
  white-space: nowrap;
}

.cancel-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.42);
}

.cancel-dialog {
  width: 520px;
  min-height: 250px;
  position: relative;
  box-sizing: border-box;
  padding: 28px 34px 28px;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 3px 14px rgba(0, 0, 0, 0.18);
}

.cancel-dialog h3 {
  margin: 0 0 22px;
  color: #111722;
  font-size: 22px;
  font-weight: 700;
}

.cancel-dialog p {
  margin: 0 0 18px;
  color: #111722;
  font-size: 18px;
  line-height: 1.7;
}

.cancel-dialog strong {
  color: #636be8;
  font-weight: 700;
}

.dialog-close {
  width: 28px;
  height: 28px;
  position: absolute;
  top: 24px;
  right: 25px;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.dialog-close::before,
.dialog-close::after {
  content: "";
  width: 26px;
  height: 3px;
  position: absolute;
  left: 1px;
  top: 13px;
  border-radius: 2px;
  background: #d7d7d7;
}

.dialog-close::before {
  transform: rotate(45deg);
}

.dialog-close::after {
  transform: rotate(-45deg);
}

.dialog-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 18px;
  padding: 16px 18px;
  background: #f0f1ff;
  color: #111722;
  font-size: 17px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  margin-top: 26px;
}

.dialog-cancel,
.dialog-confirm {
  width: 90px;
  height: 42px;
  border-radius: 4px;
  font-size: 17px;
  cursor: pointer;
}

.dialog-cancel {
  border: 1px solid #cfcfcf;
  background: #fff;
  color: #666;
}

.dialog-confirm {
  border: 0;
  background: #636be8;
  color: #fff;
  font-weight: 700;
}

@media (max-width: 1280px) {
  .record-card {
    width: calc(100% - 32px);
  }

  .filters {
    flex-wrap: wrap;
  }

  .search-box {
    margin-left: 0;
  }
}
</style>
