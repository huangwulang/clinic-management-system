import request from "./request";
import { createCrudApi, withPageDefaults } from "./crud";
import { API_PATHS } from "./paths";
import { hashPasswordFields } from "@/utils/password";

type Id = number | string;
type Params = Record<string, unknown>;
type Data = Record<string, any>;

const action = (resource: string, id: Id, name: string, data?: unknown) =>
  request.post(resource + "/" + id + "/" + name, data);

const createStockApi = (resource: string) => ({
  ...createCrudApi(resource),
  submit(id: Id) {
    return action(resource, id, "submit");
  },
  audit(id: Id, data: { approved: boolean; remark?: string }) {
    return action(resource, id, "audit", data);
  },
  approve(id: Id, data?: { remark?: string }) {
    return action(resource, id, "approve", data);
  },
  reject(id: Id, data?: { remark?: string }) {
    return action(resource, id, "reject", data);
  },
});

export const authApi = {
  login(data: { username: string; password: string }) {
    return request.post(API_PATHS.auth + "/login", hashPasswordFields(data, ["password"]));
  },
  register(data: Data) {
    return request.post(API_PATHS.auth + "/register", hashPasswordFields(data, ["password"]));
  },
  logout() {
    return request.post(API_PATHS.auth + "/logout");
  },
  me() {
    return request.get(API_PATHS.auth + "/me");
  },
  sendResetCode(data: { phone: string }) {
    return request.post(API_PATHS.auth + "/password/code", data);
  },
  resetPassword(data: { phone: string; code: string; password: string; confirmPassword: string }) {
    return request.post(API_PATHS.auth + "/password/reset", hashPasswordFields(data, ["password", "confirmPassword"]));
  },
  applyTrial(data: Data) {
    return request.post(API_PATHS.trialApplications, data);
  },
};

export const accountApi = {
  profile() {
    return request.get(API_PATHS.account + "/profile");
  },
  updateProfile(data: Data) {
    return request.put(API_PATHS.account + "/profile", data);
  },
  updatePassword(data: Data) {
    return request.put(API_PATHS.account + "/password", hashPasswordFields(data, ["oldPassword", "newPassword", "confirmPassword"]));
  },
};

export const messageApi = {
  page(params: Params = {}) {
    return request.get(API_PATHS.messages, { params: withPageDefaults(params) });
  },
  read(id: Id) {
    return request.put(API_PATHS.messages + "/" + id + "/read");
  },
  readAll() {
    return request.put(API_PATHS.messages + "/read-all");
  },
};

export const dashboardApi = {
  summary() {
    return request.get(API_PATHS.dashboard + "/summary");
  },
  trends(params: Params = {}) {
    return request.get(API_PATHS.dashboard + "/trends", { params });
  },
  rankings(params: Params = {}) {
    return request.get(API_PATHS.dashboard + "/rankings", { params });
  },
};

export const workbenchApi = {
  visits(params: Params = {}) {
    return request.get(API_PATHS.workbench + "/visits", { params });
  },
};

export const patientApi = {
  ...createCrudApi(API_PATHS.patients),
  search(params: Params = {}) {
    return request.get(API_PATHS.patients, { params: withPageDefaults(params) });
  },
  familyMembers(id: Id) {
    return request.get(API_PATHS.patients + "/" + id + "/family-members");
  },
  medicalRecords(id: Id) {
    return request.get(API_PATHS.patients + "/" + id + "/medical-records");
  },
  chargeOrders(id: Id) {
    return request.get(API_PATHS.patients + "/" + id + "/charge-orders");
  },
  setMemberLevel(id: Id, data: { levelCode: string; memberName: string; expireDate: string | null }) {
    return request.post(API_PATHS.patients + "/" + id + "/member-level", data);
  },
};
export const familyMemberApi = createCrudApi(API_PATHS.familyMembers);

export const registrationApi = {
  ...createCrudApi(API_PATHS.registrations),
  cancel(id: Id, data: { remark?: string } = {}) {
    return action(API_PATHS.registrations, id, "cancel", data);
  },
  startVisit(id: Id) {
    return action(API_PATHS.registrations, id, "start-visit");
  },
  complete(id: Id) {
    return action(API_PATHS.registrations, id, "complete");
  },
};

export const visitApi = {
  ...createCrudApi(API_PATHS.visits),
  updatePatient(id: Id, data: Data) {
    return request.put(API_PATHS.visits + "/" + id + "/patient", data);
  },
  saveMedicalRecord(id: Id, data: Data) {
    return request.put(API_PATHS.visits + "/" + id + "/medical-record", data);
  },
  savePrescription(id: Id, data: Data) {
    return request.post(API_PATHS.visits + "/" + id + "/prescriptions", data);
  },
  finish(id: Id) {
    return action(API_PATHS.visits, id, "finish");
  },
  createChargeOrder(id: Id) {
    return action(API_PATHS.visits, id, "charge-order");
  },
};
export const consultationApi = visitApi;

export const prescriptionApi = {
  update(id: Id, data: Data) {
    return request.put(API_PATHS.prescriptions + "/" + id, data);
  },
  remove(id: Id) {
    return request.delete(API_PATHS.prescriptions + "/" + id);
  },
};

export const retailOrderApi = {
  ...createCrudApi(API_PATHS.retailOrders),
  rankings(params: Params = {}) {
    return request.get(API_PATHS.retailOrders + "/rankings", { params });
  },
  pay(id: Id, data: Data) {
    return action(API_PATHS.retailOrders, id, "pay", data);
  },
  refund(id: Id, data: Data) {
    return action(API_PATHS.retailOrders, id, "refund", data);
  },
};

export const chargeOrderApi = {
  ...createCrudApi(API_PATHS.chargeOrders),
  pay(id: Id, data: Data) {
    return action(API_PATHS.chargeOrders, id, "pay", data);
  },
  refund(id: Id, data: Data) {
    return request.post(API_PATHS.chargeOrders + "/" + id + "/refunds", data);
  },
  refunds(id: Id) {
    return request.get(API_PATHS.chargeOrders + "/" + id + "/refunds");
  },
};

export const drugApi = {
  ...createCrudApi(API_PATHS.drugs),
  search(params: Params = {}) {
    return request.get(API_PATHS.drugs + "/search", {
      params: withPageDefaults(params as any),
    });
  },
  nextCode() {
    return request.get(API_PATHS.drugs + "/next-code");
  },
  saleable(params: Params = {}) {
    return request.get(API_PATHS.drugs + "/saleable", { params: withPageDefaults(params) });
  },
  enable(id: Id) {
    return action(API_PATHS.drugs, id, "enable");
  },
  disable(id: Id) {
    return action(API_PATHS.drugs, id, "disable");
  },
  updateStatus(id: Id, status: string | number) {
    return request.put(API_PATHS.drugs + "/" + id + "/status", { status });
  },
  copy(id: Id) {
    return action(API_PATHS.drugs, id, "copy");
  },
  importFile(file: File) {
    const form = new FormData();
    form.append("file", file);
    return request.post(API_PATHS.drugs + "/import", form);
  },
  export(params: Params = {}) {
    return request.get(API_PATHS.drugs + "/export", { params, responseType: "blob" });
  },
};

export const stockInOrderApi = createStockApi(API_PATHS.stockInOrders);
export const stockOutOrderApi = createStockApi(API_PATHS.stockOutOrders);
export const stockOrderApi = stockInOrderApi;

export const inventoryApi = {
  ...createCrudApi(API_PATHS.inventories),
  search(params: Params = {}) {
    return request.get(API_PATHS.inventories + "/search", {
      params: withPageDefaults(params),
    });
  },
  summary() {
    return request.get(API_PATHS.inventories + "/summary");
  },
  warnings() {
    return request.get(API_PATHS.inventories + "/warnings");
  },
  logs(id: Id) {
    return request.get(API_PATHS.inventories + "/" + id + "/logs");
  },
  increase(id: Id, data: Data) {
    return action(API_PATHS.inventories, id, "increase", data);
  },
  decrease(id: Id, data: Data) {
    return action(API_PATHS.inventories, id, "decrease", data);
  },
};

export const stockCheckOrderApi = {
  ...createCrudApi(API_PATHS.stockCheckOrders),
  confirm(id: Id) {
    return action(API_PATHS.stockCheckOrders, id, "confirm");
  },
};
export const inventoryCheckApi = stockCheckOrderApi;

export const priceAdjustOrderApi = {
  ...createCrudApi(API_PATHS.priceAdjustOrders),
  adjustableDrugs(params?: Params) {
    return request.get(API_PATHS.priceAdjustOrders + "/adjustable-drugs", { params });
  },
  records(params?: Params) {
    return request.get(API_PATHS.priceAdjustOrders + "/records", { params });
  },
  form(drugId: Id) {
    return request.get(API_PATHS.priceAdjustOrders + "/drugs/" + drugId + "/form");
  },
  history(drugId: Id) {
    return request.get(API_PATHS.priceAdjustOrders + "/drugs/" + drugId + "/history");
  },
  complete(data: Data) {
    return request.post(API_PATHS.priceAdjustOrders + "/complete", data);
  },
};
export const priceAdjustmentApi = priceAdjustOrderApi;

export const memberApi = {
  ...createCrudApi(API_PATHS.members),
  recharge(id: Id, data: Data) {
    return action(API_PATHS.members, id, "recharge", data);
  },
  adjustBalance(id: Id, data: Data) {
    return action(API_PATHS.members, id, "balance-adjust", data);
  },
  adjustPoints(id: Id, data: Data) {
    return action(API_PATHS.members, id, "points-adjust", data);
  },
  balanceLogs(id: Id) {
    return request.get(API_PATHS.members + "/" + id + "/balance-logs");
  },
  pointsLogs(id: Id) {
    return request.get(API_PATHS.members + "/" + id + "/points-logs");
  },
  changeLevel(id: Id, data: Data) {
    return action(API_PATHS.members, id, "level", data);
  },
  levelLogs(id: Id) {
    return request.get(API_PATHS.members + "/" + id + "/level-logs");
  },
};

const simpleApi = (resource: string) => createCrudApi(resource);
export const memberLevelApi = {
  list() {
    return request.get(API_PATHS.memberLevels);
  },
  create(data: Data) {
    return request.post(API_PATHS.memberLevels, data);
  },
  update(id: Id, data: Data) {
    return request.put(API_PATHS.memberLevels + "/" + id, data);
  },
  remove(id: Id) {
    return request.delete(API_PATHS.memberLevels + "/" + id);
  },
};
export const employeeApi = simpleApi(API_PATHS.employees);
export const staffApi = employeeApi;
export const departmentApi = simpleApi(API_PATHS.departments);
export const roleApi = {
  ...simpleApi(API_PATHS.roles),
  updatePermissions(id: Id, permissions: string[]) {
    return request.put(API_PATHS.roles + "/" + id + "/permissions", { permissions });
  },
};
export const medicalTemplateApi = simpleApi(API_PATHS.medicalTemplates);
export const prescriptionTemplateApi = {
  list(keyword?: string) {
    return request.get(API_PATHS.prescriptionTemplates, { params: { keyword } });
  },
  get(id: Id) {
    return request.get(API_PATHS.prescriptionTemplates + "/" + id);
  },
  create(data: Data) {
    return request.post(API_PATHS.prescriptionTemplates, data);
  },
  update(id: Id, data: Data) {
    return request.put(API_PATHS.prescriptionTemplates + "/" + id, data);
  },
  remove(id: Id) {
    return request.delete(API_PATHS.prescriptionTemplates + "/" + id);
  },
};
export const checkProjectApi = simpleApi(API_PATHS.checkProjects);
export const supplierApi = simpleApi(API_PATHS.suppliers);
export const feeItemApi = simpleApi(API_PATHS.feeItems);
export const userAccountApi = {
  ...simpleApi(API_PATHS.userAccounts),
  resetPassword(id: Id, data: Data) {
    return request.put(API_PATHS.userAccounts + "/" + id + "/password", hashPasswordFields(data, ["oldPassword", "newPassword", "confirmPassword", "password"]));
  },
  enable(id: Id) {
    return action(API_PATHS.userAccounts, id, "enable");
  },
  disable(id: Id) {
    return action(API_PATHS.userAccounts, id, "disable");
  },
};

export const dictionaryApi = (type: string) => {
  const resource = "/dictionaries/" + encodeURIComponent(type) + "/items";
  return {
    list() {
      return request.get(resource);
    },
    create(data: Data) {
      return request.post(resource, data);
    },
    update(id: Id, data: Data) {
      return request.put(resource + "/" + id, data);
    },
    remove(id: Id) {
      return request.delete(resource + "/" + id);
    },
  };
};
export const dictionaryItemApi = simpleApi("/dictionary-items");

export const clinicInfoApi = simpleApi(API_PATHS.clinic);
export const settingsApi = {
  get() {
    return request.get(API_PATHS.settings);
  },
  update(data: Record<string, string>) {
    return request.put(API_PATHS.settings, data);
  },
};
export const operationLogApi = simpleApi(API_PATHS.operationLogs);

export const statisticsApi = {
  dashboard() {
    return request.get(API_PATHS.statistics + "/dashboard");
  },
  charge(type: string, params: Params = {}) {
    return request.get(API_PATHS.statistics + "/charge/" + type, { params });
  },
  chargeDetail(id: Id) {
    return request.get(API_PATHS.statistics + "/charge/detail/" + id);
  },
  patients(type: string, params: Params = {}) {
    return request.get(API_PATHS.statistics + "/patients/" + type, { params });
  },
  drugs(type: string, params: Params = {}) {
    return request.get(API_PATHS.statistics + "/drugs/" + type, { params });
  },
  checkProjects(params: Params = {}) {
    return request.get(API_PATHS.statistics + "/check-projects", { params });
  },
  legacyCharge(params: Params = {}) {
    return request.get(API_PATHS.statistics + "/charge", { params });
  },
  legacyPatient(params: Params = {}) {
    return request.get(API_PATHS.statistics + "/patient", { params });
  },
  legacyDrug(params: Params = {}) {
    return request.get(API_PATHS.statistics + "/drug", { params });
  },
};
