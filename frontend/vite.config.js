import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Forwards API calls to the Spring Boot backend during local dev so the
    // frontend can just call fetch('/api/...') with no CORS setup needed.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
