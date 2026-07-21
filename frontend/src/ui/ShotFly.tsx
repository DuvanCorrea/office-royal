import { useEffect, useState, type CSSProperties } from "react";
import { createPortal } from "react-dom";
import type { ShotOutcome } from "../api/types";

const EMOJI: Record<string, string> = {
  MISS: "💧",
  OBJECT_HIT: "💢",
  OBJECT_DESTROYED: "💥",
  AVATAR_HIT: "🎯",
  AVATAR_ELIMINATED: "☠️",
};

export interface Fly {
  id: number;
  message: string;
  outcome: ShotOutcome | string;
  from: { x: number; y: number };
}

/**
 * Globo temporal que aparece donde el jugador hizo clic con el mensaje del disparo y luego
 * "vuela" hacia el panel de Historial (#feed-panel), desvaneciéndose. Se autodestruye.
 */
export function ShotFly({ fly, onDone }: { fly: Fly; onDone: (id: number) => void }) {
  const [style, setStyle] = useState<CSSProperties>({
    left: fly.from.x,
    top: fly.from.y,
    transform: "translate(-50%, -50%) scale(0.6)",
    opacity: 0,
  });

  useEffect(() => {
    // 1) aparece con "pop" en el punto del clic
    const t1 = setTimeout(() => {
      setStyle((s) => ({ ...s, transform: "translate(-50%, -50%) scale(1)", opacity: 1 }));
    }, 20);

    // 2) vuela hacia el panel de Historial
    const t2 = setTimeout(() => {
      const panel = document.getElementById("feed-panel");
      const target = panel?.getBoundingClientRect();
      const tx = target ? target.left + target.width / 2 : fly.from.x + 200;
      const ty = target ? target.top + 40 : fly.from.y - 80;
      setStyle({
        left: tx,
        top: ty,
        transform: "translate(-50%, -50%) scale(0.5)",
        opacity: 0,
      });
    }, 620);

    const t3 = setTimeout(() => onDone(fly.id), 1300);
    return () => {
      clearTimeout(t1);
      clearTimeout(t2);
      clearTimeout(t3);
    };
  }, [fly, onDone]);

  const cls = fly.outcome === "MISS" ? "miss" : "hit";

  return createPortal(
    <div className={`shot-fly ${cls}`} style={style}>
      <span className="shot-fly-emoji">{EMOJI[fly.outcome] ?? "•"}</span>
      <span className="shot-fly-msg">{fly.message}</span>
    </div>,
    document.body
  );
}
