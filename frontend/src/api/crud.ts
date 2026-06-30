import request from "./request";

export type PageParams = {
  page?: number;
  size?: number;
  keyword?: string;
};

export type PageResult<T> = {
  records: T[];
  total: number;
  page: number;
  size: number;
};

export type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
  timestamp: string;
};

export const withPageDefaults = (params: PageParams = {}): Required<Pick<PageParams, "page" | "size">> & PageParams => ({
  ...params,
  page: Number(params.page) > 0 ? Number(params.page) : 1,
  size: Number(params.size) > 0 ? Number(params.size) : 10,
});

export const createCrudApi = <T extends Record<string, any>>(resource: string) => ({
  page(params: PageParams = {}) {
    return request.get<any, ApiResult<PageResult<T>>>(resource, {
      params: withPageDefaults(params),
    });
  },
  list(keyword?: string) {
    return request.get<any, ApiResult<T[]>>(`${resource}/list`, { params: { keyword } });
  },
  get(id: number | string) {
    return request.get<any, ApiResult<T>>(`${resource}/${id}`);
  },
  create(data: Partial<T>) {
    return request.post<any, ApiResult<T>>(resource, data);
  },
  update(id: number | string, data: Partial<T>) {
    return request.put<any, ApiResult<T>>(`${resource}/${id}`, data);
  },
  remove(id: number | string) {
    return request.delete<any, ApiResult<void>>(`${resource}/${id}`);
  },
});
