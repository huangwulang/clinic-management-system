<template>
  <div class="dictionary-page">
    <section class="dictionary-card" :class="{ 'compact-card': currentViewConfig.compact }">
      <header class="dictionary-toolbar">
        <nav class="dictionary-tabs" aria-label="字典分类">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="['dictionary-tab', { active: activeTab === tab.key }]"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </nav>

        <button type="button" class="add-btn" @click="openModal()">
          <el-icon><CirclePlusFilled /></el-icon>
          <span>新增</span>
        </button>
      </header>

      <div class="dictionary-divider"></div>

      <section class="dictionary-body">
        <aside class="side-menu">
          <button
            v-for="item in currentConfig.sideMenu"
            :key="item"
            type="button"
            :class="{ active: activeSideItem === item }"
            @click="switchSideItem(item)"
          >
            {{ item }}
          </button>
        </aside>

        <main class="list-main">
          <section class="filter-line">
            <label v-if="currentViewConfig.filter" class="type-filter">
              <span>{{ currentViewConfig.filter.label }}</span>
              <select v-model="categoryFilter">
                <option value="">全部</option>
                <option v-for="option in currentViewConfig.filter.options" :key="option">{{ option }}</option>
              </select>
            </label>

            <label class="search-filter">
              <input v-model="keyword" type="text" :placeholder="currentViewConfig.searchPlaceholder" />
              <button type="button" aria-label="搜索">
                <el-icon><Search /></el-icon>
              </button>
            </label>
          </section>

          <div class="content-divider"></div>

          <table class="dictionary-table" :class="tableClass">
            <thead>
              <tr>
                <th v-for="column in currentViewConfig.columns" :key="column.key">{{ column.label }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in pageRows" :key="`${activeTab}-${activeSideItem}-${row.index}`">
                <td v-for="column in currentViewConfig.columns" :key="column.key">
                  <template v-if="column.key === 'actions'">
                    <button type="button" class="text-link" @click="openModal(row)">编辑</button>
                    <button type="button" class="text-link" @click="openDeleteConfirm(row)">删除</button>
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
        </main>
      </section>
    </section>

    <div v-if="showModal" class="modal-mask">
      <section class="dict-modal" :class="`${activeTab}-modal`">
        <button type="button" class="modal-close" @click="closeModal">×</button>
        <h2>{{ modalTitle }}</h2>

        <div v-if="activeTab === 'medical'" class="modal-form medical-modal-form">
          <label class="modal-field textarea-field">
            <span>{{ modalFieldLabel }}<em>*</em></span>
            <textarea v-model="modalName"></textarea>
          </label>
        </div>

        <div v-else-if="activeTab === 'drug'" class="modal-form drug-modal-form">
          <label class="modal-field compact-select">
            <span>处方类型<em>*</em></span>
            <select>
              <option>请选择</option>
              <option>西/成药处方</option>
              <option>中药处方</option>
            </select>
          </label>
          <label class="modal-field">
            <span>{{ modalFieldLabel }}<em>*</em></span>
            <input v-model="modalName" placeholder="请输入分类名称，限20个字符" />
          </label>
        </div>

        <div v-else class="modal-form single-modal-form">
          <label class="modal-field">
            <span>{{ modalFieldLabel }}<em>*</em></span>
            <input v-model="modalName" />
          </label>
        </div>

        <footer class="modal-actions">
          <button type="button" class="cancel-btn" @click="closeModal">取消</button>
          <button type="button" class="confirm-btn" @click="saveDictionary">确定</button>
        </footer>
      </section>
    </div>

    <div v-if="showDeleteConfirm" class="modal-mask">
      <section class="delete-confirm">
        <h2>删除确认</h2>
        <p>确定要删除该条信息吗？</p>
        <footer class="confirm-actions">
          <button type="button" class="cancel-btn" @click="showDeleteConfirm = false">取消</button>
          <button type="button" class="confirm-btn" @click="deleteDictionary">确定</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { CirclePlusFilled, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { dictionaryApi } from "@/api";
import { useClientPagination } from "@/composables/useClientPagination";

type TabKey = "medical" | "drug" | "project" | "patient";
const route = useRoute();
const router = useRouter();
type Column = { key: string; label: string };
type Row = Record<string, string | number>;
type ViewConfig = {
  searchPlaceholder: string;
  columns: Column[];
  rows: Row[];
  filter?: { label: string; options: string[]; key: string };
  compact?: boolean;
};

const tabs: { key: TabKey; label: string }[] = [
  { key: "medical", label: "病历信息" },
  { key: "drug", label: "药品信息" },
  { key: "project", label: "检查项目" },
  { key: "patient", label: "患者信息" },
];

const medicalSideMenu = ["诊断信息", "医嘱信息", "主诉信息", "现病史", "既往史", "过敏史", "个人史", "辅助检查", "治疗意见"];
const drugSideMenu = ["药品分类", "药品用法", "药品剂型", "发票项目", "生产厂家", "包装单位", "入库类型", "出库类型"];
const activeTab = ref<TabKey>("medical");
const activeSideItem = ref(medicalSideMenu[0]);
const keyword = ref("");
const categoryFilter = ref("");
const showModal = ref(false);
const modalName = ref("");
const showDeleteConfirm = ref(false);
const modalAction = ref<"add" | "edit">("add");
const editingRow = ref<Row | null>(null);
const rowsVersion = ref(0);

const creators = ["王冕", "林鹤"];
const times = [
  "2019-11-12 12:08:12",
  "2019-11-12 12:08:12",
  "2019-11-12 12:08:12",
  "2019-11-12 12:08:14",
  "2019-11-12 12:08:16",
  "2019-11-12 12:08:18",
  "2019-11-12 12:08:20",
  "2019-11-12 12:08:22",
  "2019-11-12 12:08:24",
  "2019-11-12 12:08:26",
];

const makeRows = (names: string[], extra?: (index: number) => Partial<Row>) =>
  Array.from({ length: 10 }, (_, index) => ({
    index: index + 1,
    name: names[index % names.length],
    createTime: times[index],
    creator: creators[index % creators.length],
    actions: "",
    ...(extra ? extra(index) : {}),
  }));

const medicalColumns = (nameLabel: string): Column[] => [
  { key: "index", label: "序号" },
  { key: "name", label: nameLabel },
  { key: "createTime", label: "创建时间" },
  { key: "creator", label: "创建人" },
  { key: "actions", label: "操作" },
];

const prescriptionColumns = (nameLabel: string): Column[] => [
  { key: "index", label: "序号" },
  { key: "prescription", label: "处方分类" },
  { key: "name", label: nameLabel },
  { key: "createTime", label: "创建时间" },
  { key: "creator", label: "创建人" },
  { key: "actions", label: "操作" },
];

const numberedRows = (prefix: string) => makeRows(Array.from({ length: 10 }, (_, index) => `${prefix}${index + 1}`));

const medicalViews: Record<string, ViewConfig> = {
  诊断信息: {
    searchPlaceholder: "模板内容",
    columns: medicalColumns("诊断内容"),
    rows: makeRows(["心悸", "头疼", "病毒性感冒", "腹部积水", "感冒", "发烧", "支气管炎", "急性肠胃炎", "脊椎炎", "心悸"]),
  },
  医嘱信息: {
    searchPlaceholder: "模板内容",
    columns: medicalColumns("医嘱内容"),
    rows: makeRows(["低盐低塘", "饮食忌辛冷等食物", "忌受凉吹风", "少糖", "忌熬夜", "忌饮酒", "忌辛辣", "多喝开水", "忌油腻", "忌海鲜"]),
  },
  主诉信息: {
    searchPlaceholder: "主诉内容",
    filter: { label: "主诉分类", options: ["常用症状", "常用时间", "常用标点", "常用词汇"], key: "category" },
    columns: [
      { key: "index", label: "序号" },
      { key: "category", label: "分类" },
      { key: "name", label: "主诉内容" },
      { key: "createTime", label: "创建时间" },
      { key: "creator", label: "创建人" },
      { key: "actions", label: "操作" },
    ],
    rows: makeRows(["头疼", "早晨", "，", "经常", "头晕", "傍晚", "；", "持续", "恶心", "中午"], (index) => ({
      category: ["常用症状", "常用时间", "常用标点", "常用词汇"][index % 4],
    })),
  },
  现病史: {
    searchPlaceholder: "内容",
    columns: medicalColumns("现病史"),
    rows: numberedRows("现病史"),
  },
  既往史: {
    searchPlaceholder: "内容",
    columns: medicalColumns("既往史"),
    rows: numberedRows("既往史"),
  },
  过敏史: {
    searchPlaceholder: "内容",
    columns: medicalColumns("过敏史"),
    rows: numberedRows("过敏史"),
  },
  个人史: {
    searchPlaceholder: "内容",
    columns: medicalColumns("个人史"),
    rows: numberedRows("个人史"),
  },
  辅助检查: {
    searchPlaceholder: "内容",
    columns: medicalColumns("辅助检查情况"),
    rows: numberedRows("辅助检查"),
  },
  治疗意见: {
    searchPlaceholder: "内容",
    columns: medicalColumns("治疗意见"),
    rows: numberedRows("治疗意见"),
  },
};

const withPrescription = (names: string[]) => makeRows(names, (index) => ({
  prescription: index % 2 === 0 ? "西/成药处方" : "中药处方",
}));

const drugViews: Record<string, ViewConfig> = {
  药品分类: {
    searchPlaceholder: "分类名称",
    filter: { label: "处方类别", options: ["西/成药处方", "中药处方"], key: "prescription" },
    columns: prescriptionColumns("分类名称"),
    rows: withPrescription(["中草药", "西成药", "西药", "清热类", "注射类", "常用药品", "感冒药类", "外用药", "中药颗粒", "中草药"]),
  },
  药品用法: {
    searchPlaceholder: "用法名称",
    filter: { label: "处方类别", options: ["西/成药处方", "中药处方"], key: "prescription" },
    columns: prescriptionColumns("用法名称"),
    rows: withPrescription(["口服", "注射", "外用", "湿敷", "煎服", "温水吞服", "静脉点滴", "雾化", "皮试", "冲服"]),
  },
  药品剂型: {
    searchPlaceholder: "剂型名称",
    columns: medicalColumns("剂型名称"),
    rows: makeRows(["溶剂", "颗粒", "丸剂", "胶囊", "片剂", "栓剂", "口服液", "注射剂", "粉剂", "散剂"]),
  },
  发票项目: {
    searchPlaceholder: "项目名称",
    columns: medicalColumns("项目名称"),
    rows: makeRows(["注射费", "检查费", "材料费", "注射费", "检查费", "材料费", "注射费", "检查费", "材料费", "注射费"]),
  },
  生产厂家: {
    searchPlaceholder: "厂家名称",
    columns: medicalColumns("厂家名称"),
    rows: makeRows(["上海医药（集团）有限公司", "山东罗欣药业股份有限公司", "昆明制药集团", "武汉五景药业", "上海医药（集团）有限公司", "山东罗欣药业股份有限公司", "昆明制药集团", "武汉五景药业", "上海医药（集团）有限公司", "山东罗欣药业股份有限公司"]),
  },
  包装单位: {
    searchPlaceholder: "单位名称",
    columns: medicalColumns("单位名称"),
    rows: makeRows(["包", "袋", "盒", "支", "片", "颗", "丸", "两", "板", "克"]),
  },
  入库类型: {
    searchPlaceholder: "入库类型",
    columns: medicalColumns("入库类型"),
    rows: makeRows(["采购入库", "退货入库", "其它入库"]).slice(0, 3),
    compact: true,
  },
  出库类型: {
    searchPlaceholder: "出库类型",
    columns: medicalColumns("出库类型"),
    rows: makeRows(["科室领药", "报损出库", "采购出库"]).slice(0, 3),
    compact: true,
  },
};

const projectViews: Record<string, ViewConfig> = {
  项目分类: {
    searchPlaceholder: "分类名称",
    columns: medicalColumns("分类名称"),
    rows: makeRows(["检查费", "检验费", "针灸费", "治疗费", "手术费", "检查费", "检验费", "针灸费", "治疗费", "手术费"]),
  },
  项目单位: {
    searchPlaceholder: "单位名称",
    columns: medicalColumns("单位名称"),
    rows: makeRows(["次", "项", "盒", "支", "片", "张", "袋", "套", "次", "项"]),
  },
};

const patientViews: Record<string, ViewConfig> = {
  患者来源: {
    searchPlaceholder: "分类名称",
    columns: medicalColumns("来源名称"),
    rows: makeRows(["自来", "广告", "朋友介绍", "自来", "广告", "朋友介绍", "自来", "广告", "朋友介绍", "自来"]),
  },
  患者学历: {
    searchPlaceholder: "学历名称",
    columns: medicalColumns("学历名称"),
    rows: makeRows(["初中以下", "高中", "大专", "技校", "本科", "硕士及以上", "博士", "未说明", "初中以下", "高中"]),
  },
  患者职业: {
    searchPlaceholder: "分类名称",
    columns: medicalColumns("职业名称"),
    rows: makeRows(["工人", "公务员", "医生", "教师", "互联网从业者", "未说明", "工人", "公务员", "医生", "教师"]),
  },
};

const configs: Record<TabKey, { sideMenu: string[] }> = {
  medical: { sideMenu: medicalSideMenu },
  drug: { sideMenu: drugSideMenu },
  project: { sideMenu: ["项目分类", "项目单位"] },
  patient: { sideMenu: ["患者来源", "患者学历", "患者职业"] },
};

const currentConfig = computed(() => configs[activeTab.value]);
const currentViewConfig = computed(() => {
  if (activeTab.value === "medical") return medicalViews[activeSideItem.value];
  if (activeTab.value === "drug") return drugViews[activeSideItem.value];
  if (activeTab.value === "project") return projectViews[activeSideItem.value];
  return patientViews[activeSideItem.value];
});
const tableClass = computed(() => ({
  "medical-table": activeTab.value === "medical" && activeSideItem.value !== "主诉信息",
  "chief-table": activeTab.value === "medical" && activeSideItem.value === "主诉信息",
  "drug-table": activeTab.value === "drug" && Boolean(currentViewConfig.value.filter),
  "plain-drug-table": activeTab.value === "drug" && !currentViewConfig.value.filter,
  "project-table": activeTab.value === "project",
  "patient-table": activeTab.value === "patient",
}));
const filteredRows = computed(() => {
  rowsVersion.value;
  const word = keyword.value.trim();
  const viewConfig = currentViewConfig.value;

  return viewConfig.rows.filter((row) => {
    const filterKey = viewConfig.filter?.key;
    const matchesFilter = !filterKey || !categoryFilter.value || row[filterKey] === categoryFilter.value;
    const matchesKeyword = !word || String(row.name).includes(word);

    return matchesFilter && matchesKeyword;
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
    activeSideItem.value,
    keyword.value,
    categoryFilter.value,
  ],
  resetPage,
);

const modalTitle = computed(() => `${modalAction.value === "add" ? "新增" : "编辑"}${activeSideItem.value}`);
const modalFieldLabel = computed(() => {
  if (activeTab.value === "medical") return activeSideItem.value.replace("信息", "");
  if (activeTab.value === "drug") return activeSideItem.value === "药品分类" ? "分类名称" : activeSideItem.value;
  if (activeTab.value === "project") return activeSideItem.value === "项目单位" ? "单位名称" : "分类名称";
  return activeSideItem.value === "患者来源" ? "来源名称" : activeSideItem.value.replace("患者", "");
});

const resetFilters = () => {
  keyword.value = "";
  categoryFilter.value = "";
};

const switchTab = (tab: TabKey) => {
  activeTab.value = tab;
  router.replace({ name: "SystemDictionary", params: { type: tab } });
  activeSideItem.value = configs[tab].sideMenu[0];
  resetFilters();
};

const switchSideItem = (item: string) => {
  activeSideItem.value = item;
  resetFilters();
};

watch(
  () => route.params.type,
  (type) => {
    const requested = String(type || "medical") as TabKey;
    if (tabs.some((tab) => tab.key === requested)) switchTab(requested);
  },
  { immediate: true },
);

const openModal = (row?: Row) => {
  modalAction.value = row ? "edit" : "add";
  editingRow.value = row || null;
  modalName.value = String(row?.name || "");
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  editingRow.value = null;
};

const openDeleteConfirm = (row?: Row) => {
  editingRow.value = row || editingRow.value;
  showDeleteConfirm.value = true;
};


const dictionaryType = computed(() =>
  activeTab.value + "-" + activeSideItem.value.replace(/\s+/g, "-"));

const loadDictionary = async () => {
  const response: any = await dictionaryApi(dictionaryType.value).list();
  const records = response?.data || [];
  currentViewConfig.value.rows.splice(0, currentViewConfig.value.rows.length,
    ...records.map((item: any, index: number) => ({
      index: index + 1,
      id: item.id,
      name: item.itemName || item.name || item.label || item.value || "",
      code: item.itemCode || item.code || "",
      category: item.category || "",
      creator: item.creator || "",
      createTime: item.createdAt || "",
      enabled: item.enabled,
    })));
  rowsVersion.value += 1;
};

const saveDictionary = async () => {
  if (!modalName.value.trim()) {
    ElMessage.warning("请输入字典名称");
    return;
  }
  const api = dictionaryApi(dictionaryType.value);
  const name = modalName.value.trim();
  const payload = {
    itemCode: String(editingRow.value?.code || name).trim(),
    itemName: name,
    enabled: true,
  };
  if (editingRow.value?.id) await api.update(editingRow.value.id, payload);
  else await api.create(payload);
  closeModal();
  await loadDictionary();
  ElMessage.success("字典保存成功");
};

const deleteDictionary = async () => {
  if (editingRow.value?.id) await dictionaryApi(dictionaryType.value).remove(editingRow.value.id);
  showDeleteConfirm.value = false;
  await loadDictionary();
  ElMessage.success("字典删除成功");
};

watch([activeTab, activeSideItem], loadDictionary, { immediate: true });
</script>

<style scoped>
.dictionary-page {
  min-height: 100%;
  padding: 24px 0 54px;
  overflow-x: hidden;
  box-sizing: border-box;
}

.dictionary-card {
  width: min(1630px, calc(100% - 96px));
  min-height: 1222px;
  margin: 0 auto;
  padding: 31px 31px 40px;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.08);
  box-sizing: border-box;
}

.dictionary-card.compact-card {
  min-height: 678px;
}

.dictionary-toolbar {
  display: flex;
  min-height: 46px;
  align-items: center;
  justify-content: space-between;
}

.dictionary-tabs {
  display: inline-flex;
  overflow: hidden;
  border: 1px solid #c5c7cd;
  border-radius: 4px;
}

.dictionary-tab {
  width: 136px;
  height: 44px;
  border: 0;
  border-right: 1px solid #c5c7cd;
  background: #fff;
  color: #c5c5c5;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.dictionary-tab:last-child {
  border-right: 0;
}

.dictionary-tab.active {
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

.dictionary-divider {
  height: 1px;
  margin: 28px 0 24px;
  background: #eee;
}

.dictionary-body {
  display: grid;
  grid-template-columns: 226px 1fr;
  column-gap: 34px;
}

.side-menu {
  align-self: start;
  border: 1px solid #edf0ff;
}

.side-menu button {
  display: block;
  width: 100%;
  height: 57px;
  border: 0;
  border-bottom: 1px solid #edf0ff;
  background: #fff;
  color: #111;
  font-size: 18px;
  cursor: pointer;
}

.side-menu button:last-child {
  border-bottom: 0;
}

.side-menu button.active {
  background: #6269e8;
  color: #fff;
}

.list-main {
  min-width: 0;
}

.filter-line {
  display: flex;
  align-items: center;
  gap: 32px;
  min-height: 48px;
}

.type-filter {
  display: inline-flex;
  align-items: center;
  gap: 19px;
  color: #111;
  font-size: 18px;
  white-space: nowrap;
}

.type-filter select {
  width: 193px;
  height: 44px;
  padding: 0 18px;
  border: 1px solid #c8c8c8;
  border-radius: 4px;
  outline: none;
  background: #fff;
  color: #111;
  font-size: 16px;
}

.search-filter {
  display: grid;
  grid-template-columns: 397px 62px;
  width: 459px;
  height: 44px;
  overflow: hidden;
  border: 1px solid #c8c8c8;
  border-radius: 4px;
  background: #fff;
  box-sizing: border-box;
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

.content-divider {
  height: 1px;
  margin: 24px 0;
  background: #eee;
}

.dictionary-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.dictionary-table th {
  height: 72px;
  padding: 0 8px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
}

.dictionary-table td {
  height: 84px;
  padding: 0 8px;
  border-bottom: 1px solid #eee;
  color: #000;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
}

.medical-table th:nth-child(1),
.medical-table td:nth-child(1),
.plain-drug-table th:nth-child(1),
.plain-drug-table td:nth-child(1),
.project-table th:nth-child(1),
.project-table td:nth-child(1),
.patient-table th:nth-child(1),
.patient-table td:nth-child(1) {
  width: 110px;
}

.medical-table th:nth-child(2),
.medical-table td:nth-child(2),
.plain-drug-table th:nth-child(2),
.plain-drug-table td:nth-child(2),
.project-table th:nth-child(2),
.project-table td:nth-child(2),
.patient-table th:nth-child(2),
.patient-table td:nth-child(2) {
  width: 40%;
  text-align: left;
  padding-left: 94px;
}

.chief-table th:nth-child(1),
.chief-table td:nth-child(1),
.drug-table th:nth-child(1),
.drug-table td:nth-child(1) {
  width: 110px;
}

.chief-table th:nth-child(2),
.chief-table td:nth-child(2),
.chief-table th:nth-child(3),
.chief-table td:nth-child(3),
.drug-table th:nth-child(2),
.drug-table td:nth-child(2),
.drug-table th:nth-child(3),
.drug-table td:nth-child(3) {
  width: 170px;
}

.dictionary-table th:last-child,
.dictionary-table td:last-child {
  width: 170px;
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
  margin-top: 32px;
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
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.42);
}

.dict-modal {
  position: relative;
  width: 650px;
  min-height: 264px;
  padding: 24px 30px 26px;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  box-sizing: border-box;
}

.medical-modal {
  width: 716px;
  min-height: 301px;
}

.drug-modal {
  width: 612px;
  min-height: 333px;
}

.project-modal,
.patient-modal {
  width: 630px;
  min-height: 254px;
}

.dict-modal h2 {
  margin: 0;
  color: #101828;
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
}

.modal-close {
  position: absolute;
  top: 22px;
  right: 30px;
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  color: #c9c9c9;
  font-size: 34px;
  font-weight: 300;
  line-height: 24px;
  cursor: pointer;
}

.modal-form {
  margin-top: 41px;
}

.modal-field {
  display: grid;
  grid-template-columns: 90px 1fr;
  align-items: center;
  color: #101828;
  font-size: 18px;
}

.modal-field span {
  white-space: nowrap;
}

.modal-field em {
  color: #ff1d1d;
  font-style: normal;
}

.modal-field input,
.modal-field select,
.modal-field textarea {
  width: 100%;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  color: #101828;
  font-size: 16px;
  box-sizing: border-box;
}

.modal-field input,
.modal-field select {
  height: 54px;
  padding: 0 16px;
}

.modal-field input::placeholder {
  color: #c5c5c5;
}

.textarea-field {
  align-items: start;
}

.textarea-field span {
  padding-top: 18px;
}

.textarea-field textarea {
  height: 116px;
  padding: 12px 16px;
  resize: none;
}

.drug-modal-form {
  display: grid;
  row-gap: 30px;
}

.compact-select {
  grid-template-columns: 90px 200px;
}

.single-modal-form {
  margin-top: 45px;
}

.single-modal-form .modal-field {
  grid-template-columns: 100px 1fr;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 31px;
}

.medical-modal .modal-actions {
  margin-top: 30px;
}

.drug-modal .modal-actions {
  margin-top: 45px;
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
  color: #101828;
}

.confirm-btn {
  border: 1px solid #6269e8;
  background: #6269e8;
  color: #fff;
}

.delete-confirm {
  width: 420px;
  padding: 28px 32px 26px;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.18);
  box-sizing: border-box;
}

.delete-confirm h2 {
  margin: 0;
  color: #101828;
  font-size: 22px;
  font-weight: 700;
}

.delete-confirm p {
  margin: 28px 0 30px;
  color: #344054;
  font-size: 18px;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

@media (max-width: 1200px) {
  .dictionary-card {
    width: calc(100% - 32px);
  }

  .dictionary-body {
    grid-template-columns: 190px 1fr;
    column-gap: 24px;
  }

  .dictionary-tab {
    width: 120px;
  }
}
</style>
