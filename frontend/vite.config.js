import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    host: true, // bind all interfaces (IPv4 + IPv6) so 127.0.0.1/localhost both work
    open: true,
    proxy: {
      // Forward API calls to the Spring Cloud Gateway (avoids CORS in dev).
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
