import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// ✅ Proxy setup for Spring Boot backend (port 8080)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173, // frontend port
    proxy: {
      "/api": "http://localhost:8080",
      "/oauth2": "http://localhost:8080",
      "/logout": "http://localhost:8080",
    },
  },
});
