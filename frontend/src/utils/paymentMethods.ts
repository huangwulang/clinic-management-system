import { dictionaryApi } from "@/api";

const defaultPaymentMethods = ["现金", "支付宝", "微信", "银行卡", "会员卡", "挂账"];

const isEnabled = (value: unknown) => (
  value === true
  || value === 1
  || String(value).toUpperCase() === "TRUE"
);

export const loadEnabledPaymentMethods = async () => {
  try {
    const response: any = await dictionaryApi("PAYMENT_METHOD").list();
    const records = response?.data || [];
    if (!records.length) return defaultPaymentMethods;
    const enabledMethods = records
      .filter((item: any) => isEnabled(item.enabled))
      .sort((a: any, b: any) => Number(a.sortNo || 0) - Number(b.sortNo || 0))
      .map((item: any) => item.itemName)
      .filter(Boolean);
    return enabledMethods.length ? enabledMethods : defaultPaymentMethods;
  } catch {
    return defaultPaymentMethods;
  }
};
