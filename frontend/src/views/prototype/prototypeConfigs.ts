export type PrototypeTab = {
  label: string;
  active?: boolean;
};

export type PrototypeAction = {
  label: string;
  type?: "primary" | "default";
};

export type PrototypeFilter =
  | { label: string; type: "select"; value?: string; options?: string[] }
  | { label: string; type: "input"; placeholder?: string }
  | { label: string; type: "daterange"; value?: string[] }
  | { label: string; type: "checkbox"; checked?: boolean };

export type PrototypeColumn = {
  key: string;
  label: string;
  type?: "text" | "status" | "actions" | "switch";
};

export type PrototypeRow = Record<string, string | number | boolean | string[]>;

export type PrototypeSummaryCard = {
  label: string;
  value: string;
  accent?: boolean;
};

export type PrototypeSection =
  | { type: "switch"; title: string; description: string; value: boolean }
  | {
      type: "input";
      title: string;
      label: string;
      value: string;
      suffix?: string;
      helper?: string;
      actionText?: string;
    }
  | { type: "radio"; title: string; options: string[]; value: string }
  | { type: "checkbox-group"; title: string; description?: string; checked: string[]; options: string[] };

export type PrototypePageConfig = {
  title: string;
  layout?: "list" | "split" | "settings";
  tabs?: PrototypeTab[];
  actions?: PrototypeAction[];
  filters?: PrototypeFilter[];
  columns?: PrototypeColumn[];
  rows?: PrototypeRow[];
  summaryCards?: PrototypeSummaryCard[];
  footerNote?: string;
  leftNav?: string[];
  leftNavActive?: string;
  sections?: PrototypeSection[];
};

const createRows = (count: number, factory: (index: number) => PrototypeRow): PrototypeRow[] =>
  Array.from({ length: count }, (_, index) => factory(index + 1));

const departments = ["全科", "内科", "儿科"];
const doctors = ["王冕", "林鹤", "李忠云"];
const visitTypes = ["初诊", "复诊"];

const registrationRows = createRows(10, (i) => ({
  index: i,
  orderNo: `2019120900${31 - i}`,
  name: `姓名${i}`,
  gender: i % 2 ? "男" : "女",
  age: 11 + i,
  phone: `1775413877${i}`,
  dept: departments[(i - 1) % departments.length],
  doctor: doctors[(i - 1) % doctors.length],
  time: `2019-11-12 12:08:1${i - 1}`,
  amount: i % 3 === 1 ? "20.00" : "30.00",
  paid: i % 3 === 1 ? "20.00" : "30.00",
  status: "未就诊",
  actions: ["编辑", "就诊", "退号"],
}));

const chargeManageRows = createRows(10, (i) => ({
  index: i,
  orderNo: `2019120900${31 - i}`,
  orderType: i % 2 ? "处方开立" : "药品零售",
  name: i % 2 ? `姓名${i * 2 - 1}` : "",
  gender: i % 2 ? (i % 4 ? "男" : "女") : "",
  age: i % 2 ? 11 + i : "",
  phone: i % 2 ? `1775413877${i - 1}` : "",
  dept: i % 2 ? departments[(i - 1) % departments.length] : "",
  doctor: i % 2 ? doctors[(i - 1) % doctors.length] : "",
  time: `2019-11-12 12:08:1${i - 1}`,
  amount: i === 1 ? "10" : i === 10 ? "20" : "30",
  status: "待收费",
  actions: ["收费", "编辑", "删除"],
}));

const patientRows = createRows(10, (i) => ({
  index: i,
  code: 100130 - (i - 1),
  name: `姓名${i * 2 - 1}`,
  gender: "男",
  age: 11 + ((i - 1) % 15),
  phone: `1775413876${9 + i - 1}`,
  level: i % 5 === 1 ? "非会员" : `VIP${((i - 1) % 7) + 1}`,
  time: `2019-11-12 12:08:1${i - 1}`,
  operator: i % 2 ? "林鹤" : "王冕",
  actions: ["接诊", "编辑", "设置会员", "删除"],
}));

const drugRows = createRows(10, (i) => ({
  index: i,
  code: `D10000${i}`,
  name: ["阿莫西林胶囊", "头孢克肟片", "葡萄糖注射液", "维生素C片"][(i - 1) % 4],
  spec: `${5 + i}g*10袋/盒`,
  chargeType: i % 2 ? "西成药" : "中成药",
  purchasePrice: `${11 + i}.00`,
  retailPrice: `${14 + i}.00`,
  factory: i % 2 ? "上海医药（集团）有限公司" : "山东罗欣药业股份有限公司",
  status: i % 3 === 1 ? "停用" : "启用",
  time: `2026-04-22 10:0${i}:56`,
  actions: ["编辑", "复制", "删除"],
}));

const stockInRows = createRows(10, (i) => ({
  index: i,
  orderNo: `SP2019110908${17 + i}`,
  type: "采购入库",
  supplier: "白云制药厂",
  maker: doctors[(i - 1) % doctors.length],
  purchase: `${26900 + i * 100}.00`,
  retail: `${26900 + i * 100}.00`,
  operator: doctors[(i - 1) % doctors.length],
  time: "2019-02-09 10:23:56",
  status: i === 1 ? "审核通过" : i === 2 ? "审核未通过" : "未审核",
  actions: i <= 2 ? ["查看", "再次入库"] : ["编辑", "删除", "再次入库"],
}));

const stockOutRows = createRows(10, (i) => ({
  index: i,
  orderNo: `CB2019111900${31 - i}`,
  type: ["科室领药", "报损出库", "采购退货"][(i - 1) % 3],
  maker: doctors[(i - 1) % doctors.length],
  purchase: `${26900 + i * 100}.00`,
  retail: `${26900 + i * 100}.00`,
  operator: doctors[(i - 1) % doctors.length],
  time: "2019-02-09 10:23:56",
  status: i === 1 ? "审核通过" : i === 2 ? "审核未通过" : "未审核",
  actions: i <= 2 ? ["查看", "再次出库"] : ["编辑", "删除", "再次出库"],
}));

const inventoryRows = createRows(10, (i) => ({
  index: i,
  code: `10000${i}`,
  shelf: `10${i}`,
  name: ["超敏C-反应蛋白", "甲硝唑氯化钠注射液", "氯化钠0.9%", "青霉素针剂"][(i - 1) % 4],
  category: "西成药",
  spec: `6g*${9 + i}袋/盒`,
  dosage: ["口服", "注射", "皮试", "其它"][(i - 1) % 4],
  factory: i % 2 ? "上海医药（集团）有限公司" : "山东罗欣药业股份有限公司",
  stock: i % 2 ? `${109 + i}盒` : `${96 + i}盒${8 + i}袋`,
  purchase: `${26900 + i * 100}.00`,
  retail: `${26900 + i * 100}.00`,
  actions: ["查看明细"],
}));

const inventoryCheckRows = createRows(10, (i) => ({
  index: i,
  orderNo: `SA2019111900${31 - i}`,
  date: "2019-02-09 10:23:56",
  maker: doctors[(i - 1) % doctors.length],
  status: i === 1 ? "正在盘点" : "盘点完成",
  actions: i === 1 ? ["查看", "删除"] : ["查看"],
}));

const priceAdjustRows = createRows(10, (i) => ({
  index: i,
  code: `10000${i}`,
  name: ["超敏C-反应蛋白", "甲硝唑氯化钠注射液", "氯化钠0.9%", "青霉素针剂"][(i - 1) % 4],
  category: "西成药",
  spec: `6g*${9 + i}袋/盒`,
  dosage: ["口服", "注射", "皮试", "其它"][(i - 1) % 4],
  factory: i % 2 ? "上海医药（集团）有限公司" : "山东罗欣药业股份有限公司",
  actions: ["调价"],
}));

const memberRows = createRows(10, (i) => ({
  index: i,
  cardNo: `0281001${60 - i}`,
  memberType: ["普通会员", "初级会员", "高级会员", "白银会员", "黄金会员", "钻石会员"][(i - 1) % 6],
  level: `VIP${((i - 1) % 7) + 1}`,
  name: `姓名${i * 2 - 1}`,
  phone: `177541387${68 + i}`,
  consume: `${1200 + (i - 1) * 1000}.90`,
  stored: `${1200 + (i - 1) * 1000}.90`,
  totalStored: `${1200 + (i - 1) * 1000}.90`,
  points: 900 + i * 100,
  openTime: `2019-11-12 12:08:1${i - 1}`,
  expireTime: `2019-11-12 12:08:1${i - 1}`,
  enabled: true,
  actions: ["操作"],
}));

const chargeStatRows = createRows(10, (i) => ({
  index: i,
  status: i % 2 ? "已收费" : "已退费",
  orderType: ["处方开立", "药品零售", "挂号费"][(i - 1) % 3],
  orderNo: `2019120900${31 - i}`,
  name: `姓名${i * 2 - 1}`,
  dept: "全科",
  doctor: doctors[(i - 1) % doctors.length],
  should: "210.00",
  discount: "10.00",
  actual: "200.00",
  insurance: "50.00",
  member: "150.00",
  cash: "0.00",
  alipay: "0/00",
  wechat: "0.00",
  bank: "0.00",
  date: "2019-10-30 10:34:45",
  cashier: "王冕",
  actions: ["查看详情"],
}));

const patientStatRows = createRows(10, (i) => ({
  index: i,
  time: "2019-10-30 10:34:45",
  patientNo: 100130 - i + 1,
  name: `姓名${60 - i}`,
  gender: "女",
  age: 34,
  phone: `177541387${68 + i}`,
  visitType: visitTypes[(i - 1) % visitTypes.length],
  diagnosis: "流行性感冒",
  dept: "科室",
  doctor: doctors[(i - 1) % doctors.length],
  chargeStatus: i % 3 === 0 ? "待收费" : i % 2 ? "已收费" : "已退费",
}));

const checkProjectRows = createRows(10, (i) => ({
  index: i,
  orderNo: `2019120900${31 - i}`,
  itemType: ["煎药费", "拔牙", "手术费"][(i - 1) % 3],
  itemCode: `10000${i}`,
  itemName: ["脑电图", "血压测量", "超声检查", "针灸"][(i - 1) % 4],
  invoiceType: ["注射费", "检查费", "材料费"][(i - 1) % 3],
  quantity: 1,
  purchasePrice: "12.00",
  retailPrice: "15.00",
  purchaseTotal: "24.00",
  retailTotal: "30.00",
  profit: "6.00",
  orderDate: "2019-10-23 10:23:34",
}));

const drugStatRows = createRows(10, (i) => ({
  index: i,
  orderNo: `SP2019110908${17 + i}`,
  drugCode: `10000${i}`,
  drugName: ["复方氯化钠注射液", "替硝唑氯化钠注射液", "0.9%氯化钠注射液", "生脉注射液"][(i - 1) % 4],
  spec: "6g*10袋/盒",
  factory: "昆明制药集团",
  type: "采购入库",
  batchNo: "20191109897",
  supplier: i % 3 === 0 ? "蓝天制药厂" : "白云制药厂",
  price: "20.00",
  count: "40盒",
  total: "800.00",
  operator: doctors[(i - 1) % doctors.length],
  inDate: "2019-10-23",
  checker: doctors[(i - 1) % doctors.length],
  checkDate: "2019-10-23",
}));

const dictionaryRows = createRows(10, (i) => ({
  index: i,
  content: ["心悸", "头疼", "病毒性感冒", "腹部积水", "感冒", "发烧", "支气管炎", "急性肠胃炎", "脊椎炎"][i - 1],
  time: `2019-11-12 12:08:1${i - 1}`,
  creator: i % 2 ? "王冕" : "林鹤",
  actions: ["编辑", "删除"],
}));

const staffRows = createRows(10, (i) => ({
  index: i,
  workNo: 1031 - i,
  name: `姓名${60 - i}`,
  gender: i % 2 ? "男" : "女",
  age: 24 + i,
  phone: `1885413678${i - 1}`,
  clinic: `支所${i % 2 ? 1 : 2}`,
  dept: ["全科", "内科", "外科", "儿科"][(i - 1) % 4],
  role: ["医生", "护士", "财务", "医生, 护士", "管理员"][(i - 1) % 5],
  time: `2019-11-12 12:08:2${(i - 1) % 4}`,
  creator: i % 2 ? "王冕" : "林鹤",
  enabled: true,
  actions: ["编辑", "删除"],
}));

const projectRows = createRows(10, (i) => ({
  index: i,
  code: `10000${i}`,
  name: ["煎药费", "拔牙", "手术费"][(i - 1) % 3],
  category: ["脑电图", "血压测量", "超声检查", "针灸"][(i - 1) % 4],
  invoiceType: ["注射费", "检查费", "材料费"][(i - 1) % 3],
  retailPrice: `${14 + i}.00`,
  costPrice: `${12 + i}.00`,
  unit: "次",
  status: "启用",
  time: "2019-02-09 10:23:56",
  actions: ["编辑", "停用"],
}));

const supplierRows = createRows(4, (i) => ({
  index: i,
  code: 1005 - i,
  name: i === 1 ? "蓝天制药厂" : "白云制药厂",
  contact: ["王宽硕", "李明", "赵健", "曲晓静"][i - 1],
  phone: "18865789099",
  time: `2019-11-12 12:08:2${7 + i}`,
  creator: i % 2 ? "王冕" : "林鹤",
  status: "启用",
  actions: ["编辑", "删除"],
}));

const templateRows = createRows(10, (i) => ({
  index: i,
  code: `RZ000${4 + i}`,
  name: `病历名称${i}`,
  diagnosis: ["心悸", "头疼", "病毒性感冒", "腹部积水", "感冒", "发烧", "支气管炎", "急性肠胃炎", "脊椎炎", "心悸"][i - 1],
  permission: i % 2 ? "私人模板" : "公共模板",
  time: `2019-11-12 12:08:2${(i - 1) % 4}`,
  creator: i % 2 ? "王冕" : "林鹤",
  actions: ["编辑", "删除"],
}));

const feeRows: PrototypeRow[] = [
  {
    index: 1,
    name: "材料费",
    category: "西/成药处方",
    amount: "20.00",
    cost: "10.00",
    time: "2019-11-12 12:08:28",
    creator: "王冕",
    memberDiscount: "是",
    status: "启用",
    actions: ["编辑", "删除"],
  },
  {
    index: 2,
    name: "注射费",
    category: "中药处方",
    amount: "20.00",
    cost: "10.00",
    time: "2019-11-12 12:08:29",
    creator: "林鹤",
    memberDiscount: "是",
    status: "启用",
    actions: ["编辑", "删除"],
  },
  {
    index: 3,
    name: "煎药费",
    category: "西/成药处方",
    amount: "15.00",
    cost: "0.00",
    time: "2019-11-12 12:08:30",
    creator: "王冕",
    memberDiscount: "是",
    status: "启用",
    actions: ["编辑", "删除"],
  },
  {
    index: 4,
    name: "皮试",
    category: "中药处方",
    amount: "30.00",
    cost: "0.00",
    time: "2019-11-12 12:08:31",
    creator: "林鹤",
    memberDiscount: "是",
    status: "启用",
    actions: ["编辑", "删除"],
  },
];

export const prototypeConfigs: Record<string, PrototypePageConfig> = {
  "/registration/record": {
    title: "挂号记录",
    tabs: [{ label: "未就诊", active: true }, { label: "已就诊" }, { label: "已退号" }],
    actions: [{ label: "新增挂号", type: "primary" }, { label: "导出" }],
    filters: [
      { label: "挂号科室", type: "select", value: "全部", options: ["全部"] },
      { label: "挂号医生", type: "select", value: "全部", options: ["全部"] },
      { label: "就诊时间", type: "daterange" },
      { label: "", type: "input", placeholder: "输入患者姓名" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "orderNo", label: "挂号单号" },
      { key: "name", label: "患者姓名" },
      { key: "gender", label: "性别" },
      { key: "age", label: "年龄" },
      { key: "phone", label: "手机号" },
      { key: "dept", label: "科室" },
      { key: "doctor", label: "接诊医生" },
      { key: "time", label: "接诊时间" },
      { key: "amount", label: "应收金额(元)" },
      { key: "paid", label: "实收金额(元)" },
      { key: "status", label: "就诊状态", type: "status" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: registrationRows,
  },
  "/charge": {
    title: "收费管理",
    tabs: [{ label: "待收费", active: true }, { label: "已收费" }, { label: "已退费" }],
    filters: [
      { label: "订单类型", type: "select", value: "全部", options: ["全部"] },
      { label: "创建时间", type: "daterange" },
      { label: "", type: "input", placeholder: "输入订单编号/患者姓名" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "orderNo", label: "订单编号" },
      { key: "orderType", label: "订单类型" },
      { key: "name", label: "姓名" },
      { key: "gender", label: "性别" },
      { key: "age", label: "年龄" },
      { key: "phone", label: "手机号" },
      { key: "dept", label: "科室" },
      { key: "doctor", label: "接诊医生" },
      { key: "time", label: "创建时间" },
      { key: "amount", label: "应收金额" },
      { key: "status", label: "收费状态", type: "status" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: chargeManageRows,
  },
  "/patient": {
    title: "患者管理",
    actions: [{ label: "新增患者", type: "primary" }, { label: "导出" }],
    filters: [
      { label: "会员类型", type: "select", value: "全部", options: ["全部"] },
      { label: "创建时间", type: "daterange" },
      { label: "", type: "input", placeholder: "患者姓名/手机号" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "患者编码" },
      { key: "name", label: "患者姓名" },
      { key: "gender", label: "性别" },
      { key: "age", label: "年龄" },
      { key: "phone", label: "手机号" },
      { key: "level", label: "会员等级", type: "status" },
      { key: "time", label: "创建时间" },
      { key: "operator", label: "操作人员" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: patientRows,
  },
  "/drug": {
    title: "药品信息维护",
    actions: [{ label: "新增药品", type: "primary" }, { label: "扫码录入" }, { label: "导入" }, { label: "导出" }],
    filters: [
      { label: "处方类别", type: "select", value: "全部", options: ["全部"] },
      { label: "药品状态", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "daterange", value: "2026-04-01 - 2026-04-22" },
      { label: "", type: "input", placeholder: "输入药品名称/编码/生产厂家" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "编码" },
      { key: "name", label: "药品名称" },
      { key: "spec", label: "规格" },
      { key: "chargeType", label: "收费类别" },
      { key: "purchasePrice", label: "采购价" },
      { key: "retailPrice", label: "零售价" },
      { key: "factory", label: "生产厂家" },
      { key: "status", label: "状态", type: "status" },
      { key: "time", label: "创建时间" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: drugRows,
  },
  "/drug/stock-in": {
    title: "入库管理",
    actions: [{ label: "新增入库", type: "primary" }, { label: "扫码入库" }],
    filters: [
      { label: "审核状态", type: "select", value: "全部", options: ["全部"] },
      { label: "入库类型", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "输入入库单号/供应商" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "orderNo", label: "入库单号" },
      { key: "type", label: "入库类型" },
      { key: "supplier", label: "供应商名称" },
      { key: "maker", label: "制单人员" },
      { key: "purchase", label: "采购金额" },
      { key: "retail", label: "零售金额" },
      { key: "operator", label: "入库人员" },
      { key: "time", label: "创建时间" },
      { key: "status", label: "审核状态", type: "status" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: stockInRows,
  },
  "/drug/stock-out": {
    title: "出库管理",
    actions: [{ label: "新增出库", type: "primary" }, { label: "扫码出库" }],
    filters: [
      { label: "审核状态", type: "select", value: "全部", options: ["全部"] },
      { label: "出库类型", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "输入出库单号" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "orderNo", label: "出库单号" },
      { key: "type", label: "出库类型" },
      { key: "maker", label: "制单人员" },
      { key: "purchase", label: "采购金额" },
      { key: "retail", label: "零售金额" },
      { key: "operator", label: "出库人员" },
      { key: "time", label: "创建时间" },
      { key: "status", label: "审核状态", type: "status" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: stockOutRows,
  },
  "/drug/inventory": {
    title: "库存管理",
    actions: [{ label: "导出" }],
    filters: [
      { label: "处方类别", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "输入药品名称/编码/生产厂家" },
      { label: "库存预警", type: "checkbox", checked: false },
      { label: "有效期预警", type: "checkbox", checked: false },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "药品编码" },
      { key: "shelf", label: "货位号" },
      { key: "name", label: "药品名称" },
      { key: "category", label: "处方类别" },
      { key: "spec", label: "规格" },
      { key: "dosage", label: "剂型" },
      { key: "factory", label: "厂家" },
      { key: "stock", label: "库存", type: "status" },
      { key: "purchase", label: "采购金额（元）" },
      { key: "retail", label: "零售金额（元）" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: inventoryRows,
    footerNote: "采购金额合计：67379.12元  零售金额合计：81111.08元",
  },
  "/drug/inventory-check": {
    title: "库存盘点",
    actions: [{ label: "新增盘点", type: "primary" }, { label: "扫码出库" }],
    filters: [
      { label: "创建时间", type: "daterange" },
      { label: "", type: "input", placeholder: "输入盘点单号" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "orderNo", label: "盘点单号" },
      { key: "date", label: "盘点日期" },
      { key: "maker", label: "制单人" },
      { key: "status", label: "盘点状态", type: "status" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: inventoryCheckRows,
  },
  "/drug/price-adjust": {
    title: "药品调价",
    tabs: [{ label: "药品调价", active: true }, { label: "调价记录" }],
    actions: [{ label: "导出" }],
    filters: [
      { label: "处方类别", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "输入药品名称/编码/生产厂家" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "药品编码" },
      { key: "name", label: "药品名称" },
      { key: "category", label: "处方类别" },
      { key: "spec", label: "规格" },
      { key: "dosage", label: "剂型" },
      { key: "factory", label: "生产厂家" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: priceAdjustRows,
  },
  "/member": {
    title: "会员管理",
    tabs: [{ label: "会员列表", active: true }, { label: "储值管理" }, { label: "积分管理" }, { label: "会员设置" }],
    actions: [{ label: "导出" }],
    filters: [
      { label: "会员等级", type: "select", value: "全部", options: ["全部"] },
      { label: "创建时间", type: "daterange" },
      { label: "", type: "input", placeholder: "患者姓名/手机号/会员卡号" },
      { label: "会员到期预警", type: "checkbox", checked: false },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "cardNo", label: "卡号" },
      { key: "memberType", label: "会员类型" },
      { key: "level", label: "会员等级", type: "status" },
      { key: "name", label: "会员姓名" },
      { key: "phone", label: "手机号" },
      { key: "consume", label: "累计消费(元)" },
      { key: "stored", label: "储值余额(元)" },
      { key: "totalStored", label: "累计储值(元)" },
      { key: "points", label: "积分" },
      { key: "openTime", label: "开卡时间" },
      { key: "expireTime", label: "到期时间" },
      { key: "enabled", label: "会员状态", type: "switch" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: memberRows,
  },
  "/statistics/charge": {
    title: "收费统计",
    tabs: [
      { label: "收费明细", active: true },
      { label: "收费员收费统计" },
      { label: "科室收费统计" },
      { label: "医生业绩统计" },
      { label: "储值明细" },
    ],
    actions: [{ label: "导出" }],
    filters: [
      { label: "订单时间", type: "daterange" },
      { label: "订单类型", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "输入订单编号/患者姓名" },
    ],
    summaryCards: [
      { label: "合计收入（元）", value: "1200.00", accent: true },
      { label: "应收合计（元）", value: "1400.00" },
      { label: "优惠合计（元）", value: "200.00" },
      { label: "实收合计（元）", value: "1200.00" },
      { label: "医保（元）", value: "200.00" },
      { label: "会员（元）", value: "200.00" },
      { label: "现金（元）", value: "200.00" },
      { label: "支付宝（元）", value: "200.00" },
      { label: "微信（元）", value: "200.00" },
      { label: "银行卡（元）", value: "200.00" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "status", label: "订单状态", type: "status" },
      { key: "orderType", label: "订单类型" },
      { key: "orderNo", label: "订单编号" },
      { key: "name", label: "姓名" },
      { key: "dept", label: "科室" },
      { key: "doctor", label: "接诊医生" },
      { key: "should", label: "应收/退(元)" },
      { key: "discount", label: "优惠(元)" },
      { key: "actual", label: "实收/退(元)" },
      { key: "insurance", label: "医保(元)" },
      { key: "member", label: "会员卡(元)" },
      { key: "cash", label: "现金(元)" },
      { key: "alipay", label: "支付宝(元)" },
      { key: "wechat", label: "微信(元)" },
      { key: "bank", label: "银行卡(元)" },
      { key: "date", label: "收费日期" },
      { key: "cashier", label: "收费员" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: chargeStatRows,
  },
  "/statistics/patient": {
    title: "患者统计",
    tabs: [
      { label: "门诊日志", active: true },
      { label: "患者年龄分析" },
      { label: "患者性别分析" },
      { label: "患者婚姻情况分析" },
      { label: "会员类型分析" },
      { label: "就诊类型分析" },
      { label: "患者来源分析" },
    ],
    actions: [{ label: "导出" }],
    filters: [
      { label: "就诊时间", type: "daterange" },
      { label: "接诊医生", type: "select", value: "全部", options: ["全部"] },
      { label: "收费状态", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "患者姓名/手机号" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "time", label: "就诊时间" },
      { key: "patientNo", label: "患者编号" },
      { key: "name", label: "姓名" },
      { key: "gender", label: "性别" },
      { key: "age", label: "年龄" },
      { key: "phone", label: "手机号" },
      { key: "visitType", label: "门诊类型" },
      { key: "diagnosis", label: "诊断" },
      { key: "dept", label: "科室" },
      { key: "doctor", label: "接诊医生" },
      { key: "chargeStatus", label: "收费状态", type: "status" },
    ],
    rows: patientStatRows,
  },
  "/statistics/check-project": {
    title: "检查项目统计",
    actions: [{ label: "导出" }],
    filters: [
      { label: "订单时间", type: "daterange" },
      { label: "", type: "input", placeholder: "输入单号/项目名称" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "orderNo", label: "订单编号" },
      { key: "itemType", label: "项目类型" },
      { key: "itemCode", label: "项目编码" },
      { key: "itemName", label: "项目名称" },
      { key: "invoiceType", label: "发票类型" },
      { key: "quantity", label: "数量" },
      { key: "purchasePrice", label: "采购单价(元)" },
      { key: "retailPrice", label: "零售单价(元)" },
      { key: "purchaseTotal", label: "采购总价(元)" },
      { key: "retailTotal", label: "零售总价(元)" },
      { key: "profit", label: "利润(元)" },
      { key: "orderDate", label: "订单日期" },
    ],
    rows: checkProjectRows,
    footerNote: "合计：采购总价 7200.00 元  零售总价 9000.00 元  利润 1800.00 元",
  },
  "/statistics/drug": {
    title: "药品统计",
    tabs: [
      { label: "入库统计", active: true },
      { label: "出库统计" },
      { label: "进销存统计" },
      { label: "调价统计" },
      { label: "药品销售明细" },
      { label: "处方药品排行" },
      { label: "零售药品排行" },
    ],
    actions: [{ label: "导出" }],
    filters: [
      { label: "入库时间", type: "daterange" },
      { label: "入库类型", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "输入入库单号/供应商/药品名称" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "orderNo", label: "单号" },
      { key: "drugCode", label: "药品编码" },
      { key: "drugName", label: "药品名称" },
      { key: "spec", label: "规格" },
      { key: "factory", label: "生产厂商" },
      { key: "type", label: "入库类型" },
      { key: "batchNo", label: "批号" },
      { key: "supplier", label: "供应商" },
      { key: "price", label: "采购价(元)" },
      { key: "count", label: "数量" },
      { key: "total", label: "采购成本(元)" },
      { key: "operator", label: "入库人员" },
      { key: "inDate", label: "入库日期" },
      { key: "checker", label: "审核人员" },
      { key: "checkDate", label: "审核日期" },
    ],
    rows: drugStatRows,
  },
  "/system/dictionary": {
    title: "字典表维护",
    tabs: [
      { label: "病历信息", active: true },
      { label: "药品信息" },
      { label: "检查项目" },
      { label: "患者信息" },
    ],
    actions: [{ label: "新增", type: "primary" }],
    layout: "split",
    leftNav: ["诊断信息", "医嘱信息", "主诉信息", "现病史", "既往史", "过敏史", "个人史", "辅助检查", "治疗意见"],
    leftNavActive: "诊断信息",
    filters: [{ label: "", type: "input", placeholder: "模板内容" }],
    columns: [
      { key: "index", label: "序号" },
      { key: "content", label: "诊断内容" },
      { key: "time", label: "创建时间" },
      { key: "creator", label: "创建人" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: dictionaryRows,
  },
  "/system/staff": {
    title: "员工管理",
    tabs: [{ label: "员工列表", active: true }, { label: "科室列表" }, { label: "角色列表" }],
    actions: [{ label: "新增", type: "primary" }],
    filters: [
      { label: "所属科室", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "员工姓名" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "workNo", label: "工号" },
      { key: "name", label: "员工姓名" },
      { key: "gender", label: "员工性别" },
      { key: "age", label: "员工年龄" },
      { key: "phone", label: "手机号" },
      { key: "clinic", label: "所属诊所" },
      { key: "dept", label: "所属科室" },
      { key: "role", label: "角色" },
      { key: "time", label: "创建时间" },
      { key: "creator", label: "创建人员" },
      { key: "enabled", label: "员工状态", type: "switch" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: staffRows,
  },
  "/system/check-project-setting": {
    title: "检查项目设置",
    actions: [{ label: "新增项目", type: "primary" }, { label: "导入" }, { label: "导出" }],
    filters: [
      { label: "项目状态", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "项目名称/项目编号" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "项目编号" },
      { key: "name", label: "项目名称" },
      { key: "category", label: "项目类别" },
      { key: "invoiceType", label: "发票项目" },
      { key: "retailPrice", label: "零售价（元）" },
      { key: "costPrice", label: "成本价（元）" },
      { key: "unit", label: "单位" },
      { key: "status", label: "项目状态" },
      { key: "time", label: "创建时间" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: projectRows,
  },
  "/system/supplier": {
    title: "供应商管理",
    actions: [{ label: "新增", type: "primary" }],
    filters: [{ label: "", type: "input", placeholder: "供应商名称" }],
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "供应商编号" },
      { key: "name", label: "供应商名称" },
      { key: "contact", label: "联系人" },
      { key: "phone", label: "联系电话" },
      { key: "time", label: "创建时间" },
      { key: "creator", label: "创建人员" },
      { key: "status", label: "供应商状态" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: supplierRows,
  },
  "/system/template": {
    title: "模板维护",
    tabs: [{ label: "病历模板", active: true }, { label: "处方模板" }],
    actions: [{ label: "新增", type: "primary" }],
    filters: [
      { label: "模板权限", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "模板编码/模板模板名称" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "code", label: "模板编号" },
      { key: "name", label: "模板名称" },
      { key: "diagnosis", label: "诊断" },
      { key: "permission", label: "模板权限" },
      { key: "time", label: "创建时间" },
      { key: "creator", label: "创建人员" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: templateRows,
  },
  "/system/fee-setting": {
    title: "费用设置",
    tabs: [{ label: "附加费用设置", active: true }, { label: "诊疗费设置" }, { label: "挂号费设置" }, { label: "支付方式设置" }],
    actions: [{ label: "新增", type: "primary" }],
    filters: [
      { label: "处方类别", type: "select", value: "全部", options: ["全部"] },
      { label: "", type: "input", placeholder: "费用名称" },
    ],
    columns: [
      { key: "index", label: "序号" },
      { key: "name", label: "附加费名称" },
      { key: "category", label: "处方类型" },
      { key: "amount", label: "金额（元）" },
      { key: "cost", label: "成本价（元）" },
      { key: "time", label: "创建时间" },
      { key: "creator", label: "创建人员" },
      { key: "memberDiscount", label: "允许会员折扣" },
      { key: "status", label: "费用状态" },
      { key: "actions", label: "操作", type: "actions" },
    ],
    rows: feeRows,
  },
  "/system/basic-setting": {
    title: "基础设置",
    layout: "settings",
    sections: [
      { type: "switch", title: "处方单价锁定", description: "开启后，处方结算时无法修改药品及附加费用等费用项目价格", value: false },
      { type: "switch", title: "费用设置", description: "开启后，开具处方及药品零售时支持添加附件费", value: true },
      {
        type: "input",
        title: "挂号功能设置",
        label: "挂号有效天数",
        value: "3",
        suffix: "天",
        helper: "0为当天，-1不限天数，>0有效天数",
        actionText: "修改",
      },
      { type: "radio", title: "金额精度", value: "分", options: ["分", "角", "元"] },
      {
        type: "checkbox-group",
        title: "病历字段设置",
        description: "系统默认（不可修改）",
        checked: ["诊断", "医嘱", "主诉", "发病日期", "体格信息", "现病史", "既往史", "过敏史", "个人史", "家族史", "辅助检查", "治疗意见", "备注"],
        options: ["诊断", "医嘱", "主诉", "发病日期", "体格信息", "现病史", "既往史", "过敏史", "个人史", "家族史", "辅助检查", "治疗意见", "备注"],
      },
      {
        type: "checkbox-group",
        title: "自定义设置",
        checked: [],
        options: ["望闻问切", "疾病详情", "辨证分析", "治疗意见", "文件", "门诊处理"],
      },
    ],
  },
};
