import React from "react";
import ReactDOM from "react-dom/client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import "@fontsource-variable/nunito";
import { useTheme } from "./state/theme";
import { App } from "./app/App";
import "./index.css";

// Inicializa el tema (aplica data-theme en <html>) antes del primer render.
useTheme.getState();

const queryClient = new QueryClient();

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>
);
