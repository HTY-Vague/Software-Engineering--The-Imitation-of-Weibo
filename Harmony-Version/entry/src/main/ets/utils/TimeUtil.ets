/**
 * 时间格式化，等价于 Web 端 utils/time.js 的 formatDateTime。
 * 后端返回 "yyyy-MM-ddTHH:mm:ss"，此处统一转为 "yyyy-MM-dd HH:mm"。
 */
export function formatDateTime(value: string): string {
  if (value === undefined || value === null || value.length === 0) {
    return '-';
  }

  const date: Date = new Date(value);
  if (isNaN(date.getTime())) {
    return value;
  }

  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())} ` +
    `${pad2(date.getHours())}:${pad2(date.getMinutes())}`;
}

/** 仅格式化日期部分 */
export function formatDate(value: string): string {
  if (value === undefined || value === null || value.length === 0) {
    return '-';
  }

  const date: Date = new Date(value);
  if (isNaN(date.getTime())) {
    return value;
  }

  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
}

function pad2(value: number): string {
  return value < 10 ? `0${value}` : `${value}`;
}
