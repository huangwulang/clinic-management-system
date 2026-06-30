import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { ElMessage } from "element-plus";
import { authApi } from "@/api";
import {
  clearCurrentUser,
  hasPermissionForPath,
  normalizeUserPermissions,
  NO_PERMISSION_MESSAGE,
  storeCurrentUser,
} from "@/utils/permissions";

const redirect = (path: string, target: string): RouteRecordRaw => ({
  path,
  redirect: target,
  meta: { hidden: true },
});

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    component: () => import("@/views/login/loginView.vue"),
    meta: { title: "诊所管理系统", hidden: true, notNeedAuth: true },
  },
  {
    path: "/register",
    component: () => import("@/views/register/registerView.vue"),
    meta: { title: "注册账号", hidden: true, notNeedAuth: true },
  },
  {
    path: "/forgot-password",
    component: () => import("@/views/login/authSupportView.vue"),
    meta: { title: "找回密码", hidden: true, notNeedAuth: true },
  },
  {
    path: "/trial/apply",
    component: () => import("@/views/login/authSupportView.vue"),
    meta: { title: "申请试用", hidden: true, notNeedAuth: true },
  },
  {
    path: "/404",
    component: () => import("@/views/404View.vue"),
    meta: { title: "页面不存在", hidden: true, notNeedAuth: true },
  },
  {
    path: "/",
    component: () => import("@/views/dashboard/indexView.vue"),
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        component: () => import("@/views/dashboard/dashboardView.vue"),
        name: "Dashboard",
        meta: { title: "经营概况", affix: true },
      },
      {
        path: "workbench",
        component: () => import("@/views/workbench/workbenchView.vue"),
        name: "Workbench",
        meta: { title: "工作台", affix: true },
      },
      {
        path: "account/profile",
        component: () => import("@/views/account/profileView.vue"),
        name: "AccountProfile",
        meta: { title: "账号资料", affix: true },
      },
      {
        path: "account/password",
        component: () => import("@/views/account/passwordView.vue"),
        name: "AccountPassword",
        meta: { title: "修改密码", affix: true },
      },
      {
        path: "account/messages",
        component: () => import("@/views/account/notificationsView.vue"),
        name: "AccountMessages",
        meta: { title: "消息通知", affix: true },
      },
      {
        path: "consultation/new",
        component: () => import("@/views/consultation/consultationView.vue"),
        name: "Consultation",
        meta: { title: "新开接诊", affix: true },
      },
      {
        path: "registration/create",
        component: () => import("@/views/registration/addRegistrationView.vue"),
        name: "RegistrationAdd",
        meta: { title: "新增挂号", affix: true },
      },
      {
        path: "registration/list",
        component: () => import("@/views/registration/registrationRecordView.vue"),
        name: "RegistrationRecord",
        meta: { title: "挂号记录", affix: true },
      },
      {
        path: "registration/update",
        component: () => import("@/views/registration/updateRegistrationView.vue"),
        name: "RegistrationUpdate",
        meta: { title: "编辑挂号", affix: true },
      },
      {
        path: "registration/detail",
        component: () => import("@/views/registration/registrationDetailView.vue"),
        name: "RegistrationDetail",
        meta: { title: "查看挂号", affix: true },
      },
      {
        path: "retail",
        component: () => import("@/views/retail/drugRetailView.vue"),
        name: "Retail",
        meta: { title: "药品零售", affix: true },
      },
      {
        path: "charge/orders",
        component: () => import("@/views/charge/chargeView.vue"),
        name: "Charge",
        meta: { title: "收费管理", affix: true },
      },
      {
        path: "charge/payment",
        component: () => import("@/views/charge/paymentView.vue"),
        name: "ChargePayment",
        meta: { title: "收费", affix: true },
      },
      {
        path: "charge/refund",
        component: () => import("@/views/charge/refundView.vue"),
        name: "ChargeRefund",
        meta: { title: "退费", affix: true },
      },
      {
        path: "charge/refund-detail",
        component: () => import("@/views/charge/refundDetailView.vue"),
        name: "ChargeRefundDetail",
        meta: { title: "退费详情", affix: true },
      },
      {
        path: "patients",
        component: () => import("@/views/patient/patientView.vue"),
        name: "PatientList",
        meta: { title: "患者管理", affix: true },
      },
      {
        path: "patients/create",
        component: () => import("@/views/patient/addPatientView.vue"),
        name: "PatientAdd",
        meta: { title: "新增患者", affix: true },
      },
      {
        path: "patients/edit",
        component: () => import("@/views/patient/addPatientView.vue"),
        name: "PatientEdit",
        meta: { title: "编辑患者", affix: true },
      },
      {
        path: "drugs",
        component: () => import("@/views/drug/drugManagementView.vue"),
        name: "Drug",
        meta: { title: "药品信息维护", affix: true },
      },
      {
        path: "drugs/create",
        component: () => import("@/views/drug/addDrugView.vue"),
        name: "DrugAdd",
        meta: { title: "新增药品", affix: true },
      },
      {
        path: "drugs/stock-in",
        component: () => import("@/views/drug/drugManagementView.vue"),
        name: "DrugStockIn",
        meta: { title: "入库管理", affix: true },
      },
      {
        path: "drugs/stock-in/create",
        component: () => import("@/views/drug/stockInFormView.vue"),
        name: "DrugStockInForm",
        meta: { title: "新增入库信息", affix: true },
      },
      {
        path: "drugs/stock-in/detail",
        component: () => import("@/views/drug/stockInDetailView.vue"),
        name: "DrugStockInDetail",
        meta: { title: "入库详情", affix: true },
      },
      {
        path: "drugs/stock-out",
        component: () => import("@/views/drug/drugManagementView.vue"),
        name: "DrugStockOut",
        meta: { title: "出库管理", affix: true },
      },
      {
        path: "drugs/stock-out/create",
        component: () => import("@/views/drug/stockOutFormView.vue"),
        name: "DrugStockOutForm",
        meta: { title: "新增出库信息", affix: true },
      },
      {
        path: "drugs/stock-out/detail",
        component: () => import("@/views/drug/stockOutDetailView.vue"),
        name: "DrugStockOutDetail",
        meta: { title: "出库详情", affix: true },
      },
      {
        path: "drugs/inventory",
        component: () => import("@/views/drug/drugManagementView.vue"),
        name: "DrugInventory",
        meta: { title: "库存管理", affix: true },
      },
      {
        path: "drugs/inventory/detail",
        component: () => import("@/views/drug/inventoryDetailView.vue"),
        name: "DrugInventoryDetail",
        meta: { title: "库存明细", affix: true },
      },
      {
        path: "drugs/stock-check",
        component: () => import("@/views/drug/drugManagementView.vue"),
        name: "DrugInventoryCheck",
        meta: { title: "库存盘点", affix: true },
      },
      {
        path: "drugs/stock-check/create",
        component: () => import("@/views/drug/inventoryCheckFormView.vue"),
        name: "DrugInventoryCheckForm",
        meta: { title: "新增盘点", affix: true },
      },
      {
        path: "drugs/stock-check/detail",
        component: () => import("@/views/drug/inventoryCheckDetailView.vue"),
        name: "DrugInventoryCheckDetail",
        meta: { title: "盘点详情", affix: true },
      },
      {
        path: "drugs/price-adjust",
        component: () => import("@/views/drug/drugManagementView.vue"),
        name: "DrugPriceAdjust",
        meta: { title: "药品调价", affix: true },
      },
      {
        path: "drugs/price-adjust/create",
        component: () => import("@/views/drug/priceAdjustFormView.vue"),
        name: "DrugPriceAdjustForm",
        meta: { title: "新增调价", affix: true },
      },
      {
        path: "drugs/price-adjust/detail",
        component: () => import("@/views/drug/priceAdjustDetailView.vue"),
        name: "DrugPriceAdjustDetail",
        meta: { title: "调价详情", affix: true },
      },
      {
        path: "members/:type?",
        component: () => import("@/views/member/memberView.vue"),
        name: "Members",
        meta: { title: "会员管理", affix: true },
      },
      {
        path: "statistics",
        redirect: "/statistics/charge/",
        meta: { title: "统计报表", affix: true },
      },
      {
        path: "statistics/charge/detail",
        component: () => import("@/views/statistics/chargeOrderDetailView.vue"),
        name: "StatisticsChargeDetail",
        meta: { title: "订单详情", affix: true },
      },
      {
        path: "statistics/charge/:type?",
        component: () => import("@/views/statistics/statisticsManagementView.vue"),
        name: "StatisticsCharge",
        meta: { title: "收费统计", affix: true },
      },
      {
        path: "statistics/patients/:type?",
        component: () => import("@/views/statistics/statisticsManagementView.vue"),
        name: "StatisticsPatient",
        meta: { title: "患者统计", affix: true },
      },
      {
        path: "statistics/check-projects",
        component: () => import("@/views/statistics/statisticsManagementView.vue"),
        name: "StatisticsCheckProject",
        meta: { title: "检查项目统计", affix: true },
      },
      {
        path: "statistics/drugs/:type?",
        component: () => import("@/views/statistics/statisticsManagementView.vue"),
        name: "StatisticsDrug",
        meta: { title: "药品统计", affix: true },
      },
      {
        path: "system/clinic",
        component: () => import("@/views/system/systemManagementView.vue"),
        name: "System",
        meta: { title: "诊所信息维护", affix: true },
      },
      {
        path: "system/dictionaries/:type?",
        component: () => import("@/views/system/dictionaryManagementView.vue"),
        name: "SystemDictionary",
        meta: { title: "字典表维护", affix: true },
      },
      {
        path: "system/staff/:type?",
        component: () => import("@/views/system/staffManagementView.vue"),
        name: "SystemStaff",
        meta: { title: "员工、科室与角色", affix: true },
      },
      {
        path: "system/check-projects",
        component: () => import("@/views/system/systemManagementView.vue"),
        name: "SystemCheckProjectSetting",
        meta: { title: "检查项目设置", affix: true },
      },
      {
        path: "system/check-projects/form",
        component: () => import("@/views/system/checkProjectFormView.vue"),
        name: "SystemCheckProjectForm",
        meta: { title: "检查项目表单", affix: true },
      },
      {
        path: "system/suppliers",
        component: () => import("@/views/system/systemManagementView.vue"),
        name: "SystemSupplier",
        meta: { title: "供应商管理", affix: true },
      },
      {
        path: "system/suppliers/form",
        component: () => import("@/views/system/supplierFormView.vue"),
        name: "SystemSupplierForm",
        meta: { title: "供应商表单", affix: true },
      },
      {
        path: "system/templates/:type?",
        component: () => import("@/views/system/templateManagementView.vue"),
        name: "SystemTemplate",
        meta: { title: "模板维护", affix: true },
      },
      {
        path: "system/templates/medical/form",
        component: () => import("@/views/system/medicalTemplateFormView.vue"),
        name: "SystemMedicalTemplateForm",
        meta: { title: "病历模板表单", affix: true },
      },
      {
        path: "system/templates/prescription/form",
        component: () => import("@/views/system/prescriptionTemplateFormView.vue"),
        name: "SystemPrescriptionTemplateForm",
        meta: { title: "处方模板表单", affix: true },
      },
      {
        path: "system/fees/:type?",
        component: () => import("@/views/system/feeSettingView.vue"),
        name: "SystemFeeSetting",
        meta: { title: "费用设置", affix: true },
      },
      {
        path: "system/fees/form",
        component: () => import("@/views/system/feeFormView.vue"),
        name: "SystemFeeForm",
        meta: { title: "费用项目表单", affix: true },
      },
      {
        path: "system/basic",
        component: () => import("@/views/system/systemManagementView.vue"),
        name: "SystemBasicSetting",
        meta: { title: "基础设置", affix: true },
      },
      {
        path: "system/logs",
        component: () => import("@/views/system/operationLogView.vue"),
        name: "SystemLogs",
        meta: { title: "操作日志", affix: true },
      },
      redirect("account/notifications", "/account/messages"),
      redirect("consultation", "/consultation/new"),
      redirect("registration/add", "/registration/create"),
      redirect("registration/record", "/registration/list"),
      redirect("charge", "/charge/orders"),
      redirect("patient", "/patients"),
      redirect("patient/add", "/patients/create"),
      redirect("patient/edit", "/patients/edit"),
      redirect("drug", "/drugs"),
      redirect("drug/add", "/drugs/create"),
      redirect("drug/stock-in", "/drugs/stock-in"),
      redirect("drug/stock-in/add", "/drugs/stock-in/create"),
      redirect("drug/stock-in/detail", "/drugs/stock-in/detail"),
      redirect("drug/stock-out", "/drugs/stock-out"),
      redirect("drug/stock-out/add", "/drugs/stock-out/create"),
      redirect("drug/stock-out/detail", "/drugs/stock-out/detail"),
      redirect("drug/inventory", "/drugs/inventory"),
      redirect("drug/inventory/detail", "/drugs/inventory/detail"),
      redirect("drug/inventory-check", "/drugs/stock-check"),
      redirect("drug/inventory-check/add", "/drugs/stock-check/create"),
      redirect("drug/inventory-check/detail", "/drugs/stock-check/detail"),
      redirect("drug/price-adjust", "/drugs/price-adjust"),
      redirect("drug/price-adjust/add", "/drugs/price-adjust/create"),
      redirect("drug/price-adjust/detail", "/drugs/price-adjust/detail"),
      redirect("member", "/members"),
      redirect("statistics/patient", "/statistics/patients/log"),
      redirect("statistics/check-project", "/statistics/check-projects"),
      redirect("statistics/drug", "/statistics/drugs/stockIn"),
      redirect("system", "/system/clinic"),
      redirect("system/dictionary", "/system/dictionaries/medical"),
      redirect("system/check-project-setting", "/system/check-projects"),
      redirect("system/check-project-setting/form", "/system/check-projects/form"),
      redirect("system/supplier", "/system/suppliers"),
      redirect("system/supplier/form", "/system/suppliers/form"),
      redirect("system/template", "/system/templates/medical"),
      redirect("system/template/medical-form", "/system/templates/medical/form"),
      redirect("system/template/prescription-form", "/system/templates/prescription/form"),
      redirect("system/fee-setting", "/system/fees/addon"),
      redirect("system/fee-setting/form", "/system/fees/form"),
      redirect("system/basic-setting", "/system/basic"),
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/404",
    meta: { hidden: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

router.beforeEach(async (to) => {
  const isPublicRoute = to.matched.some((record) => record.meta.notNeedAuth);
  if (isPublicRoute) {
    return true;
  }

  const token = localStorage.getItem("token") || sessionStorage.getItem("token");
  if (!token) {
    return {
      path: "/login",
      query: { redirect: to.fullPath },
    };
  }

  try {
    const response: any = await authApi.me();
    const currentUser = response?.data;
    storeCurrentUser(currentUser);
    if (!hasPermissionForPath(to.path, normalizeUserPermissions(currentUser))) {
      ElMessage.warning(NO_PERMISSION_MESSAGE);
      return false;
    }
    return true;
  } catch {
    localStorage.removeItem("token");
    sessionStorage.removeItem("token");
    clearCurrentUser();
    return {
      path: "/login",
      query: { redirect: to.fullPath },
    };
  }
});

export default router;
