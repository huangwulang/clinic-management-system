import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'node:path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  build: {
    chunkSizeWarningLimit: 900,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return undefined;
          }

          if (id.includes('echarts')) {
            return 'vendor-echarts';
          }

          if (id.includes('element-plus') || id.includes('@element-plus')) {
            return 'vendor-element-plus';
          }

          if (id.includes('vue-router')) {
            return 'vendor-router';
          }

          if (
            id.includes('/vue/') ||
            id.includes('\\vue\\') ||
            id.includes('vuex')
          ) {
            return 'vendor-vue';
          }

          return 'vendor';
        },
      },
    },
  },
  server: {
    host: '0.0.0.0',
    port: 7777,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
