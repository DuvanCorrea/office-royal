import { useEffect, useRef } from "react";

const NEUTRAL_EMOJI = "🏢";
const ALERT_EMOJI = "🚨";
const NEUTRAL_TITLE = "Office Wars";
const ALERT_TITLE = "🎯 ¡Tu turno! — Office Wars";
const BLINK_MS = 900;

function emojiFavicon(emoji: string): string {
  const size = 64;
  const canvas = document.createElement("canvas");
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext("2d")!;
  ctx.font = `${size * 0.8}px "Segoe UI Emoji", "Noto Color Emoji", "Apple Color Emoji", sans-serif`;
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";
  ctx.fillText(emoji, size / 2, size / 2 + size * 0.06);
  return canvas.toDataURL("image/png");
}

function faviconLink(): HTMLLinkElement {
  let link = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
  if (!link) {
    link = document.createElement("link");
    link.rel = "icon";
    document.head.appendChild(link);
  }
  return link;
}

let neutralHref: string | null = null;
let alertHref: string | null = null;

/**
 * Hace parpadear el título y el favicon de la pestaña mientras es tu turno, para notarlo
 * aunque la pestaña esté en segundo plano (donde una animación en pantalla no se ve).
 */
export function useTurnAlert(active: boolean) {
  const blinking = useRef(false);

  useEffect(() => {
    const link = faviconLink();
    if (!neutralHref) neutralHref = emojiFavicon(NEUTRAL_EMOJI);
    if (!alertHref) alertHref = emojiFavicon(ALERT_EMOJI);
    link.href = neutralHref;

    if (!active) {
      document.title = NEUTRAL_TITLE;
      return;
    }

    const interval = setInterval(() => {
      blinking.current = !blinking.current;
      link.href = blinking.current ? alertHref! : neutralHref!;
      document.title = blinking.current ? ALERT_TITLE : NEUTRAL_TITLE;
    }, BLINK_MS);

    return () => {
      clearInterval(interval);
      link.href = neutralHref!;
      document.title = NEUTRAL_TITLE;
    };
  }, [active]);
}
