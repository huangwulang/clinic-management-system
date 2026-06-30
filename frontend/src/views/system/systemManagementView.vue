<template>
  <div class="system-page">
    <div v-if="deleteSuccessVisible" class="success-toast">
      删除成功
    </div>

    <section class="system-card">
      <template v-if="page.kind === 'clinic'">
        <header class="clinic-toolbar">
          <button type="button" class="primary-btn save-btn" @click="saveClinic">
            <Document class="btn-icon" />
            <span>保存</span>
          </button>
        </header>

        <section class="clinic-form">
          <label class="form-field">
            <span>诊所编号</span>
            <input value="10030" />
          </label>
          <label class="form-field">
            <span>诊所名称*</span>
            <input value="**社区诊所" />
          </label>
          <div class="form-field logo-field">
            <span>诊所Logo:</span>
            <div class="logo-row">
              <img :src="logo" alt="诊所 Logo" />
              <button type="button">修改</button>
            </div>
          </div>
          <label class="form-field">
            <span>诊所所有人</span>
            <input value="李明" />
          </label>
          <label class="form-field">
            <span>诊所所有人电话</span>
            <input value="18890978900" />
          </label>
          <label class="form-field">
            <span>诊所所有人邮箱</span>
            <input value="18890978900@163.com" />
          </label>
          <label class="form-field address-select">
            <span>地址</span>
            <select>
              <option>广东省/深圳市</option>
            </select>
          </label>
          <label class="form-field address-detail">
            <span>&nbsp;</span>
            <input placeholder="请输入详细地址" />
          </label>
          <label class="form-field intro-field">
            <span>诊所介绍</span>
            <textarea></textarea>
          </label>
          <div class="clinic-meta">
            <span>诊所状态：</span>
            <span class="switch is-on"><span></span></span>
          </div>
        </section>
      </template>

      <template v-else-if="page.kind === 'basic'">
        <header class="page-toolbar basic-head">
          <div class="setting-tabs">
            <button
              v-for="tab in page.tabs"
              :key="tab"
              type="button"
              :class="['setting-tab', { 'is-active': tab === page.tabs?.[0] }]"
            >
              {{ tab }}
            </button>
          </div>
        </header>

        <section class="basic-panel">
          <div class="basic-block">
            <div>
              <h3>处方单价锁定</h3>
              <p>开启后，处方结算时无法修改药品及附加费等费用项目价格</p>
            </div>
            <span class="switch is-off"><span></span></span>
          </div>
          <div class="basic-block">
            <div>
              <h3>费加费用设置</h3>
              <p>开启后，开具处方及药品零售时支持添加附件费</p>
            </div>
            <span class="switch is-on"><span></span></span>
          </div>
          <div class="basic-block stacked">
            <div class="block-title">
              <h3>挂号功能设置</h3>
              <span class="switch is-on"><span></span></span>
            </div>
            <div class="valid-days">
              <span>挂号有效天数</span>
              <input value="3" />
              <span>天</span>
              <button type="button">修改</button>
            </div>
            <p>0为当天，-1不限天数，&gt;0有效天数</p>
          </div>
          <div class="basic-block stacked compact">
            <h3>金额精度</h3>
            <div class="radio-row">
              <label><input checked name="precision" type="radio" /> 分</label>
              <label><input name="precision" type="radio" /> 角</label>
              <label><input name="precision" type="radio" /> 元</label>
            </div>
          </div>
          <div class="basic-block stacked fields-block">
            <h3>病历字段设置</h3>
            <p class="dark">系统默认（不可修改）</p>
            <div class="check-grid">
              <label v-for="item in defaultFields" :key="item">
                <input checked type="checkbox" /> {{ item }}
              </label>
            </div>
            <p class="dark custom-title">自定义设置</p>
            <div class="check-grid custom">
              <label v-for="item in customFields" :key="item">
                <input type="checkbox" /> {{ item }}
              </label>
            </div>
          </div>
        </section>
      </template>

      <template v-else>
        <header class="page-toolbar">
          <div v-if="page.tabs?.length" class="setting-tabs">
            <button
              v-for="tab in page.tabs"
              :key="tab"
              type="button"
              :class="['setting-tab', { 'is-active': tab === page.tabs?.[0] }]"
            >
              {{ tab }}
            </button>
          </div>
          <div class="header-actions">
            <button
              v-for="action in headerActions"
              :key="action.label"
              type="button"
              :class="['header-btn', action.primary ? 'primary' : 'outline']"
              @click="handleHeaderAction(action)"
            >
              <component :is="action.icon" class="btn-icon" />
              <span>{{ action.label }}</span>
            </button>
          </div>
        </header>

        <div class="page-divider"></div>

        <section :class="['filters-row', { 'with-side': page.sideMenu }]">
          <aside v-if="page.sideMenu" class="side-menu">
            <button
              v-for="item in page.sideMenu"
              :key="item"
              type="button"
              :class="{ active: item === page.sideMenu[0] }"
            >
              {{ item }}
            </button>
          </aside>

          <div class="list-main">
            <div class="filter-line">
              <label v-if="page.selectLabel" class="filter-item">
                <span>{{ page.selectLabel }}</span>
                <select><option>全部</option></select>
              </label>
              <label v-if="page.search" class="search-filter">
                <input v-model="listKeyword" :placeholder="page.search" @keyup.enter="loadSystemPage" />
                <button type="button" aria-label="搜索" @click="loadSystemPage"><Search class="search-icon" /></button>
              </label>
            </div>

            <table :class="['system-table', page.tableClass]">
              <thead>
                <tr>
                  <th v-for="column in page.columns" :key="column.key">{{ column.label }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in page.rows" :key="row.id">
                  <td v-for="column in page.columns" :key="column.key">
                    <template v-if="column.key === 'statusSwitch'">
                      <span class="switch is-on"><span></span></span>
                    </template>
                    <template v-else-if="column.key === 'actions'">
                      <div class="actions">
                        <button
                          v-for="action in row.actions"
                          :key="action"
                          type="button"
                          @click="handleRowAction(action, row)"
                        >
                          {{ action }}
                        </button>
                      </div>
                    </template>
                    <span v-else>{{ row[column.key] }}</span>
                  </td>
                </tr>
              </tbody>
            </table>

            <footer class="pagination">
              <button type="button" class="pager-btn">&lt;</button>
              <button type="button" class="pager-btn is-active">1</button>
              <button v-if="page.fullPager" type="button" class="pager-btn">2</button>
              <button v-if="page.fullPager" type="button" class="pager-btn">3</button>
              <button v-if="page.fullPager" type="button" class="pager-btn">4</button>
              <button v-if="page.fullPager" type="button" class="pager-btn">5</button>
              <span v-if="page.fullPager" class="pager-ellipsis">......</span>
              <button v-if="page.fullPager" type="button" class="pager-btn">60</button>
              <button type="button" class="pager-btn">&gt;</button>
              <span class="pager-info">每页10条，共30条&nbsp;&nbsp;前往第</span>
              <span class="pager-input">1</span>
              <span class="pager-info">页</span>
            </footer>
          </div>
        </section>
      </template>
    </section>

    <div v-if="deleteDialogVisible" class="modal-mask">
      <div class="delete-dialog">
        <button class="dialog-close" type="button" @click="closeDeleteDialog">×</button>
        <h3>删除确认</h3>
        <div class="dialog-content">
          <span class="warning-icon">!</span>
          <span>确定要删除此供应商信息吗?</span>
        </div>
        <div class="dialog-actions">
          <button class="cancel-btn" type="button" @click="closeDeleteDialog">取消</button>
          <button class="confirm-btn" type="button" @click="confirmDeleteSupplier">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  CirclePlusFilled,
  Document,
  Download,
  Search,
  Upload,
} from "@element-plus/icons-vue";
import logo from "@/assets/logo.png";
import { ElMessage } from "element-plus";
import { checkProjectApi, clinicInfoApi, settingsApi, supplierApi } from "@/api";

type RowData = Record<string, string | number | string[]>;
type Column = { key: string; label: string };
type HeaderAction = { label: string; primary?: boolean; icon: typeof CirclePlusFilled };
type PageConfig = {
  kind: "clinic" | "list" | "basic";
  tabs?: string[];
  addText?: string;
  actions?: HeaderAction[];
  selectLabel?: string;
  search?: string;
  sideMenu?: string[];
  columns?: Column[];
  rows?: RowData[];
  tableClass?: string;
  fullPager?: boolean;
};

const route = useRoute();
const router = useRouter();
const legacyPath = computed(() => ({
  "/system/clinic": "/system",
  "/system/check-projects": "/system/check-project-setting",
  "/system/suppliers": "/system/supplier",
  "/system/basic": "/system/basic-setting",
}[route.path] || route.path));

const defaultFields = ["诊断", "医嘱", "主诉", "发病日期", "体格信息", "现病史", "既往史", "过敏史", "个人史", "家族史", "辅助检查", "治疗意见", "备注"];
const customFields = ["望闻问切", "疾病详情", "辩证分析", "治疗意见", "文件", "门诊处理"];

const times = ["2019-11-12 12:08:28", "2019-11-12 12:08:29", "2019-11-12 12:08:30", "2019-11-12 12:08:31"];
const creators = ["王冕", "林鹤"];
const diseases = ["心悸", "头疼", "病毒性感冒", "腹部积水", "感冒", "发烧", "支气管炎", "急性肠胃炎", "脊椎炎", "心悸"];

const dictRows = diseases.map((name, index) => ({
  id: `dict-${index}`,
  index: index + 1,
  content: name,
  createTime: index < 3 ? "2019-11-12 12:08:12" : `2019-11-12 12:08:${12 + (index - 2) * 2}`,
  creator: creators[index % 2],
  actions: ["编辑", "删除"],
}));

const staffRows = Array.from({ length: 10 }, (_, index) => ({
  id: `staff-${index}`,
  index: index + 1,
  jobNo: 1030 - index,
  name: `姓名${59 - index}`,
  gender: index % 2 === 0 ? "男" : "女",
  age: 25 + index,
  phone: String(18854136788 + index),
  clinic: index % 2 === 0 ? "支所1" : "支所2",
  department: ["全科", "内科", "外科", "儿科"][index % 4],
  role: ["医生", "护士", "财务", "医生，护士", "管理员"][index % 5],
  createTime: times[index % 4],
  creator: creators[index % 2],
  statusSwitch: "",
  actions: ["编辑", "删除"],
}));

const projectRows = reactive(Array.from({ length: 10 }, (_, index) => ({
  id: `project-${index}`,
  index: index + 1,
  code: 1000001 + index,
  name: ["煎药费", "拔牙", "手术费"][index % 3],
  category: ["脑电图", "血压测量", "超声检查", "针灸"][index % 4],
  invoice: index % 2 === 0 ? "注射费" : "检查费",
  retail: `${15 + index}.00`,
  cost: `${13 + index}.00`,
  unit: "次",
  status: "启用",
  createTime: "2019-02-09 10:23:56",
  actions: ["编辑", "停用"],
})));

const supplierRows = reactive(["蓝天制药厂", "白云制药厂", "白云制药厂", "白云制药厂"].map((name, index) => ({
  id: `supplier-${index}`,
  index: index + 1,
  code: 1004 - index,
  name,
  contact: ["王宽硕", "李明", "赵健", "曲晓静"][index],
  phone: "18865789099",
  createTime: times[index],
  creator: creators[index % 2],
  status: "启用",
  actions: ["编辑", "删除"],
})));

const templateRows = diseases.map((name, index) => ({
  id: `template-${index}`,
  index: index + 1,
  code: `RZ${String(index + 5).padStart(5, "0")}`,
  name: `病历名称${index + 1}`,
  diagnosis: name,
  permission: index % 2 === 0 ? "私人模板" : "公共模板",
  createTime: times[index % 4],
  creator: creators[index % 2],
  actions: ["编辑", "删除"],
}));

const feeRows = ["材料费", "注射费", "煎药费", "皮试"].map((name, index) => ({
  id: `fee-${index}`,
  index: index + 1,
  name,
  type: index % 2 === 0 ? "西/成药处方" : "中药处方",
  amount: index < 2 ? "20.00" : index === 2 ? "15.00" : "30.00",
  cost: index < 2 ? "10.00" : "0.00",
  createTime: times[index],
  creator: creators[index % 2],
  discount: "是",
  status: "启用",
  actions: ["编辑", "删除"],
}));

const pages: Record<string, PageConfig> = {
  "/system": { kind: "clinic" },
  "/system/dictionary": {
    kind: "list",
    tabs: ["病历信息", "药品信息", "检查项目", "患者信息"],
    addText: "新增",
    sideMenu: ["诊断信息", "医嘱信息", "主诉信息", "现病史", "既往史", "过敏史", "个人史", "辅助检查", "治疗意见"],
    search: "模板内容",
    columns: [
      { key: "index", label: "序号" },
      { key: "content", label: "诊断内容" },
      { key: "createTime", label: "创建时间" },
      { key: "creator", label: "创建人" },
      { key: "actions", label: "操作" },
    ],
    rows: dictRows,
    tableClass: "table-dictionary",
    fullPager: true,
  },
  "/system/staff": {
    kind: "list",
    tabs: ["员工列表", "科室列表", "角色列表"],
    addText: "新增",
    selectLabel: "所属科室",
    search: "员工姓名",
    columns: [
      { key: "index", label: "序号" },
      { key: "jobNo", label: "工号" },
      { key: "name", label: "员工姓名" },
      { key: "gender", label: "员工性别" },
      { key: "age", label: "员工年龄" },
      { key: "phone", label: "手机号码" },
      { key: "clinic", label: "所属诊所" },
      { key: "department", label: "所属科室" },
      { key: "role", label: "角色" },
      { key: "createTime", label: "创建时间" },
      { key: "creator", label: "创建人员" },
      { key: "statusSwitch", label: "员工状态" },
      { key: "actions", label: "操作" },
    ],
    rows: staffRows,
    tableClass: "table-staff",
    fullPager: true,
  },
  "/system/check-project-setting": {
    kind: "list",
    actions: [
      { label: "新增项目", primary: true, icon: CirclePlusFilled },
      { label: "导入", icon: Download },
      { label: "导出", icon: Upload },
    ],
    selectLabel: "项目状态",
    search: "项目名称/项目编号",
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "项目编号" },
      { key: "name", label: "项目名称" },
      { key: "category", label: "项目类别" },
      { key: "invoice", label: "发票项目" },
      { key: "retail", label: "零售价（元）" },
      { key: "cost", label: "成本价（元）" },
      { key: "unit", label: "单位" },
      { key: "status", label: "项目状态" },
      { key: "createTime", label: "创建时间" },
      { key: "actions", label: "操作" },
    ],
    rows: projectRows,
    tableClass: "table-project",
    fullPager: true,
  },
  "/system/supplier": {
    kind: "list",
    addText: "新增",
    search: "供应商名称",
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "供应商编号" },
      { key: "name", label: "供应商名称" },
      { key: "contact", label: "联系人" },
      { key: "phone", label: "联系电话" },
      { key: "createTime", label: "创建时间" },
      { key: "creator", label: "创建人员" },
      { key: "status", label: "供应商状态" },
      { key: "actions", label: "操作" },
    ],
    rows: supplierRows,
    tableClass: "table-supplier",
  },
  "/system/template": {
    kind: "list",
    tabs: ["病历模板", "处方模板"],
    addText: "新增",
    selectLabel: "模板权限",
    search: "模板编码/模板模板名称",
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "模板编号" },
      { key: "name", label: "模板名称" },
      { key: "diagnosis", label: "诊断" },
      { key: "permission", label: "模板权限" },
      { key: "createTime", label: "创建时间" },
      { key: "creator", label: "创建人员" },
      { key: "actions", label: "操作" },
    ],
    rows: templateRows,
    tableClass: "table-template",
    fullPager: true,
  },
  "/system/fee-setting": {
    kind: "list",
    tabs: ["附加费用设置", "诊疗费设置", "挂号费设置", "支付方式设置"],
    addText: "新增",
    selectLabel: "处方类别",
    search: "费用名称",
    columns: [
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
    ],
    rows: feeRows,
    tableClass: "table-fee",
  },
  "/system/basic-setting": {
    kind: "basic",
    tabs: ["基础信息设置", "短信设置", "医保设置"],
  },
};

const page = computed(() => pages[legacyPath.value] || pages["/system"]);
const headerActions = computed(() => {
  return page.value.actions || [
    { label: page.value.addText || "新增", primary: true, icon: CirclePlusFilled },
  ];
});

const isCheckProjectSetting = computed(() => legacyPath.value === "/system/check-project-setting");
const isSupplierPage = computed(() => legacyPath.value === "/system/supplier");
const listKeyword = ref("");
const deleteSupplierId = ref<string | number | null>(null);
const deleteDialogVisible = ref(false);
const deleteSuccessVisible = ref(false);
let deleteSuccessTimer: ReturnType<typeof window.setTimeout> | null = null;

const handleHeaderAction = (action: HeaderAction) => {
  if (isCheckProjectSetting.value && action.label === "新增项目") {
    router.push({ name: "SystemCheckProjectForm" });
  }

  if (isSupplierPage.value && action.label === "新增") {
    router.push({ name: "SystemSupplierForm" });
  }
};

const handleRowAction = async (action: string, row: RowData) => {
  if (isCheckProjectSetting.value && action === "编辑") {
    router.push({ name: "SystemCheckProjectForm", state: { id: row.id } });
  }

  if (isCheckProjectSetting.value && (action === "停用" || action === "启用")) {
    await checkProjectApi.update(row.id, { status: action === "停用" ? "DISABLED" : "ENABLED" });
    await loadSystemPage();
    ElMessage.success(action === "停用" ? "项目已停用" : "项目已启用");
  }

  if (isSupplierPage.value && action === "编辑") {
    router.push({ name: "SystemSupplierForm", state: { id: row.id } });
  }

  if (isSupplierPage.value && action === "删除") {
    deleteSupplierId.value = row.id;
    deleteDialogVisible.value = true;
  }
};

const closeDeleteDialog = () => {
  deleteDialogVisible.value = false;
  deleteSupplierId.value = null;
};

const confirmDeleteSupplier = async () => {
  if (deleteSupplierId.value) await supplierApi.remove(deleteSupplierId.value);
  const targetIndex = supplierRows.findIndex((row) => row.id === deleteSupplierId.value);
  if (targetIndex > -1) {
    supplierRows.splice(targetIndex, 1);
    supplierRows.forEach((row, index) => {
      row.index = index + 1;
    });
    deleteSuccessVisible.value = true;
    if (deleteSuccessTimer) {
      window.clearTimeout(deleteSuccessTimer);
    }
    deleteSuccessTimer = window.setTimeout(() => {
      deleteSuccessVisible.value = false;
      deleteSuccessTimer = null;
    }, 3000);
  }
  closeDeleteDialog();
};

const statusLabel = (status: unknown) => {
  if (status === "DISABLED" || status === "停用" || status === "禁用" || status === false || status === 0) return "停用";
  return "启用";
};

const loadSystemPage = async () => {
  const keyword = listKeyword.value.trim();
  if (legacyPath.value === "/system/check-project-setting") {
    const response: any = await checkProjectApi.page({ page: 1, size: 100, keyword });
    projectRows.splice(0, projectRows.length, ...(response?.data?.records || []).map((item: any, index: number) => ({
      index: index + 1, id: item.id, code: item.projectCode || item.code || "", name: item.name || "",
      cost: item.costPrice ?? 0, retail: item.retailPrice ?? item.price ?? 0, unit: item.unit || "", category: item.category || "",
      invoice: item.invoiceItem || "", part: item.bodyPart || "", discount: item.allowMemberDiscount === false ? "否" : "是",
      status: statusLabel(item.status), createTime: item.createdAt || "", creator: item.creator || "",
      actions: ["编辑", statusLabel(item.status) === "启用" ? "停用" : "启用"],
    })));
  } else if (legacyPath.value === "/system/supplier") {
    const response: any = await supplierApi.page({ page: 1, size: 100, keyword });
    supplierRows.splice(0, supplierRows.length, ...(response?.data?.records || []).map((item: any, index: number) => ({
      index: index + 1, id: item.id, code: item.supplierCode || item.code || "", name: item.name || "",
      contact: item.contact || item.contactName || "", phone: item.phone || "", status: statusLabel(item.status),
      createTime: item.createdAt || "", creator: item.creator || "", actions: ["编辑", "删除"],
    })));
  } else if (legacyPath.value === "/system") {
    await clinicInfoApi.page({ page: 1, size: 1 });
  } else if (legacyPath.value === "/system/basic-setting") {
    await settingsApi.get();
  }
};

const saveClinic = async () => {
  const response: any = await clinicInfoApi.page({ page: 1, size: 1 });
  const current = response?.data?.records?.[0];
  if (current?.id) await clinicInfoApi.update(current.id, current);
  else await clinicInfoApi.create({ clinicName: "诊所管理系统" });
  ElMessage.success("诊所信息保存成功");
};

onMounted(loadSystemPage);
watch(legacyPath, () => {
  listKeyword.value = "";
  loadSystemPage();
});
</script>

<style scoped>
.system-page {
  min-height: 100%;
  padding: 32px 0 58px;
}

.system-card {
  width: min(1616px, calc(100% - 96px));
  margin: 0 auto;
  padding: 31px 32px 42px;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.success-toast {
  position: fixed;
  top: 24px;
  left: 50%;
  z-index: 1100;
  min-width: 160px;
  height: 44px;
  padding: 0 24px;
  transform: translateX(-50%);
  border-radius: 4px;
  background: #f0f9eb;
  border: 1px solid #b7eb8f;
  color: #52c41a;
  font-size: 16px;
  line-height: 44px;
  text-align: center;
  box-shadow: 0 4px 14px rgba(23, 31, 56, 0.12);
}

.page-toolbar,
.clinic-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 48px;
  gap: 18px;
}

.clinic-toolbar {
  justify-content: flex-end;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 11px;
  min-width: 142px;
  height: 48px;
  padding: 0 18px;
  border: 0;
  border-radius: 5px;
  background: #636be8;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.header-actions {
  display: inline-flex;
  align-items: center;
  gap: 20px;
  margin-left: auto;
}

.header-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 11px;
  min-width: 118px;
  height: 48px;
  padding: 0 18px;
  border: 1px solid #636be8;
  border-radius: 5px;
  background: #fff;
  color: #636be8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.header-btn.primary {
  min-width: 146px;
  background: #636be8;
  color: #fff;
}

.btn-icon {
  width: 23px;
  height: 23px;
}

.page-divider {
  height: 1px;
  margin: 28px 0 25px;
  background: #eeeeee;
}

.setting-tabs {
  display: inline-flex;
  overflow: hidden;
  border: 1px solid #c6c8cf;
  border-radius: 4px;
}

.setting-tab {
  min-width: 128px;
  height: 46px;
  padding: 0 20px;
  border: 0;
  border-right: 1px solid #c6c8cf;
  background: #fff;
  color: #b8b8b8;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.setting-tab:last-child {
  border-right: 0;
}

.setting-tab.is-active {
  background: #636be8;
  color: #fff;
}

.clinic-form {
  display: grid;
  grid-template-columns: 362px 362px 362px;
  gap: 24px 48px;
  margin-top: 32px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #111;
  font-size: 16px;
}

.form-field input,
.form-field select,
.form-field textarea {
  height: 58px;
  padding: 0 18px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  outline: none;
  color: #000;
  font-size: 18px;
}

.form-field textarea {
  height: 58px;
  padding-top: 12px;
  resize: none;
}

.logo-row {
  display: flex;
  align-items: center;
  gap: 36px;
  height: 58px;
}

.logo-row img {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.logo-row button,
.clinic-meta button,
.actions button,
.valid-days button {
  border: 0;
  background: transparent;
  color: #6068f1;
  font-size: 16px;
  cursor: pointer;
}

.address-detail {
  grid-column: span 2;
}

.intro-field {
  grid-column: 1 / span 3;
}

.clinic-meta {
  grid-column: 1 / span 3;
  display: flex;
  align-items: center;
  gap: 48px;
  margin-top: 14px;
  font-size: 18px;
}

.switch {
  display: inline-flex;
  align-items: center;
  width: 51px;
  height: 30px;
  padding: 2px;
  border-radius: 999px;
  background: #35d07f;
}

.switch span {
  display: block;
  width: 26px;
  height: 26px;
  margin-left: auto;
  border-radius: 50%;
  background: #fff;
}

.switch.is-off {
  background: #eee;
}

.switch.is-off span {
  margin-left: 0;
}

.filters-row {
  display: flex;
  gap: 34px;
}

.filters-row.with-side {
  align-items: flex-start;
}

.side-menu {
  width: 230px;
  flex: none;
  border: 1px solid #edf0ff;
}

.side-menu button {
  width: 100%;
  height: 58px;
  border: 0;
  border-bottom: 1px solid #edf0ff;
  background: #fff;
  color: #111;
  font-size: 18px;
  cursor: pointer;
}

.side-menu button.active {
  background: #636be8;
  color: #fff;
}

.list-main {
  flex: 1;
  min-width: 0;
}

.filter-line {
  display: flex;
  align-items: center;
  gap: 27px;
  margin-bottom: 31px;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  gap: 15px;
  color: #1e2533;
  font-size: 16px;
  white-space: nowrap;
}

.filter-item select,
.search-filter {
  height: 44px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  background: #fff;
}

.filter-item select {
  width: 190px;
  padding: 0 20px;
  font-size: 16px;
  outline: none;
}

.search-filter {
  display: grid;
  grid-template-columns: 397px 62px;
  overflow: hidden;
}

.search-filter input {
  height: 100%;
  padding: 0 14px;
  border: 0;
  outline: none;
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
}

.search-icon {
  width: 27px;
  height: 27px;
}

.system-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.system-table th {
  height: 72px;
  padding: 0 8px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
}

.system-table td {
  height: 84px;
  padding: 0 8px;
  border-bottom: 1px solid #eeeeee;
  color: #000;
  font-size: 16px;
  text-align: center;
}

.actions {
  display: inline-flex;
  gap: 25px;
  align-items: center;
  justify-content: center;
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
}

.pager-btn.is-active {
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

.pager-ellipsis {
  margin: 0 8px 0 14px;
}

.basic-head {
  justify-content: flex-start;
  margin-bottom: 36px;
}

.basic-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.basic-block {
  min-height: 102px;
  padding: 20px 40px;
  background: #f7f7ff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.basic-block h3 {
  margin: 0 0 12px;
  font-size: 22px;
}

.basic-block p {
  margin: 0;
  color: #8a92a1;
  font-size: 18px;
}

.basic-block.stacked {
  align-items: stretch;
  flex-direction: column;
}

.block-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.valid-days {
  display: flex;
  align-items: center;
  gap: 18px;
  margin: 18px 0 12px;
  font-size: 18px;
}

.valid-days input {
  width: 74px;
  height: 32px;
  border: 1px solid #c9c9c9;
  border-radius: 4px;
  background: #e9e9e9;
  text-align: center;
  font-size: 18px;
}

.basic-block.compact {
  min-height: 100px;
}

.radio-row,
.check-grid {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 28px 58px;
  font-size: 18px;
}

.radio-row input,
.check-grid input {
  width: 18px;
  height: 18px;
  accent-color: #636be8;
}

.fields-block {
  min-height: 275px;
}

.fields-block .dark {
  color: #111;
  margin-bottom: 20px;
}

.custom-title {
  margin-top: 34px !important;
}

.check-grid.custom {
  gap: 28px 62px;
}

.table-dictionary th:nth-child(1), .table-dictionary td:nth-child(1) { width: 110px; }
.table-dictionary th:nth-child(2), .table-dictionary td:nth-child(2) { width: 42%; text-align: left; padding-left: 96px; }
.table-dictionary th:nth-child(5), .table-dictionary td:nth-child(5) { width: 170px; }
.table-staff th:nth-child(1), .table-staff td:nth-child(1) { width: 78px; }
.table-staff th:nth-child(10), .table-staff td:nth-child(10) { width: 190px; }
.table-staff th:nth-child(12), .table-staff td:nth-child(12) { width: 105px; }
.table-project th:nth-child(1), .table-project td:nth-child(1) { width: 90px; }
.table-project th:nth-child(10), .table-project td:nth-child(10) { width: 205px; }
.table-supplier th:nth-child(1), .table-supplier td:nth-child(1) { width: 120px; }
.table-supplier th:nth-child(6), .table-supplier td:nth-child(6) { width: 240px; }
.table-template th:nth-child(1), .table-template td:nth-child(1) { width: 110px; }
.table-fee th:nth-child(1), .table-fee td:nth-child(1) { width: 90px; }

@media (max-width: 1280px) {
  .system-card {
    width: calc(100% - 32px);
    padding: 24px 20px 36px;
  }

  .clinic-form {
    grid-template-columns: 1fr 1fr;
  }

  .intro-field,
  .clinic-meta {
    grid-column: 1 / -1;
  }

  .list-main {
    overflow-x: auto;
  }

  .system-table {
    min-width: 1200px;
  }
}
</style>
