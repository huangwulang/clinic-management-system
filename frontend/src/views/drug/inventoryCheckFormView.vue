<template>
  <div class="check-form-page">
    <section v-loading="loading" class="check-card">
      <header class="page-header">
        <h1>新增盘点</h1>
        <div class="header-actions">
          <button class="primary-btn" type="button" :disabled="submitting" @click="submit(false)">
            <DocumentChecked class="btn-icon" />
            <span>保存</span>
          </button>
          <button class="primary-btn" type="button" :disabled="submitting" @click="submit(true)">
            <CircleCheckFilled class="btn-icon" />
            <span>完成盘点</span>
          </button>
          <button class="outline-btn" type="button" @click="goBack">
            <Back class="btn-icon" />
            <span>返回</span>
          </button>
        </div>
      </header>

      <div class="divider"></div>

      <section class="filters-row">
        <label class="category-filter">
          <span>处方类别</span>
          <select v-model="category" @change="currentPage = 1">
            <option>全部</option>
            <option>西/成药</option>
            <option>中药</option>
          </select>
        </label>
        <label class="search-filter">
          <input v-model.trim="keyword" type="text" placeholder="输入药品名称/编码/生产厂家" @keyup.enter="currentPage = 1" />
          <button type="button" aria-label="搜索" @click="currentPage = 1">
            <Search class="search-icon" />
          </button>
        </label>
      </section>

      <section class="info-panel">
        <div><span>盘点单号：</span><strong>{{ checkNo || "-" }}</strong></div>
        <div><span>盘点人：</span><strong>{{ maker || "-" }}</strong></div>
        <div><span>创建时间：</span><strong>{{ createdAt }}</strong></div>
      </section>

      <div class="table-wrap">
        <table class="check-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>药品编码</th>
              <th>药品名称</th>
              <th>货位号</th>
              <th>处方类别</th>
              <th>规格</th>
              <th>生产厂家</th>
              <th>当前库存数量</th>
              <th>盘点库存</th>
              <th>盘盈盘亏</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in pageRows" :key="row.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>{{ row.drugCode }}</td>
              <td class="left-cell">{{ row.drugName }}</td>
              <td>{{ row.locationNo || "-" }}</td>
              <td>{{ row.prescriptionCategory || "-" }}</td>
              <td>{{ row.specification || "-" }}</td>
              <td class="left-cell">{{ row.manufacturer || "-" }}</td>
              <td>{{ row.quantity }}{{ row.unit }}</td>
              <td>
                <div class="quantity-input">
                  <input v-model="row.checkQuantity" min="0" type="number" />
                  <span>{{ row.unit }}</span>
                </div>
              </td>
              <td :class="difference(row) < 0 ? 'loss-text' : difference(row) > 0 ? 'profit-text' : ''">
                {{ differenceText(row) }}
              </td>
              <td><input v-model.trim="row.remark" class="remark-input" type="text" placeholder="备注" /></td>
            </tr>
            <tr v-if="!loading && pageRows.length === 0">
              <td class="empty-cell" colspan="11">暂无库存数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <nav v-if="filteredRows.length" class="pagination" aria-label="分页">
        <button type="button" :disabled="currentPage === 1" @click="currentPage--">&lt;</button>
        <button
          v-for="page in totalPages"
          :key="page"
          type="button"
          :class="{ active: page === currentPage }"
          @click="currentPage = page"
        >
          {{ page }}
        </button>
        <button type="button" :disabled="currentPage === totalPages" @click="currentPage++">&gt;</button>
        <span>每页{{ pageSize }}条，共{{ filteredRows.length }}条</span>
      </nav>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { Back, CircleCheckFilled, DocumentChecked, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { authApi, inventoryApi, request, stockCheckOrderApi } from "@/api";

type CheckRow = {
  id: number | string;
  drugId?: number | string;
  drugCode: string;
  drugName: string;
  locationNo?: string;
  prescriptionCategory?: string;
  specification?: string;
  manufacturer?: string;
  quantity: number;
  unit: string;
  checkQuantity: string | number;
  remark: string;
};

const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const checkNo = ref("");
const maker = ref("");
const createdAt = ref("");
const category = ref("全部");
const keyword = ref("");
const rows = ref<CheckRow[]>([]);
const currentPage = ref(1);
const pageSize = 10;

const formatNow = () => {
  const date = new Date();
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

const filteredRows = computed(() => {
  const search = keyword.value.toLowerCase();
  return rows.value.filter((row) => {
    const matchesCategory = category.value === "全部" || row.prescriptionCategory === category.value;
    const matchesKeyword = !search || [row.drugCode, row.drugName, row.manufacturer]
      .some((value) => String(value || "").toLowerCase().includes(search));
    return matchesCategory && matchesKeyword;
  });
});
const totalPages = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize)));
const pageRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredRows.value.slice(start, start + pageSize);
});

watch([category, keyword], () => {
  currentPage.value = 1;
});

const parsedCheckQuantity = (row: CheckRow) =>
  row.checkQuantity === "" || row.checkQuantity === null ? null : Number(row.checkQuantity);
const difference = (row: CheckRow) => {
  const checked = parsedCheckQuantity(row);
  return checked === null || Number.isNaN(checked) ? 0 : checked - Number(row.quantity || 0);
};
const differenceText = (row: CheckRow) => {
  const checked = parsedCheckQuantity(row);
  if (checked === null || Number.isNaN(checked)) return "-";
  const value = difference(row);
  return `${value > 0 ? "+" : ""}${value}${row.unit}`;
};

const loadData = async () => {
  loading.value = true;
  createdAt.value = formatNow();

  try {
    const inventoryResponse: any = await inventoryApi.search({ page: 1, size: 200 });
    rows.value = (inventoryResponse?.data?.records || []).map((item: any) => ({
      id: item.id,
      drugId: item.drugId,
      drugCode: item.drugCode || "",
      drugName: item.drugName || "",
      locationNo: item.locationNo || "",
      prescriptionCategory: item.prescriptionCategory || item.category || "",
      specification: item.specification || "",
      manufacturer: item.manufacturer || "",
      quantity: Number(item.quantity || 0),
      unit: item.unit || "",
      checkQuantity: "",
      remark: "",
    }));
  } catch {
    rows.value = [];
    ElMessage.error("库存数据加载失败，请检查后端服务");
  }

  try {
    const response: any = await stockCheckOrderApi.nextNo();
    checkNo.value = response?.data?.checkNo || "";
  } catch {
    const date = new Date();
    const pad = (value: number, length = 2) => String(value).padStart(length, "0");
    checkNo.value = `PD${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}${pad(date.getMilliseconds(), 3)}`;
  }

  try {
    const response: any = await authApi.me();
    maker.value = response?.data?.name || response?.data?.username || "";
  } catch {
    maker.value = localStorage.getItem("rememberedAccount") || "admin";
  } finally {
    loading.value = false;
  }
};

const submit = async (complete: boolean) => {
  if (!rows.value.length) {
    ElMessage.warning("暂无可盘点的库存数据");
    return;
  }
  if (complete && rows.value.some((row) => parsedCheckQuantity(row) === null)) {
    ElMessage.warning("完成盘点前请填写全部药品的盘点库存");
    return;
  }
  if (rows.value.some((row) => {
    const value = parsedCheckQuantity(row);
    return value !== null && (!Number.isInteger(value) || value < 0);
  })) {
    ElMessage.warning("盘点库存必须为不小于0的整数");
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      checkNo: checkNo.value,
      maker: maker.value,
      remark: complete ? "完成库存盘点" : "库存盘点草稿",
      items: rows.value.map((row) => ({
        inventoryId: row.id,
        drugId: row.drugId,
        checkQuantity: parsedCheckQuantity(row),
        remark: row.remark,
      })),
    };
    const response: any = await request.post(
      complete ? "/stock-check-orders/complete" : "/stock-check-orders/draft",
      payload,
    );
    ElMessage.success(complete ? "库存盘点已完成" : "盘点草稿已保存");
    router.push({
      path: "/drugs/stock-check/detail",
      query: { id: String(response?.data?.id) },
    });
  } catch (error: any) {
    if (!error?.response) {
      ElMessage.error(error?.message || "盘点保存失败，请稍后重试");
    }
  } finally {
    submitting.value = false;
  }
};

const goBack = () => router.push("/drug/inventory-check");

onMounted(loadData);
</script>

<style scoped>
.check-form-page { min-height: 100%; padding: 18px 0 55px; }
.check-card {
  width: min(1650px, calc(100% - 80px)); min-height: 860px; margin: 0 auto;
  padding: 34px 30px 42px; box-sizing: border-box; background: #fff;
  border-radius: 6px; box-shadow: 0 1px 9px rgba(23, 31, 56, .07);
}
.page-header, .header-actions, .filters-row, .quantity-input, .pagination {
  display: flex; align-items: center;
}
.page-header { justify-content: space-between; }
.page-header h1 {
  position: relative; margin: 0; padding-left: 16px; font-size: 23px; color: #1d2430;
}
.page-header h1::before {
  position: absolute; left: 0; top: 1px; width: 6px; height: 25px;
  content: ""; background: #6570f2;
}
.header-actions { gap: 16px; }
.primary-btn, .outline-btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 10px;
  min-width: 130px; height: 46px; padding: 0 22px; border-radius: 5px;
  font-size: 17px; font-weight: 700; cursor: pointer;
}
.primary-btn { border: 1px solid #636be8; background: #636be8; color: #fff; }
.outline-btn { border: 1px solid #636be8; background: #fff; color: #636be8; }
.primary-btn:disabled { opacity: .65; cursor: not-allowed; }
.btn-icon { width: 23px; height: 23px; }
.divider { height: 1px; margin: 28px 0; background: #eee; }
.filters-row { gap: 25px; margin-bottom: 28px; }
.category-filter { display: flex; align-items: center; gap: 12px; white-space: nowrap; font-size: 17px; }
.category-filter select { width: 180px; height: 44px; padding: 0 14px; border: 2px solid #d3d3d3; border-radius: 4px; font-size: 16px; background: #fff; }
.search-filter { display: grid; grid-template-columns: minmax(280px, 420px) 62px; height: 44px; border: 2px solid #d3d3d3; border-radius: 4px; overflow: hidden; }
.search-filter input { min-width: 0; padding: 0 15px; border: 0; outline: 0; font-size: 16px; }
.search-filter button { border: 0; background: #636be8; color: #fff; cursor: pointer; }
.search-icon { width: 27px; height: 27px; }
.info-panel {
  display: grid; grid-template-columns: repeat(3, 1fr); align-items: center;
  min-height: 88px; margin-bottom: 15px; padding: 0 26px; box-sizing: border-box;
  background: #f0f1ff; font-size: 17px;
}
.info-panel strong { font-weight: 400; }
.table-wrap { width: 100%; overflow-x: auto; }
.check-table { width: 100%; min-width: 1400px; border-collapse: collapse; table-layout: fixed; }
.check-table th { height: 66px; background: #bfc2f6; font-size: 16px; white-space: nowrap; }
.check-table td { height: 78px; border-bottom: 1px solid #eee; text-align: center; font-size: 15px; white-space: nowrap; }
.check-table th:nth-child(1) { width: 65px; }
.check-table th:nth-child(2) { width: 100px; }
.check-table th:nth-child(3) { width: 150px; }
.check-table th:nth-child(4) { width: 90px; }
.check-table th:nth-child(5) { width: 100px; }
.check-table th:nth-child(6) { width: 120px; }
.check-table th:nth-child(7) { width: 220px; }
.check-table th:nth-child(8) { width: 125px; }
.check-table th:nth-child(9) { width: 125px; }
.check-table th:nth-child(10) { width: 105px; }
.check-table th:nth-child(11) { width: 175px; }
.left-cell { text-align: left !important; overflow: hidden; text-overflow: ellipsis; }
.quantity-input { justify-content: center; gap: 8px; }
.quantity-input input { width: 65px; height: 38px; box-sizing: border-box; border: 2px solid #d3d3d3; border-radius: 4px; text-align: center; font-size: 15px; }
.remark-input { width: 145px; height: 38px; padding: 0 10px; box-sizing: border-box; border: 2px solid #d3d3d3; border-radius: 4px; font-size: 15px; }
.loss-text { color: #f04444; }
.profit-text { color: #22a06b; }
.empty-cell { height: 150px !important; color: #909399; }
.pagination { justify-content: flex-end; gap: 10px; margin-top: 28px; }
.pagination button { min-width: 35px; height: 35px; border: 1px solid #bfc0c4; border-radius: 50%; background: #fff; cursor: pointer; }
.pagination button.active { border-color: #636be8; background: #636be8; color: #fff; }
.pagination button:disabled { opacity: .45; cursor: not-allowed; }
@media (max-width: 1280px) {
  .check-card { width: calc(100% - 30px); padding: 26px 18px 38px; }
  .page-header, .header-actions, .filters-row { flex-wrap: wrap; }
  .info-panel { grid-template-columns: 1fr 1fr; gap: 18px; padding: 20px; }
}
</style>
