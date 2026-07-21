import { create } from "zustand";

export type Theme = "dark" | "light";

const KEY = "ow-theme";

function initialTheme(): Theme {
  const stored = localStorage.getItem(KEY);
  if (stored === "dark" || stored === "light") return stored;
  return "dark"; // por defecto oscuro
}

function apply(theme: Theme) {
  document.documentElement.dataset.theme = theme;
}

interface ThemeStore {
  theme: Theme;
  toggle: () => void;
}

export const useTheme = create<ThemeStore>((set, get) => {
  const theme = initialTheme();
  apply(theme);
  return {
    theme,
    toggle: () => {
      const next: Theme = get().theme === "dark" ? "light" : "dark";
      localStorage.setItem(KEY, next);
      apply(next);
      set({ theme: next });
    },
  };
});
