/** Metadatos visuales de los objetos de oficina (emoji + etiqueta). */
export interface ObjectMeta {
  emoji: string;
  label: string;
}

export const OBJECTS: Record<string, ObjectMeta> = {
  DESK: { emoji: "🗄️", label: "Escritorio" },
  MONITOR: { emoji: "🖥️", label: "Monitor" },
  PLANT: { emoji: "🪴", label: "Planta" },
  PRINTER: { emoji: "🖨️", label: "Impresora" },
  COFFEE: { emoji: "☕", label: "Cafetera" },
  CHAIR: { emoji: "🪑", label: "Silla" },
  BIN: { emoji: "🗑️", label: "Papelera" },
  SOFA: { emoji: "🛋️", label: "Sofá" },
  WATER: { emoji: "🚰", label: "Dispensador" },
};

export const objectEmoji = (type: string): string => OBJECTS[type]?.emoji ?? "📦";
export const objectLabel = (type: string): string => OBJECTS[type]?.label ?? type;
