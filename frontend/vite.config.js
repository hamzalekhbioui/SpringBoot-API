import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/auth': 'http://localhost:8081',
      '/tasks': 'http://localhost:8081',
      '/ws': {
        target: 'http://localhost:8081',
        ws: true
      }
    }
  }
})
