import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  // 默认的 lightningcss 对部分相邻选择器（.title + .meta 等）解析失败，
  // 切到 esbuild CSS 压缩以兼容现有 scoped CSS 写法。
  build: {
    cssMinify: 'esbuild',
  },
})
