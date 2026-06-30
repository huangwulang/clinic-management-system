<template>
  <div class="add-drug-page">
    <section class="drug-card">
      <div class="drug-tabs">
        <button
          class="drug-tab"
          :class="{ active: activeTab === 'western' }"
          type="button"
          @click="activeTab = 'western'"
        >
          西/成药信息
        </button>
        <button
          class="drug-tab"
          :class="{ active: activeTab === 'chinese' }"
          type="button"
          @click="activeTab = 'chinese'"
        >
          中药信息
        </button>
      </div>

      <el-form class="drug-form" :model="formData" label-position="top">
        <section class="form-section">
          <h2 class="section-title">基本信息</h2>
          <div class="form-grid">
            <label class="field">
              <span>药品编码</span>
              <el-input v-model="formData.code" />
            </label>
            <label class="field">
              <span>药品条形码</span>
              <el-input v-model="formData.barcode" />
            </label>
            <label class="field">
              <span>药品通用名<i>*</i></span>
              <el-input v-model="formData.commonName" />
            </label>
            <label class="field">
              <span>拼音码</span>
              <el-input v-model="formData.pinyin" />
            </label>

            <label class="field category-field">
              <span>药品分类<i>*</i></span>
              <div class="control-with-add">
                <el-select
                  v-model="formData.category"
                  class="category-select"
                  placeholder="请选择"
                  popper-class="drug-category-popper"
                >
                  <el-option
                    v-for="option in categoryOptions"
                    :key="option"
                    :label="option"
                    :value="option"
                  />
                </el-select>
                <button class="add-option" type="button">+</button>
              </div>
            </label>
            <label class="field">
              <span>药品规格<i>*</i></span>
              <el-input v-model="formData.spec" />
            </label>
            <label class="field">
              <span>药品剂型</span>
              <div class="control-with-add">
                <el-select v-model="formData.dosageForm" placeholder="请选择">
                  <el-option label="片剂" value="片剂" />
                  <el-option label="注射液" value="注射液" />
                  <el-option label="口服液" value="口服液" />
                </el-select>
                <button class="add-option" type="button">+</button>
              </div>
            </label>
            <div class="field radio-field">
              <span>OTC药品：</span>
              <el-radio-group v-model="formData.isOTC">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </div>

            <label class="field">
              <span>发票项目<i>*</i></span>
              <div class="control-with-add">
                <el-select v-model="formData.invoiceItem" placeholder="请选择">
                  <el-option label="诊疗费" value="诊疗费" />
                  <el-option label="检查费" value="检查费" />
                  <el-option label="西药费" value="西药费" />
                  <el-option label="中药费" value="中药费" />
                </el-select>
                <button class="add-option" type="button">+</button>
              </div>
            </label>
            <label class="field">
              <span>批准文号</span>
              <el-input v-model="formData.approvalNumber" />
            </label>
            <label class="field">
              <span>生产厂家</span>
              <div class="control-with-add">
                <el-select v-model="formData.manufacturer" placeholder="请选择">
                  <el-option label="昆明制药集团" value="昆明制药集团" />
                  <el-option label="云南白药集团" value="云南白药集团" />
                </el-select>
                <button class="add-option" type="button">+</button>
              </div>
            </label>
            <div class="field radio-field">
              <span>药品状态：</span>
              <el-radio-group v-model="formData.status">
                <el-radio label="启用">启用</el-radio>
                <el-radio label="禁用">禁用</el-radio>
              </el-radio-group>
            </div>
          </div>
        </section>

        <section class="form-section package-section">
          <h2 class="section-title">包装信息</h2>
          <div class="form-grid">
            <label class="field">
              <span>包装单位<i>*</i></span>
              <div class="control-with-add">
                <el-select v-model="formData.packUnit" placeholder="请选择">
                  <el-option label="盒" value="盒" />
                  <el-option label="瓶" value="瓶" />
                </el-select>
                <button class="add-option" type="button">+</button>
              </div>
            </label>

            <template v-if="activeTab === 'western'">
              <label class="field">
                <span>基本系数</span>
                <el-input v-model="formData.baseRatio" />
              </label>
              <label class="field">
                <span>基本单位</span>
                <div class="control-with-add">
                  <el-select v-model="formData.baseUnit" placeholder="请选择">
                    <el-option label="片" value="片" />
                    <el-option label="粒" value="粒" />
                  </el-select>
                  <button class="add-option" type="button">+</button>
                </div>
              </label>
              <label class="field">
                <span>剂量系数</span>
                <el-input v-model="formData.doseRatio" />
              </label>
              <label class="field">
                <span>剂量单位</span>
                <div class="control-with-add">
                  <el-select v-model="formData.doseUnit" placeholder="请选择">
                    <el-option label="片" value="片" />
                    <el-option label="g" value="g" />
                  </el-select>
                  <button class="add-option" type="button">+</button>
                </div>
              </label>
            </template>

            <label class="field">
              <span>采购价（元）</span>
              <el-input v-model="formData.purchasePrice" />
            </label>
            <label class="field">
              <span>零售价（元）</span>
              <el-input v-model="formData.salePrice" />
            </label>
            <div v-if="activeTab === 'western'" class="field radio-field right-radio">
              <span>允许拆零：</span>
              <el-radio-group v-model="formData.allowSplit">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </div>
            <div class="field radio-field" :class="{ 'member-radio-chinese': activeTab === 'chinese' }">
              <span>允许会员折扣：</span>
              <el-radio-group v-model="formData.allowMemberDiscount">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </div>
          </div>
        </section>

        <section class="form-section effect-section">
          <h2 class="section-title">功效说明</h2>
          <div class="form-grid effect-grid">
            <label class="field">
              <span>用法<i>*</i></span>
              <div class="control-with-add">
                <el-select v-model="formData.usage" placeholder="请选择">
                  <el-option label="口服" value="口服" />
                  <el-option label="外用" value="外用" />
                </el-select>
                <button class="add-option" type="button">+</button>
              </div>
            </label>
            <label class="field">
              <span>单次用量</span>
              <el-input v-model="formData.singleDose">
                <template #suffix>{{ activeTab === "western" ? "片" : "g" }}</template>
              </el-input>
            </label>
            <label class="field">
              <span>频度</span>
              <el-select v-model="formData.frequency">
                <el-option label="一天1次" value="一天1次" />
                <el-option label="一天2次" value="一天2次" />
              </el-select>
            </label>

            <template v-if="activeTab === 'western'">
              <label class="field small-field">
                <span>天数</span>
                <el-select v-model="formData.days">
                  <el-option label="1" value="1" />
                  <el-option label="2" value="2" />
                </el-select>
              </label>
              <label class="field small-field">
                <span>总量</span>
                <el-input v-model="formData.total" />
              </label>
              <label class="field unit-field">
                <span>&nbsp;</span>
                <el-select v-model="formData.totalUnit">
                  <el-option label="瓶" value="瓶" />
                  <el-option label="盒" value="盒" />
                </el-select>
              </label>
            </template>
          </div>
        </section>

        <section class="form-section other-section">
          <h2 class="section-title">其他信息</h2>
          <div class="form-grid">
            <label class="field">
              <span>库存下限</span>
              <el-input v-model="formData.stockMin" />
            </label>
            <label class="field">
              <span>库存上限</span>
              <el-input v-model="formData.stockMax" />
            </label>
            <label class="field">
              <span>货位号</span>
              <el-input v-model="formData.position" />
            </label>
            <label class="field">
              <span>有效期预警</span>
              <el-input v-model="formData.expiryWarning">
                <template #suffix>天</template>
              </el-input>
            </label>
            <label class="field wide-field">
              <span>药品说明</span>
              <el-input v-model="formData.description" />
            </label>
            <label class="field wide-field">
              <span>备注</span>
              <el-input v-model="formData.remark" />
            </label>
          </div>
        </section>
      </el-form>
    </section>

    <footer class="bottom-actions">
      <el-button type="primary" :icon="FolderChecked" @click="saveDrug">保存</el-button>
      <el-button :icon="Back" @click="router.back()">返回</el-button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Back, FolderChecked } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { drugApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const activeTab = ref<"western" | "chinese">("western");
const categoryOptions = ["注射类", "消炎类", "西药"];
const drugId = readNavigationState(route);
const isEditMode = readNavigationState(route, "mode") === "edit" || Boolean(drugId);

const formData = reactive({
  code: "",
  barcode: "",
  commonName: "",
  pinyin: "",
  category: "",
  spec: "",
  dosageForm: "片剂",
  isOTC: false,
  invoiceItem: "",
  approvalNumber: "",
  manufacturer: "昆明制药集团",
  status: "",
  packUnit: "",
  baseRatio: "",
  baseUnit: "",
  doseRatio: "",
  doseUnit: "",
  purchasePrice: "",
  salePrice: "",
  allowSplit: true,
  allowMemberDiscount: false,
  usage: "",
  singleDose: "1",
  frequency: "一天1次",
  days: "1",
  total: "1",
  totalUnit: "瓶",
  stockMin: "",
  stockMax: "",
  position: "",
  expiryWarning: "",
  description: "",
  remark: "",
});

watch(activeTab, (tab) => {
  if (tab === "chinese") {
    formData.dosageForm = "";
    formData.manufacturer = "";
    formData.isOTC = true;
    formData.status = "启用";
    formData.allowMemberDiscount = true;
  } else {
    formData.dosageForm = "片剂";
    formData.manufacturer = "昆明制药集团";
    formData.isOTC = false;
    formData.status = "";
    formData.allowMemberDiscount = false;
  }
});


onMounted(async () => {
  if (!isEditMode) {
    if (!formData.code) {
      const response: any = await drugApi.nextCode();
      formData.code = String(response?.data || "");
    }
    return;
  }
  if (!drugId) return;
  const response: any = await drugApi.get(drugId);
  const item = response?.data || {};
  Object.assign(formData, {
    code: item.drugCode || item.code || formData.code,
    barcode: item.barcode || "",
    commonName: item.name || item.drugName || "",
    pinyin: item.pinyin || "",
    category: item.category || "",
    spec: item.specification || "",
    dosageForm: item.dosageForm || "",
    isOTC: Boolean(item.otc),
    invoiceItem: item.invoiceItem || "",
    approvalNumber: item.approvalNo || "",
    manufacturer: item.manufacturer || "",
    status: String(item.status || "").toUpperCase() === "DISABLED" ? "禁用" : "启用",
    packUnit: item.packUnit || item.unit || "",
    baseRatio: String(item.baseRatio || ""),
    baseUnit: item.baseUnit || "",
    doseRatio: String(item.doseRatio || ""),
    doseUnit: item.doseUnit || "",
    purchasePrice: String(item.purchasePrice || ""),
    salePrice: String(item.sellPrice || item.salePrice || item.retailPrice || ""),
    allowSplit: item.allowSplit !== false,
    allowMemberDiscount: Boolean(item.allowMemberDiscount),
    usage: item.usageMethod || "",
    singleDose: String(item.singleDose || ""),
    frequency: item.frequency || "",
    days: String(item.defaultDays || ""),
    total: String(item.defaultTotal || ""),
    totalUnit: item.totalUnit || "",
    stockMin: String(item.stockMin || ""),
    stockMax: String(item.stockMax || ""),
    position: item.position || item.locationNo || "",
    expiryWarning: String(item.expiryWarningDays || ""),
    description: item.description || "",
    remark: item.remark || "",
  });
});

const saveDrug = async () => {
  const payload = {
    drugCode: formData.code,
    barcode: formData.barcode,
    name: formData.commonName,
    pinyin: formData.pinyin,
    category: formData.category,
    specification: formData.spec,
    dosageForm: formData.dosageForm,
    otc: formData.isOTC,
    invoiceItem: formData.invoiceItem,
    approvalNo: formData.approvalNumber,
    manufacturer: formData.manufacturer,
    status: formData.status === "禁用" ? "DISABLED" : "ENABLED",
    unit: formData.packUnit,
    packUnit: formData.packUnit,
    baseRatio: Number(formData.baseRatio || 0),
    baseUnit: formData.baseUnit,
    doseRatio: Number(formData.doseRatio || 0),
    doseUnit: formData.doseUnit,
    purchasePrice: Number(formData.purchasePrice || 0),
    sellPrice: Number(formData.salePrice || 0),
    allowSplit: formData.allowSplit,
    allowMemberDiscount: formData.allowMemberDiscount,
    usageMethod: formData.usage,
    singleDose: Number(formData.singleDose || 0),
    frequency: formData.frequency,
    defaultDays: Number(formData.days || 0),
    defaultTotal: Number(formData.total || 0),
    totalUnit: formData.totalUnit,
    stockMin: Number(formData.stockMin || 0),
    stockMax: Number(formData.stockMax || 0),
    locationNo: formData.position,
    expiryWarningDays: Number(formData.expiryWarning || 0),
    warningStock: Number(formData.stockMin || 0),
    description: formData.description,
    remark: formData.remark,
  };
  if (isEditMode && drugId) await drugApi.update(drugId, payload);
  else await drugApi.create(payload);
  ElMessage.success("药品信息保存成功");
  router.push("/drugs");
};
</script>

<style scoped>
.add-drug-page {
  min-height: calc(100vh - 70px);
  padding: 20px 0 92px;
}

.drug-card {
  width: 1340px;
  min-height: 1040px;
  margin: 0 auto;
  padding: 24px 26px 46px;
  background: #ffffff;
  border-radius: 4px;
  box-sizing: border-box;
}

.drug-tabs {
  display: flex;
  width: fit-content;
  height: 36px;
  margin-bottom: 34px;
  border: 1px solid #bcbec5;
  border-radius: 3px;
  overflow: hidden;
}

.drug-tab {
  width: 112px;
  height: 36px;
  border: 0;
  border-right: 1px solid #c5c7cd;
  background: #ffffff;
  color: #b7b8bf;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.drug-tab:last-child {
  border-right: 0;
}

.drug-tab.active {
  background: #6264e8;
  color: #ffffff;
}

.form-section {
  margin-bottom: 31px;
}

.section-title {
  margin: 0 0 20px;
  color: #6264f1;
  font-size: 26px;
  line-height: 1;
  font-weight: 800;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(4, 300px);
  column-gap: 30px;
  row-gap: 20px;
  align-items: end;
}

.field {
  position: relative;
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
  color: #000000;
  font-size: 14px;
  line-height: 16px;
}

.field > span {
  height: 16px;
}

.field i {
  color: #ff2f2f;
  font-style: normal;
}

.control-with-add {
  display: grid;
  grid-template-columns: 1fr 22px;
  gap: 4px;
  align-items: center;
}

.add-option {
  width: 20px;
  height: 20px;
  padding: 0;
  border: 2px solid #6264f1;
  border-radius: 50%;
  background: #ffffff;
  color: #6264f1;
  font-size: 20px;
  line-height: 14px;
  font-weight: 700;
}

.category-field {
  z-index: 10;
}

.radio-field {
  height: 44px;
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.radio-field > span {
  height: auto;
  white-space: nowrap;
}

.radio-field :deep(.el-radio) {
  margin-right: 26px;
  color: #000000;
}

.radio-field :deep(.el-radio__label) {
  padding-left: 8px;
}

.right-radio {
  grid-column: 4;
}

.member-radio-chinese {
  grid-column: 4;
}

.effect-grid {
  grid-template-columns: 300px 300px 300px 82px 82px 86px;
}

.small-field,
.unit-field {
  width: 82px;
}

.unit-field {
  width: 86px;
}

.wide-field {
  grid-column: span 2;
}

.bottom-actions {
  position: fixed;
  left: 50%;
  bottom: 0;
  z-index: 30;
  display: flex;
  justify-content: flex-end;
  gap: 18px;
  width: 1288px;
  padding: 12px 22px 14px;
  transform: translateX(-50%);
  background: #ffffff;
  border-radius: 4px 4px 0 0;
  box-shadow: 0 -10px 34px rgba(0, 0, 0, 0.12);
  box-sizing: border-box;
}

.bottom-actions :deep(.el-button) {
  width: 120px;
  height: 38px;
  border-radius: 3px;
  font-weight: 700;
}

.bottom-actions :deep(.el-button--primary) {
  --el-button-bg-color: #6264e8;
  --el-button-border-color: #6264e8;
  --el-button-hover-bg-color: #5658df;
  --el-button-hover-border-color: #5658df;
}

.bottom-actions :deep(.el-button:not(.el-button--primary)) {
  border-color: #6264e8;
  color: #6264e8;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  min-height: 44px;
  border-radius: 3px;
  box-shadow: 0 0 0 1px #bdbdbd inset;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f4f4f4;
}

:deep(.el-input__inner),
:deep(.el-select__placeholder),
:deep(.el-select__selected-item) {
  color: #000000;
  font-size: 14px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #1677ee;
  background: #1677ee;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #000000;
}

@media (max-width: 1450px) {
  .drug-card {
    width: calc(100vw - 220px);
  }

  .bottom-actions {
    width: calc(100vw - 260px);
  }
}
</style>
