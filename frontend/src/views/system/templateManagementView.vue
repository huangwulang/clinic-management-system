<template>
  <div class="template-page">
    <section class="template-card">
      <header class="template-toolbar">
        <nav class="template-tabs" aria-label="模板类型">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="['template-tab', { active: activeTab === tab.key }]"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </nav>

        <div class="add-menu-wrap">
          <button type="button" class="add-btn" @click="handleAdd">
            <el-icon><CirclePlusFilled /></el-icon>
            <span>{{ activeTab === "prescription" ? "新增模板" : "新增" }}</span>
          </button>
          <div v-if="activeTab === 'prescription' && addMenuVisible" class="add-menu">
            <button type="button" @click="openPrescriptionForm('western')">西/成药处方模板</button>
            <button type="button" class="active" @click="openPrescriptionForm('chinese')">中药处方模板</button>
            <button type="button" @click="openPrescriptionForm('check')">检查项目模板</button>
          </div>
        </div>
      </header>

      <div class="template-divider"></div>

      <section class="filters" :class="{ prescription: activeTab === 'prescription' }">
        <label v-if="activeTab === 'prescription'" class="select-filter">
          <span>处方类别</span>
          <select v-model="prescriptionType">
            <option value="">全部</option>
            <option>西/成药处方</option>
            <option>中药处方</option>
            <option>检查项目</option>
          </select>
        </label>

        <label class="select-filter">
          <span>模板权限</span>
          <select v-model="permission">
            <option value="">全部</option>
            <option>私人模板</option>
            <option>公共模板</option>
          </select>
        </label>

        <label class="search-filter">
          <input v-model="keyword" type="text" placeholder="模板编码/模板模板名称" />
          <button type="button" aria-label="搜索">
            <el-icon><Search /></el-icon>
          </button>
        </label>
      </section>

      <table class="template-table" :class="`${activeTab}-table`">
        <thead>
          <tr>
            <th v-for="column in columns" :key="column.key">{{ column.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in pageRows" :key="`${activeTab}-${row.id}`">
            <td v-for="column in columns" :key="column.key">
              <template v-if="column.key === 'actions'">
                <button type="button" class="text-link" @click="handleRowAction('edit', row)">编辑</button>
                <button type="button" class="text-link" @click="handleRowAction('delete', row)">删除</button>
              </template>
              <template v-else>{{ row[column.key] }}</template>
            </td>
          </tr>
        </tbody>
      </table>

      <footer class="pagination">
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
    </section>

    <div v-if="deleteDialogVisible" class="modal-mask">
      <div class="delete-dialog">
        <button class="dialog-close" type="button" @click="closeDeleteDialog">×</button>
        <h3>删除确认</h3>
        <div class="dialog-content">
          <span class="warning-icon">!</span>
          <span>确定要删除此{{ activeTab === "prescription" ? "处方" : "病历" }}模板信息吗?</span>
        </div>
        <div class="dialog-actions">
          <button class="cancel-btn" type="button" @click="closeDeleteDialog">取消</button>
          <button class="confirm-btn" type="button" @click="confirmDeleteTemplate">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { CirclePlusFilled, Search } from "@element-plus/icons-vue";
import { medicalTemplateApi, prescriptionTemplateApi } from "@/api";
import { useClientPagination } from "@/composables/useClientPagination";

type TabKey = "medical" | "prescription";
type PrescriptionTemplateType = "western" | "chinese" | "check";
type Row = Record<string, string | number>;

const route = useRoute();
const router = useRouter();

const tabs: { key: TabKey; label: string }[] = [
  { key: "medical", label: "病历模板" },
  { key: "prescription", label: "处方模板" },
];

const activeTab = ref<TabKey>("medical");
const permission = ref("");
const prescriptionType = ref("");
const keyword = ref("");
const addMenuVisible = ref(false);

const diagnosis = ["心悸", "头疼", "病毒性感冒", "腹部积水", "感冒", "发烧", "支气管炎", "急性肠胃炎", "脊椎炎", "心悸"];
const creators = ["王冕", "林鹤"];
const permissions = ["私人模板", "公共模板"];
const times = [
  "2019-11-12 12:08:28",
  "2019-11-12 12:08:29",
  "2019-11-12 12:08:30",
  "2019-11-12 12:08:31",
  "2019-11-12 12:08:28",
  "2019-11-12 12:08:28",
  "2019-11-12 12:08:29",
  "2019-11-12 12:08:30",
  "2019-11-12 12:08:31",
  "2019-11-12 12:08:28",
];

const makeRows = (type: TabKey) =>
  Array.from({ length: 10 }, (_, index) => ({
    id: `${type}-${index}`,
    index: index + 1,
    code: `RZ${String(index + 5).padStart(5, "0")}`,
    name: `${type === "medical" ? "病历名称" : "处方模板名称"}${index + 1}`,
    prescriptionType: ["西/成药处方", "中药处方", "检查项目"][index % 3],
    diagnosis: diagnosis[index],
    permission: permissions[index % 2],
    createTime: times[index],
    creator: creators[index % 2],
    actions: "",
  }));

const rows: Record<TabKey, Row[]> = {
  medical: makeRows("medical"),
  prescription: makeRows("prescription"),
};

const medicalRows = reactive(rows.medical);
const prescriptionRows = reactive(rows.prescription);
rows.medical = medicalRows;
rows.prescription = prescriptionRows;

const columns = computed(() => {
  const base = [
    { key: "index", label: "序号" },
    { key: "code", label: "模板编号" },
    { key: "name", label: "模板名称" },
  ];

  if (activeTab.value === "prescription") {
    base.push({ key: "prescriptionType", label: "处方类型" });
  }

  return [
    ...base,
    { key: "diagnosis", label: "诊断" },
    { key: "permission", label: "模板权限" },
    { key: "createTime", label: "创建时间" },
    { key: "creator", label: "创建人员" },
    { key: "actions", label: "操作" },
  ];
});

const filteredRows = computed(() => {
  const word = keyword.value.trim();

  return rows[activeTab.value].filter((row) => {
    const matchesPermission = !permission.value || row.permission === permission.value;
    const matchesPrescription = activeTab.value !== "prescription" || !prescriptionType.value || row.prescriptionType === prescriptionType.value;
    const matchesKeyword = !word || String(row.code).includes(word) || String(row.name).includes(word);

    return matchesPermission && matchesPrescription && matchesKeyword;
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
  () => [
    activeTab.value,
    permission.value,
    prescriptionType.value,
    keyword.value,
  ],
  resetPage,
);

const switchTab = (tab: TabKey) => {
  activeTab.value = tab;
  router.replace({ name: "SystemTemplate", params: { type: tab } });
  permission.value = "";
  prescriptionType.value = "";
  keyword.value = "";
  addMenuVisible.value = false;
};

const deleteDialogVisible = ref(false);
const deleteTemplateId = ref<string | number | null>(null);

watch(
  () => route.params.type,
  (type) => {
    const requested = String(type || "medical") as TabKey;
    if (tabs.some((tab) => tab.key === requested)) activeTab.value = requested;
  },
  { immediate: true },
);

const handleAdd = () => {
  if (activeTab.value === "medical") {
    router.push({ name: "SystemMedicalTemplateForm" });
  }

  if (activeTab.value === "prescription") {
    addMenuVisible.value = !addMenuVisible.value;
  }
};

const handleRowAction = (action: "edit" | "delete", row: Row) => {
  if (activeTab.value === "medical" && action === "edit") {
    router.push({ name: "SystemMedicalTemplateForm", state: { id: row.id } });
  }

  if (activeTab.value === "prescription" && action === "edit") {
    router.push({
      name: "SystemPrescriptionTemplateForm",
      state: { id: row.id, type: getPrescriptionFormType(String(row.prescriptionType)) },
    });
  }

  if (action === "delete") {
    deleteTemplateId.value = row.id;
    deleteDialogVisible.value = true;
  }
};

const openPrescriptionForm = (type: PrescriptionTemplateType) => {
  addMenuVisible.value = false;
  router.push({ name: "SystemPrescriptionTemplateForm", state: { type } });
};

const getPrescriptionFormType = (type: string): PrescriptionTemplateType => {
  if (type === "中药处方") return "chinese";
  if (type === "检查项目") return "check";
  return "western";
};

const closeDeleteDialog = () => {
  deleteDialogVisible.value = false;
  deleteTemplateId.value = null;
};

const confirmDeleteTemplate = async () => {
  const api = activeTab.value === "prescription" ? prescriptionTemplateApi : medicalTemplateApi;
  if (deleteTemplateId.value) await api.remove(deleteTemplateId.value);
  const currentRows = activeTab.value === "prescription" ? prescriptionRows : medicalRows;
  const targetIndex = currentRows.findIndex((row) => row.id === deleteTemplateId.value);
  if (targetIndex > -1) {
    currentRows.splice(targetIndex, 1);
    currentRows.forEach((row, index) => {
      row.index = index + 1;
    });
  }
  closeDeleteDialog();
};

const parseTemplateContent = (content: unknown) => {
  if (typeof content !== "string" || !content.trim()) return {};
  try {
    const parsed = JSON.parse(content);
    return parsed && typeof parsed === "object" ? parsed as Record<string, any> : {};
  } catch {
    return {};
  }
};

const displayTemplateType = (item: any, content: Record<string, any>) => {
  const type = content.templateType || item.prescriptionType || item.templateType || "";
  if (type === "PRESCRIPTION") return "西/成药处方";
  return type;
};

const loadTemplates = async () => {
  const [medicalResponse, prescriptionResponse]: any[] = await Promise.all([
    medicalTemplateApi.page({ page: 1, size: 100 }),
    prescriptionTemplateApi.list(),
  ]);
  const mapRows = (records: any[]) => records.map((item: any, index: number) => {
    const content = parseTemplateContent(item.content);
    return {
      index: index + 1,
      id: item.id,
      code: item.templateCode || item.code || "",
      name: item.name || item.templateName || "",
      permission: item.permission || content.permission || "",
      prescriptionType: displayTemplateType(item, content),
      diagnosis: item.diagnosis || content.diagnosis || "",
      creator: item.creator || content.creator || "",
      createTime: item.createdAt || content.createTime || content.createDate || "",
      actions: ["编辑", "删除"],
    };
  });
  const medicalRecords = (medicalResponse?.data?.records || []).filter((item: any) => (
    String(item.templateType || "").toUpperCase() !== "PRESCRIPTION"
  ));
  medicalRows.splice(0, medicalRows.length, ...mapRows(medicalRecords));
  prescriptionRows.splice(0, prescriptionRows.length, ...mapRows(prescriptionResponse?.data || []));
};
onMounted(loadTemplates);
</script>

<style scoped>
.template-page {
  min-height: 100%;
  padding: 28px 0 58px;
  overflow-x: hidden;
  box-sizing: border-box;
}

.template-card {
  width: min(1660px, calc(100% - 96px));
  min-height: 1216px;
  margin: 0 auto;
  padding: 31px 31px 40px;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.08);
  box-sizing: border-box;
}

.template-toolbar {
  display: flex;
  min-height: 46px;
  align-items: center;
  justify-content: space-between;
}

.template-tabs {
  display: inline-flex;
  overflow: hidden;
  border: 1px solid #c5c7cd;
  border-radius: 4px;
}

.template-tab {
  width: 132px;
  height: 44px;
  border: 0;
  border-right: 1px solid #c5c7cd;
  background: #fff;
  color: #c5c5c5;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.template-tab:last-child {
  border-right: 0;
}

.template-tab.active {
  background: #6269e8;
  color: #fff;
}

.add-btn {
  display: inline-flex;
  width: 142px;
  height: 46px;
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

.add-menu-wrap {
  position: relative;
}

.add-menu {
  position: absolute;
  top: 72px;
  left: 50%;
  z-index: 20;
  width: 202px;
  transform: translateX(-50%);
  background: #ffffff;
  box-shadow: 0 2px 9px rgba(23, 31, 56, 0.14);
}

.add-menu::before {
  content: "";
  position: absolute;
  top: -7px;
  left: 50%;
  width: 14px;
  height: 14px;
  transform: translateX(-50%) rotate(45deg);
  background: #ffffff;
}

.add-menu button {
  position: relative;
  z-index: 1;
  display: block;
  width: 100%;
  height: 54px;
  border: 0;
  background: #ffffff;
  color: #111111;
  font-size: 20px;
  text-align: center;
  cursor: pointer;
}

.add-menu button.active,
.add-menu button:hover {
  background: #f5f5ff;
  color: #636be8;
}

.template-divider {
  height: 1px;
  margin: 28px 0 30px;
  background: #eee;
}

.filters {
  display: flex;
  min-height: 44px;
  align-items: center;
  flex-wrap: wrap;
  gap: 30px;
  margin-bottom: 30px;
  max-width: 100%;
}

.filters.prescription {
  gap: 26px;
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
  width: min(193px, 100%);
  height: 44px;
  padding: 0 20px;
  border: 1px solid #c8c8c8;
  border-radius: 4px;
  outline: none;
  background: #fff;
  color: #111;
  font-size: 16px;
}

.search-filter {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 62px;
  width: min(482px, 100%);
  height: 44px;
  overflow: hidden;
  border: 1px solid #c8c8c8;
  border-radius: 4px;
  background: #fff;
  box-sizing: border-box;
}

.filters.prescription .search-filter {
  grid-template-columns: minmax(0, 1fr) 62px;
  width: min(491px, 100%);
  max-width: 100%;
}

.search-filter input {
  width: 100%;
  height: 100%;
  padding: 0 14px;
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

.template-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.template-table th {
  height: 72px;
  padding: 0 8px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
}

.template-table td {
  height: 84px;
  padding: 0 8px;
  border-bottom: 1px solid #eee;
  color: #000;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
}

.medical-table th:nth-child(1),
.medical-table td:nth-child(1) {
  width: 90px;
}

.medical-table th:nth-child(2),
.medical-table td:nth-child(2) {
  width: 160px;
}

.medical-table th:nth-child(3),
.medical-table td:nth-child(3) {
  width: 190px;
}

.medical-table th:nth-child(4),
.medical-table td:nth-child(4),
.medical-table th:nth-child(5),
.medical-table td:nth-child(5) {
  width: 150px;
}

.medical-table th:nth-child(6),
.medical-table td:nth-child(6) {
  width: 230px;
}

.medical-table th:nth-child(7),
.medical-table td:nth-child(7) {
  width: 170px;
}

.prescription-table th:nth-child(1),
.prescription-table td:nth-child(1) {
  width: 80px;
}

.prescription-table th:nth-child(2),
.prescription-table td:nth-child(2) {
  width: 135px;
}

.prescription-table th:nth-child(3),
.prescription-table td:nth-child(3) {
  width: 220px;
}

.prescription-table th:nth-child(4),
.prescription-table td:nth-child(4),
.prescription-table th:nth-child(5),
.prescription-table td:nth-child(5),
.prescription-table th:nth-child(6),
.prescription-table td:nth-child(6) {
  width: 145px;
}

.prescription-table th:nth-child(7),
.prescription-table td:nth-child(7) {
  width: 210px;
}

.prescription-table th:nth-child(8),
.prescription-table td:nth-child(8) {
  width: 160px;
}

.template-table th:last-child,
.template-table td:last-child {
  width: 150px;
}

.prescription-table th:nth-child(1),
.prescription-table td:nth-child(1) {
  width: 6%;
}

.prescription-table th:nth-child(2),
.prescription-table td:nth-child(2) {
  width: 10%;
}

.prescription-table th:nth-child(3),
.prescription-table td:nth-child(3) {
  width: 16%;
}

.prescription-table th:nth-child(4),
.prescription-table td:nth-child(4),
.prescription-table th:nth-child(5),
.prescription-table td:nth-child(5),
.prescription-table th:nth-child(6),
.prescription-table td:nth-child(6) {
  width: 11%;
}

.prescription-table th:nth-child(7),
.prescription-table td:nth-child(7) {
  width: 15%;
}

.prescription-table th:nth-child(8),
.prescription-table td:nth-child(8) {
  width: 11%;
}

.prescription-table th:last-child,
.prescription-table td:last-child {
  width: 9%;
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
  margin-left: 30px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
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

.pager-ellipsis {
  margin: 0 8px 0 14px;
}

.pager-summary {
  margin-left: 2px;
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
  .template-card {
    width: calc(100% - 32px);
  }

  .filters {
    flex-wrap: wrap;
  }
}
</style>
