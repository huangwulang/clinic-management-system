import { computed, ref, watch, type ComputedRef } from "vue";

export const useClientPagination = <T>(
  rows: ComputedRef<T[]>,
  pageSize = 10,
) => {
  const currentPage = ref(1);
  const totalPages = computed(() => Math.max(1, Math.ceil(rows.value.length / pageSize)));
  const pageRows = computed(() => {
    const start = (currentPage.value - 1) * pageSize;
    return rows.value.slice(start, start + pageSize);
  });

  const changePage = (page: number) => {
    currentPage.value = Math.min(Math.max(1, page), totalPages.value);
  };

  const resetPage = () => {
    currentPage.value = 1;
  };

  watch(totalPages, (pages) => {
    if (currentPage.value > pages) currentPage.value = pages;
  });

  return {
    pageSize,
    currentPage,
    totalPages,
    pageRows,
    changePage,
    resetPage,
  };
};
