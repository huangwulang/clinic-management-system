<template>
  <div class="page-shell">
    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
          <el-input
            v-model="filters.keyword"
            placeholder="输入单号/项目名称"
            clearable
            style="width: 320px"
          >
            <template #append>
              <el-button :icon="Search" />
            </template>
          </el-input>
        </div>
        <el-button plain>
          导出
        </el-button>
      </div>

      <div class="summary">
        <span>合计</span>
        <span>采购总价 {{ summary.purchaseTotal.toFixed(2) }} 元</span>
        <span>零售总价 {{ summary.retailTotal.toFixed(2) }} 元</span>
        <span>利润 {{ summary.profit.toFixed(2) }} 元</span>
      </div>

      <el-table :data="filteredTableData" border stripe>
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="orderNo" label="订单编号" width="140" />
        <el-table-column prop="itemType" label="项目类型" width="100" />
        <el-table-column prop="itemCode" label="项目编码" width="110" />
        <el-table-column prop="itemName" label="项目名称" width="120" />
        <el-table-column prop="invoiceType" label="发票类型" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="purchasePrice" label="采购单价" width="100" />
        <el-table-column prop="retailPrice" label="零售单价" width="100" />
        <el-table-column prop="purchaseTotal" label="采购总价" width="100" />
        <el-table-column prop="retailTotal" label="零售总价" width="100" />
        <el-table-column prop="profit" label="利润" width="90" />
        <el-table-column prop="orderDate" label="订单日期" min-width="170" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :page-size="10"
          :total="tableData.length * 3"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { Search } from "@element-plus/icons-vue";

const filters = ref({
  dateRange: [] as string[],
  keyword: "",
});

const tableData = ref(
  Array.from({ length: 10 }, (_, index) => ({
    orderNo: `TJ20260422${String(index + 1).padStart(3, "0")}`,
    itemType: ["煎药费", "拔牙", "手术费"][index % 3],
    itemCode: `${1000001 + index}`,
    itemName: ["脑电图", "血压测量", "超声检查", "针灸"][index % 4],
    invoiceType: ["注射费", "检查费", "材料费"][index % 3],
    quantity: 1,
    purchasePrice: 12,
    retailPrice: 15,
    purchaseTotal: 24,
    retailTotal: 30,
    profit: 6,
    orderDate: `2026-04-22 10:${String(index).padStart(2, "0")}:34`,
  })),
);

const filteredTableData = computed(() => tableData.value.filter((item) => {
  const keyword = filters.value.keyword.trim();
  const matchesKeyword = !keyword
    || item.orderNo.includes(keyword)
    || item.itemName.includes(keyword);
  const rowDate = item.orderDate.slice(0, 10);
  const matchesDate = filters.value.dateRange.length !== 2
    || (rowDate >= filters.value.dateRange[0] && rowDate <= filters.value.dateRange[1]);
  return matchesKeyword && matchesDate;
}));

const summary = computed(() => {
  return filteredTableData.value.reduce(
    (acc, item) => {
      acc.purchaseTotal += item.purchaseTotal;
      acc.retailTotal += item.retailTotal;
      acc.profit += item.profit;
      return acc;
    },
    { purchaseTotal: 0, retailTotal: 0, profit: 0 },
  );
});
</script>

<style scoped>
.page-shell {
  padding: 24px;
}

.page-card {
  background: #fff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.06);
}

.toolbar,
.filters,
.pagination-wrap,
.summary {
  display: flex;
  align-items: center;
}

.toolbar {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.filters {
  flex-wrap: wrap;
  gap: 16px;
}

.summary {
  gap: 24px;
  padding: 14px 18px;
  margin-bottom: 20px;
  border-radius: 12px;
  background: #f7f8fd;
  font-weight: 600;
}

.pagination-wrap {
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
