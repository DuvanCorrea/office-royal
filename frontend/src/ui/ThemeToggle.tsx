import { useTheme } from "../state/theme";

export function ThemeToggle() {
  const theme = useTheme((s) => s.theme);
  const toggle = useTheme((s) => s.toggle);
  return (
    <button
      className="theme-toggle"
      onClick={toggle}
      title={theme === "dark" ? "Cambiar a claro" : "Cambiar a oscuro"}
      aria-label="Cambiar tema"
    >
      {theme === "dark" ? "☀️" : "🌙"}
    </button>
  );
}
