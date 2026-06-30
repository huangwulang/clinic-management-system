<template>
  <div class="stock-in-page">
    <section class="stock-card">
      <header class="stock-header">
        <h1>{{ pageTitle }}</h1>
        <div class="header-actions">
          <button
            v-if="isEditMode"
            class="action-btn approve"
            type="button"
            :disabled="auditLoading"
            @click="handleAudit(true)"
          >
            <CircleCheckFilled class="action-icon" />
            <span>审核通过</span>
          </button>
          <button
            v-if="isEditMode"
            class="action-btn reject"
            type="button"
            :disabled="auditLoading"
            @click="handleAudit(false)"
          >
            <CircleCloseFilled class="action-icon" />
            <span>审核不通过</span>
          </button>
          <button
            v-if="!isEditMode"
            class="action-btn primary"
            type="button"
            @click="saveOrder(false)"
          >
            <Tickets class="action-icon" />
            <span>提交审核</span>
          </button>
          <button
            v-if="!isEditMode"
            class="action-btn primary"
            type="button"
            @click="saveOrder(true)"
          >
            <Shop class="action-icon" />
            <span>直接入库</span>
          </button>
          <button class="action-btn outline" type="button" @click="goBack">
            <Back class="action-icon" />
            <span>返回</span>
          </button>
        </div>
      </header>

      <section class="form-panel">
        <label class="field">
          <span>入库单号</span>
          <input v-model="formData.orderNo" readonly />
        </label>
        <div class="field">
          <span>入库日期</span>
          <el-date-picker
            v-model="formData.stockDate"
            class="date-input"
            type="date"
            placeholder="请选择入库日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
          />
        </div>
        <label class="field">
          <span>入库人员</span>
          <select v-model="formData.operatorId">
            <option value="">请选择</option>
            <option
              v-for="employee in employeeOptions"
              :key="employee.id"
              :value="String(employee.id)"
            >
              {{ employee.name }}
            </option>
          </select>
        </label>
        <label class="field">
          <span>入库类型</span>
          <select v-model="formData.stockType">
            <option
              v-for="type in stockTypeOptions"
              :key="type.value"
              :value="type.value"
            >
              {{ type.label }}
            </option>
          </select>
        </label>
        <label class="field">
          <span>供应商</span>
          <select v-model="formData.supplierId">
            <option value="">请选择</option>
            <option
              v-for="supplier in supplierOptions"
              :key="supplier.id"
              :value="String(supplier.id)"
            >
              {{ supplier.name }}
            </option>
          </select>
        </label>
        <div class="field">
          <span>制单日期</span>
          <el-date-picker
            v-model="formData.documentDate"
            class="date-input"
            type="date"
            placeholder="请选择制单日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
          />
        </div>
        <label class="field">
          <span>制单人员</span>
          <input v-model="formData.maker" readonly />
        </label>
        <label class="field">
          <span>备注</span>
          <input v-model="formData.remark" />
        </label>
      </section>

      <div class="table-wrap">
        <table class="medicine-table">
          <thead>
            <tr>
              <th class="check-cell"><input type="checkbox" /></th>
              <th>序号</th>
              <th>药品编码</th>
              <th>药品名称</th>
              <th>生产厂家</th>
              <th>数量</th>
              <th>单位</th>
              <th>采购价<br />（元）</th>
              <th>零售价<br />（元）</th>
              <th>批号</th>
              <th>药品有效期</th>
              <th>采购金额<br />（元）</th>
              <th>零售金额<br />（元）</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in selectedMedicines" :key="item.id">
              <td><input type="checkbox" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.factory }}</td>
              <td>
                <input
                  v-model.number="item.quantity"
                  class="detail-input quantity-input"
                  type="number"
                  min="1"
                  step="1"
                />
              </td>
              <td>{{ item.unit }}</td>
              <td>{{ formatAmount(item.purchasePrice) }}</td>
              <td>{{ formatAmount(item.retailPrice) }}</td>
              <td>
                <input v-model.trim="item.batchNo" class="detail-input" type="text" />
              </td>
              <td>
                <input v-model="item.expireDate" class="detail-input date-detail-input" type="date" />
              </td>
              <td>{{ formatAmount(calculateLineAmount(item.quantity, item.purchasePrice)) }}</td>
              <td>{{ formatAmount(calculateLineAmount(item.quantity, item.retailPrice)) }}</td>
            </tr>
            <tr v-if="selectedMedicines.length === 0">
              <td class="medicine-empty" colspan="13">请点击“添加药品”选择入库药品</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="add-medicine">
        <button type="button" @click="openMedicineDialog">
          <CirclePlusFilled class="plus-icon" />
          <span>添加药品</span>
        </button>
      </div>

      <footer class="totals">
        <span>采购金额合计：</span>
        <strong>{{ formatAmount(purchaseAmountTotal) }}</strong>
        <span>元</span>
        <span class="gap">零售金额合计：</span>
        <strong>{{ formatAmount(retailAmountTotal) }}</strong>
        <span>元</span>
      </footer>
    </section>

    <div v-if="showMedicineDialog" class="dialog-mask">
      <section class="medicine-dialog">
        <header class="dialog-header">
          <h2>选择药品</h2>
          <button type="button" aria-label="关闭" @click="showMedicineDialog = false">
            <Close class="close-icon" />
          </button>
        </header>

        <section class="dialog-filters">
          <label class="category-filter">
            <span>处方类别</span>
            <select v-model="medicineFilters.category" @change="handleMedicineFilterChange">
              <option value="">全部</option>
              <option value="西/成药">西/成药</option>
              <option value="中药">中药</option>
            </select>
          </label>
          <label class="dialog-search">
            <input
              v-model.trim="medicineFilters.keyword"
              placeholder="输入药品编码/药品名称"
              type="text"
              @keyup.enter="handleMedicineSearch"
            />
            <button type="button" aria-label="搜索" @click="handleMedicineSearch">
              <Search class="search-icon" />
            </button>
          </label>
        </section>

        <div class="dialog-table-wrap">
          <table class="dialog-table">
            <thead>
              <tr>
                <th>
                  <input
                    type="checkbox"
                    :checked="isCurrentMedicinePageSelected"
                    @change="toggleCurrentMedicinePage"
                  />
                </th>
                <th>序号</th>
                <th>药品编码</th>
                <th>药品名称</th>
                <th>收费类别</th>
                <th>规格</th>
                <th>厂家</th>
                <th>库存</th>
                <th>采购价格（元）</th>
                <th>零售价格元）</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="medicineLoading">
                <td class="dialog-status" colspan="10">正在加载药品数据...</td>
              </tr>
              <tr v-for="item in medicineOptions" :key="item.id">
                <td>
                  <input
                    type="checkbox"
                    :checked="dialogSelectedMedicineIds.includes(String(item.id))"
                    @change="toggleDialogMedicine(item)"
                  />
                </td>
                <td>{{ item.index }}</td>
                <td>{{ item.code }}</td>
                <td>{{ item.name }}</td>
                <td>{{ item.category }}</td>
                <td>{{ item.spec }}</td>
                <td>{{ item.factory }}</td>
                <td class="stock-text">{{ item.stock }}</td>
                <td>{{ item.purchasePrice }}</td>
                <td>{{ item.retailPrice }}</td>
              </tr>
              <tr v-if="!medicineLoading && medicineOptions.length === 0">
                <td class="dialog-status" colspan="10">暂无符合条件的药品</td>
              </tr>
            </tbody>
          </table>
        </div>

        <footer class="dialog-footer">
          <el-pagination
            class="dialog-pagination"
            layout="prev, pager, next, total"
            :current-page="medicinePage"
            :page-size="medicinePageSize"
            :total="medicineTotal"
            @current-change="handleMedicinePageChange"
          />
          <button class="cancel-btn" type="button" @click="showMedicineDialog = false">取消</button>
          <button class="confirm-btn" type="button" @click="confirmMedicineSelection">确定</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Back,
  CircleCheckFilled,
  CircleCloseFilled,
  CirclePlusFilled,
  Close,
  Search,
  Shop,
  Tickets,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import {
  authApi,
  drugApi,
  inventoryApi,
  staffApi,
  stockInOrderApi,
  supplierApi,
} from "@/api";
import { readNavigationState } from "@/utils/navigation";

type SelectOption = {
  id: number | string;
  name: string;
};

type MedicineOption = {
  id: number | string;
  index: number;
  code: string;
  name: string;
  category: string;
  spec: string;
  factory: string;
  stock: string;
  unit: string;
  purchasePrice: string | number;
  retailPrice: string | number;
};

type SelectedMedicine = MedicineOption & {
  quantity: number;
  batchNo: string;
  expireDate: string;
};

const route = useRoute();
const router = useRouter();
const showMedicineDialog = ref(false);
const medicineLoading = ref(false);
const auditLoading = ref(false);
const medicineOptions = ref<MedicineOption[]>([]);
const selectedMedicines = ref<SelectedMedicine[]>([]);
const dialogSelectedMedicines = ref<Record<string, MedicineOption>>({});
const dialogSelectedMedicineIds = ref<string[]>([]);
const medicinePage = ref(1);
const medicinePageSize = 10;
const medicineTotal = ref(0);
const medicineFilters = reactive({
  category: "",
  keyword: "",
});
const sourceOrderId = readNavigationState(route);
const navigationMode = readNavigationState(route, "mode");
const isEditMode = navigationMode === "edit";
const isRestockMode = navigationMode === "restock";
const employeeOptions = ref<SelectOption[]>([]);
const supplierOptions = ref<SelectOption[]>([]);
const stockTypeOptions = [
  { label: "采购入库", value: "PURCHASE_IN" },
  { label: "退货入库", value: "RETURN_IN" },
  { label: "其他入库", value: "OTHER_IN" },
];
const generateStockInOrderNo = () =>
  `${Math.floor(Math.random() * 9) + 1}${Array.from(
    { length: 11 },
    () => Math.floor(Math.random() * 10),
  ).join("")}`;

const formData = reactive({
  orderNo: generateStockInOrderNo(),
  stockDate: "",
  documentDate: "",
  operatorId: "",
  stockType: "PURCHASE_IN",
  supplierId: "",
  maker: "",
  remark: "",
});

const pageTitle = computed(() =>
  isEditMode ? "编辑入库信息" : isRestockMode ? "再次入库" : "新增入库信息",
);

const normalizeQuantity = (quantity: unknown) => {
  const value = Number(quantity);
  return Number.isFinite(value) && value > 0 ? value : 0;
};

const calculateLineAmount = (quantity: unknown, price: unknown) =>
  normalizeQuantity(quantity) * (Number(price) || 0);

const purchaseAmountTotal = computed(() =>
  selectedMedicines.value.reduce(
    (total, item) => total + calculateLineAmount(item.quantity, item.purchasePrice),
    0,
  ),
);

const retailAmountTotal = computed(() =>
  selectedMedicines.value.reduce(
    (total, item) => total + calculateLineAmount(item.quantity, item.retailPrice),
    0,
  ),
);

const formatAmount = (amount: unknown) => (Number(amount) || 0).toFixed(2);

const isCurrentMedicinePageSelected = computed(
  () =>
    medicineOptions.value.length > 0 &&
    medicineOptions.value.every((item) =>
      dialogSelectedMedicineIds.value.includes(String(item.id)),
    ),
);

const fetchMedicineOptions = async () => {
  medicineLoading.value = true;
  try {
    const [drugResponse, inventoryResponse]: any[] = await Promise.all([
      drugApi.search({
        page: medicinePage.value,
        size: medicinePageSize,
        keyword: medicineFilters.keyword || undefined,
        category: medicineFilters.category || undefined,
      }),
      inventoryApi.page({ page: 1, size: 200 }),
    ]);
    const inventoryQuantities = new Map<string, number>();
    for (const item of inventoryResponse?.data?.records || []) {
      const keys = [item.drugId, item.drugCode]
        .filter((value) => value !== null && value !== undefined && value !== "")
        .map(String);
      for (const key of keys) {
        inventoryQuantities.set(
          key,
          (inventoryQuantities.get(key) || 0) + Number(item.quantity || 0),
        );
      }
    }

    const records = drugResponse?.data?.records || [];
    medicineTotal.value = Number(drugResponse?.data?.total || 0);
    medicineOptions.value = records.map((item: any, index: number) => {
      const stock =
        inventoryQuantities.get(String(item.id)) ??
        inventoryQuantities.get(String(item.drugCode)) ??
        0;
      return {
        id: item.id,
        index: (medicinePage.value - 1) * medicinePageSize + index + 1,
        code: item.drugCode || "",
        name: item.name || "",
        category: item.category || "",
        spec: item.specification || "",
        factory: item.manufacturer || "",
        stock: `${stock}${item.unit || item.packUnit || ""}`,
        unit: item.unit || item.packUnit || "",
        purchasePrice: item.purchasePrice ?? "",
        retailPrice: item.sellPrice ?? "",
      };
    });
  } catch {
    medicineOptions.value = [];
    medicineTotal.value = 0;
    ElMessage.error("药品数据加载失败，请稍后重试");
  } finally {
    medicineLoading.value = false;
  }
};

const openMedicineDialog = async () => {
  dialogSelectedMedicines.value = Object.fromEntries(
    selectedMedicines.value.map((item) => [String(item.id), { ...item }]),
  );
  dialogSelectedMedicineIds.value = selectedMedicines.value.map((item) => String(item.id));
  showMedicineDialog.value = true;
  medicinePage.value = 1;
  await fetchMedicineOptions();
};

const toggleDialogMedicine = (item: MedicineOption) => {
  const id = String(item.id);
  if (dialogSelectedMedicineIds.value.includes(id)) {
    dialogSelectedMedicineIds.value = dialogSelectedMedicineIds.value.filter(
      (selectedId) => selectedId !== id,
    );
    delete dialogSelectedMedicines.value[id];
    return;
  }
  dialogSelectedMedicineIds.value = [...dialogSelectedMedicineIds.value, id];
  dialogSelectedMedicines.value[id] = item;
};

const toggleCurrentMedicinePage = () => {
  const shouldSelect = !isCurrentMedicinePageSelected.value;
  for (const item of medicineOptions.value) {
    const id = String(item.id);
    const isSelected = dialogSelectedMedicineIds.value.includes(id);
    if (shouldSelect && !isSelected) {
      dialogSelectedMedicineIds.value.push(id);
      dialogSelectedMedicines.value[id] = item;
    } else if (!shouldSelect && isSelected) {
      dialogSelectedMedicineIds.value = dialogSelectedMedicineIds.value.filter(
        (selectedId) => selectedId !== id,
      );
      delete dialogSelectedMedicines.value[id];
    }
  }
};

const confirmMedicineSelection = () => {
  const existingMedicines = new Map(
    selectedMedicines.value.map((item) => [String(item.id), item]),
  );
  selectedMedicines.value = dialogSelectedMedicineIds.value
    .map((id) => {
      const option = dialogSelectedMedicines.value[id];
      if (!option) return null;
      const existing = existingMedicines.get(id);
      return existing
        ? { ...existing, ...option }
        : {
            ...option,
            quantity: 1,
            batchNo: "",
            expireDate: "",
          };
    })
    .filter((item): item is SelectedMedicine => Boolean(item));
  showMedicineDialog.value = false;
};

const handleMedicineFilterChange = async () => {
  medicinePage.value = 1;
  await fetchMedicineOptions();
};

const handleMedicineSearch = async () => {
  medicinePage.value = 1;
  await fetchMedicineOptions();
};

const handleMedicinePageChange = async (page: number) => {
  medicinePage.value = page;
  await fetchMedicineOptions();
};

const goBack = () => {
  router.push("/drug/stock-in");
};


onMounted(async () => {
  const [employeeResponse, supplierResponse, userResponse, orderResponse]: any[] = await Promise.all([
    staffApi.page({ page: 1, size: 200 }),
    supplierApi.page({ page: 1, size: 200 }),
    authApi.me(),
    sourceOrderId ? stockInOrderApi.get(sourceOrderId) : Promise.resolve(null),
  ]);

  employeeOptions.value = (employeeResponse?.data?.records || []).map((item: any) => ({
    id: item.id,
    name: item.name || item.jobNo || String(item.id),
  }));
  supplierOptions.value = (supplierResponse?.data?.records || []).map((item: any) => ({
    id: item.id,
    name: item.name || item.supplierCode || String(item.id),
  }));
  formData.maker = userResponse?.data?.name || userResponse?.data?.username || "";

  const order = orderResponse?.data;
  if (!order) return;

  const remark = parseOrderRemark(order.remark);
  if (isEditMode) {
    formData.orderNo = String(order.orderNo || formData.orderNo);
  }
  formData.stockDate = remark.stockDate || "";
  formData.documentDate = remark.documentDate || "";
  formData.operatorId = String(
    employeeOptions.value.find((item) => item.name === order.operator)?.id || "",
  );
  formData.stockType = stockTypeOptions.some((item) => item.value === order.stockType)
    ? order.stockType
    : "PURCHASE_IN";
  formData.supplierId = String(order.supplierId || "");
  if (isEditMode) {
    formData.maker = order.maker || formData.maker;
  }
  formData.remark = remark.remark || "";
  selectedMedicines.value = Array.isArray(remark.medicines)
    ? remark.medicines.map((item: any, index: number) => ({
        id: item.id ?? item.drugId ?? item.code ?? `medicine-${index}`,
        index: index + 1,
        code: item.code || item.drugCode || "",
        name: item.name || item.drugName || "",
        category: item.category || "",
        spec: item.spec || item.specification || "",
        factory: item.factory || item.manufacturer || "",
        stock: item.stock || "",
        unit: item.unit || "",
        purchasePrice: item.purchasePrice ?? 0,
        retailPrice: item.retailPrice ?? item.sellPrice ?? 0,
        quantity: normalizeQuantity(item.quantity) || 1,
        batchNo: item.batchNo || "",
        expireDate: item.expireDate || "",
      }))
    : [];
});

const buildOrderPayload = () => {
  const operator = employeeOptions.value.find(
    (item) => String(item.id) === formData.operatorId,
  );
  const supplier = supplierOptions.value.find(
    (item) => String(item.id) === formData.supplierId,
  );
  if (!operator) {
    ElMessage.warning("请选择入库人员");
    return null;
  }
  if (!supplier) {
    ElMessage.warning("请选择供应商");
    return null;
  }
  if (selectedMedicines.value.length === 0) {
    ElMessage.warning("请至少选择一种入库药品");
    return null;
  }
  if (selectedMedicines.value.some((item) => normalizeQuantity(item.quantity) <= 0)) {
    ElMessage.warning("药品入库数量必须大于 0");
    return null;
  }

  return {
    orderNo: formData.orderNo,
    direction: "IN",
    stockType: formData.stockType,
    supplierId: Number(supplier.id),
    supplierName: supplier.name,
    maker: formData.maker,
    purchaseAmount: purchaseAmountTotal.value,
    retailAmount: retailAmountTotal.value,
    auditStatus: "PENDING",
    operator: operator.name,
    remark: JSON.stringify({
      stockDate: formData.stockDate,
      documentDate: formData.documentDate,
      remark: formData.remark,
      medicines: selectedMedicines.value,
    }),
  };
};

const saveOrder = async (submit: boolean) => {
  const payload = buildOrderPayload();
  if (!payload) return;
  const response: any = isEditMode && sourceOrderId
    ? await stockInOrderApi.update(sourceOrderId, payload)
    : await stockInOrderApi.create(payload);
  const id = response?.data?.id || (isEditMode ? sourceOrderId : "");
  if (submit && id) await stockInOrderApi.submit(String(id));
  ElMessage.success(submit ? "单据已保存并提交审核" : "单据保存成功");
  router.push("/drugs/stock-in");
};

const handleAudit = async (approved: boolean) => {
  if (!sourceOrderId || auditLoading.value) return;
  const payload = buildOrderPayload();
  if (!payload) return;

  auditLoading.value = true;
  try {
    await stockInOrderApi.update(sourceOrderId, payload);
    const response: any = await stockInOrderApi.audit(sourceOrderId, { approved });
    const expectedStatus = approved ? "AUDIT_APPROVED" : "AUDIT_REJECTED";
    const actualStatus = String(response?.data?.auditStatus || "").toUpperCase();
    if (actualStatus !== expectedStatus) {
      throw new Error(`审核状态更新失败：${actualStatus || "接口未返回状态"}`);
    }
    ElMessage.success(approved ? "审核通过" : "审核不通过");
    router.push("/drugs/stock-in");
  } catch (error: any) {
    ElMessage.error(error?.message || "审核状态更新失败，请稍后重试");
  } finally {
    auditLoading.value = false;
  }
};

const parseOrderRemark = (remark: unknown): Record<string, any> => {
  if (typeof remark !== "string" || !remark) return {};
  try {
    return JSON.parse(remark);
  } catch {
    return { remark };
  }
};
</script>

<style scoped>
.stock-in-page {
  min-height: 100%;
  padding: 16px 0 58px;
}

.stock-card {
  width: min(1604px, calc(100% - 96px));
  min-height: 876px;
  margin: 0 auto;
  padding: 32px 31px 46px;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
}

.stock-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 32px;
}

.stock-header h1 {
  position: relative;
  margin: 0;
  padding-left: 16px;
  color: #1d2430;
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.stock-header h1::before {
  position: absolute;
  left: 0;
  top: 1px;
  width: 6px;
  height: 24px;
  content: "";
  background: #6570f2;
}

.header-actions {
  display: inline-flex;
  align-items: center;
  gap: 22px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-width: 145px;
  height: 46px;
  padding: 0 19px;
  border: 1px solid #636be8;
  border-radius: 5px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.action-btn.primary {
  background: #636be8;
  color: #fff;
}

.action-btn.approve {
  background: #8589ed;
  color: #fff;
}

.action-btn.reject {
  border-color: #22c997;
  background: #22c997;
  color: #fff;
}

.action-btn.outline {
  background: #fff;
  color: #636be8;
}

.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.action-icon {
  width: 25px;
  height: 25px;
}

.form-panel {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  column-gap: 46px;
  row-gap: 30px;
  padding: 35px 16px 35px;
  background: #f0f1ff;
}

.field {
  display: grid;
  gap: 11px;
  min-width: 0;
  color: #1d2430;
  font-size: 16px;
  font-weight: 700;
}

.field input,
.field select,
.date-input {
  width: 100%;
  height: 56px;
  box-sizing: border-box;
  border: 2px solid #d2d2d2;
  border-radius: 4px;
  background: #fff;
  color: #1b2230;
  font-size: 18px;
  font-weight: 400;
}

.field input {
  padding: 0 22px;
}

.field select {
  padding: 0 20px;
  appearance: none;
  background-image:
    linear-gradient(45deg, transparent 50%, #d5d5d5 50%),
    linear-gradient(135deg, #d5d5d5 50%, transparent 50%);
  background-position:
    calc(100% - 22px) 24px,
    calc(100% - 14px) 24px;
  background-size: 9px 9px;
  background-repeat: no-repeat;
}

.date-input {
  position: relative;
}

.date-input input {
  border: 0;
}

.date-icon {
  position: absolute;
  right: 15px;
  top: 17px;
  width: 22px;
  height: 22px;
  color: #c9c9c9;
}

.table-wrap {
  margin-top: 22px;
  overflow-x: auto;
}

.medicine-table {
  width: 100%;
  min-width: 1380px;
  border-collapse: collapse;
  table-layout: fixed;
}

.medicine-table th {
  height: 72px;
  background: #bfc2f6;
  color: #03060b;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
  text-align: center;
}

.medicine-table td {
  height: 58px;
  padding: 6px 4px;
  border-bottom: 1px solid #ececf4;
  color: #1d2430;
  font-size: 15px;
  text-align: center;
}

.medicine-table .medicine-empty {
  height: 100px;
  color: #909399;
}

.detail-input {
  width: 100%;
  height: 34px;
  box-sizing: border-box;
  padding: 0 6px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  text-align: center;
}

.quantity-input {
  min-width: 58px;
}

.date-detail-input {
  min-width: 132px;
}

.medicine-table th:nth-child(1) {
  width: 42px;
}

.medicine-table th:nth-child(2) {
  width: 55px;
}

.medicine-table th:nth-child(3),
.medicine-table th:nth-child(4) {
  width: 105px;
}

.medicine-table th:nth-child(5) {
  width: 210px;
}

.medicine-table th:nth-child(6),
.medicine-table th:nth-child(7) {
  width: 75px;
}

.medicine-table th:nth-child(8),
.medicine-table th:nth-child(9) {
  width: 90px;
}

.medicine-table th:nth-child(10) {
  width: 145px;
}

.medicine-table th:nth-child(11) {
  width: 170px;
}

.medicine-table th:nth-child(12),
.medicine-table th:nth-child(13) {
  width: 118px;
}

.check-cell input {
  width: 14px;
  height: 14px;
}

.add-medicine {
  display: flex;
  justify-content: center;
  padding-top: 66px;
}

.add-medicine button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  width: 195px;
  height: 58px;
  border: 0;
  border-radius: 5px;
  background: #27cc93;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.plus-icon {
  width: 28px;
  height: 28px;
}

.totals {
  display: flex;
  align-items: center;
  margin-top: 36px;
  color: #1d2430;
  font-size: 22px;
  line-height: 1;
}

.totals strong {
  margin: 0 18px 0 5px;
  color: #f00;
  font-size: 22px;
  letter-spacing: 1px;
}

.totals .gap {
  margin-left: 36px;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.39);
}

.medicine-dialog {
  width: min(1210px, calc(100vw - 120px));
  height: 664px;
  box-sizing: border-box;
  padding: 23px 32px 25px;
  overflow: hidden;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.24);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 26px;
}

.dialog-header h2 {
  margin: 0;
  color: #1d2430;
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
}

.dialog-header button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #cfcfcf;
  cursor: pointer;
}

.close-icon {
  width: 28px;
  height: 28px;
}

.dialog-filters {
  display: flex;
  align-items: center;
  gap: 23px;
  margin-bottom: 17px;
}

.category-filter {
  display: inline-flex;
  align-items: center;
  gap: 15px;
  color: #1d2430;
  font-size: 18px;
  white-space: nowrap;
}

.category-filter select {
  width: 194px;
  height: 45px;
  box-sizing: border-box;
  padding: 0 49px 0 20px;
  border: 2px solid #d5d5d5;
  border-radius: 4px;
  appearance: none;
  background:
    linear-gradient(45deg, transparent 50%, #d4d4d4 50%) calc(100% - 24px) 18px / 9px 9px no-repeat,
    linear-gradient(135deg, #d4d4d4 50%, transparent 50%) calc(100% - 16px) 18px / 9px 9px no-repeat,
    #fff;
  color: #1d2430;
  font-size: 18px;
}

.dialog-search {
  display: grid;
  grid-template-columns: 420px 63px;
  height: 45px;
  overflow: hidden;
  border: 2px solid #d5d5d5;
  border-radius: 4px;
  background: #fff;
}

.dialog-search input {
  min-width: 0;
  padding: 0 14px;
  border: 0;
  outline: none;
  color: #1d2430;
  font-size: 18px;
}

.dialog-search input::placeholder {
  color: #c5c5c5;
}

.dialog-search button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  background: #636be8;
  color: #fff;
  cursor: pointer;
}

.search-icon {
  width: 28px;
  height: 28px;
}

.dialog-table-wrap {
  height: 456px;
  overflow-y: auto;
  overflow-x: hidden;
}

.dialog-table-wrap::-webkit-scrollbar {
  width: 17px;
}

.dialog-table-wrap::-webkit-scrollbar-track {
  background: #fff;
}

.dialog-table-wrap::-webkit-scrollbar-thumb {
  border: 4px solid #fff;
  border-radius: 12px;
  background: #888;
}

.dialog-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.dialog-table th {
  height: 58px;
  background: #bfc2f6;
  color: #03060b;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
}

.dialog-table td {
  height: 72px;
  border-bottom: 1px solid #eee;
  color: #05070c;
  font-size: 18px;
  text-align: center;
  white-space: nowrap;
}

.dialog-table th:nth-child(1),
.dialog-table td:nth-child(1) {
  width: 42px;
}

.dialog-table th:nth-child(2),
.dialog-table td:nth-child(2) {
  width: 56px;
}

.dialog-table th:nth-child(3),
.dialog-table td:nth-child(3) {
  width: 98px;
}

.dialog-table th:nth-child(4),
.dialog-table td:nth-child(4) {
  width: 168px;
  text-align: left;
}

.dialog-table th:nth-child(5),
.dialog-table td:nth-child(5) {
  width: 90px;
}

.dialog-table th:nth-child(6),
.dialog-table td:nth-child(6) {
  width: 136px;
}

.dialog-table th:nth-child(7),
.dialog-table td:nth-child(7) {
  width: 235px;
  text-align: left;
}

.dialog-table th:nth-child(8),
.dialog-table td:nth-child(8) {
  width: 100px;
}

.dialog-table th:nth-child(9),
.dialog-table td:nth-child(9),
.dialog-table th:nth-child(10),
.dialog-table td:nth-child(10) {
  width: 112px;
}

.dialog-table input[type="checkbox"] {
  width: 15px;
  height: 15px;
}

.stock-text {
  color: #6670f2 !important;
  font-weight: 700;
}

.dialog-table .dialog-status {
  height: 120px;
  color: #909399;
  text-align: center;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 15px;
  padding-top: 21px;
}

.dialog-pagination {
  margin-right: auto;
}

.dialog-footer button {
  width: 82px;
  height: 42px;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.cancel-btn {
  border: 2px solid #dddddd;
  background: #fff;
  color: #1d2430;
}

.confirm-btn {
  border: 0;
  background: #636be8;
  color: #fff;
}

@media (max-width: 1280px) {
  .stock-card {
    width: calc(100% - 32px);
    padding: 24px 18px 36px;
  }

  .stock-header {
    flex-wrap: wrap;
  }

  .form-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .totals {
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 140px;
  }

  .medicine-dialog {
    width: calc(100vw - 32px);
  }
}
</style>
