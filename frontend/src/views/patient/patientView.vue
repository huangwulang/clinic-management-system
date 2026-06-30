<template>
  <div class="patient-page">
    <section class="patient-card">
      <header class="page-toolbar">
        <button type="button" class="top-btn primary" @click="goToAddPatient">
          <CirclePlusFilled class="btn-icon" />
          <span>新增患者</span>
        </button>
        <button type="button" class="top-btn outline">
          <Upload class="btn-icon" />
          <span>导出</span>
        </button>
      </header>

      <div class="patient-divider"></div>

      <section class="filters-row" aria-label="筛选条件">
        <label class="filter-item member-filter">
          <span>会员类型</span>
          <select v-model="filters.memberType">
            <option value="">全部</option>
            <option value="非会员">非会员</option>
            <option value="VIP1">VIP1</option>
            <option value="VIP2">VIP2</option>
            <option value="VIP3">VIP3</option>
            <option value="VIP4">VIP4</option>
            <option value="VIP5">VIP5</option>
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
          />
        </div>

        <label class="search-filter">
          <input
            v-model="filters.keyword"
            type="text"
            placeholder="患者姓名/手机号码"
          />
          <button type="button" aria-label="搜索">
            <Search class="search-icon" />
          </button>
        </label>
      </section>

      <div class="table-wrapper">
        <table class="patient-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>患者编码</th>
              <th>患者姓名</th>
              <th>性别</th>
              <th>年龄</th>
              <th>手机号码</th>
              <th>会员等级</th>
              <th>创建时间</th>
              <th>操作人员</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pageRows" :key="row.code">
              <td>{{ row.index }}</td>
              <td>{{ row.code }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.gender }}</td>
              <td>{{ row.age }}</td>
              <td>{{ row.phone }}</td>
              <td><span class="member-level">{{ row.level }}</span></td>
              <td>{{ row.createdAt }}</td>
              <td>{{ row.operator }}</td>
              <td>
                <div class="actions">
                  <button type="button" @click="goToConsultation(row)">接诊</button>
                  <button type="button" @click="goToEditPatient(row)">编辑</button>
                  <button type="button" @click="openMemberDialog(row)">设置会员</button>
                  <button type="button" @click="openDeleteConfirm(row)">删除</button>
                </div>
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
          :class="['pager-btn', { 'is-active': currentPage === page }]"
          @click="changePage(page)"
        >{{ page }}</button>
        <button type="button" class="pager-btn" aria-label="下一页" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">&gt;</button>
        <span class="pager-info">每页{{ pageSize }}条&nbsp;&nbsp;共{{ filteredRows.length }}条&nbsp;&nbsp;前往第</span>
        <span class="pager-input">{{ currentPage }}</span>
        <span class="pager-info">页</span>
      </footer>
    </section>

    <div v-if="memberDialogVisible" class="modal-mask">
      <section class="member-dialog" role="dialog" aria-modal="true" aria-label="设置会员等级">
        <button type="button" class="dialog-close" aria-label="关闭" @click="memberDialogVisible = false">×</button>
        <h2>设置会员等级</h2>

        <div class="member-meta">
          <span>会员卡号： {{ selectedPatient?.cardNo || "201909890090" }}</span>
          <span>会员姓名： {{ selectedPatient?.name || "" }}</span>
        </div>

        <div class="expire-row">
          <div class="expire-date-field">
            <span>到期时间</span>
            <el-date-picker
              v-model="memberExpireDate"
              class="expire-control"
              type="date"
              placeholder="请选择到期日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
              :disabled="neverExpire"
            />
          </div>
          <label class="never-row">
            <input v-model="neverExpire" type="checkbox" />
            <span>永不过期</span>
          </label>
        </div>

        <table class="member-table">
          <thead>
            <tr>
              <th></th>
              <th>会员等级</th>
              <th>会员名称</th>
              <th>折扣</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="level in memberLevels" :key="level.level">
              <td>
                <input v-model="selectedMemberLevel" type="radio" :value="level.level" />
              </td>
              <td><strong>{{ level.level }}</strong></td>
              <td>{{ level.name }}</td>
              <td>{{ level.discount }}</td>
            </tr>
          </tbody>
        </table>

        <footer class="dialog-actions">
          <button type="button" class="dialog-btn cancel" @click="memberDialogVisible = false">取消</button>
          <button type="button" class="dialog-btn confirm" @click="confirmMemberLevel">确定</button>
        </footer>
      </section>
    </div>

    <div v-if="deleteConfirmVisible" class="modal-mask">
      <section class="delete-dialog" role="dialog" aria-modal="true" aria-label="确认删除">
        <h2>确认删除</h2>
        <p>确定删除患者“{{ selectedPatient?.name }}”吗？</p>
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
import {
  CirclePlusFilled,
  Search,
  Upload,
} from "@element-plus/icons-vue";
import { patientApi } from "@/api";
import { ElMessage } from "element-plus";
import { useClientPagination } from "@/composables/useClientPagination";

type PatientRow = {
  index: number;
  id: string;
  code: string;
  name: string;
  gender: string;
  age: number;
  phone: string;
  level: string;
  memberExpireDate?: string;
  createdAt: string;
  operator: string;
  cardNo?: string;
  birthday?: string;
};

const router = useRouter();

const filters = reactive({
  memberType: "",
  dateRange: [] as string[] | null,
  keyword: "",
});

const rows: PatientRow[] = [
  {
    index: 1,
    id: "patient-1",
    code: "100130",
    name: "姓名1",
    gender: "男",
    age: 12,
    phone: "17754138769",
    level: "非会员",
    createdAt: "2019-11-12 12:08:12",
    operator: "林鹤",
    cardNo: "201909890090",
    birthday: "1998-10-09",
  },
  {
    index: 2,
    id: "patient-2",
    code: "100129",
    name: "姓名3",
    gender: "男",
    age: 14,
    phone: "17754138771",
    level: "VIP2",
    createdAt: "2019-11-12 12:08:12",
    operator: "王冕",
    cardNo: "201909890091",
    birthday: "1997-10-09",
  },
  {
    index: 3,
    id: "patient-3",
    code: "100128",
    name: "姓名5",
    gender: "男",
    age: 16,
    phone: "17754138773",
    level: "VIP3",
    createdAt: "2019-11-12 12:08:12",
    operator: "林鹤",
    cardNo: "201909890092",
    birthday: "1996-10-09",
  },
  {
    index: 4,
    id: "patient-4",
    code: "100127",
    name: "姓名7",
    gender: "男",
    age: 13,
    phone: "17754138775",
    level: "VIP4",
    createdAt: "2019-11-12 12:08:14",
    operator: "王冕",
    cardNo: "201909890093",
    birthday: "1995-10-09",
  },
  {
    index: 5,
    id: "patient-5",
    code: "100126",
    name: "姓名9",
    gender: "男",
    age: 15,
    phone: "17754138777",
    level: "非会员",
    createdAt: "2019-11-12 12:08:16",
    operator: "林鹤",
    cardNo: "201909890094",
    birthday: "1994-10-09",
  },
  {
    index: 6,
    id: "patient-6",
    code: "100125",
    name: "姓名11",
    gender: "男",
    age: 17,
    phone: "17754138779",
    level: "VIP7",
    createdAt: "2019-11-12 12:08:18",
    operator: "王冕",
    cardNo: "201909890095",
    birthday: "1993-10-09",
  },
  {
    index: 7,
    id: "patient-7",
    code: "100124",
    name: "姓名13",
    gender: "男",
    age: 19,
    phone: "17754138781",
    level: "VIP1",
    createdAt: "2019-11-12 12:08:20",
    operator: "林鹤",
    cardNo: "201909890096",
    birthday: "1992-10-09",
  },
  {
    index: 8,
    id: "patient-8",
    code: "100123",
    name: "姓名15",
    gender: "男",
    age: 21,
    phone: "17754138783",
    level: "VIP2",
    createdAt: "2019-11-12 12:08:22",
    operator: "王冕",
    cardNo: "201909890097",
    birthday: "1991-10-09",
  },
  {
    index: 9,
    id: "patient-9",
    code: "100122",
    name: "姓名17",
    gender: "男",
    age: 23,
    phone: "17754138785",
    level: "VIP3",
    createdAt: "2019-11-12 12:08:24",
    operator: "林鹤",
    cardNo: "201909890098",
    birthday: "1990-10-09",
  },
  {
    index: 10,
    id: "patient-10",
    code: "100121",
    name: "姓名19",
    gender: "男",
    age: 25,
    phone: "17754138787",
    level: "VIP4",
    createdAt: "2019-11-12 12:08:26",
    operator: "王冕",
    cardNo: "201909890099",
    birthday: "1989-10-09",
  },
];

const remoteRows = ref<PatientRow[]>([]);
const memberDialogVisible = ref(false);
const deleteConfirmVisible = ref(false);
const selectedPatient = ref<PatientRow | null>(null);
const selectedMemberLevel = ref("VIP1");
const memberExpireDate = ref("");
const neverExpire = ref(false);

const memberLevels = [
  { level: "VIP1", name: "初级会员", discount: "9.80" },
  { level: "VIP2", name: "高级会员", discount: "9.50" },
  { level: "VIP3", name: "白银会员", discount: "9.00" },
  { level: "VIP4", name: "黄金会员", discount: "8.80" },
  { level: "VIP5", name: "钻石会员", discount: "8.50" },
];

const filteredRows = computed(() => {
  const keyword = filters.keyword.trim();
  const sourceRows = remoteRows.value;

  return sourceRows.filter((row) => {
    const matchesType = !filters.memberType || row.level === filters.memberType;
    const rowDate = String(row.createdAt || "").slice(0, 10);
    const matchesDate = !Array.isArray(filters.dateRange) || filters.dateRange.length !== 2
      || (rowDate >= filters.dateRange[0] && rowDate <= filters.dateRange[1]);
    const matchesKeyword =
      !keyword ||
      row.name.includes(keyword) ||
      row.phone.includes(keyword) ||
      row.code.includes(keyword);

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
  () => [filters.memberType, filters.keyword, ...(filters.dateRange || [])],
  resetPage,
);

onMounted(async () => {
  try {
    const response = await patientApi.page({ page: 1, size: 50 });
    remoteRows.value = (response.data.records || []).map((item: any, index: number) => ({
      index: index + 1,
      id: String(item.id || item.patientCode || `patient-${index + 1}`),
      code: item.patientCode || String(item.id || ""),
      name: item.name || "",
      gender: item.gender || "",
      age: item.age || 0,
      phone: item.phone || "",
      level: item.memberLevel || "非会员",
      memberExpireDate: item.memberExpireDate || "",
      createdAt: item.createdAt || "",
      operator: item.operator || "",
      cardNo: item.cardNo || item.memberCardNo || "",
      birthday: item.birthday || item.birthDate || "",
    }));
  } catch {
    remoteRows.value = [];
  }
});

const goToAddPatient = () => {
  router.push("/patient/add");
};

const goToConsultation = (row: PatientRow) => {
  router.push({
    path: "/consultation",
    state: { patientId: String(row.id) },
  });
};

const goToEditPatient = (row: PatientRow) => {
  router.push({
    path: "/patient/edit",
    state: { id: String(row.id) },
  });
};

const openMemberDialog = (row: PatientRow) => {
  selectedPatient.value = row;
  selectedMemberLevel.value = row.level.startsWith("VIP") ? row.level : "VIP1";
  memberExpireDate.value = row.memberExpireDate || "";
  neverExpire.value = !row.memberExpireDate;
  memberDialogVisible.value = true;
};

const clearDateRange = () => {
  filters.dateRange = [];
};

const confirmMemberLevel = async () => {
  if (selectedPatient.value) {
    const selectedLevel = memberLevels.find((item) => item.level === selectedMemberLevel.value);
    if (!selectedLevel) {
      ElMessage.warning("请选择会员等级");
      return;
    }
    await patientApi.setMemberLevel(selectedPatient.value.id, {
      levelCode: selectedLevel.level,
      memberName: selectedLevel.name,
      expireDate: neverExpire.value ? null : memberExpireDate.value || null,
    });
    selectedPatient.value.level = selectedMemberLevel.value;
    selectedPatient.value.memberExpireDate = neverExpire.value ? "" : memberExpireDate.value;
    ElMessage.success("会员等级设置成功");
  }
  memberDialogVisible.value = false;
};

const openDeleteConfirm = (row: PatientRow) => {
  selectedPatient.value = row;
  deleteConfirmVisible.value = true;
};

const confirmDelete = async () => {
  const target = selectedPatient.value;
  if (target) {
    await patientApi.remove(target.id);
    const remoteIndex = remoteRows.value.findIndex((row) => row.id === target.id);
    if (remoteIndex >= 0) {
      remoteRows.value.splice(remoteIndex, 1);
    } else {
      const localIndex = rows.findIndex((row) => row.id === target.id);
      if (localIndex >= 0) rows.splice(localIndex, 1);
    }
  }
  deleteConfirmVisible.value = false;
  ElMessage.success("患者删除成功");
};
</script>

<style scoped>
.patient-page {
  min-height: 100%;
  padding: 26px 0 104px;
}

.patient-card {
  width: min(1500px, calc(100% - 96px));
  margin: 0 auto;
  padding: 26px 26px 40px;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.page-toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}

.top-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 38px;
  padding: 0 16px;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.top-btn.primary {
  min-width: 121px;
  border: 1px solid #636be8;
  background: #636be8;
  color: #fff;
}

.top-btn.outline {
  min-width: 120px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
}

.btn-icon {
  width: 21px;
  height: 21px;
}

.patient-divider {
  height: 1px;
  margin: 25px 0 26px;
  background: #eeeeee;
}

.filters-row {
  display: flex;
  align-items: center;
  gap: 26px;
  margin-bottom: 26px;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  color: #1e2533;
  font-size: 16px;
  white-space: nowrap;
}

.filter-item select,
.date-control,
.search-filter {
  height: 40px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  background: #fff;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.02);
}

.filter-item select {
  width: 162px;
  padding: 0 17px;
  color: #2f3541;
  font-size: 16px;
  outline: none;
}

.date-control {
  position: relative;
  width: 252px;
  display: flex;
  align-items: center;
}

.date-control input {
  width: 100%;
  height: 100%;
  padding: 0 42px 0 17px;
  border: 0;
  outline: none;
  color: #2f3541;
  font-size: 16px;
}

.date-icon {
  position: absolute;
  right: 13px;
  width: 20px;
  height: 20px;
  color: #c7c7c7;
  pointer-events: none;
}

.search-filter {
  display: grid;
  grid-template-columns: 344px 52px;
  overflow: hidden;
}

.search-filter input {
  height: 100%;
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
  border-left: 1px solid #c9c9c9;
  background: #636be8;
  color: #fff;
  cursor: pointer;
}

.search-icon {
  width: 24px;
  height: 24px;
}

.table-wrapper {
  overflow-x: auto;
  overflow-y: hidden;
}

.patient-table {
  width: 100%;
  min-width: 1260px;
  border-collapse: collapse;
  table-layout: fixed;
}

.patient-table th,
.patient-table td {
  box-sizing: border-box;
}

.patient-table th {
  height: 62px;
  padding: 0 8px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
}

.patient-table td {
  height: 72px;
  padding: 0 8px;
  border-bottom: 1px solid #eeeeee;
  color: #000;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
}

.patient-table th:nth-child(1),
.patient-table td:nth-child(1) {
  width: 64px;
}

.patient-table th:nth-child(2),
.patient-table td:nth-child(2) {
  width: 112px;
}

.patient-table th:nth-child(3),
.patient-table td:nth-child(3) {
  width: 110px;
}

.patient-table th:nth-child(4),
.patient-table td:nth-child(4),
.patient-table th:nth-child(5),
.patient-table td:nth-child(5) {
  width: 68px;
}

.patient-table th:nth-child(6),
.patient-table td:nth-child(6) {
  width: 135px;
}

.patient-table th:nth-child(7),
.patient-table td:nth-child(7) {
  width: 112px;
}

.patient-table th:nth-child(8),
.patient-table td:nth-child(8) {
  width: 180px;
}

.patient-table th:nth-child(9),
.patient-table td:nth-child(9) {
  width: 110px;
}

.patient-table th:nth-child(10),
.patient-table td:nth-child(10) {
  width: 240px;
}

.member-level {
  color: #ffb300;
  font-weight: 700;
  font-style: italic;
}

.actions {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  white-space: nowrap;
}

.actions button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #6c73e9;
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

.member-dialog,
.delete-dialog {
  position: relative;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(18, 25, 48, 0.18);
}

.member-dialog {
  width: 626px;
  min-height: 546px;
  padding: 25px 34px 30px;
}

.member-dialog h2,
.delete-dialog h2 {
  margin: 0;
  color: #111722;
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}

.dialog-close {
  position: absolute;
  top: 21px;
  right: 31px;
  width: 32px;
  height: 32px;
  border: 0;
  background: transparent;
  color: #cfcfcf;
  font-size: 42px;
  font-weight: 300;
  line-height: 28px;
  cursor: pointer;
}

.member-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin-top: 27px;
  color: #333a45;
  font-size: 16px;
}

.expire-row {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-top: 25px;
}

.expire-row label,
.expire-date-field {
  display: inline-flex;
  align-items: center;
  gap: 20px;
  color: #333a45;
  font-size: 16px;
}

.expire-control {
  position: relative;
  width: 297px;
  height: 45px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  background: #fff;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.04);
}

.expire-control input {
  width: 100%;
  height: 100%;
  padding: 0 44px 0 14px;
  box-sizing: border-box;
  border: 0;
  outline: none;
  color: #111722;
  font-size: 16px;
}

.expire-icon {
  position: absolute;
  right: 15px;
  top: 12px;
  width: 22px;
  height: 22px;
  color: #c7c7c7;
  pointer-events: none;
}

.never-row {
  gap: 8px !important;
}

.never-row input {
  width: 17px;
  height: 17px;
  accent-color: #636be8;
}

.member-table {
  width: 100%;
  margin-top: 35px;
  border-collapse: collapse;
  table-layout: fixed;
}

.member-table th,
.member-table td {
  border: 1px solid #e2e5ff;
  text-align: center;
}

.member-table th {
  height: 54px;
  background: #d7d9f7;
  color: #111722;
  font-size: 18px;
  font-weight: 700;
}

.member-table td {
  height: 42px;
  color: #333a45;
  font-size: 16px;
}

.member-table th:first-child,
.member-table td:first-child {
  width: 76px;
}

.member-table input[type="radio"] {
  width: 20px;
  height: 20px;
  accent-color: #1677ee;
}

.member-table strong {
  color: #ffb800;
  font-size: 18px;
  font-style: italic;
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

.dialog-btn.confirm {
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

.dialog-btn.danger {
  border-color: #f15b5b;
  background: #f15b5b;
  color: #fff;
}

.delete-dialog {
  width: 420px;
  padding: 28px 30px 26px;
}

.delete-dialog p {
  margin: 26px 0 0;
  color: #333a45;
  font-size: 18px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 28px;
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
  margin: 0 8px 0 14px;
}

.pager-info {
  color: #222;
}

@media (max-width: 1200px) {
  .patient-card {
    width: calc(100% - 32px);
    padding: 24px 20px 36px;
  }

  .filters-row {
    flex-wrap: wrap;
  }

  .search-filter {
    grid-template-columns: minmax(240px, 344px) 52px;
  }

  .table-wrapper {
    overflow-x: auto;
  }

  .patient-table {
    min-width: 1320px;
  }
}

@media (max-width: 720px) {
  .patient-page {
    padding: 16px 0 32px;
  }

  .patient-card {
    width: calc(100% - 20px);
    padding: 18px 12px 28px;
  }

  .page-toolbar {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .filter-item,
  .search-filter {
    width: 100%;
  }

  .filter-item {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .filter-item select,
  .date-control {
    width: 100%;
  }

  .search-filter {
    grid-template-columns: 1fr 52px;
  }

  .pagination {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 4px;
  }
}
</style>
