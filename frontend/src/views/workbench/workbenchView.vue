<template>
  <div class="workbench-page">
    <section class="workbench-panel">
      <div class="toolbar">
        <div class="toolbar-group">
          <label class="toolbar-label">创建时间</label>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="toolbar-control toolbar-date"
            clearable
            @change="loadVisits"
          />
        </div>

        <div class="toolbar-group compact">
          <label class="toolbar-label">接诊状态</label>
          <el-select
            v-model="status"
            class="toolbar-control toolbar-select"
            @change="loadVisits"
          >
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>

        <div class="toolbar-search">
          <el-input
            v-model="keyword"
            placeholder="输入患者姓名"
            class="toolbar-control"
            clearable
            @keyup.enter="loadVisits"
            @clear="loadVisits"
          >
            <template #append>
              <el-button :icon="Search" @click="loadVisits" />
            </template>
          </el-input>
        </div>
      </div>

      <div class="card-grid">
        <article
          v-for="patient in pagePatientCards"
          :key="patient.id"
          class="patient-card"
        >
          <div class="card-body">
            <div class="card-avatar" :class="patient.avatarTone">
              <span class="avatar-head" />
              <span class="avatar-body" />
            </div>

            <div class="card-main">
              <div class="card-header">
                <div class="card-name-row">
                  <span class="card-name">{{ patient.name }}</span>
                  <span v-if="patient.memberLevel" class="card-vip">{{ patient.memberLevel }}</span>
                  <span class="card-meta">{{ patient.gender }} {{ patient.age }}</span>
                </div>
                <span :class="['status-pill', patient.statusClass]">
                  {{ patient.statusLabel }}
                </span>
              </div>

              <div class="card-line">创建时间: {{ patient.time }}</div>
              <div class="card-line">科室: {{ patient.department }}　　医生: {{ patient.doctor }}</div>
              <div class="card-line">手机号码: {{ patient.phone }}</div>
            </div>
          </div>

          <div class="card-actions">
            <button type="button" class="card-action primary" @click="startVisit(patient)">接诊</button>
            <button type="button" class="card-action" @click="viewPatient(patient)">查看患者信息</button>
          </div>
        </article>
      </div>

      <div class="pagination-row">
        <button
          type="button"
          class="page-circle"
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          &lt;
        </button>
        <button
          v-for="page in totalPages"
          :key="page"
          type="button"
          class="page-circle"
          :class="{ active: currentPage === page }"
          @click="changePage(page)"
        >
          {{ page }}
        </button>
        <button
          type="button"
          class="page-circle"
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          &gt;
        </button>
        <span class="page-text">每页{{ pageSize }}条, 共{{ patientCards.length }}条</span>
        <span class="page-text">前往第</span>
        <span class="page-input">{{ currentPage }}</span>
        <span class="page-text">页</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { Search } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { registrationApi, workbenchApi } from "@/api";
import { useClientPagination } from "@/composables/useClientPagination";

const router = useRouter();
const dateRange = ref<string[]>([]);
const status = ref("all");
const keyword = ref("");

const statusOptions = [
  { label: "全部", value: "all" },
  { label: "待接诊", value: "pending" },
  { label: "接诊中", value: "processing" },
  { label: "已完成", value: "done" },
];

const patientCards = ref<any[]>([]);
const paginationRows = computed(() => patientCards.value);
const {
  pageSize,
  currentPage,
  totalPages,
  pageRows: pagePatientCards,
  changePage,
  resetPage,
} = useClientPagination(paginationRows, 9);

const statusText: Record<string, { label: string; className: string }> = {
  PENDING: { label: "待接诊", className: "pending" },
  WAITING: { label: "待接诊", className: "pending" },
  PROCESSING: { label: "接诊中", className: "processing" },
  IN_PROGRESS: { label: "接诊中", className: "processing" },
  DONE: { label: "已完成", className: "done" },
  COMPLETED: { label: "已完成", className: "done" },
  FINISHED: { label: "已完成", className: "done" },
  CANCELLED: { label: "已退号", className: "done" },
};

const loadVisits = async () => {
  resetPage();
  try {
    const response = await workbenchApi.visits({
      keyword: keyword.value || undefined,
      status: status.value === "all" ? undefined : status.value.toUpperCase(),
      startDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined,
    });
    const records = Array.isArray(response.data) ? response.data : response.data?.records || [];
    patientCards.value = records.map((item: any, index: number) => {
      const currentStatus = statusText[String(item.status || "PENDING").toUpperCase()]
        || { label: String(item.status || "待接诊"), className: "pending" };
      return {
      id: item.id || index,
      registrationId: item.id || index,
      patientId: item.patientId,
      name: item.patientName || "",
      gender: item.gender || "",
      age: item.age || "",
      time: item.createdAt || item.visitTime || "",
      department: item.departmentName || "",
      doctor: item.doctorName || "",
      phone: item.phone || "",
      memberLevel: item.memberLevel || "",
      statusLabel: currentStatus.label,
      statusClass: currentStatus.className,
      avatarTone: item.gender === "女" ? "female" : "male",
    };
    });
  } catch {
    patientCards.value = [];
  }
};

const startVisit = async (patient: any) => {
  if (patient.registrationId) {
    await registrationApi.startVisit(patient.registrationId);
  }
  router.push({
    path: "/consultation/new",
    state: { registrationId: String(patient.registrationId) },
  });
};

const viewPatient = (patient: any) => {
  router.push({
    path: "/patients/edit",
    state: { id: String(patient.patientId) },
  });
};

onMounted(loadVisits);
</script>

<style scoped>
.workbench-page { padding: 4px; }
.workbench-panel { background: #fff; border-radius: 14px; padding: 28px 34px 28px; box-shadow: 0 14px 36px rgb(15 23 42 / 5%); }
.toolbar { display: grid; grid-template-columns: 420px 280px minmax(320px, 1fr); gap: 24px; align-items: center; margin-bottom: 28px; }
.toolbar-group { display: flex; align-items: center; gap: 14px; }
.toolbar-label { flex: none; font-size: 16px; color: #1f2937; }
.toolbar-control { width: 100%; }
.toolbar-select { width: 188px; }
.card-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 30px 28px; }
.patient-card { overflow: hidden; border-radius: 24px; background: #fff; box-shadow: 0 12px 36px rgb(15 23 42 / 6%); border: 1px solid #f2f4fb; }
.card-body { display: flex; gap: 20px; padding: 28px 28px 26px; }
.card-avatar { position: relative; flex: none; width: 64px; height: 64px; border-radius: 50%; background: linear-gradient(180deg, #ffffff 0%, #f5f7fd 100%); box-shadow: 0 8px 18px rgb(72 87 120 / 12%); }
.avatar-head { position: absolute; top: 10px; left: 20px; width: 24px; height: 24px; border-radius: 50%; background: #2d304a; }
.avatar-body { position: absolute; left: 14px; bottom: 8px; width: 36px; height: 24px; border-radius: 18px 18px 10px 10px; background: #666b9d; }
.card-avatar.female .avatar-head { background: #2c2940; }
.card-avatar.female .avatar-body { background: #9fa4da; }
.card-main { flex: 1; min-width: 0; }
.card-header { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; margin-bottom: 12px; }
.card-name-row { display: flex; align-items: baseline; gap: 14px; flex-wrap: wrap; }
.card-name { font-size: 22px; font-weight: 700; color: #0f172a; }
.card-vip { color: #ffb400; font-size: 14px; font-style: italic; font-weight: 700; }
.card-meta { font-size: 16px; color: #8b94ab; }
.card-line { margin-top: 10px; font-size: 16px; line-height: 1.2; color: #98a1b5; }
.status-pill { flex: none; min-width: 72px; height: 34px; padding: 0 14px; border-radius: 999px; display: inline-flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; color: #fff; }
.status-pill.pending { background: #ff666e; }
.status-pill.processing { background: #2fcf95; }
.status-pill.done { background: #c9c9cf; }
.card-actions { display: grid; grid-template-columns: 1fr 1fr; border-top: 1px solid #edf0f7; }
.card-action { height: 68px; border: 0; background: #fff; font-size: 17px; color: #8d96f6; cursor: pointer; }
.card-action + .card-action { border-left: 1px solid #edf0f7; color: #6f7a90; }
.card-action.primary { font-weight: 700; }
.pagination-row { margin-top: 28px; display: flex; justify-content: flex-end; align-items: center; gap: 14px; }
.page-circle, .page-input { width: 42px; height: 42px; border-radius: 50%; border: 2px solid #c7cbd8; background: #fff; color: #667085; display: inline-flex; align-items: center; justify-content: center; font-size: 18px; }
.page-circle.active { border-color: #6473ff; background: #6473ff; color: #fff; }
.page-circle:disabled { opacity: .45; cursor: not-allowed; }
.page-text { font-size: 16px; color: #3b4558; }
@media (max-width: 1440px) { .toolbar { grid-template-columns: 1fr 1fr; } .toolbar-search { grid-column: 1 / -1; } .card-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 960px) { .toolbar { grid-template-columns: 1fr; } .card-grid { grid-template-columns: 1fr; } }
</style>
