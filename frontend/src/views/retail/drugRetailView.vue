<template>
  <el-config-provider :locale="zhCn">
    <div class="retail-page">
      <section class="retail-shell">
        <header class="top-actions">
          <button class="action-btn save" type="button" @click="handleSaveOrder">
            <DocumentChecked class="btn-icon" />
            <span>保存</span>
          </button>
          <button class="action-btn charge" type="button" @click="goPayment">
            <Money class="btn-icon" />
            <span>收费</span>
          </button>
        </header>

        <section class="patient-form">
          <div class="field">
            <span>患者姓名</span>
            <div class="control has-icon patient-search">
              <input
                v-model="patient.name"
                placeholder="患者姓名/手机号码/证件号码/卡号"
                @focus="showPatientDropdown = true"
                @click="showPatientDropdown = true"
                @input="showPatientDropdown = true"
                @blur="hidePatientDropdown"
              />
              <Search class="field-icon" />
            </div>
            <div
              v-if="showPatientDropdown"
              class="patient-dropdown"
              @mousedown.prevent
            >
              <button
                v-for="item in filteredPatients"
                :key="item.cardNo"
                class="patient-option"
                type="button"
                @mousedown.prevent="selectPatient(item)"
              >
                <span class="patient-avatar"></span>
                <span class="patient-main">
                  <strong>{{ item.name }}</strong>
                  <em>{{ item.phone }}</em>
                </span>
                <span class="patient-meta">{{ item.gender }}</span>
                <span class="patient-meta">{{ item.age }}岁</span>
              </button>
            </div>
          </div>

          <label class="field">
            <span>患者卡号</span>
            <div class="control">
              <input v-model="patient.cardNo" placeholder="请输入病人卡号" />
            </div>
          </label>

          <label class="field">
            <span>患者年龄</span>
            <div class="control age-control">
              <input v-model="patient.age" placeholder="请输入数字" />
              <button type="button">岁<ArrowDown class="select-icon" /></button>
            </div>
          </label>

          <div class="field">
            <span>出生日期</span>
            <div class="control date-control">
              <el-date-picker
                v-model="patient.birth"
                type="date"
                placeholder=""
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :editable="false"
                clearable
              />
            </div>
          </div>

          <label class="field">
            <span>性别</span>
            <div class="control select-control">
              <select v-model="patient.gender">
                <option>男</option>
                <option>女</option>
              </select>
              <ArrowDown class="field-icon" />
            </div>
          </label>

          <label class="field">
            <span>手机号码</span>
            <div class="control">
              <input v-model="patient.phone" placeholder="请输入手机号码" />
            </div>
          </label>

          <label class="field">
            <span>证件号码</span>
            <div class="control">
              <input v-model="patient.idCard" placeholder="请输入证件号码" />
            </div>
          </label>
        </section>

        <main class="retail-main">
          <section class="drug-card">
            <h2><Tickets class="section-icon" />药品信息</h2>

            <table class="drug-table">
              <colgroup>
                <col class="c-index" />
                <col class="c-code" />
                <col class="c-name" />
                <col class="c-spec" />
                <col class="c-count" />
                <col class="c-unit" />
                <col class="c-price" />
                <col class="c-total" />
                <col class="c-action" />
              </colgroup>
              <thead>
                <tr>
                  <th>序号</th>
                  <th>药品编码</th>
                  <th>药品名称</th>
                  <th>药品规格</th>
                  <th>数量</th>
                  <th>单位</th>
                  <th>单价</th>
                  <th>总价</th>
                  <th>编辑</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(drug, index) in drugList" :key="`${drug.code}-${index}`">
                  <td>{{ index + 1 }}</td>
                  <td>{{ drug.code }}</td>
                  <td>{{ drug.name }}</td>
                  <td>{{ drug.spec }}</td>
                  <td>
                    <input
                      v-model.number="drug.quantity"
                      class="mini-input"
                      type="number"
                      min="0"
                      @input="normalizeDrug(drug)"
                    />
                  </td>
                  <td>
                    <div class="unit-select">
                      <select v-model="drug.unit">
                        <option>盒</option>
                        <option>瓶</option>
                        <option>袋</option>
                        <option>支</option>
                      </select>
                      <ArrowDown />
                    </div>
                  </td>
                  <td>
                    <input
                      v-model.number="drug.price"
                      class="mini-input"
                      type="number"
                      min="0"
                      step="0.01"
                      @input="normalizeDrug(drug)"
                    />
                  </td>
                  <td>
                    <input class="mini-input" type="text" :value="drugTotal(drug)" />
                  </td>
                  <td>
                    <button class="delete-btn" type="button" @click="removeDrug(index)">
                      <Delete />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>

            <div class="empty-area">
              <button class="add-drug" type="button" @click="showDrugDialog = true">
                <CirclePlusFilled class="plus-icon" />
                <span>添加药品</span>
              </button>
            </div>

            <footer class="drug-footer">
              <div class="footer-left">
                <button class="extra-btn" type="button" @click="showExtraDialog = true">
                  <CirclePlusFilled class="footer-plus" />
                  <span>附加费用</span>
                </button>

                <label class="operator-field">
                  <span>操作员<i>*</i></span>
                  <div class="operator-text">{{ operator }}</div>
                </label>

                <div v-if="selectedExtraFees.length" class="selected-fees">
                  <span
                    v-for="fee in selectedExtraFees"
                    :key="fee.code"
                    class="selected-fee"
                  >
                    {{ fee.name }}（总额）
                    <strong>{{ fee.amount }}</strong>
                    <button type="button" @click="removeExtraFee(fee.code)">
                      <Delete />
                    </button>
                  </span>
                </div>
              </div>

              <div class="total-price">
                <span>总计:</span>
                <strong>{{ totalPrice.toFixed(2) }}</strong>
                <em>元</em>
              </div>
            </footer>
          </section>

          <aside class="ranking-card">
            <h2><Medal class="rank-icon" />本周药品销售排行</h2>

            <div class="rank-list">
              <div v-for="(item, index) in topDrugs" :key="item.code" class="rank-row">
                <span class="rank-index">{{ index + 1 }}.</span>
                <div class="rank-box">
                  <span class="rank-code">{{ item.code }}</span>
                  <span class="rank-name">{{ item.name }}</span>
                  <button type="button" @click="addDrugFromItem(item)">添加</button>
                </div>
              </div>
            </div>
          </aside>
        </main>
      </section>

      <div v-if="showDrugDialog" class="dialog-mask">
        <section class="select-drug-dialog dialog-panel">
          <button class="dialog-close" type="button" @click="showDrugDialog = false"><Close /></button>
          <h3>选择药品</h3>
          <div class="drug-filter">
            <span>处方类别</span>
            <div class="filter-select">
              <select v-model="drugFilter.category">
                <option>全部</option>
                <option>西/成药</option>
                <option>中药</option>
              </select>
              <ArrowDown />
            </div>
            <div class="filter-search">
              <input v-model="drugFilter.keyword" placeholder="输入药品编码/药品名称" />
              <button type="button"><Search /></button>
            </div>
          </div>

          <div class="select-table-wrap">
            <table class="select-table">
              <thead>
                <tr>
                  <th><input type="checkbox" :checked="allVisibleSelected" @change="toggleAllDrugs" /></th>
                  <th>序号</th>
                  <th>药品编码</th>
                  <th>药品名称</th>
                  <th>收费类别</th>
                  <th>规格</th>
                  <th>厂家</th>
                  <th>库存</th>
                  <th>零售价格(元)</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(drug, index) in filteredDialogDrugs" :key="drug.code">
                  <td><input v-model="selectedDrugCodes" type="checkbox" :value="drug.code" /></td>
                  <td>{{ index + 1 }}</td>
                  <td>{{ drug.code }}</td>
                  <td>{{ drug.name }}</td>
                  <td>{{ drug.category }}</td>
                  <td>{{ drug.spec }}</td>
                  <td>{{ drug.factory }}</td>
                  <td><strong>{{ drug.stock }}</strong></td>
                  <td>{{ drug.price.toFixed(2) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="dialog-actions">
            <button class="plain-btn" type="button" @click="showDrugDialog = false">取消</button>
            <button class="primary-btn" type="button" @click="confirmDrugs">确定</button>
          </div>
        </section>
      </div>

      <div v-if="showExtraDialog" class="dialog-mask">
        <section class="extra-dialog dialog-panel">
          <button class="dialog-close" type="button" @click="showExtraDialog = false"><Close /></button>
          <h3>附加费用</h3>
          <p>名称与金额可在【系统设置-处方费用设置】进行设置</p>

          <table class="extra-table">
            <thead>
              <tr>
                <th></th>
                <th>序号</th>
                <th>单次用量</th>
                <th>数量</th>
                <th>价格</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="fee in extraFees" :key="fee.code">
                <td><input v-model="fee.checked" type="checkbox" /></td>
                <td>{{ fee.code }}</td>
                <td>{{ fee.name }}</td>
                <td><input v-model.number="fee.quantity" class="fee-input" min="0" type="number" /></td>
                <td><input v-model.number="fee.price" class="fee-input" min="0" step="0.01" type="number" /></td>
              </tr>
            </tbody>
          </table>

          <div class="dialog-actions">
            <button class="plain-btn" type="button" @click="showExtraDialog = false">取消</button>
            <button class="primary-btn" type="button" @click="confirmExtraFees">添加</button>
          </div>
        </section>
      </div>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { onMounted } from "vue";
import { accountApi, drugApi, feeItemApi, patientApi, retailOrderApi } from "@/api";
import { ElMessage } from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import {
  ArrowDown,
  Calendar,
  CirclePlusFilled,
  Close,
  Delete,
  DocumentChecked,
  Medal,
  Money,
  Search,
  Tickets,
} from "@element-plus/icons-vue";

type DrugItem = {
  id?: number | string;
  code: string;
  name: string;
  spec: string;
  quantity: number;
  unit: string;
  price: number;
};

type DialogDrug = DrugItem & {
  category: string;
  factory: string;
  stock: string;
};

type ExtraFee = {
  code: string;
  name: string;
  checked: boolean;
  quantity: number;
  price: number;
};

type PatientOption = {
  id?: number | string;
  name: string;
  cardNo: string;
  age: string;
  birth: string;
  gender: string;
  phone: string;
  idCard: string;
  level: string;
};

const patient = reactive({
  id: "" as number | string,
  name: "",
  cardNo: "",
  age: "",
  birth: "",
  gender: "男",
  phone: "",
  idCard: "",
});

const router = useRouter();
const operator = ref("");
const showPatientDropdown = ref(false);
const showDrugDialog = ref(false);
const showExtraDialog = ref(false);
const selectedDrugCodes = ref<string[]>([]);
const savedOrderId = ref<number | string>("");
const savedOrderNo = ref("");

const drugFilter = reactive({
  category: "全部",
  keyword: "",
});

const patientOptions = ref<PatientOption[]>([
  { name: "王猛", cardNo: "028100158", age: "25", birth: "2001-06-12", gender: "男", phone: "18854138766", idCard: "370102200106121234", level: "VIP2" },
  { name: "王猛", cardNo: "028100159", age: "25", birth: "2001-06-12", gender: "男", phone: "18854138766", idCard: "370102200106121235", level: "VIP2" },
  { name: "王猛", cardNo: "028100160", age: "25", birth: "2001-06-12", gender: "男", phone: "18854138766", idCard: "370102200106121236", level: "VIP2" },
  { name: "王猛", cardNo: "028100161", age: "25", birth: "2001-06-12", gender: "男", phone: "18854138766", idCard: "370102200106121237", level: "VIP2" },
  { name: "王猛", cardNo: "028100162", age: "25", birth: "2001-06-12", gender: "男", phone: "18854138766", idCard: "370102200106121238", level: "VIP2" },
]);

const drugList = ref<DrugItem[]>([]);

const dialogDrugs = ref<DialogDrug[]>([
  { code: "1000001", name: "超敏C-反应蛋白", category: "西/成药", spec: "6g*10袋/盒", factory: "上海医药（集团）有限公司", stock: "110盒", quantity: 1, unit: "盒", price: 5 },
  { code: "1000002", name: "甲硝唑氯化钠注射液", category: "西/成药", spec: "6g*11袋/盒", factory: "山东罗欣药业股份有限公司", stock: "98盒10袋", quantity: 1, unit: "盒", price: 5 },
  { code: "1000003", name: "氯化钠0.9%", category: "西/成药", spec: "6g*12袋/盒", factory: "山东罗欣药业股份有限公司", stock: "111盒", quantity: 1, unit: "盒", price: 5 },
  { code: "1000004", name: "氯化钠注射液0.9%", category: "西/成药", spec: "6g*13袋/盒", factory: "山东罗欣药业股份有限公司", stock: "98盒11袋", quantity: 1, unit: "盒", price: 5 },
  { code: "1000005", name: "青霉素针注射用", category: "中药", spec: "6g*14袋/盒", factory: "山东罗欣药业股份有限公司", stock: "112盒", quantity: 1, unit: "盒", price: 5 },
]);

const topDrugs = ref<DrugItem[]>([
  { code: "1000001", name: "兰索拉唑肠溶片", spec: "6g*10袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000002", name: "甲钴胺片", spec: "6g*11袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000003", name: "复方氨吡沙宗胶囊", spec: "6g*12袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000004", name: "对乙酰氨基酚片", spec: "6g*13袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000005", name: "双嘧达莫片", spec: "6g*14袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000006", name: "阿莫西林胶囊", spec: "6g*15袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000007", name: "骨肽注射液", spec: "6g*16袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000008", name: "双氯芬酸钠肠溶片", spec: "6g*17袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000009", name: "滴眼用利福平", spec: "6g*18袋/盒", quantity: 1, unit: "盒", price: 1.2 },
  { code: "1000010", name: "红霉素眼膏", spec: "6g*19袋/盒", quantity: 1, unit: "盒", price: 1.2 },
]);

const extraFees = ref<ExtraFee[]>([
  { code: "01", name: "注射费", checked: false, quantity: 1, price: 3 },
  { code: "02", name: "材料费", checked: false, quantity: 1, price: 4 },
  { code: "03", name: "处理费", checked: false, quantity: 1, price: 6 },
  { code: "04", name: "诊疗费", checked: false, quantity: 1, price: 8 },
]);

const filteredDialogDrugs = computed(() => {
  const keyword = drugFilter.keyword.trim();
  return dialogDrugs.value.filter((drug) => {
    const matchCategory = drugFilter.category === "全部" || drug.category === drugFilter.category;
    const matchKeyword = !keyword || drug.code.includes(keyword) || drug.name.includes(keyword);
    return matchCategory && matchKeyword;
  });
});

const allVisibleSelected = computed(() => (
  filteredDialogDrugs.value.length > 0
  && filteredDialogDrugs.value.every((drug) => selectedDrugCodes.value.includes(drug.code))
));

const filteredPatients = computed(() => {
  const keyword = patient.name.trim().replace(/\s+/g, "");
  if (!keyword) return patientOptions.value;
  return patientOptions.value.filter((item) => (
    item.name.includes(keyword)
    || item.phone.includes(keyword)
    || item.cardNo.includes(keyword)
    || item.idCard.includes(keyword)
    || `${item.name}${item.level}`.includes(keyword)
  ));
});

const drugTotalValue = (drug: DrugItem) => Number((Number(drug.quantity || 0) * Number(drug.price || 0)).toFixed(2));
const drugTotal = (drug: DrugItem) => drugTotalValue(drug).toFixed(2);

const extraTotal = computed(() => extraFees.value.reduce((sum, fee) => (
  fee.checked ? sum + Number(fee.quantity || 0) * Number(fee.price || 0) : sum
), 0));

const selectedExtraFees = computed(() => extraFees.value
  .filter((fee) => fee.checked)
  .map((fee) => ({
    code: fee.code,
    name: fee.name,
    quantity: Number(fee.quantity || 0),
    price: Number(fee.price || 0),
    amount: (Number(fee.quantity || 0) * Number(fee.price || 0)).toFixed(2),
  })));

const totalPrice = computed(() => {
  const total = drugList.value.reduce((sum, item) => sum + drugTotalValue(item), 0) + extraTotal.value;
  return total;
});

const normalizeDrug = (drug: DrugItem) => {
  if (!Number.isFinite(Number(drug.quantity)) || Number(drug.quantity) < 0) drug.quantity = 0;
  if (!Number.isFinite(Number(drug.price)) || Number(drug.price) < 0) drug.price = 0;
};

const hidePatientDropdown = () => {
  window.setTimeout(() => {
    showPatientDropdown.value = false;
  }, 120);
};

const selectPatient = (item: PatientOption) => {
  patient.id = item.id || "";
  patient.name = `${item.name}        ${item.level}`;
  patient.cardNo = item.cardNo;
  patient.age = item.age;
  patient.birth = item.birth;
  patient.gender = item.gender;
  patient.phone = item.phone;
  patient.idCard = item.idCard;
  showPatientDropdown.value = false;
};

const addDrugFromItem = (drug: DrugItem) => {
  const currentDrug = dialogDrugs.value.find((item) => item.code === drug.code || item.name === drug.name);
  drugList.value.push({ ...(currentDrug || drug), quantity: 1 });
};

const removeDrug = (index: number) => {
  drugList.value.splice(index, 1);
};

const toggleAllDrugs = (event: Event) => {
  const checked = (event.target as HTMLInputElement).checked;
  const visibleCodes = filteredDialogDrugs.value.map((drug) => drug.code);
  selectedDrugCodes.value = checked
    ? Array.from(new Set([...selectedDrugCodes.value, ...visibleCodes]))
    : selectedDrugCodes.value.filter((code) => !visibleCodes.includes(code));
};

const confirmDrugs = () => {
  dialogDrugs.value
    .filter((drug) => selectedDrugCodes.value.includes(drug.code))
    .forEach((drug) => addDrugFromItem(drug));
  selectedDrugCodes.value = [];
  showDrugDialog.value = false;
};

const confirmExtraFees = () => {
  showExtraDialog.value = false;
};

const removeExtraFee = (code: string) => {
  const fee = extraFees.value.find((item) => item.code === code);
  if (fee) fee.checked = false;
};

const normalizePatientName = () => patient.name.replace(/\s+VIP\d+$/, "").trim();
const padDatePart = (value: number) => String(value).padStart(2, "0");
const nowLocalDateTime = () => {
  const now = new Date();
  return `${now.getFullYear()}-${padDatePart(now.getMonth() + 1)}-${padDatePart(now.getDate())}`
    + `T${padDatePart(now.getHours())}:${padDatePart(now.getMinutes())}:${padDatePart(now.getSeconds())}`;
};

const buildOrderPayload = (paid = false) => {
  const orderItems = drugList.value.map((drug, index) => ({
    index: index + 1,
    drugId: drug.id,
    code: drug.code,
    name: drug.name,
    spec: drug.spec,
    price: Number(drug.price || 0).toFixed(2),
    quantity: Number(drug.quantity || 0),
    unit: drug.unit,
    retailAmount: drugTotal(drug),
    discount: "10.00",
    discountAmount: drugTotal(drug),
  }));

  const paymentPayload = {
    amount: totalPrice.value.toFixed(2),
    patient: {
      code: patient.cardNo || "1000100",
      name: normalizePatientName() || "未选择",
      age: patient.age || "",
      gender: patient.gender || "",
      phone: patient.phone || "",
      level: patient.name.match(/VIP\d+/)?.[0] || "VIP3",
      balance: "2400.00",
      points: "600",
    },
    items: orderItems,
    fees: selectedExtraFees.value.map((fee) => ({
      code: fee.code,
      name: fee.name,
      quantity: fee.quantity,
      price: fee.price.toFixed(2),
      amount: fee.amount,
    })),
    source: "retail",
    operator: operator.value,
  };

  const orderNo = savedOrderNo.value || "LS" + Date.now();
  const amount = Number(paymentPayload.amount);
  return {
    orderNo,
    patientId: patient.id || undefined,
    patientName: paymentPayload.patient.name,
    chargeType: "DRUG_RETAIL",
    receivableAmount: amount,
    discountAmount: 0,
    paidAmount: paid ? amount : 0,
    refundAmount: 0,
    payMethod: paid ? "现金" : undefined,
    status: paid ? "PAID" : "PENDING",
    cashier: operator.value,
    paidAt: paid ? nowLocalDateTime() : undefined,
    remark: JSON.stringify(paymentPayload),
  };
};

const saveRetailOrder = async (paid = false) => {
  if (!drugList.value.length && !selectedExtraFees.value.length) {
    ElMessage.warning("请先添加药品或附加费用");
    return null;
  }
  const data = buildOrderPayload(paid);
  const response: any = savedOrderId.value
    ? await retailOrderApi.update(savedOrderId.value, data)
    : await retailOrderApi.create(data);
  const order = response?.data || {};
  savedOrderId.value = order.id || savedOrderId.value;
  savedOrderNo.value = order.orderNo || data.orderNo;
  return order;
};

const handleSaveOrder = async () => {
  const order = await saveRetailOrder();
  if (!order) return;
  ElMessage.success("零售订单保存成功");
};

const goPayment = async () => {
  const order = await saveRetailOrder(true);
  if (!order) return;
  ElMessage.success("零售订单创建成功");
  router.push({
    path: "/charge/payment",
    query: {
      id: String(order.id || savedOrderId.value),
      source: "retail",
    },
    state: {
      id: order.id || savedOrderId.value,
      source: "retail",
    },
  });
};

const isEnabledFeeItem = (item: any) => {
  const status = String(item.status ?? "").toUpperCase();
  return status !== "DISABLED" && status !== "0" && status !== "FALSE" && item.status !== false;
};

const isAddonFeeItem = (item: any) => {
  const category = String(item.category || "");
  return category !== "挂号费" && category !== "诊疗费";
};

const currentRetailDrug = (item: any) => dialogDrugs.value.find((drug) => (
  drug.code === (item.code || item.drugCode) || drug.name === (item.name || item.drugName)
));

onMounted(async () => {
  const [patientResponse, drugResponse, profileResponse, feeResponse, rankingResponse]: any[] = await Promise.all([
    patientApi.page({ page: 1, size: 100 }),
    drugApi.saleable({ page: 1, size: 100 }),
    accountApi.profile(),
    feeItemApi.page({ page: 1, size: 100 }),
    retailOrderApi.rankings({ limit: 10 }),
  ]);
  operator.value = profileResponse?.data?.name || profileResponse?.data?.username || localStorage.getItem("rememberedAccount") || "当前操作员";
  patientOptions.value = (patientResponse?.data?.records || []).map((item: any) => ({
    id: item.id,
    name: item.name || "",
    cardNo: item.cardNo || item.patientCode || "",
    age: String(item.age || ""),
    birth: item.birthday || item.birthDate || "",
    gender: item.gender || "",
    phone: item.phone || "",
    idCard: item.idCard || "",
    level: item.memberLevel || "非会员",
  }));
  dialogDrugs.value = (drugResponse?.data?.records || drugResponse?.data || []).map((item: any) => ({
    id: item.id,
    code: item.drugCode || String(item.id),
    name: item.name || item.drugName || "",
    category: item.category || "",
    spec: item.specification || "",
    factory: item.manufacturer || "",
    stock: String(item.stockText || `${item.stockQuantity ?? item.stock ?? item.quantity ?? 0}${item.unit || ""}`),
    quantity: 1,
    unit: item.unit || "盒",
    price: Number(item.sellPrice ?? item.salePrice ?? item.retailPrice ?? 0),
  }));
  const feeRecords = (feeResponse?.data?.records || []).filter((item: any) => isEnabledFeeItem(item) && isAddonFeeItem(item));
  if (feeRecords.length) {
    extraFees.value = feeRecords.map((item: any, index: number) => ({
      code: item.feeCode || String(item.id || index + 1),
      name: item.name || "",
      checked: false,
      quantity: 1,
      price: Number(item.price || item.amount || 0),
    }));
  }
  topDrugs.value = (rankingResponse?.data || []).map((item: any) => ({
    id: currentRetailDrug(item)?.id,
    code: currentRetailDrug(item)?.code || item.code || item.drugCode || "",
    name: currentRetailDrug(item)?.name || item.name || item.drugName || "",
    spec: currentRetailDrug(item)?.spec || item.spec || item.specification || "",
    quantity: 1,
    unit: currentRetailDrug(item)?.unit || item.unit || "盒",
    price: Number(currentRetailDrug(item)?.price ?? item.sellPrice ?? item.salePrice ?? item.retailPrice ?? item.price ?? 0),
  }));
});
</script>

<style scoped>
.retail-page {
  min-height: 100%;
  padding: 31px 0 56px;
}

.retail-shell {
  width: min(1700px, calc(100% - 208px));
  min-height: 1164px;
  margin: 0 auto;
  padding: 32px 31px 52px;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.top-actions {
  display: flex;
  justify-content: flex-end;
  gap: 23px;
}

.action-btn {
  width: 151px;
  height: 51px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border: 0;
  border-radius: 5px;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.action-btn.save { background: #636be8; }
.action-btn.charge { background: #ffc21a; }

.btn-icon {
  width: 25px;
  height: 25px;
}

.patient-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 25px 47px;
  margin-top: 34px;
}

.field {
  display: block;
  position: relative;
  min-width: 0;
}

.field > span {
  display: block;
  margin-bottom: 13px;
  color: #111722;
  font-size: 17px;
}

.control {
  height: 61px;
  display: flex;
  align-items: center;
  position: relative;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
}

.control input,
.control select {
  width: 100%;
  height: 100%;
  min-width: 0;
  padding: 0 18px;
  box-sizing: border-box;
  border: 0;
  outline: none;
  background: transparent;
  color: #222936;
  font-size: 18px;
  appearance: none;
}

.control input::placeholder {
  color: #c7c7c7;
}

.patient-search input {
  color: #bf8a00;
  font-weight: 700;
}

.patient-dropdown {
  position: absolute;
  top: 96px;
  left: 0;
  z-index: 30;
  width: 100%;
  max-height: 417px;
  box-sizing: border-box;
  border: 1px solid #e6e8ff;
  border-radius: 3px;
  background: #fff;
  box-shadow: 0 2px 13px rgba(99, 107, 232, 0.14);
  overflow-y: auto;
}

.patient-dropdown::before {
  content: "";
  position: absolute;
  top: -12px;
  left: 96px;
  width: 0;
  height: 0;
  border-left: 13px solid transparent;
  border-right: 13px solid transparent;
  border-bottom: 13px solid #fff;
  filter: drop-shadow(0 -1px 1px rgba(99, 107, 232, 0.08));
}

.patient-dropdown::-webkit-scrollbar {
  width: 18px;
}

.patient-dropdown::-webkit-scrollbar-track {
  background: #fff;
}

.patient-dropdown::-webkit-scrollbar-thumb {
  border: 5px solid #fff;
  border-radius: 12px;
  background: #8b8b8b;
}

.patient-option {
  width: 100%;
  height: 83px;
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) 45px 58px;
  align-items: center;
  padding: 0 14px 0 19px;
  box-sizing: border-box;
  border: 0;
  border-bottom: 1px solid #eeeeee;
  background: #fff;
  color: #111722;
  text-align: left;
  cursor: pointer;
}

.patient-option:hover {
  background: #f7f8ff;
}

.patient-avatar {
  width: 48px;
  height: 48px;
  position: relative;
  border-radius: 50%;
  background: #f7f7fb;
  box-shadow: 0 1px 8px rgba(56, 61, 120, 0.16);
}

.patient-avatar::before {
  content: "";
  position: absolute;
  top: 9px;
  left: 16px;
  width: 17px;
  height: 18px;
  border-radius: 50% 50% 44% 44%;
  background: #ffb5b5;
  box-shadow: 0 -6px 0 2px #32385b;
}

.patient-avatar::after {
  content: "";
  position: absolute;
  left: 10px;
  bottom: 7px;
  width: 29px;
  height: 18px;
  border-radius: 18px 18px 9px 9px;
  background: #4a4d82;
}

.patient-main {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 5px;
}

.patient-main strong {
  color: #111722;
  font-size: 19px;
  font-weight: 700;
}

.patient-main em,
.patient-meta {
  color: #8c94a3;
  font-size: 16px;
  font-style: normal;
}

.has-icon input {
  padding-right: 50px;
}

.field-icon {
  position: absolute;
  right: 15px;
  width: 24px;
  height: 24px;
  color: #d1d1d1;
  pointer-events: none;
}

.age-control {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 109px;
}

.age-control button {
  height: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  border: 0;
  border-left: 2px solid #e0e0e0;
  background: #fff;
  color: #333a45;
  font-size: 18px;
}

.select-icon {
  width: 18px;
  height: 18px;
  color: #c7c7c7;
}

.date-control {
  padding: 0;
}

.date-control :deep(.el-date-editor.el-input) {
  width: 100%;
  height: 100%;
}

.date-control :deep(.el-input__wrapper) {
  height: 100%;
  padding: 0 13px 0 18px;
  box-shadow: none;
  border-radius: 4px;
  background: transparent;
}

.date-control :deep(.el-input__inner) {
  color: #222936;
  font-size: 18px;
}

.date-control :deep(.el-input__prefix) {
  order: 2;
  margin-left: auto;
  margin-right: 0;
  color: #c7c7c7;
}

.select-control select {
  cursor: pointer;
}

.retail-main {
  display: grid;
  grid-template-columns: minmax(0, 1128px) 475px;
  gap: 36px;
  margin-top: 50px;
  align-items: start;
}

.drug-card {
  min-height: 744px;
  display: flex;
  flex-direction: column;
  border: 1px solid #e3e6ff;
  border-radius: 5px;
  box-shadow: 0 2px 12px rgba(99, 107, 232, 0.12);
}

.drug-card h2 {
  height: 97px;
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0 31px;
  color: #636be8;
  font-size: 33px;
  font-weight: 700;
}

.section-icon {
  width: 28px;
  height: 28px;
  margin-right: 12px;
  color: #636be8;
}

.drug-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.drug-table th {
  height: 69px;
  background: #d8daf7;
  color: #111722;
  font-size: 20px;
  font-weight: 700;
  text-align: center;
}

.drug-table td {
  height: 84px;
  border-bottom: 1px solid #eeeeee;
  color: #111722;
  font-size: 18px;
  text-align: center;
}

.mini-input {
  width: 74px;
  height: 42px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  color: #000;
  font-size: 20px;
  text-align: center;
}

.mini-input[readonly] {
  background: #fff;
}

.unit-select {
  width: 88px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  position: relative;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
}

.unit-select select {
  width: 100%;
  height: 100%;
  padding-left: 14px;
  padding-right: 34px;
  border: 0;
  outline: none;
  appearance: none;
  background: transparent;
  font-size: 16px;
}

.unit-select svg {
  position: absolute;
  right: 10px;
  width: 18px;
  height: 18px;
  color: #c7c7c7;
  pointer-events: none;
}

.delete-btn {
  border: 0;
  background: transparent;
  color: #8791ff;
  cursor: pointer;
}

.delete-btn svg {
  width: 20px;
  height: 20px;
}

.c-index { width: 7%; }
.c-code { width: 14%; }
.c-name { width: 17%; }
.c-spec { width: 17%; }
.c-count { width: 8%; }
.c-unit { width: 8%; }
.c-price { width: 11%; }
.c-total { width: 11%; }
.c-action { width: 7%; }

.empty-area {
  flex: 1;
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-drug {
  width: 204px;
  height: 60px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 19px;
  border: 0;
  border-radius: 5px;
  background: #24cf95;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.plus-icon,
.footer-plus {
  width: 27px;
  height: 27px;
}

.drug-footer {
  height: 117px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 20px;
  border-top: 1px solid #eeeeee;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 21px;
  flex-wrap: wrap;
}

.extra-btn {
  width: 136px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 0;
  border-radius: 5px;
  background: #24cf95;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.operator-field {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #111722;
  font-size: 16px;
}

.operator-field i {
  color: #ff0000;
  font-style: normal;
}

.operator-select {
  position: relative;
  width: 142px;
  height: 39px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
}

.operator-text {
  min-width: 142px;
  height: 39px;
  display: inline-flex;
  align-items: center;
  padding: 0 13px;
  box-sizing: border-box;
  color: #111722;
  font-size: 16px;
}

.selected-fees {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
  color: #111722;
  font-size: 16px;
}

.selected-fee {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.selected-fee strong {
  min-width: 50px;
  height: 31px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  color: #111722;
  font-weight: 700;
}

.selected-fee button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  background: transparent;
  color: #8791ff;
  cursor: pointer;
}

.selected-fee svg {
  width: 18px;
  height: 18px;
}

.operator-select select {
  width: 100%;
  height: 100%;
  padding: 0 38px 0 13px;
  border: 0;
  outline: none;
  appearance: none;
  background: transparent;
  color: #111722;
  font-size: 16px;
}

.operator-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  width: 18px;
  height: 18px;
  color: #c7c7c7;
  transform: translateY(-50%);
  pointer-events: none;
}

.total-price {
  display: flex;
  align-items: baseline;
  gap: 15px;
  color: #111722;
  font-size: 18px;
}

.total-price strong {
  color: #ff0000;
  font-size: 22px;
  font-weight: 700;
}

.total-price em {
  color: #111722;
  font-style: normal;
}

.ranking-card {
  padding-top: 2px;
}

.ranking-card h2 {
  display: flex;
  align-items: center;
  gap: 21px;
  margin: 0;
  color: #111722;
  font-size: 27px;
  font-weight: 700;
}

.rank-icon {
  width: 28px;
  height: 28px;
  color: #ffc21a;
}

.rank-list {
  margin-top: 28px;
}

.rank-row {
  display: grid;
  grid-template-columns: 45px minmax(0, 1fr);
  align-items: center;
  min-height: 56px;
  margin-bottom: 15px;
}

.rank-index {
  color: #8791ff;
  font-size: 25px;
  font-style: italic;
  font-weight: 700;
}

.rank-box {
  height: 54px;
  display: grid;
  grid-template-columns: 101px minmax(0, 1fr) 54px;
  align-items: center;
  padding: 0 28px 0 23px;
  box-sizing: border-box;
  border: 1px solid #e2e5ff;
  border-radius: 5px;
}

.rank-code {
  color: #9aa3b5;
  font-size: 18px;
}

.rank-name {
  color: #111722;
  font-size: 19px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-box button {
  border: 0;
  background: transparent;
  color: #8791ff;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.38);
}

.dialog-panel {
  position: relative;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.dialog-close {
  position: absolute;
  top: 26px;
  right: 22px;
  border: 0;
  background: transparent;
  color: #d0d0d0;
  cursor: pointer;
}

.dialog-close svg {
  width: 26px;
  height: 26px;
}

.dialog-panel h3 {
  margin: 0;
  color: #111722;
  font-size: 22px;
  font-weight: 700;
}

.select-drug-dialog {
  width: 1206px;
  min-height: 560px;
  padding: 34px 32px 30px;
}

.drug-filter {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 27px;
  font-size: 18px;
}

.filter-select {
  width: 202px;
  height: 46px;
  display: flex;
  align-items: center;
  position: relative;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
}

.filter-select select {
  width: 100%;
  height: 100%;
  padding: 0 44px 0 21px;
  border: 0;
  outline: none;
  appearance: none;
  background: transparent;
  font-size: 16px;
}

.filter-select svg,
.filter-search svg {
  width: 23px;
  height: 23px;
}

.filter-select svg {
  position: absolute;
  right: 12px;
  color: #c7c7c7;
}

.filter-search {
  width: 493px;
  height: 46px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 66px;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  overflow: hidden;
}

.filter-search input {
  min-width: 0;
  padding: 0 15px;
  border: 0;
  outline: none;
  font-size: 16px;
}

.filter-search input::placeholder {
  color: #c7c7c7;
}

.filter-search button {
  border: 0;
  background: #636be8;
  color: #fff;
}

.select-table-wrap {
  max-height: 418px;
  margin-top: 30px;
  overflow-y: auto;
}

.select-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: auto;
}

.select-table thead {
  position: sticky;
  top: 0;
  z-index: 1;
}

.select-table th {
  height: 58px;
  background: #bbbef5;
  color: #111722;
  font-size: 18px;
  font-weight: 700;
  text-align: left;
}

.select-table th:first-child,
.select-table td:first-child {
  width: 48px;
  text-align: center;
}

.select-table td {
  height: 72px;
  border-bottom: 1px solid #eeeeee;
  color: #000;
  font-size: 16px;
}

.select-table strong {
  color: #636be8;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 18px;
  margin-top: 28px;
}

.plain-btn,
.primary-btn {
  min-width: 82px;
  height: 42px;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
}

.plain-btn {
  border: 1px solid #d0d0d0;
  background: #fff;
  color: #333;
}

.primary-btn {
  border: 0;
  background: #636be8;
  color: #fff;
}

.extra-dialog {
  width: 436px;
  min-height: 548px;
  padding: 28px 25px 26px;
}

.extra-dialog p {
  margin: 15px 0 22px;
  color: #8e95a5;
  font-size: 14px;
}

.extra-table {
  width: 100%;
  border-collapse: collapse;
}

.extra-table th {
  height: 42px;
  color: #111722;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
}

.extra-table td {
  height: 73px;
  border-top: 1px solid #eeeeee;
  color: #000;
  font-size: 16px;
  text-align: center;
}

.fee-input {
  width: 52px;
  height: 39px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  outline: none;
  font-size: 18px;
  text-align: center;
}

@media (max-width: 1280px) {
  .retail-shell {
    width: calc(100% - 32px);
  }

  .patient-form,
  .retail-main {
    grid-template-columns: 1fr;
  }
}
</style>
