<template>
  <div class="registration-detail-page">
    <section class="registration-detail-card">
      <header class="detail-header">
        <h1 class="detail-page-title">查看挂号信息</h1>
        <div class="detail-actions">
          <button class="primary-action" type="button" @click="startVisit">
            <span class="save-icon"></span>
            <span>保存</span>
          </button>
          <button class="back-action" type="button" @click="goBack">
            <span class="back-icon"></span>
            <span>返回</span>
          </button>
        </div>
      </header>

      <section class="detail-section order-section">
        <h2 class="detail-section-title">订单信息</h2>
        <div :class="['info-table', 'order-table', { cancelled: isCancelled }]">
          <div>订单编号：201909090001</div>
          <div>应收金额（元）：60.00</div>
          <div>优惠金额（元）：-20</div>
          <div>医保支付（元）：30</div>
          <div>实收金额（元）：<strong>10.00</strong></div>
          <div>支付方式：{{ isCancelled ? "微信" : "微信支付" }}</div>
          <div>找零（元）：0.00元</div>
          <div>收费日期：2019-12-11 10:45:12</div>
          <div>收费员：林鹤</div>
          <div></div>
          <template v-if="isCancelled">
            <div>退费金额（元）：10.00</div>
            <div>退费方式：微信</div>
            <div>退费日期：2019-12-11 10:45:12</div>
            <div>操作员：林鹤</div>
            <div></div>
          </template>
        </div>
      </section>

      <section class="detail-section registration-section">
        <h2 class="detail-section-title">挂号信息</h2>
        <div class="info-table registration-table">
          <div>挂号科室：全科</div>
          <div>接诊类型：初诊</div>
          <div>接诊医生：林鹤</div>
          <div>挂号费（元）：10.00</div>
          <div>诊疗费（元）：50.00</div>
        </div>
      </section>

      <section class="detail-section patient-section">
        <h2 class="detail-section-title">患者信息</h2>
        <div class="patient-grid">
          <label class="detail-field">
            <span>患者姓名<i>*</i></span>
            <div class="text-control muted search-control">王蒙</div>
          </label>
          <label class="detail-field">
            <span>患者卡号</span>
            <input class="text-control muted" />
          </label>
          <label class="detail-field">
            <span>患者年龄<i>*</i></span>
            <div class="age-control">
              <span>23</span>
              <b>岁</b>
            </div>
          </label>
          <label class="detail-field">
            <span>出生日期<i>*</i></span>
            <div class="date-control">
              <span>1998-10-09</span>
            </div>
          </label>

          <label class="detail-field">
            <span>性别<i>*</i></span>
            <div class="text-control select-control">男</div>
          </label>
          <label class="detail-field">
            <span>手机号码</span>
            <input class="text-control" value="18865178977" />
          </label>
          <label class="detail-field">
            <span>证件号码</span>
            <input class="text-control" placeholder="请输入证件号码" />
          </label>

          <label class="detail-field address-province">
            <span>地址</span>
            <div class="text-control select-control">广东省/深圳市</div>
          </label>
          <label class="detail-field address-detail">
            <span>&nbsp;</span>
            <input class="text-control" placeholder="请输入详细地址" />
          </label>

          <label class="detail-field remark-field">
            <span>备注</span>
            <input class="text-control" />
          </label>
        </div>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { registrationApi } from "@/api";
import { readNavigationState } from "@/utils/navigation";

const route = useRoute();
const router = useRouter();
const detail = ref<any>(null);
const registrationId = readNavigationState(route);

const isCancelled = computed(() => String(detail.value?.status || "").toUpperCase() === "CANCELLED");

const goBack = () => {
  router.back();
};


onMounted(async () => {
  if (!registrationId) return;
  const response: any = await registrationApi.get(registrationId);
  detail.value = response?.data || null;
});

const startVisit = async () => {
  if (!registrationId || isCancelled.value) return;
  await registrationApi.startVisit(registrationId);
  router.push({ path: "/consultation/new", state: { registrationId } });
};
</script>

<style scoped>
.registration-detail-page {
  min-height: 100%;
  padding: 32px 0 88px;
  box-sizing: border-box;
}

.registration-detail-card {
  width: min(1715px, calc(100% - 184px));
  min-height: 1090px;
  margin: 0 auto;
  padding: 38px 32px 64px;
  box-sizing: border-box;
  border-radius: 5px;
  background: #fff;
  box-shadow: 0 1px 9px rgba(23, 31, 56, 0.06);
  overflow: hidden;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-page-title {
  display: flex;
  align-items: center;
  margin: 0;
  color: #111722;
  font-size: 25px;
  font-weight: 700;
  line-height: 1;
}

.detail-page-title::before {
  content: "";
  width: 6px;
  height: 25px;
  margin-right: 10px;
  background: #636be8;
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 21px;
}

.primary-action,
.back-action {
  height: 50px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 13px;
  border-radius: 4px;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
}

.primary-action {
  width: 149px;
  border: 0;
  background: #636be8;
  color: #fff;
}

.back-action {
  width: 149px;
  border: 1px solid #636be8;
  background: #fff;
  color: #636be8;
}

.save-icon {
  width: 21px;
  height: 21px;
  position: relative;
  box-sizing: border-box;
  border: 4px solid currentColor;
  border-radius: 2px;
}

.save-icon::before {
  content: "";
  position: absolute;
  left: 4px;
  right: 4px;
  bottom: -4px;
  height: 9px;
  border: 3px solid currentColor;
  border-bottom: 0;
  background: transparent;
}

.back-icon {
  width: 29px;
  height: 22px;
  position: relative;
}

.back-icon::before {
  content: "";
  position: absolute;
  left: 2px;
  top: 2px;
  width: 16px;
  height: 16px;
  border-left: 8px solid currentColor;
  border-bottom: 8px solid currentColor;
  transform: rotate(45deg);
}

.back-icon::after {
  content: "";
  position: absolute;
  left: 10px;
  top: 8px;
  width: 20px;
  height: 8px;
  background: currentColor;
}

.detail-section-title {
  margin: 0;
  color: #636be8;
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.order-section {
  margin-top: 42px;
}

.registration-section {
  margin-top: 56px;
}

.patient-section {
  margin-top: 58px;
}

.info-table {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  margin-top: 34px;
  border-top: 1px solid #dfe2ff;
  border-left: 1px solid #dfe2ff;
}

.info-table > div {
  min-height: 53px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  padding: 0 14px;
  border-right: 1px solid #dfe2ff;
  border-bottom: 1px solid #dfe2ff;
  color: #111722;
  font-size: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-table strong {
  color: #ffbc00;
  font-weight: 400;
}

.registration-table {
  margin-top: 39px;
}

.patient-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 368px));
  column-gap: 44px;
  row-gap: 19px;
  margin-top: 39px;
}

.detail-field {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #111722;
  font-size: 16px;
  line-height: 1;
}

.detail-field i {
  color: #ff0000;
  font-style: normal;
}

.text-control,
.age-control,
.date-control {
  width: 100%;
  height: 58px;
  box-sizing: border-box;
  border: 2px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
  color: #111722;
  font-size: 20px;
}

input.text-control {
  padding: 0 18px;
  outline: none;
}

input.text-control::placeholder {
  color: #c9c9c9;
}

.muted {
  background: #f5f5f5;
}

div.text-control {
  display: flex;
  align-items: center;
  padding: 0 18px;
}

.search-control {
  position: relative;
  padding-right: 48px;
}

.search-control::before {
  content: "";
  position: absolute;
  right: 26px;
  top: 20px;
  width: 13px;
  height: 13px;
  box-sizing: border-box;
  border: 3px solid #d8d8d8;
  border-radius: 50%;
}

.search-control::after {
  content: "";
  position: absolute;
  right: 19px;
  top: 35px;
  width: 11px;
  height: 3px;
  border-radius: 3px;
  background: #d8d8d8;
  transform: rotate(45deg);
}

.select-control {
  position: relative;
  padding-right: 48px;
}

.select-control::after {
  content: "";
  position: absolute;
  right: 24px;
  top: 21px;
  width: 12px;
  height: 12px;
  border-right: 3px solid #d1d1d1;
  border-bottom: 3px solid #d1d1d1;
  transform: rotate(45deg);
}

.age-control {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 103px;
  overflow: hidden;
}

.age-control span,
.age-control b {
  display: flex;
  align-items: center;
  height: 100%;
  box-sizing: border-box;
}

.age-control span {
  padding-left: 18px;
}

.age-control b {
  justify-content: center;
  border-left: 2px solid #eeeeee;
  font-weight: 400;
}

.date-control {
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 50px 0 18px;
}

.date-control::before {
  content: "";
  position: absolute;
  right: 20px;
  top: 20px;
  width: 19px;
  height: 17px;
  box-sizing: border-box;
  border: 3px solid #cfcfcf;
  border-top-width: 5px;
}

.date-control::after {
  content: "";
  position: absolute;
  right: 24px;
  top: 16px;
  width: 11px;
  height: 6px;
  border-left: 3px solid #cfcfcf;
  border-right: 3px solid #cfcfcf;
}

.address-province {
  grid-column: 1 / 2;
}

.address-detail {
  grid-column: 2 / 4;
}

.remark-field {
  grid-column: 1 / 4;
}

@media (max-width: 1500px) {
  .registration-detail-card {
    width: calc(100% - 64px);
  }

  .patient-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    column-gap: 28px;
  }

  .info-table > div {
    font-size: 18px;
  }
}

@media (max-width: 1180px) {
  .registration-detail-card {
    width: calc(100% - 32px);
  }

  .patient-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .address-detail,
  .remark-field {
    grid-column: 1 / 3;
  }
}
</style>
