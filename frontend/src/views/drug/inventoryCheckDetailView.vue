<template>
  <div class="check-detail-page">
    <section v-loading="loading" class="check-card">
      <header class="page-header">
        <h1>查看盘点详情</h1>
        <div class="header-actions">
          <button class="outline-btn" type="button" @click="exportDetail">
            <Upload class="btn-icon" />
            <span>导出</span>
          </button>
          <button class="outline-btn" type="button" @click="goBack">
            <Back class="btn-icon" />
            <span>返回</span>
          </button>
        </div>
      </header>
      <div class="divider"></div>

      <section class="info-panel">
        <div><span>盘点单号：</span><strong>{{ detail.checkNo || "-" }}</strong></div>
        <div><span>盘点人：</span><strong>{{ detail.maker || "-" }}</strong></div>
        <div><span>创建时间：</span><strong>{{ detail.createdAt || detail.checkDate || "-" }}</strong></div>
        <div><span>盘点状态：</span><strong>{{ statusText }}</strong></div>
      </section>

      <section class="filters-row">
        <label class="category-filter">
          <span>处方类别</span>
          <select v-model="category">
            <option>全部</option>
            <option>西/成药</option>
            <option>中药</option>
          </select>
        </label>
        <label class="search-filter">
          <input v-model.trim="keyword" type="text" placeholder="输入药品名称/编码/生产厂家" />
          <button type="button" aria-label="搜索"><Search class="search-icon" /></button>
        </label>
      </section>

      <div class="table-wrap">
        <table class="check-table">
          <thead>
            <tr>
              <th>序号</th><th>药品编码</th><th>药品名称</th><th>货位号</th>
              <th>处方类别</th><th>规格</th><th>生产厂家</th><th>当前库存数量</th>
              <th>盘点库存</th><th>盘盈盘亏</th><th>备注</th>
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
              <td>{{ quantityText(row.systemQuantity, row.unit) }}</td>
              <td>{{ quantityText(row.checkQuantity, row.unit) }}</td>
              <td :class="Number(row.differenceQuantity) < 0 ? 'loss-text' : Number(row.differenceQuantity) > 0 ? 'profit-text' : ''">
                {{ differenceText(row) }}
              </td>
              <td>{{ row.remark || "-" }}</td>
            </tr>
            <tr v-if="!loading && pageRows.length === 0">
              <td class="empty-cell" colspan="11">暂无盘点明细</td>
            </tr>
          </tbody>
        </table>
      </div>

      <nav v-if="filteredRows.length" class="pagination" aria-label="分页">
        <button type="button" :disabled="currentPage === 1" @click="currentPage--">&lt;</button>
        <button v-for="page in totalPages" :key="page" type="button" :class="{ active: page === currentPage }" @click="currentPage = page">{{ page }}</button>
        <button type="button" :disabled="currentPage === totalPages" @click="currentPage++">&gt;</button>
        <span>每页{{ pageSize }}条，共{{ filteredRows.length }}条</span>
      </nav>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, Search, Upload } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { request } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const checkId = readNavigationState(route);
const loading = ref(false);
const detail = ref<any>({ items: [] });
const category = ref("全部");
const keyword = ref("");
const currentPage = ref(1);
const pageSize = 10;

const statusText = computed(() => String(detail.value.checkStatus || "").toUpperCase() === "CONFIRMED" ? "盘点完成" : "正在盘点");
const filteredRows = computed(() => {
  const search = keyword.value.toLowerCase();
  return (detail.value.items || []).filter((row: any) => {
    const matchesCategory = category.value === "全部" || row.prescriptionCategory === category.value;
    const matchesKeyword = !search || [row.drugCode, row.drugName, row.manufacturer]
      .some((value) => String(value || "").toLowerCase().includes(search));
    return matchesCategory && matchesKeyword;
  });
});
const totalPages = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize)));
const pageRows = computed(() => filteredRows.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize));
watch([category, keyword], () => { currentPage.value = 1; });

const quantityText = (value: unknown, unit: string) => value === null || value === undefined ? "-" : `${value}${unit || ""}`;
const differenceText = (row: any) => {
  if (row.differenceQuantity === null || row.differenceQuantity === undefined) return "-";
  const value = Number(row.differenceQuantity);
  return `${value > 0 ? "+" : ""}${value}${row.unit || ""}`;
};
const goBack = () => router.push("/drug/inventory-check");

const exportDetail = () => {
  if (!detail.value.items?.length) {
    ElMessage.warning("暂无可导出的盘点明细");
    return;
  }
  const headers = ["药品编码", "药品名称", "货位号", "处方类别", "规格", "生产厂家", "当前库存", "盘点库存", "盘盈盘亏", "备注"];
  const lines = detail.value.items.map((row: any) => [
    row.drugCode, row.drugName, row.locationNo, row.prescriptionCategory, row.specification,
    row.manufacturer, row.systemQuantity, row.checkQuantity, row.differenceQuantity, row.remark,
  ].map((value) => `"${String(value ?? "").replaceAll('"', '""')}"`).join(","));
  const blob = new Blob(["\ufeff" + [headers.join(","), ...lines].join("\n")], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `${detail.value.checkNo || "库存盘点"}.csv`;
  link.click();
  URL.revokeObjectURL(url);
};

onMounted(async () => {
  if (!checkId) {
    ElMessage.error("缺少盘点单 ID，无法加载盘点详情");
    return;
  }
  loading.value = true;
  try {
    const response: any = await request.get(`/stock-check-orders/${checkId}/detail`);
    detail.value = response?.data || { items: [] };
  } catch {
    ElMessage.error("盘点详情加载失败，请稍后重试");
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.check-detail-page { min-height: 100%; padding: 18px 0 55px; }
.check-card { width: min(1650px, calc(100% - 80px)); min-height: 820px; margin: 0 auto; padding: 34px 30px 42px; box-sizing: border-box; background: #fff; border-radius: 6px; box-shadow: 0 1px 9px rgba(23,31,56,.07); }
.page-header, .header-actions, .filters-row, .pagination { display: flex; align-items: center; }
.page-header { justify-content: space-between; }
.page-header h1 { position: relative; margin: 0; padding-left: 16px; font-size: 23px; }
.page-header h1::before { position: absolute; left: 0; top: 1px; width: 6px; height: 25px; content: ""; background: #6570f2; }
.header-actions { gap: 14px; }
.outline-btn { display: inline-flex; align-items: center; justify-content: center; gap: 10px; min-width: 130px; height: 46px; padding: 0 22px; border: 1px solid #636be8; border-radius: 5px; background: #fff; color: #636be8; font-size: 17px; font-weight: 700; cursor: pointer; }
.btn-icon { width: 23px; height: 23px; }
.divider { height: 1px; margin: 28px 0; background: #eee; }
.info-panel { display: grid; grid-template-columns: repeat(4, 1fr); align-items: center; min-height: 88px; padding: 0 26px; background: #f0f1ff; font-size: 16px; }
.info-panel strong { font-weight: 400; }
.filters-row { gap: 25px; margin: 28px 0; }
.category-filter { display: flex; align-items: center; gap: 12px; white-space: nowrap; font-size: 17px; }
.category-filter select { width: 180px; height: 44px; padding: 0 14px; border: 2px solid #d3d3d3; border-radius: 4px; font-size: 16px; background: #fff; }
.search-filter { display: grid; grid-template-columns: minmax(280px, 420px) 62px; height: 44px; border: 2px solid #d3d3d3; border-radius: 4px; overflow: hidden; }
.search-filter input { min-width: 0; padding: 0 15px; border: 0; outline: 0; font-size: 16px; }
.search-filter button { border: 0; background: #636be8; color: #fff; }
.search-icon { width: 27px; height: 27px; }
.table-wrap { overflow-x: auto; }
.check-table { width: 100%; min-width: 1400px; border-collapse: collapse; table-layout: fixed; }
.check-table th { height: 66px; background: #bfc2f6; font-size: 16px; white-space: nowrap; }
.check-table td { height: 78px; border-bottom: 1px solid #eee; text-align: center; font-size: 15px; white-space: nowrap; }
.check-table th:nth-child(1) { width: 65px; } .check-table th:nth-child(2) { width: 100px; }
.check-table th:nth-child(3) { width: 150px; } .check-table th:nth-child(4) { width: 90px; }
.check-table th:nth-child(5) { width: 100px; } .check-table th:nth-child(6) { width: 120px; }
.check-table th:nth-child(7) { width: 220px; } .check-table th:nth-child(8) { width: 125px; }
.check-table th:nth-child(9) { width: 115px; } .check-table th:nth-child(10) { width: 105px; }
.check-table th:nth-child(11) { width: 175px; }
.left-cell { text-align: left !important; overflow: hidden; text-overflow: ellipsis; }
.loss-text { color: #f04444; } .profit-text { color: #22a06b; }
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
