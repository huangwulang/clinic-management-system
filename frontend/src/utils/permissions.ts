export const ADMIN_PERMISSION = "*";
export const NO_PERMISSION_MESSAGE = "没有权限，如想使用，请联系系统管理员！";

type PathRule = string | { path: string; exact?: boolean };

export type PermissionChild = {
  code: string;
  title: string;
  paths: PathRule[];
  legacyCodes?: string[];
};

export type PermissionModule = {
  code: string;
  title: string;
  path: string;
  legacyCodes?: string[];
  children: PermissionChild[];
};

export const permissionModules: PermissionModule[] = [
  {
    code: "dashboard",
    title: "首页",
    path: "/dashboard",
    children: [{ code: "dashboard.overview", title: "首页", paths: [{ path: "/dashboard", exact: true }] }],
  },
  {
    code: "workbench",
    title: "工作台",
    path: "/workbench",
    children: [{ code: "workbench.index", title: "工作台", paths: ["/workbench"] }],
  },
  {
    code: "consultation",
    title: "就诊模块",
    path: "/consultation/new",
    children: [{ code: "consultation.new", title: "新开接诊", paths: ["/consultation", "/consultation/new"] }],
  },
  {
    code: "registration",
    title: "挂号模块",
    path: "/registration/create",
    children: [
      { code: "registration.create", title: "新增挂号", paths: ["/registration/create", "/registration/add"] },
      {
        code: "registration.list",
        title: "挂号记录",
        paths: ["/registration/list", "/registration/record", "/registration/update", "/registration/detail"],
      },
    ],
  },
  {
    code: "retail",
    title: "药品零售模块",
    path: "/retail",
    children: [{ code: "retail.index", title: "药品零售", paths: ["/retail"] }],
  },
  {
    code: "charge",
    title: "收费管理模块",
    path: "/charge/orders",
    children: [
      {
        code: "charge.orders",
        title: "收费管理",
        paths: ["/charge", "/charge/orders", "/charge/payment", "/charge/refund", "/charge/refund-detail"],
      },
    ],
  },
  {
    code: "patients",
    title: "患者管理模块",
    path: "/patients",
    legacyCodes: ["patient"],
    children: [{ code: "patients.list", title: "患者管理", paths: ["/patients", "/patient"] }],
  },
  {
    code: "drugs",
    title: "药品管理模块",
    path: "/drugs",
    legacyCodes: ["drug", "stock", "inventory"],
    children: [
      {
        code: "drugs.info",
        title: "药品信息维护",
        paths: [{ path: "/drugs", exact: true }, "/drugs/create", { path: "/drug", exact: true }, "/drug/add"],
      },
      { code: "drugs.stockIn", title: "入库管理", paths: ["/drugs/stock-in", "/drug/stock-in"] },
      { code: "drugs.stockOut", title: "出库管理", paths: ["/drugs/stock-out", "/drug/stock-out"] },
      { code: "drugs.inventory", title: "库存管理", paths: ["/drugs/inventory", "/drug/inventory"] },
      { code: "drugs.stockCheck", title: "库存盘点", paths: ["/drugs/stock-check", "/drug/inventory-check"] },
      { code: "drugs.priceAdjust", title: "药品调价", paths: ["/drugs/price-adjust", "/drug/price-adjust"] },
    ],
  },
  {
    code: "members",
    title: "会员管理模块",
    path: "/members",
    legacyCodes: ["member"],
    children: [{ code: "members.list", title: "会员管理", paths: ["/members", "/member"] }],
  },
  {
    code: "statistics",
    title: "统计分析模块",
    path: "/statistics",
    children: [
      { code: "statistics.charge", title: "收费统计", paths: ["/statistics/charge"] },
      { code: "statistics.patients", title: "患者统计", paths: ["/statistics/patients", "/statistics/patient"] },
      { code: "statistics.checkProjects", title: "检查项目统计", paths: ["/statistics/check-projects", "/statistics/check-project"] },
      { code: "statistics.drugs", title: "药品统计", paths: ["/statistics/drugs", "/statistics/drug"] },
    ],
  },
  {
    code: "system",
    title: "系统管理模块",
    path: "/system/clinic",
    children: [
      { code: "system.clinic", title: "诊所信息维护", paths: ["/system/clinic"] },
      { code: "system.dictionaries", title: "字典表维护", paths: ["/system/dictionaries", "/system/dictionary"] },
      { code: "system.staff", title: "员工管理", paths: ["/system/staff"] },
      { code: "system.checkProjects", title: "检查项目设置", paths: ["/system/check-projects", "/system/check-project-setting"] },
      { code: "system.suppliers", title: "供应商管理", paths: ["/system/suppliers", "/system/supplier"] },
      { code: "system.templates", title: "模板维护", paths: ["/system/templates", "/system/template"] },
      { code: "system.fees", title: "费用设置", paths: ["/system/fees", "/system/fee-setting"] },
      { code: "system.basic", title: "基础设置", paths: ["/system/basic", "/system/basic-setting"] },
    ],
  },
];

const alwaysAllowedPrefixes = ["/login", "/register", "/forgot-password", "/trial", "/404", "/account"];

const legacyPermissionMap = new Map<string, string>();
for (const module of permissionModules) {
  for (const legacyCode of module.legacyCodes || []) {
    legacyPermissionMap.set(legacyCode, module.code);
  }
  for (const child of module.children) {
    for (const legacyCode of child.legacyCodes || []) {
      legacyPermissionMap.set(legacyCode, child.code);
    }
  }
}

const normalizePath = (path: string) => {
  if (!path) return "/";
  const [withoutQuery] = path.split(/[?#]/);
  if (withoutQuery.length > 1 && withoutQuery.endsWith("/")) {
    return withoutQuery.slice(0, -1);
  }
  return withoutQuery;
};

const matchesRule = (path: string, rule: PathRule) => {
  const normalizedPath = normalizePath(path);
  const rulePath = typeof rule === "string" ? normalizePath(rule) : normalizePath(rule.path);
  const exact = typeof rule === "object" && rule.exact === true;
  return exact ? normalizedPath === rulePath : normalizedPath === rulePath || normalizedPath.startsWith(rulePath + "/");
};

const canonicalizePermission = (code: string) => legacyPermissionMap.get(code) || code;

export const normalizePermissions = (value: unknown): string[] => {
  let permissions: unknown = value;
  if (typeof value === "string") {
    try {
      permissions = JSON.parse(value);
    } catch {
      permissions = value.split(",");
    }
  }
  if (!Array.isArray(permissions)) return [];
  return Array.from(
    new Set(
      permissions
        .map((permission) => String(permission || "").trim())
        .filter(Boolean)
        .map(canonicalizePermission),
    ),
  );
};

export const isAdminUser = (user: any) => {
  const values = [user?.roleName, user?.roleCode, user?.name, user?.username]
    .map((value) => String(value || "").trim().toUpperCase())
    .filter(Boolean);
  return values.includes("ADMIN") || values.includes("系统管理员") || values.includes("超级管理员");
};

const isDefaultUserRole = (user: any) => String(user?.roleName || user?.roleCode || "").trim().toUpperCase() === "USER";

export const normalizeUserPermissions = (user: any): string[] => {
  if (isAdminUser(user)) return [ADMIN_PERMISSION];
  const permissions = normalizePermissions(user?.permissions);
  if (permissions.length === 0 && isDefaultUserRole(user)) return ["dashboard"];
  return permissions;
};

export const getStoredCurrentUser = () => {
  const raw = localStorage.getItem("currentUser") || sessionStorage.getItem("currentUser");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
};

export const storeCurrentUser = (user: any) => {
  if (!user) return;
  localStorage.setItem("currentUser", JSON.stringify(user));
};

export const clearCurrentUser = () => {
  localStorage.removeItem("currentUser");
  sessionStorage.removeItem("currentUser");
};

export const getCurrentPermissions = () => normalizeUserPermissions(getStoredCurrentUser());

export const hasPermissionCode = (code: string, permissions = getCurrentPermissions()) => {
  const normalized = normalizePermissions(permissions);
  if (normalized.includes(ADMIN_PERMISSION)) return true;
  const module = permissionModules.find((item) => item.code === code || item.children.some((child) => child.code === code));
  if (!module) return normalized.includes(code);
  return normalized.includes(module.code) || normalized.includes(code);
};

export const findPermissionByPath = (path: string) => {
  const normalizedPath = normalizePath(path);
  for (const prefix of alwaysAllowedPrefixes) {
    if (normalizedPath === prefix || normalizedPath.startsWith(prefix + "/")) {
      return null;
    }
  }
  for (const module of permissionModules) {
    for (const child of module.children) {
      if (child.paths.some((rule) => matchesRule(normalizedPath, rule))) {
        return { module, child };
      }
    }
  }
  return undefined;
};

export const hasPermissionForPath = (path: string, permissions = getCurrentPermissions()) => {
  const matched = findPermissionByPath(path);
  if (matched === null || matched === undefined) return true;
  const normalized = normalizePermissions(permissions);
  if (normalized.includes(ADMIN_PERMISSION)) return true;
  return normalized.includes(matched.module.code) || normalized.includes(matched.child.code);
};
