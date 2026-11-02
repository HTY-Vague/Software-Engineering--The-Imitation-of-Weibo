/**
 * 文本处理工具：空值兜底、HTML 标签剥离（用于列表预览）、纯文本转 HTML。
 */

/** 空值兜底，避免后端字段缺失时界面出现 undefined */
export function safeText(value: string, fallback: string = ''): string {
  if (value === undefined || value === null || value.length === 0) {
    return fallback;
  }
  return value;
}

/** 安全取整，后端字段缺失时返回兜底值 */
export function safeNumber(value: number, fallback: number = 0): number {
  if (value === undefined || value === null || isNaN(value)) {
    return fallback;
  }
  return value;
}

/** 剥离 HTML 标签并压缩空白，用于帖子卡片正文预览 */
export function stripHtml(source: string): string {
  if (source === undefined || source === null) {
    return '';
  }

  let result: string = '';
  let inTag: boolean = false;
  for (let i: number = 0; i < source.length; i++) {
    const ch: string = source[i];
    if (ch === '<') {
      inTag = true;
      continue;
    }
    if (ch === '>') {
      inTag = false;
      continue;
    }
    if (!inTag) {
      result += ch;
    }
  }

  return result.replace(/\s+/g, ' ').trim();
}

/** 生成卡片预览文本，对齐 Web 端 PostCard 的 140 字截断 */
export function buildPreview(source: string, maxLength: number = 140): string {
  const plain: string = stripHtml(source);
  if (plain.length === 0) {
    return '';
  }
  return plain.length > maxLength ? `${plain.slice(0, maxLength)}...` : plain;
}

/**
 * 纯文本转 HTML：App 端发帖降级为纯文本，
 * 换行转成 <br/> 后再提交，保证 Web 端与 App 端展示一致。
 */
export function plainTextToHtml(source: string): string {
  if (source === undefined || source === null || source.length === 0) {
    return '';
  }

  const escaped: string = source
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
  return escaped.replace(/\n/g, '<br/>');
}

/** 去掉回复框中开头的 "@xxx " 前缀，对齐 Web 端 CommentItem 的提交逻辑 */
export function trimReplyPrefix(source: string): string {
  return source.replace(/^@\S+\s*/, '').trim();
}
