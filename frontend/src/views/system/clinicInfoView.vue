<template>
  <div class="page-shell">
    <div class="page-card">
      <div class="toolbar">
        <div class="toolbar-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>
        <el-button type="primary" @click="saveClinic">
          保存
        </el-button>
      </div>

      <el-form
        :model="form"
        label-position="top"
        class="system-form"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="诊所编号">
              <el-input v-model="form.clinicCode" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="诊所名称">
              <el-input v-model="form.clinicName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="诊所负责人">
              <el-input v-model="form.ownerName" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="form.ownerPhone" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="邮箱">
              <el-input v-model="form.ownerEmail" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="所在地区">
              <el-select v-model="form.address" placeholder="请选择地区">
                <el-option label="山东省 济南市" value="山东省 济南市" />
                <el-option label="广东省 深圳市" value="广东省 深圳市" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址">
          <el-input v-model="form.detailAddress" />
        </el-form-item>

        <el-form-item label="诊所简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
          />
        </el-form-item>

        <div class="status-grid">
          <div class="status-item">
            <span class="status-label">诊所状态</span>
            <el-switch v-model="form.status" />
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { clinicInfoApi } from "@/api";

const activeTab = ref("clinic");

const tabs = [
  { key: "clinic", label: "诊所信息" },
  { key: "message", label: "短信设置" },
  { key: "insurance", label: "医保设置" },
];

const form = reactive({
  id: 0,
  clinicCode: "",
  clinicName: "",
  ownerName: "",
  ownerPhone: "",
  ownerEmail: "",
  address: "",
  detailAddress: "",
  description: "",
  status: true,
});

const loadClinic = async () => {
  const clinicResponse: any = await clinicInfoApi.page({ page: 1, size: 1 });
  const clinic = clinicResponse?.data?.records?.[0];
  if (clinic) {
    form.id = Number(clinic.id || 0);
    form.clinicCode = clinic.clinicCode || "";
    form.clinicName = clinic.clinicName || "";
    form.ownerName = clinic.principal || "";
    form.ownerPhone = clinic.phone || "";
    form.ownerEmail = clinic.email || "";
    form.address = clinic.region || "";
    form.detailAddress = clinic.address || "";
    form.description = clinic.introduction || "";
  }
};

const saveClinic = async () => {
  const payload = {
    clinicCode: form.clinicCode,
    clinicName: form.clinicName,
    principal: form.ownerName,
    phone: form.ownerPhone,
    email: form.ownerEmail,
    region: form.address,
    address: form.detailAddress,
    introduction: form.description,
  };
  const response: any = form.id
    ? await clinicInfoApi.update(form.id, payload)
    : await clinicInfoApi.create(payload);
  form.id = Number(response?.data?.id || form.id);
  ElMessage.success("保存成功");
};

onMounted(loadClinic);
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.toolbar-tabs {
  display: flex;
}

.tab-btn {
  min-width: 128px;
  height: 42px;
  border: 1px solid #d8dcf0;
  background: #fff;
  color: #7b8298;
  font-size: 16px;
  cursor: pointer;
}

.tab-btn:first-child {
  border-radius: 10px 0 0 10px;
}

.tab-btn:last-child {
  border-radius: 0 10px 10px 0;
}

.tab-btn.active {
  background: #6574f7;
  border-color: #6574f7;
  color: #fff;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(1, minmax(0, 1fr));
  gap: 16px;
  margin-top: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  padding: 20px;
  border-radius: 14px;
  background: #f7f8fd;
}

.status-label {
  color: #6b7280;
}

.system-form :deep(.el-input__wrapper),
.system-form :deep(.el-textarea__inner),
.system-form :deep(.el-select__wrapper) {
  min-height: 42px;
}
</style>
