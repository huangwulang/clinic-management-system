<template>
  <div class="page-shell">
    <div class="page-card">
      <div class="toolbar">
        <div class="toolbar-actions">
          <el-button type="primary" @click="goToAddDrug">
            新增药品
          </el-button>
          <el-button plain>
            扫码录入
          </el-button>
          <el-button plain>
            导入
          </el-button>
          <el-button plain>
            导出
          </el-button>
        </div>
      </div>

      <div class="filters">
        <el-select
          v-model="filters.prescriptionType"
          placeholder="处方类别"
          style="width: 180px"
        >
          <el-option label="全部" value="" />
          <el-option label="西成药" value="西成药" />
          <el-option label="中成药" value="中成药" />
        </el-select>
        <el-select
          v-model="filters.status"
          placeholder="药品状态"
          style="width: 180px"
        >
          <el-option label="全部" value="" />
          <el-option label="启用" value="启用" />
          <el-option label="停用" value="停用" />
        </el-select>
        <el-date-picker
          v-model="filters.createTime"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
        <el-input
          v-model="filters.keyword"
          placeholder="输入药品名称/编码/生产厂家"
          clearable
          style="width: 340px"
        >
          <template #append>
            <el-button :icon="Search" />
          </template>
        </el-input>
      </div>

      <el-table :data="drugList" border stripe>
        <el-table-column prop="index" label="序号" width="70" />
        <el-table-column prop="code" label="编码" width="110" />
        <el-table-column prop="name" label="药品名称" min-width="160" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="category" label="收费类别" width="110" />
        <el-table-column prop="purchasePrice" label="采购价" width="90" />
        <el-table-column prop="sellPrice" label="零售价" width="90" />
        <el-table-column prop="manufacturer" label="生产厂家" min-width="180" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '启用' ? 'success' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default>
            <el-button link type="primary">编辑</el-button>
            <el-button link type="primary">复制</el-button>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :page-size="10"
          :total="drugList.length * 3"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Search } from "@element-plus/icons-vue";

const router = useRouter();

const filters = ref({
  prescriptionType: "",
  status: "",
  createTime: ["2026-04-01", "2026-04-22"],
  keyword: "",
});

const drugList = ref(
  Array.from({ length: 10 }, (_, index) => ({
    index: index + 1,
    code: `D${String(1000001 + index)}`,
    name: ["阿莫西林胶囊", "头孢克肟片", "葡萄糖注射液", "维生素C片"][index % 4],
    spec: `${6 + index}g*10袋/盒`,
    category: index % 2 === 0 ? "西成药" : "中成药",
    purchasePrice: (12 + index).toFixed(2),
    sellPrice: (15 + index).toFixed(2),
    manufacturer: ["上海医药（集团）有限公司", "山东罗欣药业股份有限公司"][index % 2],
    status: index % 3 === 0 ? "停用" : "启用",
    createTime: `2026-04-22 10:${String(index).padStart(2, "0")}:56`,
  })),
);

const goToAddDrug = () => {
  router.push("/drug/add");
};
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
.toolbar-actions,
.filters,
.pagination-wrap {
  display: flex;
  align-items: center;
}

.toolbar {
  justify-content: flex-end;
  margin-bottom: 24px;
}

.toolbar-actions,
.filters {
  gap: 16px;
}

.filters {
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.pagination-wrap {
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
