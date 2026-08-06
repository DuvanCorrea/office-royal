import { Billboard } from "@react-three/drei";
import { emojiTexture } from "./emojiTexture";

/** Sprite tipo billboard (siempre mirando a cámara) para un emoji: avatar u objeto de oficina. */
export function EmojiSprite({
  emoji,
  size = 0.6,
  position,
  opacity = 1,
}: {
  emoji: string;
  size?: number;
  position?: [number, number, number];
  opacity?: number;
}) {
  const texture = emojiTexture(emoji);
  return (
    <Billboard position={position}>
      <mesh>
        <planeGeometry args={[size, size]} />
        {/* dispose={null}: la textura es un THREE.CanvasTexture cacheado y compartido entre
            todos los sprites del mismo emoji (ver emojiTexture.ts) — si React-three-fiber la
            auto-destruyera al desmontar UN sprite, rompería a todos los demás que la siguen
            usando (rendering roto o crash al desmontar la escena, p. ej. al salir de la sala).
            El tipo de @react-three/fiber@8 para este prop no incluye `null` aunque el
            reconciler sí lo soporta en runtime (ver node_modules/.../events-*.esm.js) — de
            ahí el cast. */}
        <meshBasicMaterial
          map={texture}
          transparent
          opacity={opacity}
          depthWrite={false}
          dispose={null as unknown as undefined}
        />
      </mesh>
    </Billboard>
  );
}
