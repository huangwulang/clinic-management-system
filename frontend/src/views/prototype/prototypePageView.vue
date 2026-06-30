<template>
  <div :class="['prototype-page', pageClass]">
    <div class="prototype-card">
      <div class="prototype-top">
        <div
          v-if="config.tabs?.length"
          class="prototype-tabs"
        >
          <button
            v-for="tab in config.tabs"
            :key="tab.label"
            :class="['prototype-tab', { active: tab.active }]"
            type="button"
          >
            {{ tab.label }}
          </button>
        </div>

        <div
          v-if="config.actions?.length"
          class="prototype-actions"
        >
          <button
            v-for="action in config.actions"
            :key="action.label"
            :class="['prototype-action', action.type === 'primary' ? 'primary' : 'ghost']"
            type="button"
          >
            {{ action.label }}
          </button>
        </div>
      </div>

      <div class="prototype-divider" />

      <template v-if="config.layout === 'settings'">
        <div class="settings-sections">
          <div
            v-for="section in config.sections"
            :key="section.title"
            class="settings-section"
          >
            <template v-if="section.type === 'switch'">
              <div class="settings-row">
                <div>
                  <div class="settings-title">{{ section.title }}</div>
                  <div class="settings-description">{{ section.description }}</div>
                </div>
                <el-switch :model-value="section.value" />
              </div>
            </template>

            <template v-else-if="section.type === 'input'">
              <div class="settings-title">{{ section.title }}</div>
              <div class="settings-input-row">
                <span>{{ section.label }}</span>
                <el-input
                  :model-value="section.value"
                  class="settings-input"
                />
                <span v-if="section.suffix">{{ section.suffix }}</span>
                <button
                  v-if="section.actionText"
                  class="text-link"
                  type="button"
                >
                  {{ section.actionText }}
                </button>
              </div>
              <div
                v-if="section.helper"
                class="settings-description"
              >
                {{ section.helper }}
              </div>
            </template>

            <template v-else-if="section.type === 'radio'">
              <div class="settings-title">{{ section.title }}</div>
              <el-radio-group :model-value="section.value">
                <el-radio
                  v-for="option in section.options"
                  :key="option"
                  :label="option"
                  :value="option"
                >
                  {{ option }}
                </el-radio>
              </el-radio-group>
            </template>

            <template v-else-if="section.type === 'checkbox-group'">
              <div class="settings-title">{{ section.title }}</div>
              <div
                v-if="section.description"
                class="settings-description"
              >
                {{ section.description }}
              </div>
              <el-checkbox-group :model-value="section.checked">
                <el-checkbox
                  v-for="option in section.options"
                  :key="option"
                  :label="option"
                  :value="option"
                >
                  {{ option }}
                </el-checkbox>
              </el-checkbox-group>
            </template>
          </div>
        </div>
      </template>

      <template v-else>
        <div
          v-if="config.filters?.length"
          class="prototype-filters"
        >
          <div
            v-for="filter in config.filters"
            :key="`${config.title}-${filter.label}-${filter.type}`"
            class="prototype-filter"
            :class="filter.type"
          >
            <span
              v-if="filter.label && filter.type !== 'checkbox'"
              class="filter-label"
            >
              {{ filter.label }}
            </span>

            <el-select
              v-if="filter.type === 'select'"
              :model-value="filter.value"
              class="filter-control select"
            >
              <el-option
                v-for="option in filter.options"
                :key="option"
                :label="option"
                :value="option"
              />
            </el-select>

            <el-date-picker
              v-else-if="filter.type === 'daterange'"
              v-model="dateFilters[`${config.title}-${filter.label}`]"
              class="filter-control date"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
            />

            <el-input
              v-else-if="filter.type === 'input'"
              :placeholder="filter.placeholder"
              class="filter-control input"
            >
              <template #suffix>
                <el-icon class="search-mark">
                  <Search />
                </el-icon>
              </template>
            </el-input>

            <el-checkbox
              v-else-if="filter.type === 'checkbox'"
              :model-value="filter.checked"
            >
              {{ filter.label }}
            </el-checkbox>
          </div>
        </div>

        <div
          v-if="config.summaryCards?.length"
          class="summary-grid"
        >
          <div
            v-for="card in config.summaryCards"
            :key="card.label"
            :class="['summary-card', { accent: card.accent }]"
          >
            <div class="summary-label">{{ card.label }}</div>
            <div class="summary-value">{{ card.value }}</div>
          </div>
        </div>

        <div
          v-if="config.layout === 'split'"
          class="split-layout"
        >
          <div class="split-sidebar">
            <button
              v-for="navItem in config.leftNav"
              :key="navItem"
              :class="['split-nav-item', { active: navItem === config.leftNavActive }]"
              type="button"
            >
              {{ navItem }}
            </button>
          </div>
          <div class="split-content">
            <PrototypeTable
              :columns="config.columns || []"
              :rows="config.rows || []"
            />
          </div>
        </div>

        <PrototypeTable
          v-else-if="config.columns?.length"
          :columns="config.columns"
          :rows="config.rows || []"
        />

        <div
          v-if="config.footerNote"
          class="footer-note"
        >
          {{ config.footerNote }}
        </div>

        <div class="prototype-pagination">
          <div class="pager-group">
            <button class="pager ghost" type="button">&lt;</button>
            <button
              v-for="page in pages"
              :key="page"
              :class="['pager', { active: page === 1 }]"
              type="button"
            >
              {{ page }}
            </button>
            <span class="pager-ellipsis">......</span>
            <button class="pager ghost" type="button">60</button>
            <button class="pager ghost" type="button">&gt;</button>
          </div>
          <div class="pager-text">
            <span>每页10条, 共30条</span>
            <span>前往第</span>
            <span class="page-input">1</span>
            <span>页</span>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Search } from "@element-plus/icons-vue";
import { computed, defineComponent, h, reactive } from "vue";
import type { PropType } from "vue";
import { useRoute } from "vue-router";
import { prototypeConfigs } from "./prototypeConfigs";
import type { PrototypeColumn, PrototypeRow } from "./prototypeConfigs";

const route = useRoute();
const dateFilters = reactive<Record<string, string[]>>({});

const config = computed(
  () =>
    prototypeConfigs[route.path] ?? {
      title: "原型页面",
      layout: "list" as const,
      columns: [],
      rows: [],
    },
);

const pages = [1, 2, 3, 4, 5];

const pageClass = computed(() => `page-${route.path.replace(/[\\/]/g, "-").replace(/^-+/, "")}`);

const getStatusClass = (value: unknown) => {
  const text = String(value ?? "");

  if (
    text.includes("未就诊") ||
    text.includes("待收费") ||
    text.includes("启用") ||
    text.includes("审核通过") ||
    text.includes("已收费")
  ) {
    return "success";
  }

  if (
    text.includes("已退费") ||
    text.includes("审核未通过") ||
    text.includes("停用")
  ) {
    return "danger";
  }

  if (
    text.includes("非会员") ||
    text.includes("VIP") ||
    text.includes("盘点完成") ||
    text.includes("已完成")
  ) {
    return "warning";
  }

  if (
    text.includes("未审核") ||
    text.includes("正在盘点") ||
    text.includes("已就诊") ||
    text.includes("已退号")
  ) {
    return "muted";
  }

  return "success";
};

const PrototypeTable = defineComponent({
  name: "PrototypeTable",
  props: {
    columns: {
      type: Array as PropType<PrototypeColumn[]>,
      required: true,
    },
    rows: {
      type: Array as PropType<PrototypeRow[]>,
      required: true,
    },
  },
  setup(props) {
    return () =>
      h("div", { class: "table-wrapper" }, [
        h("table", { class: "prototype-table" }, [
          h(
            "thead",
            h(
              "tr",
              props.columns.map((column) => h("th", { key: column.key }, column.label)),
            ),
          ),
          h(
            "tbody",
            props.rows.map((row, rowIndex) =>
              h(
                "tr",
                { key: `${rowIndex}` },
                props.columns.map((column) => {
                  const value = row[column.key];

                  if (column.type === "actions" && Array.isArray(value)) {
                    return h(
                      "td",
                      { class: "actions-cell", key: column.key },
                      value.map((action, actionIndex) =>
                        h(
                          "button",
                          {
                            class: "text-link",
                            type: "button",
                            key: `${column.key}-${actionIndex}-${action}`,
                          },
                          action,
                        ),
                      ),
                    );
                  }

                  if (column.type === "switch") {
                    return h("td", { key: column.key }, [
                      h("span", { class: ["mini-switch", value ? "on" : "off"] }),
                    ]);
                  }

                  if (column.type === "status") {
                    return h(
                      "td",
                      { key: column.key },
                      h(
                        "span",
                        {
                          class: ["status-text", getStatusClass(value)],
                        },
                        String(value ?? ""),
                      ),
                    );
                  }

                  return h("td", { key: column.key }, String(value ?? ""));
                }),
              ),
            ),
          ),
        ]),
      ]);
  },
});
</script>

<style scoped>
.prototype-page {
  width: 100%;
}

.prototype-card {
  padding: 30px 34px 26px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(72, 84, 159, 0.08);
}

.prototype-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.prototype-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.prototype-tab {
  min-width: 104px;
  height: 52px;
  padding: 0 24px;
  border: 1px solid #cfd3df;
  background: #fff;
  color: #b7bac6;
  font-size: 16px;
  font-weight: 700;
  transition: all 0.2s ease;
}

.prototype-tab:first-child {
  border-radius: 6px 0 0 6px;
}

.prototype-tab:last-child {
  border-radius: 0 6px 6px 0;
}

.prototype-tab + .prototype-tab {
  margin-left: -1px;
}

.prototype-tab.active {
  border-color: #6972f6;
  background: #6972f6;
  color: #fff;
}

.prototype-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}

.prototype-action {
  min-width: 118px;
  height: 48px;
  padding: 0 22px;
  border-radius: 8px;
  border: 1px solid #6972f6;
  background: #fff;
  color: #6972f6;
  font-size: 16px;
  font-weight: 700;
  transition: all 0.2s ease;
}

.prototype-action.primary {
  background: #6972f6;
  color: #fff;
}

.prototype-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(103, 114, 246, 0.12);
}

.prototype-divider {
  height: 1px;
  margin: 20px 0 28px;
  background: #eceff6;
}

.prototype-filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 22px 24px;
  margin-bottom: 28px;
}

.prototype-filter {
  display: flex;
  align-items: center;
  gap: 14px;
}

.prototype-filter.checkbox {
  justify-content: flex-end;
}

.filter-label {
  flex-shrink: 0;
  color: #111827;
  font-size: 16px;
}

.filter-control {
  width: 100%;
}

.search-mark {
  color: #6972f6;
  font-size: 18px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}

.summary-card {
  min-height: 112px;
  padding: 18px 20px;
  border-radius: 6px;
  background: #fff8df;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  gap: 10px;
}

.summary-card.accent {
  background: #fff4cf;
}

.summary-label {
  color: #535862;
  font-size: 14px;
  font-weight: 700;
}

.summary-value {
  color: #ffb800;
  font-size: 24px;
  font-weight: 700;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
  border-radius: 8px;
}

.prototype-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
}

.prototype-table th {
  padding: 16px 18px;
  background: #c8cbfb;
  color: #101828;
  font-size: 16px;
  font-weight: 700;
  text-align: left;
  white-space: nowrap;
}

.prototype-table td {
  padding: 18px 18px;
  border-bottom: 1px solid #edf0f7;
  color: #101828;
  font-size: 15px;
  white-space: nowrap;
}

.prototype-table tbody tr:hover {
  background: #fafbff;
}

.split-layout {
  display: grid;
  grid-template-columns: 228px minmax(0, 1fr);
  gap: 28px;
}

.split-sidebar {
  border: 1px solid #e7eaf2;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.split-nav-item {
  width: 100%;
  min-height: 58px;
  padding: 16px 20px;
  border: 0;
  border-bottom: 1px solid #eceff6;
  background: #fff;
  color: #111827;
  font-size: 16px;
  text-align: center;
}

.split-nav-item.active {
  background: #6972f6;
  color: #fff;
}

.settings-sections {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.settings-section {
  padding: 22px 26px;
  border-radius: 8px;
  background: #f8f9ff;
}

.settings-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.settings-title {
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.settings-description {
  margin-top: 8px;
  color: #8a90a2;
  font-size: 14px;
  line-height: 1.7;
}

.settings-input-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.settings-input {
  width: 120px;
}

.status-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  font-weight: 600;
}

.status-text.success {
  color: #24c56a;
}

.status-text.danger {
  color: #ff6363;
}

.status-text.warning {
  color: #f5b400;
}

.status-text.muted {
  color: #9aa3b5;
}

.actions-cell {
  white-space: nowrap;
}

.actions-cell .text-link:last-child {
  margin-right: 0;
}

.actions-cell .text-link:hover {
  text-decoration: underline;
}

.text-link {
  margin-right: 8px;
  border: 0;
  background: transparent;
  color: #5c66f4;
  font-size: 15px;
}

.footer-note {
  margin-top: 18px;
  color: #d32f2f;
  font-weight: 600;
}

.prototype-pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
  margin-top: 28px;
}

.pager-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pager {
  width: 42px;
  height: 42px;
  border: 1px solid #cfd6e4;
  border-radius: 999px;
  background: #fff;
  color: #6b7280;
  font-size: 18px;
  transition: all 0.2s ease;
}

.pager:hover {
  border-color: #6972f6;
  color: #6972f6;
}

.pager.active {
  border-color: #6972f6;
  background: #6972f6;
  color: #fff;
}

.pager.ghost {
  color: #6b7280;
}

.pager-ellipsis {
  color: #6b7280;
  font-size: 18px;
}

.pager-text {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-size: 14px;
}

.page-input {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 1px solid #cfd6e4;
  border-radius: 999px;
  color: #6b7280;
}

.mini-switch {
  position: relative;
  display: inline-flex;
  width: 52px;
  height: 32px;
  border-radius: 999px;
  background: #d1d5db;
}

.mini-switch::after {
  content: "";
  position: absolute;
  top: 2px;
  left: 2px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #fff;
}

.mini-switch.on {
  background: #47d16d;
}

.mini-switch.on::after {
  left: 22px;
}

.prototype-page.page-registration-record .prototype-table td,
.prototype-page.page-charge .prototype-table td,
.prototype-page.page-patient .prototype-table td,
.prototype-page.page-drug .prototype-table td,
.prototype-page.page-drug-stock-in .prototype-table td,
.prototype-page.page-drug-stock-out .prototype-table td,
.prototype-page.page-drug-inventory .prototype-table td,
.prototype-page.page-drug-inventory-check .prototype-table td,
.prototype-page.page-drug-price-adjust .prototype-table td,
.prototype-page.page-member .prototype-table td,
.prototype-page.page-system-staff .prototype-table td,
.prototype-page.page-system-check-project-setting .prototype-table td,
.prototype-page.page-system-supplier .prototype-table td,
.prototype-page.page-system-template .prototype-table td,
.prototype-page.page-system-fee-setting .prototype-table td {
  padding-top: 20px;
  padding-bottom: 20px;
  font-size: 15px;
}

.prototype-page.page-registration-record .prototype-table th,
.prototype-page.page-charge .prototype-table th,
.prototype-page.page-patient .prototype-table th,
.prototype-page.page-drug-stock-in .prototype-table th,
.prototype-page.page-drug-stock-out .prototype-table th,
.prototype-page.page-drug-inventory .prototype-table th,
.prototype-page.page-drug-inventory-check .prototype-table th,
.prototype-page.page-drug-price-adjust .prototype-table th,
.prototype-page.page-member .prototype-table th,
.prototype-page.page-statistics-charge .prototype-table th,
.prototype-page.page-statistics-patient .prototype-table th,
.prototype-page.page-statistics-check-project .prototype-table th,
.prototype-page.page-statistics-drug .prototype-table th,
.prototype-page.page-system-dictionary .prototype-table th,
.prototype-page.page-system-staff .prototype-table th,
.prototype-page.page-system-check-project-setting .prototype-table th,
.prototype-page.page-system-supplier .prototype-table th,
.prototype-page.page-system-template .prototype-table th,
.prototype-page.page-system-fee-setting .prototype-table th {
  padding-top: 18px;
  padding-bottom: 18px;
  font-size: 15px;
  letter-spacing: 0.02em;
}

.prototype-page.page-registration-record .prototype-filters,
.prototype-page.page-charge .prototype-filters,
.prototype-page.page-patient .prototype-filters,
.prototype-page.page-drug .prototype-filters,
.prototype-page.page-drug-stock-in .prototype-filters,
.prototype-page.page-drug-stock-out .prototype-filters,
.prototype-page.page-drug-inventory .prototype-filters,
.prototype-page.page-drug-inventory-check .prototype-filters,
.prototype-page.page-drug-price-adjust .prototype-filters,
.prototype-page.page-member .prototype-filters,
.prototype-page.page-statistics-charge .prototype-filters,
.prototype-page.page-statistics-patient .prototype-filters,
.prototype-page.page-statistics-check-project .prototype-filters,
.prototype-page.page-statistics-drug .prototype-filters,
.prototype-page.page-system-staff .prototype-filters,
.prototype-page.page-system-check-project-setting .prototype-filters,
.prototype-page.page-system-supplier .prototype-filters,
.prototype-page.page-system-template .prototype-filters,
.prototype-page.page-system-fee-setting .prototype-filters {
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  align-items: end;
}

.prototype-page.page-charge .prototype-table,
.prototype-page.page-statistics-charge .prototype-table,
.prototype-page.page-statistics-patient .prototype-table,
.prototype-page.page-statistics-check-project .prototype-table,
.prototype-page.page-statistics-drug .prototype-table {
  min-width: 1240px;
}

.prototype-page.page-patient .status-text.warning,
.prototype-page.page-member .status-text.warning {
  color: #f4b400;
  font-style: italic;
  font-weight: 700;
}

.prototype-page.page-system-staff .actions-cell,
.prototype-page.page-member .actions-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.prototype-page.page-system-dictionary .split-sidebar {
  box-shadow: 0 8px 20px rgba(88, 97, 173, 0.06);
}

.prototype-page.page-system-dictionary .split-nav-item {
  font-size: 15px;
}

.prototype-page.page-registration-record .prototype-filters,
.prototype-page.page-charge .prototype-filters,
.prototype-page.page-patient .prototype-filters,
.prototype-page.page-drug .prototype-filters,
.prototype-page.page-member .prototype-filters,
.prototype-page.page-statistics-charge .prototype-filters,
.prototype-page.page-statistics-patient .prototype-filters,
.prototype-page.page-statistics-check-project .prototype-filters,
.prototype-page.page-statistics-drug .prototype-filters {
  margin-bottom: 32px;
}

.prototype-page.page-registration-record .prototype-actions .prototype-action.primary,
.prototype-page.page-patient .prototype-actions .prototype-action.primary,
.prototype-page.page-drug .prototype-actions .prototype-action.primary,
.prototype-page.page-drug-stock-in .prototype-actions .prototype-action.primary,
.prototype-page.page-drug-stock-out .prototype-actions .prototype-action.primary,
.prototype-page.page-drug-inventory-check .prototype-actions .prototype-action.primary,
.prototype-page.page-system-check-project-setting .prototype-actions .prototype-action.primary,
.prototype-page.page-system-supplier .prototype-actions .prototype-action.primary,
.prototype-page.page-system-template .prototype-actions .prototype-action.primary,
.prototype-page.page-system-fee-setting .prototype-actions .prototype-action.primary {
  min-width: 136px;
  box-shadow: 0 10px 20px rgba(103, 114, 246, 0.14);
}

.prototype-page.page-statistics-charge .summary-grid {
  grid-template-columns: 190px repeat(3, minmax(180px, 1fr)) repeat(6, minmax(140px, 1fr));
}

.prototype-page.page-registration-record .prototype-tabs,
.prototype-page.page-charge .prototype-tabs,
.prototype-page.page-member .prototype-tabs,
.prototype-page.page-statistics-charge .prototype-tabs,
.prototype-page.page-statistics-patient .prototype-tabs {
  gap: 0;
}

.prototype-page.page-registration-record .prototype-tab,
.prototype-page.page-charge .prototype-tab,
.prototype-page.page-member .prototype-tab,
.prototype-page.page-statistics-charge .prototype-tab,
.prototype-page.page-statistics-patient .prototype-tab {
  min-width: 132px;
  height: 50px;
  font-size: 15px;
}

.prototype-page.page-registration-record .prototype-actions,
.prototype-page.page-patient .prototype-actions,
.prototype-page.page-drug .prototype-actions,
.prototype-page.page-drug-stock-in .prototype-actions,
.prototype-page.page-drug-stock-out .prototype-actions,
.prototype-page.page-drug-inventory-check .prototype-actions,
.prototype-page.page-system-staff .prototype-actions,
.prototype-page.page-system-check-project-setting .prototype-actions,
.prototype-page.page-system-supplier .prototype-actions,
.prototype-page.page-system-template .prototype-actions,
.prototype-page.page-system-fee-setting .prototype-actions {
  gap: 14px;
}

.prototype-page.page-registration-record .prototype-filter.input,
.prototype-page.page-charge .prototype-filter.input,
.prototype-page.page-patient .prototype-filter.input,
.prototype-page.page-statistics-charge .prototype-filter.input,
.prototype-page.page-statistics-patient .prototype-filter.input {
  grid-column: span 2;
}

.prototype-page.page-registration-record .prototype-filter.daterange,
.prototype-page.page-charge .prototype-filter.daterange,
.prototype-page.page-patient .prototype-filter.daterange,
.prototype-page.page-statistics-charge .prototype-filter.daterange,
.prototype-page.page-statistics-patient .prototype-filter.daterange,
.prototype-page.page-statistics-check-project .prototype-filter.daterange,
.prototype-page.page-statistics-drug .prototype-filter.daterange {
  min-width: 320px;
}

.prototype-page.page-drug .prototype-filter.daterange {
  grid-column: span 2;
}

.prototype-page.page-registration-record .prototype-card,
.prototype-page.page-charge .prototype-card,
.prototype-page.page-patient .prototype-card,
.prototype-page.page-drug .prototype-card,
.prototype-page.page-drug-stock-in .prototype-card,
.prototype-page.page-drug-stock-out .prototype-card,
.prototype-page.page-drug-inventory .prototype-card,
.prototype-page.page-drug-inventory-check .prototype-card,
.prototype-page.page-drug-price-adjust .prototype-card,
.prototype-page.page-member .prototype-card,
.prototype-page.page-statistics-charge .prototype-card,
.prototype-page.page-statistics-patient .prototype-card,
.prototype-page.page-statistics-check-project .prototype-card,
.prototype-page.page-statistics-drug .prototype-card,
.prototype-page.page-system-dictionary .prototype-card,
.prototype-page.page-system-staff .prototype-card,
.prototype-page.page-system-check-project-setting .prototype-card,
.prototype-page.page-system-supplier .prototype-card,
.prototype-page.page-system-template .prototype-card,
.prototype-page.page-system-fee-setting .prototype-card {
  padding: 30px 36px 28px;
}

.prototype-page.page-registration-record .prototype-top,
.prototype-page.page-charge .prototype-top,
.prototype-page.page-patient .prototype-top,
.prototype-page.page-drug .prototype-top {
  align-items: flex-start;
}

.prototype-page.page-registration-record .prototype-actions,
.prototype-page.page-patient .prototype-actions,
.prototype-page.page-drug .prototype-actions {
  margin-left: auto;
}

.prototype-page.page-registration-record .prototype-divider,
.prototype-page.page-charge .prototype-divider,
.prototype-page.page-patient .prototype-divider,
.prototype-page.page-drug .prototype-divider,
.prototype-page.page-drug-stock-in .prototype-divider,
.prototype-page.page-drug-stock-out .prototype-divider,
.prototype-page.page-drug-inventory .prototype-divider,
.prototype-page.page-drug-inventory-check .prototype-divider,
.prototype-page.page-drug-price-adjust .prototype-divider,
.prototype-page.page-member .prototype-divider,
.prototype-page.page-statistics-charge .prototype-divider,
.prototype-page.page-statistics-patient .prototype-divider,
.prototype-page.page-statistics-check-project .prototype-divider,
.prototype-page.page-statistics-drug .prototype-divider,
.prototype-page.page-system-staff .prototype-divider,
.prototype-page.page-system-check-project-setting .prototype-divider,
.prototype-page.page-system-supplier .prototype-divider,
.prototype-page.page-system-template .prototype-divider,
.prototype-page.page-system-fee-setting .prototype-divider {
  margin-bottom: 30px;
}

.prototype-page.page-registration-record .prototype-table th:nth-child(10),
.prototype-page.page-registration-record .prototype-table th:nth-child(11),
.prototype-page.page-registration-record .prototype-table td:nth-child(10),
.prototype-page.page-registration-record .prototype-table td:nth-child(11),
.prototype-page.page-charge .prototype-table th:nth-child(11),
.prototype-page.page-charge .prototype-table td:nth-child(11),
.prototype-page.page-drug .prototype-table th:nth-child(6),
.prototype-page.page-drug .prototype-table th:nth-child(7),
.prototype-page.page-drug .prototype-table td:nth-child(6),
.prototype-page.page-drug .prototype-table td:nth-child(7),
.prototype-page.page-drug-stock-in .prototype-table th:nth-child(6),
.prototype-page.page-drug-stock-in .prototype-table th:nth-child(7),
.prototype-page.page-drug-stock-in .prototype-table td:nth-child(6),
.prototype-page.page-drug-stock-in .prototype-table td:nth-child(7),
.prototype-page.page-drug-stock-out .prototype-table th:nth-child(5),
.prototype-page.page-drug-stock-out .prototype-table th:nth-child(6),
.prototype-page.page-drug-stock-out .prototype-table td:nth-child(5),
.prototype-page.page-drug-stock-out .prototype-table td:nth-child(6),
.prototype-page.page-drug-inventory .prototype-table th:nth-child(10),
.prototype-page.page-drug-inventory .prototype-table th:nth-child(11),
.prototype-page.page-drug-inventory .prototype-table td:nth-child(10),
.prototype-page.page-drug-inventory .prototype-table td:nth-child(11),
.prototype-page.page-member .prototype-table th:nth-child(7),
.prototype-page.page-member .prototype-table th:nth-child(8),
.prototype-page.page-member .prototype-table th:nth-child(9),
.prototype-page.page-member .prototype-table td:nth-child(7),
.prototype-page.page-member .prototype-table td:nth-child(8),
.prototype-page.page-member .prototype-table td:nth-child(9),
.prototype-page.page-statistics-charge .prototype-table th:nth-child(8),
.prototype-page.page-statistics-charge .prototype-table th:nth-child(9),
.prototype-page.page-statistics-charge .prototype-table th:nth-child(10),
.prototype-page.page-statistics-charge .prototype-table th:nth-child(11),
.prototype-page.page-statistics-charge .prototype-table th:nth-child(12),
.prototype-page.page-statistics-charge .prototype-table th:nth-child(13),
.prototype-page.page-statistics-charge .prototype-table th:nth-child(14),
.prototype-page.page-statistics-charge .prototype-table th:nth-child(15),
.prototype-page.page-statistics-charge .prototype-table td:nth-child(8),
.prototype-page.page-statistics-charge .prototype-table td:nth-child(9),
.prototype-page.page-statistics-charge .prototype-table td:nth-child(10),
.prototype-page.page-statistics-charge .prototype-table td:nth-child(11),
.prototype-page.page-statistics-charge .prototype-table td:nth-child(12),
.prototype-page.page-statistics-charge .prototype-table td:nth-child(13),
.prototype-page.page-statistics-charge .prototype-table td:nth-child(14),
.prototype-page.page-statistics-charge .prototype-table td:nth-child(15) {
  text-align: right;
}

.prototype-page.page-registration-record .prototype-table th:first-child,
.prototype-page.page-charge .prototype-table th:first-child,
.prototype-page.page-patient .prototype-table th:first-child,
.prototype-page.page-drug .prototype-table th:first-child,
.prototype-page.page-drug-stock-in .prototype-table th:first-child,
.prototype-page.page-drug-stock-out .prototype-table th:first-child,
.prototype-page.page-drug-inventory .prototype-table th:first-child,
.prototype-page.page-drug-inventory-check .prototype-table th:first-child,
.prototype-page.page-drug-price-adjust .prototype-table th:first-child,
.prototype-page.page-member .prototype-table th:first-child,
.prototype-page.page-statistics-charge .prototype-table th:first-child,
.prototype-page.page-statistics-patient .prototype-table th:first-child,
.prototype-page.page-statistics-check-project .prototype-table th:first-child,
.prototype-page.page-statistics-drug .prototype-table th:first-child,
.prototype-page.page-system-staff .prototype-table th:first-child,
.prototype-page.page-system-check-project-setting .prototype-table th:first-child,
.prototype-page.page-system-supplier .prototype-table th:first-child,
.prototype-page.page-system-template .prototype-table th:first-child,
.prototype-page.page-system-fee-setting .prototype-table th:first-child {
  width: 74px;
}

.prototype-page.page-registration-record .prototype-table th:nth-child(2),
.prototype-page.page-registration-record .prototype-table td:nth-child(2) {
  min-width: 158px;
}

.prototype-page.page-registration-record .prototype-table th:nth-child(3),
.prototype-page.page-registration-record .prototype-table td:nth-child(3),
.prototype-page.page-patient .prototype-table th:nth-child(3),
.prototype-page.page-patient .prototype-table td:nth-child(3) {
  min-width: 118px;
}

.prototype-page.page-charge .prototype-table th:nth-child(2),
.prototype-page.page-charge .prototype-table td:nth-child(2) {
  min-width: 160px;
}

.prototype-page.page-charge .prototype-table th:nth-child(3),
.prototype-page.page-charge .prototype-table td:nth-child(3) {
  min-width: 110px;
}

.prototype-page.page-patient .prototype-table th:nth-child(2),
.prototype-page.page-patient .prototype-table td:nth-child(2) {
  min-width: 118px;
}

.prototype-page.page-drug .prototype-table th:nth-child(2),
.prototype-page.page-drug .prototype-table td:nth-child(2) {
  min-width: 116px;
}

.prototype-page.page-drug .prototype-table th:nth-child(3),
.prototype-page.page-drug .prototype-table td:nth-child(3) {
  min-width: 178px;
}

.prototype-page.page-drug .prototype-table th:nth-child(8),
.prototype-page.page-drug .prototype-table td:nth-child(8) {
  min-width: 260px;
}

.prototype-page.page-registration-record .prototype-table th:last-child,
.prototype-page.page-registration-record .prototype-table td:last-child,
.prototype-page.page-charge .prototype-table th:last-child,
.prototype-page.page-charge .prototype-table td:last-child,
.prototype-page.page-patient .prototype-table th:last-child,
.prototype-page.page-patient .prototype-table td:last-child,
.prototype-page.page-drug .prototype-table th:last-child,
.prototype-page.page-drug .prototype-table td:last-child,
.prototype-page.page-drug-stock-in .prototype-table th:last-child,
.prototype-page.page-drug-stock-in .prototype-table td:last-child,
.prototype-page.page-drug-stock-out .prototype-table th:last-child,
.prototype-page.page-drug-stock-out .prototype-table td:last-child,
.prototype-page.page-drug-inventory-check .prototype-table th:last-child,
.prototype-page.page-drug-inventory-check .prototype-table td:last-child,
.prototype-page.page-drug-price-adjust .prototype-table th:last-child,
.prototype-page.page-drug-price-adjust .prototype-table td:last-child,
.prototype-page.page-member .prototype-table th:last-child,
.prototype-page.page-member .prototype-table td:last-child,
.prototype-page.page-statistics-charge .prototype-table th:last-child,
.prototype-page.page-statistics-charge .prototype-table td:last-child,
.prototype-page.page-system-staff .prototype-table th:last-child,
.prototype-page.page-system-staff .prototype-table td:last-child,
.prototype-page.page-system-check-project-setting .prototype-table th:last-child,
.prototype-page.page-system-check-project-setting .prototype-table td:last-child,
.prototype-page.page-system-supplier .prototype-table th:last-child,
.prototype-page.page-system-supplier .prototype-table td:last-child,
.prototype-page.page-system-template .prototype-table th:last-child,
.prototype-page.page-system-template .prototype-table td:last-child,
.prototype-page.page-system-fee-setting .prototype-table th:last-child,
.prototype-page.page-system-fee-setting .prototype-table td:last-child {
  text-align: center;
}

.prototype-page.page-registration-record .prototype-table th:last-child,
.prototype-page.page-registration-record .prototype-table td:last-child,
.prototype-page.page-charge .prototype-table th:last-child,
.prototype-page.page-charge .prototype-table td:last-child,
.prototype-page.page-patient .prototype-table th:last-child,
.prototype-page.page-patient .prototype-table td:last-child,
.prototype-page.page-drug .prototype-table th:last-child,
.prototype-page.page-drug .prototype-table td:last-child,
.prototype-page.page-drug-stock-in .prototype-table th:last-child,
.prototype-page.page-drug-stock-in .prototype-table td:last-child,
.prototype-page.page-drug-stock-out .prototype-table th:last-child,
.prototype-page.page-drug-stock-out .prototype-table td:last-child,
.prototype-page.page-drug-inventory .prototype-table th:last-child,
.prototype-page.page-drug-inventory .prototype-table td:last-child,
.prototype-page.page-drug-inventory-check .prototype-table th:last-child,
.prototype-page.page-drug-inventory-check .prototype-table td:last-child,
.prototype-page.page-drug-price-adjust .prototype-table th:last-child,
.prototype-page.page-drug-price-adjust .prototype-table td:last-child,
.prototype-page.page-member .prototype-table th:last-child,
.prototype-page.page-member .prototype-table td:last-child,
.prototype-page.page-statistics-charge .prototype-table th:last-child,
.prototype-page.page-statistics-charge .prototype-table td:last-child,
.prototype-page.page-system-staff .prototype-table th:last-child,
.prototype-page.page-system-staff .prototype-table td:last-child,
.prototype-page.page-system-check-project-setting .prototype-table th:last-child,
.prototype-page.page-system-check-project-setting .prototype-table td:last-child,
.prototype-page.page-system-supplier .prototype-table th:last-child,
.prototype-page.page-system-supplier .prototype-table td:last-child,
.prototype-page.page-system-template .prototype-table th:last-child,
.prototype-page.page-system-template .prototype-table td:last-child,
.prototype-page.page-system-fee-setting .prototype-table th:last-child,
.prototype-page.page-system-fee-setting .prototype-table td:last-child {
  min-width: 140px;
}

.prototype-page.page-registration-record .prototype-table td:last-child,
.prototype-page.page-charge .prototype-table td:last-child,
.prototype-page.page-patient .prototype-table td:last-child,
.prototype-page.page-drug .prototype-table td:last-child {
  white-space: nowrap;
}

.prototype-page.page-registration-record .status-text,
.prototype-page.page-charge .status-text,
.prototype-page.page-patient .status-text,
.prototype-page.page-drug .status-text,
.prototype-page.page-drug-stock-in .status-text,
.prototype-page.page-drug-stock-out .status-text,
.prototype-page.page-drug-inventory-check .status-text,
.prototype-page.page-drug-price-adjust .status-text,
.prototype-page.page-member .status-text,
.prototype-page.page-statistics-charge .status-text,
.prototype-page.page-statistics-patient .status-text {
  min-width: auto;
}

.prototype-page.page-drug .status-text,
.prototype-page.page-member .status-text,
.prototype-page.page-drug-stock-in .status-text,
.prototype-page.page-drug-stock-out .status-text {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(105, 114, 246, 0.08);
}

.prototype-page.page-drug .status-text.success,
.prototype-page.page-drug-stock-in .status-text.success,
.prototype-page.page-drug-stock-out .status-text.success {
  background: rgba(36, 197, 106, 0.1);
}

.prototype-page.page-drug .status-text.danger {
  background: rgba(255, 99, 99, 0.1);
}

.prototype-page.page-member .status-text.warning {
  background: rgba(244, 180, 0, 0.12);
}

.prototype-page.page-registration-record .status-text.success,
.prototype-page.page-charge .status-text.success,
.prototype-page.page-patient .status-text.success {
  color: #23d18b;
}

.prototype-page.page-registration-record .prototype-table tbody tr,
.prototype-page.page-charge .prototype-table tbody tr,
.prototype-page.page-patient .prototype-table tbody tr,
.prototype-page.page-drug .prototype-table tbody tr {
  background: #fff;
}

.prototype-page.page-registration-record .table-wrapper,
.prototype-page.page-charge .table-wrapper,
.prototype-page.page-patient .table-wrapper,
.prototype-page.page-drug .table-wrapper {
  border-radius: 0 0 10px 10px;
}

.prototype-page.page-registration-record .prototype-table thead th,
.prototype-page.page-charge .prototype-table thead th,
.prototype-page.page-patient .prototype-table thead th,
.prototype-page.page-drug .prototype-table thead th {
  background: #c6cafb;
}

.prototype-page.page-registration-record .prototype-filter.select .filter-control,
.prototype-page.page-charge .prototype-filter.select .filter-control,
.prototype-page.page-patient .prototype-filter.select .filter-control {
  max-width: 210px;
}

.prototype-page.page-registration-record .prototype-filter.input .filter-control,
.prototype-page.page-charge .prototype-filter.input .filter-control,
.prototype-page.page-patient .prototype-filter.input .filter-control {
  max-width: 480px;
}

.prototype-page.page-drug .prototype-filter.select .filter-control {
  max-width: 194px;
}

.prototype-page.page-drug .prototype-filter.input .filter-control {
  max-width: 460px;
}

.prototype-page.page-registration-record .prototype-action,
.prototype-page.page-patient .prototype-action,
.prototype-page.page-drug .prototype-action {
  min-width: 132px;
}

.prototype-page.page-registration-record .prototype-action.ghost,
.prototype-page.page-patient .prototype-action.ghost,
.prototype-page.page-drug .prototype-action.ghost {
  background: #fff;
}

.prototype-page.page-registration-record .prototype-pagination,
.prototype-page.page-charge .prototype-pagination,
.prototype-page.page-patient .prototype-pagination,
.prototype-page.page-drug .prototype-pagination {
  justify-content: center;
}

.prototype-page.page-registration-record .pager-text,
.prototype-page.page-charge .pager-text,
.prototype-page.page-patient .pager-text,
.prototype-page.page-drug .pager-text {
  min-width: 270px;
  justify-content: flex-end;
}

.prototype-page.page-charge .prototype-table td,
.prototype-page.page-statistics-charge .prototype-table td,
.prototype-page.page-statistics-patient .prototype-table td,
.prototype-page.page-statistics-check-project .prototype-table td,
.prototype-page.page-statistics-drug .prototype-table td {
  white-space: normal;
}

.prototype-page.page-statistics-charge .summary-card:first-child .summary-value {
  font-size: 40px;
  line-height: 1;
}

.prototype-page.page-statistics-charge .summary-card:not(:first-child) .summary-value {
  font-size: 16px;
}

.prototype-page.page-registration-record .prototype-pagination,
.prototype-page.page-charge .prototype-pagination,
.prototype-page.page-patient .prototype-pagination,
.prototype-page.page-drug .prototype-pagination,
.prototype-page.page-drug-stock-in .prototype-pagination,
.prototype-page.page-drug-stock-out .prototype-pagination,
.prototype-page.page-drug-inventory .prototype-pagination,
.prototype-page.page-drug-inventory-check .prototype-pagination,
.prototype-page.page-drug-price-adjust .prototype-pagination,
.prototype-page.page-member .prototype-pagination,
.prototype-page.page-statistics-charge .prototype-pagination,
.prototype-page.page-statistics-patient .prototype-pagination,
.prototype-page.page-statistics-check-project .prototype-pagination,
.prototype-page.page-statistics-drug .prototype-pagination,
.prototype-page.page-system-dictionary .prototype-pagination,
.prototype-page.page-system-staff .prototype-pagination,
.prototype-page.page-system-check-project-setting .prototype-pagination,
.prototype-page.page-system-supplier .prototype-pagination,
.prototype-page.page-system-template .prototype-pagination,
.prototype-page.page-system-fee-setting .prototype-pagination {
  margin-top: 34px;
}

.prototype-page.page-registration-record .actions-cell .text-link,
.prototype-page.page-charge .actions-cell .text-link,
.prototype-page.page-patient .actions-cell .text-link,
.prototype-page.page-drug .actions-cell .text-link,
.prototype-page.page-drug-stock-in .actions-cell .text-link,
.prototype-page.page-drug-stock-out .actions-cell .text-link,
.prototype-page.page-drug-inventory-check .actions-cell .text-link,
.prototype-page.page-drug-price-adjust .actions-cell .text-link,
.prototype-page.page-member .actions-cell .text-link,
.prototype-page.page-statistics-charge .actions-cell .text-link,
.prototype-page.page-system-staff .actions-cell .text-link,
.prototype-page.page-system-check-project-setting .actions-cell .text-link,
.prototype-page.page-system-supplier .actions-cell .text-link,
.prototype-page.page-system-template .actions-cell .text-link,
.prototype-page.page-system-fee-setting .actions-cell .text-link {
  font-size: 16px;
}

.prototype-page.page-registration-record .actions-cell .text-link,
.prototype-page.page-charge .actions-cell .text-link,
.prototype-page.page-patient .actions-cell .text-link,
.prototype-page.page-drug-stock-in .actions-cell .text-link,
.prototype-page.page-drug-stock-out .actions-cell .text-link,
.prototype-page.page-drug-inventory-check .actions-cell .text-link,
.prototype-page.page-member .actions-cell .text-link {
  margin-right: 14px;
}

.prototype-page.page-statistics-charge .table-wrapper,
.prototype-page.page-statistics-patient .table-wrapper,
.prototype-page.page-statistics-check-project .table-wrapper,
.prototype-page.page-statistics-drug .table-wrapper {
  border: 1px solid #e8ebf5;
}

.prototype-page.page-drug .prototype-table td:nth-child(9),
.prototype-page.page-drug-stock-in .prototype-table td:nth-child(10),
.prototype-page.page-drug-stock-out .prototype-table td:nth-child(9) {
  font-weight: 600;
}

.prototype-page.page-patient .prototype-table td:nth-child(7),
.prototype-page.page-member .prototype-table td:nth-child(4) {
  font-size: 28px;
}

.prototype-page.page-patient .prototype-table td:nth-child(7),
.prototype-page.page-member .prototype-table td:nth-child(4) {
  font-size: 15px;
}

:deep(.prototype-page .filter-control.input .el-input__wrapper) {
  padding-right: 12px;
}

:deep(.prototype-page .filter-control.input .el-input__suffix) {
  color: #6972f6;
}

:deep(.prototype-page .filter-control.date .el-range-separator) {
  color: #8f96ab;
}

:deep(.prototype-page.page-registration-record .el-range-editor.el-input__wrapper),
:deep(.prototype-page.page-charge .el-range-editor.el-input__wrapper),
:deep(.prototype-page.page-patient .el-range-editor.el-input__wrapper),
:deep(.prototype-page.page-drug .el-range-editor.el-input__wrapper) {
  min-width: 300px;
}

:deep(.prototype-page .filter-control .el-input__wrapper:hover),
:deep(.prototype-page .filter-control .el-select__wrapper:hover),
:deep(.prototype-page .filter-control .el-range-editor.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #6972f6 inset !important;
}

.prototype-page.page-statistics-charge .summary-card {
  min-height: 128px;
  justify-content: center;
}

.prototype-page.page-statistics-charge .summary-card.accent {
  background: #fff7df;
}

.prototype-page.page-registration-record .prototype-tab,
.prototype-page.page-charge .prototype-tab,
.prototype-page.page-member .prototype-tab,
.prototype-page.page-statistics-charge .prototype-tab,
.prototype-page.page-statistics-patient .prototype-tab,
.prototype-page.page-statistics-drug .prototype-tab,
.prototype-page.page-system-template .prototype-tab,
.prototype-page.page-system-fee-setting .prototype-tab,
.prototype-page.page-system-staff .prototype-tab {
  min-width: 126px;
}

:deep(.filter-control .el-input__wrapper),
:deep(.filter-control .el-select__wrapper),
:deep(.filter-control .el-range-editor.el-input__wrapper),
:deep(.settings-input .el-input__wrapper) {
  min-height: 46px;
  border-radius: 6px;
  box-shadow: 0 0 0 1px #d5d9e4 inset !important;
}

:deep(.filter-control .el-input__inner),
:deep(.filter-control .el-select__selected-item),
:deep(.filter-control .el-range-input),
:deep(.settings-input .el-input__inner) {
  font-size: 15px;
}

:deep(.el-checkbox) {
  color: #4b5563;
}

:deep(.el-radio) {
  margin-right: 20px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(160px, 1fr));
  }

  .split-layout {
    grid-template-columns: 1fr;
  }

  .prototype-pagination {
    flex-direction: column;
    align-items: flex-end;
  }

  .prototype-filters,
  .prototype-page.page-registration-record .prototype-filters,
  .prototype-page.page-charge .prototype-filters,
  .prototype-page.page-patient .prototype-filters,
  .prototype-page.page-drug .prototype-filters,
  .prototype-page.page-drug-stock-in .prototype-filters,
  .prototype-page.page-drug-stock-out .prototype-filters,
  .prototype-page.page-drug-inventory .prototype-filters,
  .prototype-page.page-drug-inventory-check .prototype-filters,
  .prototype-page.page-drug-price-adjust .prototype-filters,
  .prototype-page.page-member .prototype-filters,
  .prototype-page.page-statistics-charge .prototype-filters,
  .prototype-page.page-statistics-patient .prototype-filters,
  .prototype-page.page-statistics-check-project .prototype-filters,
  .prototype-page.page-statistics-drug .prototype-filters,
  .prototype-page.page-system-staff .prototype-filters,
  .prototype-page.page-system-check-project-setting .prototype-filters,
  .prototype-page.page-system-supplier .prototype-filters,
  .prototype-page.page-system-template .prototype-filters,
  .prototype-page.page-system-fee-setting .prototype-filters {
    grid-template-columns: 1fr;
  }
}
</style>
