import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

const backendTarget = "http://localhost:8080";

const proxyPaths = [
  "/auth",
  "/users",
  "/roles",
  "/permissions",
  "/materials",
  "/material-groups",
  "/formulas",
  "/formula-groups",
  "/calculations",
];

const proxyConfig = Object.fromEntries(
  proxyPaths.map((path) => [
    path,
    { target: backendTarget, changeOrigin: true },
  ])
);

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: proxyConfig,
  },
  preview: {
    proxy: proxyConfig,
  },
});
