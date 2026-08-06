import * as THREE from "three";

const cache = new Map<string, THREE.CanvasTexture>();

/**
 * Convierte un emoji en una textura reutilizable dibujándolo en un canvas offscreen. Así se
 * evita depender de paquetes de modelos 3D licenciados: el mismo set de emoji que ya usa toda
 * la app (game/objects.ts) se convierte en sprite con una técnica estándar de Three.js.
 */
export function emojiTexture(emoji: string): THREE.CanvasTexture {
  const cached = cache.get(emoji);
  if (cached) return cached;

  const size = 128;
  const canvas = document.createElement("canvas");
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext("2d")!;
  ctx.font = `${size * 0.78}px "Segoe UI Emoji", "Noto Color Emoji", "Apple Color Emoji", sans-serif`;
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";
  ctx.fillText(emoji, size / 2, size / 2 + size * 0.05);

  const texture = new THREE.CanvasTexture(canvas);
  texture.colorSpace = THREE.SRGBColorSpace;
  cache.set(emoji, texture);
  return texture;
}
