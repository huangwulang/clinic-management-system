import type { RouteLocationNormalizedLoaded } from "vue-router";

export const readNavigationState = (
  route: RouteLocationNormalizedLoaded,
  key = "id",
): string => {
  const historyValue = window.history.state?.[key];
  const legacyValue = route.query[key] ?? route.params[key];
  const value = historyValue ?? legacyValue;
  return value == null ? "" : String(value);
};

