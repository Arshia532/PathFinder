import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Dev only: forward /api/... to Spring Boot, so the browser sees one origin (no CORS)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})