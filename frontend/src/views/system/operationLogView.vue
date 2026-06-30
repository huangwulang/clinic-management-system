<template>
  <div class="log-page">
    <section class="log-card">
      <header>
        <div>
          <h2>操作日志</h2>
          <p>记录关键业务操作、请求结果与异常信息。</p>
        </div>
        <el-button :icon="Download" @click="ElMessage.success('导出任务已创建')">导出日志</el-button>
      </header>

      <el-form class="filters" :model="filters" inline>
        <el-form-item label="操作人">
          <el-input v-model.trim="filters.operator" clearable placeholder="姓名或账号" />
        </el-form-item>
        <el-form-item label="业务模块">
          <el-select v-model="filters.module" clearable placeholder="全部模块">
            <el-option v-for="item in modules" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="filters.action" clearable placeholder="全部类型">
            <el-option v-for="item in actions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行结果">
          <el-select v-model="filters.result" clearable placeholder="全部结果">
            <el-option label="成功" value="成功" />
            <el-option label="失败" value="失败" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="filters.range"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="pageRows" stripe border>
        <el-table-column type="index" label="序号" width="68" align="center" />
        <el-table-column prop="time" label="操作时间" min-width="172" />
        <el-table-column prop="operator" label="操作人" min-width="100" />
        <el-table-column prop="module" label="业务模块" min-width="110" />
        <el-table-column prop="action" label="操作类型" min-width="100" />
        <el-table-column prop="summary" label="操作内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP 地址" min-width="130" />
        <el-table-column prop="duration" label="耗时" width="90" align="right" />
        <el-table-column label="结果" width="88" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === '成功' ? 'success' : 'danger'">{{ row.result }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="88" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="selectedLog = row">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <footer>
        <span>共 {{ filteredRows.length }} 条记录</span>
        <el-pagination
          v-model:current-page="currentPage"
          background
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filteredRows.length"
        />
      </footer>
    </section>

    <el-drawer v-model="detailVisible" title="日志详情" size="520px">
      <el-descriptions v-if="selectedLog" :column="1" border>
        <el-descriptions-item label="操作人">{{ selectedLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="模块/类型">{{ selectedLog.module }} / {{ selectedLog.action }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">{{ selectedLog.url }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">{{ selectedLog.params }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ selectedLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="执行耗时">{{ selectedLog.duration }}</el-descriptions-item>
        <el-descriptions-item label="执行结果">{{ selectedLog.result }}</el-descriptions-item>
        <el-descriptions-item label="异常信息">{{ selectedLog.error || "-" }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Download, Search } from "@element-plus/icons-vue";
import { operationLogApi } from "@/api";
import { useClientPagination } from "@/composables/useClientPagination";

type LogRow = {
  id: number;
  time: string;
  operator: string;
  module: string;
  action: string;
  summary: string;
  url: string;
  params: string;
  ip: string;
  duration: string;
  result: "成功" | "失败";
  error?: string;
};

const modules = ["接诊管理", "收费管理", "药品管理", "患者管理", "系统设置"];
const actions = ["新增", "修改", "删除", "查询", "审核", "登录"];
const filters = reactive({
  operator: "",
  module: "",
  action: "",
  result: "",
  range: [] as string[],
});

const rows: LogRow[] = Array.from({ length: 18 }, (_, index) => {
  const failed = index === 3 || index === 11;
  const module = modules[index % modules.length];
  const action = actions[index % actions.length];
  return {
    id: index + 1,
    time: `2026-06-${String(6 - Math.floor(index / 6)).padStart(2, "0")} ${String(9 + (index % 8)).padStart(2, "0")}:24:18`,
    operator: index % 3 === 0 ? "admin" : index % 3 === 1 ? "王医生" : "李护士",
    module,
    action,
    summary: `${action}${module}业务数据`,
    url: `/api/${module === "药品管理" ? "drugs" : "operations"}/${index + 1}`,
    params: `{"id":${index + 1},"source":"admin-web"}`,
    ip: `192.168.1.${20 + index}`,
    duration: `${36 + index * 7} ms`,
    result: failed ? "失败" : "成功",
    error: failed ? "业务数据状态已变更，请刷新后重试" : "",
  };
});

const selectedLog = ref<LogRow | null>(null);
const detailVisible = computed({
  get: () => Boolean(selectedLog.value),
  set: (value) => {
    if (!value) selectedLog.value = null;
  },
});

const filteredRows = computed(() => rows.value.filter((row) => (
  (!filters.operator || row.operator.includes(filters.operator))
  && (!filters.module || row.module === filters.module)
  && (!filters.action || row.action === filters.action)
  && (!filters.result || row.result === filters.result)
  && (filters.range.length !== 2 || (row.time >= filters.range[0] && row.time <= filters.range[1]))
)));

const {
  pageSize,
  currentPage,
  pageRows,
  resetPage,
} = useClientPagination(filteredRows, 10);

watch(
  () => [
    filters.operator,
    filters.module,
    filters.action,
    filters.result,
    ...filters.range,
  ],
  resetPage,
);

const resetFilters = () => {
  filters.operator = "";
  filters.module = "";
  filters.action = "";
  filters.result = "";
  filters.range = [];
};


onMounted(async () => {
  const response: any = await operationLogApi.page({ page: 1, size: 100 });
  rows.value = response?.data?.records || [];
});
</script>

<style scoped>
.log-page {
  min-height: 100%;
  padding: 28px;
  box-sizing: border-box;
}

.log-card {
  padding: 28px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(28, 40, 80, 0.07);
}

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

h2 {
  margin: 0 0 8px;
  color: #172033;
  font-size: 24px;
}

header p {
  margin: 0;
  color: #9097a5;
}

.filters {
  padding: 20px 20px 2px;
  margin-bottom: 22px;
  border-radius: 6px;
  background: #f7f8fc;
}

.filters :deep(.el-input),
.filters :deep(.el-select) {
  width: 180px;
}

footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 22px;
  color: #8a92a3;
}

@media (max-width: 900px) {
  .log-page {
    padding: 16px;
  }

  .log-card {
    padding: 18px;
    overflow-x: auto;
  }
}
</style>
