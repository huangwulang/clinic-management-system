import axios from "axios";
import { ElMessage } from "element-plus";
import { normalizeDateTimeStrings } from "@/utils/date";
import { clearCurrentUser } from "@/utils/permissions";

const request = axios.create({
  baseURL: "/api/v1",
  timeout: 15000,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem("token") || sessionStorage.getItem("token");
  if (token) {
    config.headers.Authorization = "Bearer " + token;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    response.data = normalizeDateTimeStrings(response.data);
    return response.data;
  },
  (error) => {
    const status = error.response?.status as number | undefined;
    const backendMessage = error.response?.data?.message;
    const messages: Record<number, string> = {
      400: "请求参数不正确",
      401: "登录状态已失效，请重新登录",
      403: "当前账号没有操作权限",
      404: "请求的业务数据不存在",
      409: "数据状态已变更，请刷新后重试",
      422: "提交的数据未通过校验",
      500: "服务器暂时无法处理请求",
    };

    if (status === 401) {
      localStorage.removeItem("token");
      sessionStorage.removeItem("token");
      clearCurrentUser();
      if (window.location.pathname !== "/login") {
        window.setTimeout(() => window.location.assign("/login"), 600);
      }
    }

    const message = backendMessage
      || (status ? messages[status] : undefined)
      || (error.code === "ECONNABORTED" ? "请求超时，请稍后重试" : "网络连接异常，请检查后端服务是否启动");
    ElMessage.error(message);
    return Promise.reject(error);
  },
);

export default request;
