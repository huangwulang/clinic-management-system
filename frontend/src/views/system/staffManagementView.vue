<template>
  <div class="staff-page">
    <section v-if="viewMode === 'list'" class="staff-card">
      <header class="staff-toolbar">
        <div class="staff-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="['staff-tab', { active: activeTab === tab.key }]"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </div>

        <button type="button" class="add-btn" @click="openForm()">
          <el-icon><CirclePlusFilled /></el-icon>
          <span>新增</span>
        </button>
      </header>

      <div class="staff-divider"></div>

      <section class="filter-row" :class="{ department: activeTab !== 'staff' }">
        <label v-if="activeTab === 'staff'" class="dept-filter">
          <span>所属科室</span>
          <select v-model="staffFilters.department">
            <option value="">全部</option>
            <option v-for="department in departmentOptions" :key="department" :value="department">
              {{ department }}
            </option>
          </select>
        </label>

        <label class="search-filter">
          <input v-model="keyword" type="text" :placeholder="currentConfig.searchPlaceholder" />
          <button type="button" aria-label="搜索">
            <el-icon><Search /></el-icon>
          </button>
        </label>
      </section>

      <div class="table-wrapper">
        <table v-if="activeTab === 'staff'" class="staff-table employee-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>工号</th>
              <th>员工姓名</th>
              <th>员工性别</th>
              <th>员工年龄</th>
              <th>手机号码</th>
              <th>所属诊所</th>
              <th>所属科室</th>
              <th>角色</th>
              <th>创建时间</th>
              <th>创建人员</th>
              <th>员工状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in filteredStaffRows" :key="row.jobNo">
              <td>{{ row.index }}</td>
              <td>{{ row.jobNo }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.gender }}</td>
              <td>{{ row.age }}</td>
              <td>{{ row.phone }}</td>
              <td>{{ row.clinic }}</td>
              <td>{{ row.department }}</td>
              <td>{{ row.role }}</td>
              <td>{{ row.createTime }}</td>
              <td>{{ row.creator }}</td>
              <td>
                <button
                  type="button"
                  :class="['switch', { off: !isEnabled(row) }]"
                  @click="toggleRowEnabled(row)"
                >
                  <span></span>
                </button>
              </td>
              <td class="action-cell">
                <button type="button" @click="openForm(row)">编辑</button>
                <button type="button" @click="openDeleteConfirm('员工', row)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else-if="activeTab === 'department'" class="staff-table department-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>科室编号</th>
              <th>科室名称</th>
              <th>科室简介</th>
              <th>创建时间</th>
              <th>创建人员</th>
              <th>科室状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in filteredDepartmentRows" :key="row.code">
              <td>{{ row.index }}</td>
              <td>{{ row.code }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.description }}</td>
              <td>{{ row.createTime }}</td>
              <td>{{ row.creator }}</td>
              <td>
                <button
                  type="button"
                  :class="['switch', { off: !isEnabled(row) }]"
                  @click="toggleRowEnabled(row)"
                >
                  <span></span>
                </button>
              </td>
              <td class="action-cell">
                <button type="button" @click="openForm(row)">编辑</button>
                <button type="button" @click="openDeleteConfirm('科室', row)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else class="staff-table role-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>角色编号</th>
              <th>角色名称</th>
              <th>角色描述</th>
              <th>创建时间</th>
              <th>创建人员</th>
              <th>角色状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in filteredRoleRows" :key="row.code">
              <td>{{ row.index }}</td>
              <td>{{ row.code }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.description }}</td>
              <td>{{ row.createTime }}</td>
              <td>{{ row.creator }}</td>
              <td>
                <button
                  type="button"
                  :class="['switch', { off: !isEnabled(row) }]"
                  @click="toggleRowEnabled(row)"
                >
                  <span></span>
                </button>
              </td>
              <td class="action-cell">
                <button type="button" @click="openForm(row)">编辑</button>
                <button type="button" @click="openDeleteConfirm('角色', row)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="pagination" :class="{ full: activeTab === 'staff' }">
        <button type="button" class="pager-btn">&lt;</button>
        <button type="button" class="pager-btn active">1</button>
        <template v-if="activeTab === 'staff'">
          <button type="button" class="pager-btn">2</button>
          <button type="button" class="pager-btn">3</button>
          <button type="button" class="pager-btn">4</button>
          <button type="button" class="pager-btn">5</button>
          <span class="pager-ellipsis">......</span>
          <button type="button" class="pager-btn">60</button>
        </template>
        <button type="button" class="pager-btn">&gt;</button>
        <span>每页10条，共30条&nbsp;&nbsp;前往第</span>
        <span class="pager-input">1</span>
        <span>页</span>
      </footer>
    </section>

    <section v-else class="form-card" :class="`${activeTab}-form-card`">
      <header class="form-header">
        <h2>{{ formTitle }}</h2>
        <div class="form-actions">
          <button type="button" class="save-btn" @click="saveCurrent">
            <el-icon><Document /></el-icon>
            <span>保存</span>
          </button>
          <button type="button" class="back-btn" @click="backToList">
            <el-icon><Back /></el-icon>
            <span>返回</span>
          </button>
        </div>
      </header>

      <form v-if="activeTab === 'staff'" class="staff-form">
        <label class="form-field">
          <span>员工编号</span>
          <input v-model="form.jobNo" />
        </label>
        <label class="form-field required">
          <span>员工姓名</span>
          <input v-model="form.name" />
        </label>
        <label class="form-field age-field required">
          <span>员工年龄</span>
          <div class="input-with-unit">
            <input v-model="form.age" placeholder="请输入数字" />
            <select>
              <option>岁</option>
            </select>
          </div>
        </label>
        <label class="form-field required">
          <span>性别</span>
          <select v-model="form.gender">
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </label>
        <label class="form-field">
          <span>手机号码</span>
          <input v-model="form.phone" placeholder="请输入手机号码" />
        </label>
        <label class="form-field">
          <span>电子邮箱</span>
          <input v-model="form.email" />
        </label>
        <label class="form-field">
          <span>证件号码</span>
          <input v-model="form.idCard" placeholder="请输入证件号码" />
        </label>
        <label class="form-field">
          <span>职位</span>
          <input v-model="form.positionName" placeholder="请输入职位名称" />
        </label>
        <label class="form-field address-select">
          <span>地址</span>
          <select>
            <option>广东省/深圳市</option>
          </select>
        </label>
        <label class="form-field address-detail">
          <span>&nbsp;</span>
          <input v-model="form.address" placeholder="请输入详细地址" />
        </label>
        <label class="form-field required">
          <span>所属科室</span>
          <select v-model="form.departmentName">
            <option>请选择</option>
            <option v-for="department in departmentOptions" :key="department" :value="department">{{ department }}</option>
          </select>
        </label>
        <label class="form-field required">
          <span>角色</span>
          <select v-model="form.roleName">
            <option></option>
            <option v-for="role in roleRows" :key="role.code" :value="role.name">{{ role.name }}</option>
          </select>
        </label>
        <label class="form-field">
          <span>密码</span>
          <input placeholder="6-12位，包含数字和字母" />
        </label>
        <div class="status-line">
          <span>员工状态:</span>
          <button type="button" :class="['switch', { off: !form.enabled }]" @click="form.enabled = !form.enabled">
            <span></span>
          </button>
        </div>
      </form>

      <form v-else-if="activeTab === 'department'" class="simple-form department-form">
        <label class="form-field">
          <span>科室编号</span>
          <input v-model="form.code" />
        </label>
        <label class="form-field required">
          <span>科室名称</span>
          <input v-model="form.name" />
        </label>
        <div class="status-line inline-status">
          <span>科室状态:</span>
          <button type="button" :class="['switch', { off: !form.enabled }]" @click="form.enabled = !form.enabled">
            <span></span>
          </button>
        </div>
        <label class="form-field description-field">
          <span>科室简介</span>
          <input v-model="form.description" />
        </label>
        <label class="form-field">
          <span>创建人</span>
          <input v-model="form.creator" />
        </label>
        <label class="form-field">
          <span>创建时间</span>
          <input v-model="form.createdAt" readonly />
        </label>
      </form>

      <form v-else class="simple-form role-form">
        <label class="form-field">
          <span>角色编号</span>
          <input v-model="form.code" />
        </label>
        <label class="form-field required">
          <span>角色名称</span>
          <input v-model="form.name" />
        </label>
        <div class="status-line inline-status">
          <span>角色状态:</span>
          <button type="button" :class="['switch', { off: !form.enabled }]" @click="form.enabled = !form.enabled">
            <span></span>
          </button>
        </div>
        <label class="form-field description-field">
          <span>角色描述</span>
          <input v-model="form.description" />
        </label>

        <section class="permission-section">
          <h3>权限分配</h3>
          <div class="permission-table">
            <div class="permission-head">
              <label>
                <input
                  type="checkbox"
                  :checked="allModulesChecked()"
                  @change="toggleAllModules(($event.target as HTMLInputElement).checked)"
                />模块
              </label>
              <span>功能</span>
            </div>
            <div v-for="module in permissionModules" :key="module.code" class="permission-row">
              <label class="module-check">
                <input
                  type="checkbox"
                  :checked="isModuleChecked(module)"
                  @change="toggleModule(module, ($event.target as HTMLInputElement).checked)"
                />{{ module.title }}
              </label>
              <div class="permission-functions">
                <label v-for="item in module.children" :key="item.code">
                  <input
                    type="checkbox"
                    :checked="isFunctionChecked(module, item.code)"
                    @change="toggleFunction(module, item.code, ($event.target as HTMLInputElement).checked)"
                  />{{ item.title }}
                </label>
              </div>
            </div>
          </div>
        </section>
      </form>
    </section>

    <div v-if="showDeleteConfirm" class="modal-mask">
      <section class="confirm-card">
        <h3>删除确认</h3>
        <p>确定要删除该{{ deleteTarget }}信息吗？</p>
        <div class="confirm-actions">
          <button type="button" class="cancel-btn" @click="showDeleteConfirm = false">取消</button>
          <button type="button" class="confirm-btn" @click="confirmDelete">确定</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, CirclePlusFilled, Document, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { accountApi, departmentApi, employeeApi, roleApi } from "@/api";
import { permissionModules, normalizePermissions, type PermissionModule } from "@/utils/permissions";

type TabKey = "staff" | "department" | "role";
const route = useRoute();
const router = useRouter();
type ViewMode = "list" | "form";
type RowData = Record<string, any>;

const tabs: { key: TabKey; label: string; searchPlaceholder: string }[] = [
  { key: "staff", label: "员工列表", searchPlaceholder: "员工姓名" },
  { key: "department", label: "科室列表", searchPlaceholder: "科室名称" },
  { key: "role", label: "角色列表", searchPlaceholder: "角色名称" },
];

const activeTab = ref<TabKey>("staff");
const viewMode = ref<ViewMode>("list");
const formAction = ref<"add" | "edit">("add");
const editingRow = ref<RowData | null>(null);
const keyword = ref("");
const showDeleteConfirm = ref(false);
const deleteTarget = ref("");
const deleteRow = ref<RowData | null>(null);
const staffFilters = reactive({ department: "" });
const form = reactive({
  id: "",
  jobNo: "",
  code: "",
  name: "",
  gender: "男",
  age: "",
  phone: "",
  email: "",
  idCard: "",
  positionName: "",
  address: "",
  departmentName: "",
  roleName: "",
  clinicName: "",
  description: "",
  creator: "",
  createdAt: "",
  enabled: true,
  permissions: [] as string[],
});

const departments = ["全科", "内科", "外科", "儿科"];
const times = ["2019-11-12 12:08:28", "2019-11-12 12:08:29", "2019-11-12 12:08:30", "2019-11-12 12:08:31"];
const creators = ["王冕", "林鹤"];

const staffRows = reactive(Array.from({ length: 10 }, (_, index) => ({
  index: index + 1,
  jobNo: 1030 - index,
  name: `姓名${59 - index}`,
  gender: index % 2 === 0 ? "男" : "女",
  age: 25 + index,
  phone: String(18854136788 + index),
  clinic: index % 2 === 0 ? "支所1" : "支所2",
  department: departments[index % departments.length],
  role: ["医生", "护士", "财务", "医生，护士", "管理员"][index % 5],
  createTime: times[index % times.length],
  creator: creators[index % creators.length],
})));

const departmentRows = reactive([
  { index: 1, code: "104", name: "全科", description: "通用", createTime: times[0], creator: "王冕" },
  { index: 2, code: "103", name: "内科", description: "", createTime: times[1], creator: "林鹤" },
  { index: 3, code: "102", name: "外科", description: "", createTime: times[2], creator: "王冕" },
  { index: 4, code: "101", name: "儿科", description: "", createTime: times[3], creator: "林鹤" },
]);

const roleRows = reactive([
  { index: 1, code: "04", name: "超级管理", description: "通用", createTime: times[0], creator: "王冕" },
  { index: 2, code: "03", name: "医生", description: "", createTime: times[1], creator: "林鹤" },
  { index: 3, code: "02", name: "护士", description: "", createTime: times[2], creator: "王冕" },
  { index: 4, code: "01", name: "财务", description: "", createTime: times[3], creator: "林鹤" },
]);

const departmentOptions = computed(() => {
  const names = departmentRows.map((department) => department.name).filter(Boolean);
  return names.length ? names : departments;
});

const padTime = (value: number) => String(value).padStart(2, "0");

const currentDateTime = () => {
  const now = new Date();
  return [
    now.getFullYear(),
    padTime(now.getMonth() + 1),
    padTime(now.getDate()),
  ].join("-") + " " + [
    padTime(now.getHours()),
    padTime(now.getMinutes()),
    padTime(now.getSeconds()),
  ].join(":");
};

const currentConfig = computed(() => tabs.find((tab) => tab.key === activeTab.value) || tabs[0]);
const formTitle = computed(() => {
  const verb = formAction.value === "add" ? "新增" : "编辑";
  const names: Record<TabKey, string> = { staff: "员工信息", department: "科室信息", role: "角色信息" };
  return `${verb}${names[activeTab.value]}`;
});

const filteredStaffRows = computed(() => {
  const word = keyword.value.trim();
  return staffRows.filter((row) => {
    const matchDepartment = !staffFilters.department || row.department === staffFilters.department;
    const matchKeyword = !word || row.name.includes(word);
    return matchDepartment && matchKeyword;
  });
});

const filteredDepartmentRows = computed(() => {
  const word = keyword.value.trim();
  return departmentRows.filter((row) => !word || row.name.includes(word));
});

const filteredRoleRows = computed(() => {
  const word = keyword.value.trim();
  return roleRows.filter((row) => !word || row.name.includes(word));
});

const switchTab = (tab: TabKey) => {
  activeTab.value = tab;
  router.replace({ name: "SystemStaff", params: { type: tab } });
  keyword.value = "";
};

watch(
  () => route.params.type,
  (type) => {
    const requested = String(type || "staff") as TabKey;
    if (tabs.some((tab) => tab.key === requested)) {
      activeTab.value = requested;
      viewMode.value = "list";
    }
  },
  { immediate: true },
);

const openForm = async (row?: RowData) => {
  formAction.value = row ? "edit" : "add";
  editingRow.value = row || null;
  form.id = String(row?.id || "");
  form.jobNo = String(row?.jobNo || row?.code || "");
  form.code = String(row?.code || "");
  form.name = String(row?.name || "");
  form.gender = String(row?.gender || "男");
  form.age = row?.age == null ? "" : String(row.age);
  form.phone = String(row?.phone || "");
  form.email = String(row?.email || "");
  form.idCard = String(row?.idCard || "");
  form.positionName = String(row?.positionName || "");
  form.address = String(row?.address || "");
  form.departmentName = String(row?.department || row?.departmentName || "");
  form.roleName = String(row?.role || row?.roleName || "");
  form.clinicName = String(row?.clinic || row?.clinicName || "");
  form.description = String(row?.description || "");
  form.creator = String(row?.creator || "");
  form.createdAt = String(row?.createTime || row?.createdAt || currentDateTime());
  form.enabled = row?.enabled !== false;
  form.permissions.splice(0, form.permissions.length, ...normalizePermissions(row?.permissions));
  if (!row) {
    form.permissions.splice(0, form.permissions.length);
  }
  if (!row && activeTab.value === "department") {
    form.creator = await currentEmployeeName();
    form.createdAt = currentDateTime();
  }
  viewMode.value = "form";
};

const backToList = () => {
  viewMode.value = "list";
  editingRow.value = null;
};

const openDeleteConfirm = (target: string, row?: RowData) => {
  deleteTarget.value = target;
  deleteRow.value = row || null;
  showDeleteConfirm.value = true;
};


const apiForTab = () => activeTab.value === "staff" ? employeeApi : activeTab.value === "department" ? departmentApi : roleApi;

const labelForTab = () => activeTab.value === "staff" ? "员工" : activeTab.value === "department" ? "科室" : "角色";

const isEnabled = (row: RowData) => row.enabled !== false && row.enabled !== 0;

const currentEmployeeName = async () => {
  if (form.creator.trim()) return form.creator.trim();
  try {
    const response: any = await accountApi.profile();
    const profile = response?.data || {};
    return profile.name || profile.realName || profile.employeeName || profile.username || "";
  } catch {
    return "";
  }
};

const loadStaffData = async () => {
  const [staffResponse, departmentResponse, roleResponse]: any[] = await Promise.all([
    employeeApi.page({ page: 1, size: 100 }),
    departmentApi.page({ page: 1, size: 100 }),
    roleApi.page({ page: 1, size: 100 }),
  ]);
  staffRows.splice(0, staffRows.length, ...(staffResponse?.data?.records || []).map((item: any, index: number) => ({
    index: index + 1,
    id: item.id,
    jobNo: item.jobNo || item.employeeNo || item.code || "",
    name: item.name || item.employeeName || "",
    gender: item.gender || "",
    age: item.age || "",
    phone: item.phone || "",
    email: item.email || "",
    idCard: item.idCard || "",
    positionName: item.positionName || "",
    clinic: item.clinicName || "",
    department: item.departmentName || "",
    role: item.roleName || "",
    address: item.address || "",
    createTime: item.createdAt || "",
    creator: item.creator || "",
    enabled: item.enabled !== false,
  })));
  departmentRows.splice(0, departmentRows.length, ...(departmentResponse?.data?.records || []).map((item: any, index: number) => ({
    index: index + 1,
    id: item.id,
    code: item.departmentCode || item.code || "",
    name: item.name || item.departmentName || "",
    description: item.description || "",
    createTime: item.createdAt || "",
    creator: item.creator || "",
    enabled: item.enabled !== false,
  })));
  roleRows.splice(0, roleRows.length, ...(roleResponse?.data?.records || []).map((item: any, index: number) => ({
    index: index + 1,
    id: item.id,
    code: item.roleCode || item.code || "",
    name: item.name || item.roleName || "",
    description: item.description || "",
    createTime: item.createdAt || "",
    creator: item.creator || "",
    enabled: item.enabled !== false,
    permissions: item.permissions || "",
  })));
};

const setFormPermissions = (permissions: string[]) => {
  form.permissions.splice(0, form.permissions.length, ...Array.from(new Set(permissions)));
};

const allModulesChecked = () => permissionModules.every((module) => isModuleChecked(module));

const isModuleChecked = (module: PermissionModule) => {
  const childCodes = module.children.map((child) => child.code);
  return form.permissions.includes(module.code) || childCodes.every((code) => form.permissions.includes(code));
};

const isFunctionChecked = (module: PermissionModule, code: string) =>
  form.permissions.includes(module.code) || form.permissions.includes(code);

const toggleAllModules = (checked: boolean) => {
  setFormPermissions(checked ? permissionModules.map((module) => module.code) : []);
};

const toggleModule = (module: PermissionModule, checked: boolean) => {
  const next = new Set(form.permissions);
  const childCodes = module.children.map((child) => child.code);
  if (checked) {
    next.add(module.code);
    childCodes.forEach((code) => next.delete(code));
  } else {
    next.delete(module.code);
    childCodes.forEach((code) => next.delete(code));
  }
  setFormPermissions(Array.from(next));
};

const toggleFunction = (module: PermissionModule, code: string, checked: boolean) => {
  const next = new Set(form.permissions);
  const childCodes = module.children.map((child) => child.code);
  if (next.has(module.code)) {
    next.delete(module.code);
    childCodes.forEach((childCode) => next.add(childCode));
  }
  if (checked) next.add(code);
  else next.delete(code);
  if (childCodes.every((childCode) => next.has(childCode))) {
    childCodes.forEach((childCode) => next.delete(childCode));
    next.add(module.code);
  }
  setFormPermissions(Array.from(next));
};

const toggleRowEnabled = async (row: RowData) => {
  const nextEnabled = !isEnabled(row);
  row.enabled = nextEnabled;
  if (row.id) {
    await (apiForTab() as any).update(row.id, { enabled: nextEnabled });
    await loadStaffData();
  }
  ElMessage.success(`${labelForTab()}已${nextEnabled ? "启用" : "停用"}`);
};

const saveCurrent = async () => {
  const api: any = apiForTab();
  if (!form.name.trim()) {
    ElMessage.warning("请输入名称");
    return;
  }
  const creator = await currentEmployeeName();
  const payload = activeTab.value === "staff"
    ? {
        jobNo: form.jobNo.trim(),
        name: form.name.trim(),
        gender: form.gender,
        age: form.age ? Number(form.age) : null,
        phone: form.phone.trim(),
        email: form.email.trim(),
        idCard: form.idCard.trim(),
        positionName: form.positionName.trim(),
        clinicName: form.clinicName || "康宁诊所",
        departmentName: form.departmentName,
        roleName: form.roleName,
        address: form.address.trim(),
        creator,
        enabled: form.enabled,
      }
    : activeTab.value === "department"
      ? {
          departmentCode: form.code.trim(),
          name: form.name.trim(),
          description: form.description.trim(),
          creator,
          enabled: form.enabled,
        }
      : {
          roleCode: form.code.trim(),
          name: form.name.trim(),
          description: form.description.trim(),
          permissions: JSON.stringify(form.permissions),
          creator,
          enabled: form.enabled,
        };
  if (editingRow.value?.id) await api.update(editingRow.value.id, payload);
  else await api.create(payload);
  await loadStaffData();
  backToList();
  ElMessage.success("保存成功");
};

const confirmDelete = async () => {
  if (deleteRow.value?.id) await (apiForTab() as any).remove(deleteRow.value.id);
  showDeleteConfirm.value = false;
  await loadStaffData();
  ElMessage.success("删除成功");
};

onMounted(loadStaffData);
</script>

<style scoped>
.staff-page {
  min-height: 100%;
  padding: 16px 0 58px;
  overflow-x: hidden;
}

.staff-card,
.form-card {
  margin: 0 auto;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
  box-sizing: border-box;
}

.staff-card {
  width: min(1585px, calc(100% - 64px));
  min-height: 1210px;
  padding: 31px 31px 42px;
}

.staff-toolbar {
  display: flex;
  min-height: 46px;
  align-items: center;
  justify-content: space-between;
}

.staff-tabs {
  display: inline-flex;
  overflow: hidden;
  border: 1px solid #c5c7cd;
  border-radius: 4px;
}

.staff-tab {
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

.staff-tab:last-child {
  border-right: 0;
}

.staff-tab.active {
  background: #636be8;
  color: #fff;
}

.add-btn,
.save-btn,
.back-btn {
  display: inline-flex;
  height: 46px;
  align-items: center;
  justify-content: center;
  gap: 11px;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.add-btn {
  width: 139px;
  border: 1px solid #636be8;
  background: #636be8;
  color: #fff;
}

.add-btn :deep(.el-icon),
.save-btn :deep(.el-icon),
.back-btn :deep(.el-icon) {
  font-size: 23px;
}

.staff-divider {
  height: 1px;
  margin: 27px 0 31px;
  background: #eee;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 27px;
  margin-bottom: 31px;
}

.filter-row.department {
  margin-bottom: 32px;
}

.dept-filter {
  display: inline-flex;
  align-items: center;
  gap: 15px;
  color: #1e2533;
  font-size: 16px;
  white-space: nowrap;
}

.dept-filter select,
.form-field input,
.form-field select {
  height: 58px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  background: #fff;
  color: #000;
  font-size: 18px;
  box-sizing: border-box;
}

.dept-filter select {
  width: 190px;
  height: 44px;
  padding: 0 20px;
  border-width: 1px;
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

.search-filter input::placeholder,
.form-field input::placeholder {
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

.staff-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.staff-table th,
.staff-table td {
  box-sizing: border-box;
}

.staff-table th {
  height: 72px;
  padding: 0 6px;
  background: #bfc2f6;
  color: #05070c;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
}

.staff-table td {
  height: 84px;
  padding: 0 6px;
  border-bottom: 1px solid #eee;
  color: #000;
  font-size: 16px;
  text-align: center;
  white-space: nowrap;
}

.employee-table {
  width: 100%;
}

.employee-table th:nth-child(1),
.employee-table td:nth-child(1) {
  width: 75px;
}

.employee-table th:nth-child(2),
.employee-table td:nth-child(2) {
  width: 130px;
}

.employee-table th:nth-child(3),
.employee-table td:nth-child(3) {
  width: 115px;
}

.employee-table th:nth-child(4),
.employee-table td:nth-child(4),
.employee-table th:nth-child(5),
.employee-table td:nth-child(5) {
  width: 86px;
}

.employee-table th:nth-child(6),
.employee-table td:nth-child(6) {
  width: 150px;
}

.employee-table th:nth-child(7),
.employee-table td:nth-child(7),
.employee-table th:nth-child(8),
.employee-table td:nth-child(8),
.employee-table th:nth-child(9),
.employee-table td:nth-child(9) {
  width: 96px;
}

.employee-table th:nth-child(10),
.employee-table td:nth-child(10) {
  width: 205px;
}

.employee-table th:nth-child(11),
.employee-table td:nth-child(11) {
  width: 95px;
}

.employee-table th:nth-child(12),
.employee-table td:nth-child(12) {
  width: 105px;
}

.employee-table th:nth-child(13),
.employee-table td:nth-child(13) {
  width: 114px;
}

.department-table th:nth-child(1),
.department-table td:nth-child(1),
.role-table th:nth-child(1),
.role-table td:nth-child(1) {
  width: 120px;
}

.department-table th:nth-child(2),
.department-table td:nth-child(2),
.role-table th:nth-child(2),
.role-table td:nth-child(2) {
  width: 150px;
}

.department-table th:nth-child(3),
.department-table td:nth-child(3),
.department-table th:nth-child(4),
.department-table td:nth-child(4),
.role-table th:nth-child(3),
.role-table td:nth-child(3),
.role-table th:nth-child(4),
.role-table td:nth-child(4) {
  width: 190px;
}

.department-table th:nth-child(5),
.department-table td:nth-child(5),
.role-table th:nth-child(5),
.role-table td:nth-child(5) {
  width: 245px;
}

.department-table th:nth-child(6),
.department-table td:nth-child(6),
.role-table th:nth-child(6),
.role-table td:nth-child(6) {
  width: 145px;
}

.department-table th:nth-child(7),
.department-table td:nth-child(7),
.role-table th:nth-child(7),
.role-table td:nth-child(7) {
  width: 140px;
}

.department-table th:nth-child(8),
.department-table td:nth-child(8),
.role-table th:nth-child(8),
.role-table td:nth-child(8) {
  width: 140px;
}

.switch {
  display: inline-flex;
  width: 51px;
  height: 30px;
  padding: 2px;
  align-items: center;
  border: 0;
  border-radius: 999px;
  background: #35d07f;
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
  background: #b8beca;
}

.switch.off span {
  margin-left: 0;
}

.action-cell button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #6068f1;
  font-size: 16px;
  cursor: pointer;
}

.action-cell button + button {
  margin-left: 25px;
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

.pagination.full {
  margin-top: 33px;
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
  border-color: #636be8;
  background: #636be8;
  color: #fff;
}

.pager-ellipsis {
  margin: 0 8px 0 14px;
}

.form-card {
  width: min(1660px, calc(100% - 64px));
  min-height: 798px;
  padding: 31px 34px 54px;
}

.role-form-card {
  width: min(1660px, calc(100% - 64px));
  min-height: 1016px;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-header h2 {
  position: relative;
  margin: 0;
  padding-left: 15px;
  color: #101828;
  font-size: 24px;
  font-weight: 700;
  line-height: 46px;
}

.form-header h2::before {
  content: "";
  position: absolute;
  left: 0;
  top: 15px;
  width: 6px;
  height: 25px;
  background: #636be8;
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 22px;
}

.save-btn {
  width: 148px;
  border: 1px solid #636be8;
  background: #636be8;
  color: #fff;
}

.back-btn {
  width: 148px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
}

.staff-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 363px));
  column-gap: 46px;
  row-gap: 29px;
  margin-top: 35px;
}

.simple-form {
  display: grid;
  grid-template-columns: minmax(0, 363px) minmax(0, 363px) 220px;
  column-gap: 46px;
  row-gap: 29px;
  margin-top: 43px;
}

.role-form {
  grid-template-columns: minmax(0, 363px) minmax(0, 363px) 220px 1fr;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 11px;
  color: #101828;
  font-size: 16px;
}

.form-field.required span::after {
  content: "*";
  color: #ff1d1d;
}

.form-field input,
.form-field select {
  width: 100%;
  padding: 0 18px;
}

.form-field input[readonly] {
  background: #f3f3f3;
}

.age-field .input-with-unit {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 105px;
}

.age-field input,
.age-field select {
  border-radius: 4px 0 0 4px;
}

.age-field select {
  border-left: 0;
  border-radius: 0 4px 4px 0;
}

.address-select {
  grid-column: 1;
}

.address-detail {
  grid-column: 2 / span 2;
}

.status-line {
  display: flex;
  align-items: center;
  gap: 26px;
  padding-top: 38px;
  color: #101828;
  font-size: 18px;
}

.inline-status {
  padding-top: 40px;
}

.description-field {
  grid-column: 1 / span 2;
}

.permission-section {
  grid-column: 1 / -1;
  margin-top: 9px;
}

.permission-section h3 {
  margin: 0 0 27px;
  color: #858df0;
  font-size: 25px;
  font-weight: 700;
}

.permission-table {
  width: 100%;
}

.permission-head {
  display: grid;
  grid-template-columns: 170px 1fr;
  align-items: center;
  height: 76px;
  padding: 0 40px;
  background: #bfc2f6;
  color: #1e2533;
  font-size: 18px;
  font-weight: 700;
  box-sizing: border-box;
}

.permission-row {
  display: grid;
  grid-template-columns: 170px minmax(0, 1fr);
  align-items: flex-start;
  min-height: 88px;
  padding: 24px 40px;
  border-bottom: 1px solid #eee;
  color: #344054;
  font-size: 18px;
  box-sizing: border-box;
}

.permission-head label,
.permission-row label {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.module-check {
  font-weight: 600;
}

.permission-functions {
  display: flex;
  flex-wrap: wrap;
  gap: 18px 32px;
}

.permission-head input,
.permission-row input {
  width: 16px;
  height: 16px;
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

.confirm-card {
  width: 420px;
  padding: 28px 32px 26px;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.18);
  box-sizing: border-box;
}

.confirm-card h3 {
  margin: 0;
  color: #101828;
  font-size: 22px;
}

.confirm-card p {
  margin: 28px 0 30px;
  color: #344054;
  font-size: 18px;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
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
  border: 1px solid #636be8;
  background: #636be8;
  color: #fff;
}
</style>
