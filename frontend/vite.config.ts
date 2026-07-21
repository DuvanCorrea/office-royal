import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// En desarrollo (npm run dev) se hace proxy al backend en localhost:8080.
// En producción (Docker) Nginx sirve los estáticos y hace el proxy.
export default defineConfig({
  plugins: [react()],
  // sockjs-client referencia `global`, que no existe en el navegador.
  define: {
    global: "globalThis",
  },
  server: {
    port: 5173,
    proxy: {
      "/api": "http://localhost:8080",
      "/ws": {
        target: "http://localhost:8080",
        ws: true,
      },
    },
  },
});
