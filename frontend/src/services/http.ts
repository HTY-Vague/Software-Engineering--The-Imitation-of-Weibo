import axios, { type AxiosResponse } from "axios";
import type { ApiResponse } from "../types";

const baseURL =
  import.meta.env.VITE_API_BASE_URL ?? "http://122.51.87.179:8080";

const http = axios.create({
  baseURL,
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export class ApiError extends Error {
  code: number;

  constructor(code: number, message: string) {
    super(message);
    this.code = code;
  }
}

function unwrap<T>(res: AxiosResponse<ApiResponse<T>>): T {
  const { code, message, data } = res.data;
  if (code !== 200) {
    throw new ApiError(code, message);
  }
  return data;
}

export { http, unwrap };
