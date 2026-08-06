import { useMemo, useRef } from "react";
import { useFrame } from "@react-three/fiber";
import type { Points, PointsMaterial } from "three";
import { BufferAttribute, BufferGeometry } from "three";

const DURATION = 0.55;
const PARTICLES = 14;

/** Ráfaga de partículas — resultado de un impacto (objeto golpeado/destruido o avatar herido). */
export function Explosion({
  position,
  color = "#ff9f0a",
  onDone,
}: {
  position: [number, number, number];
  color?: string;
  onDone: () => void;
}) {
  const ref = useRef<Points>(null);
  const t = useRef(0);

  const { geometry, velocities } = useMemo(() => {
    const positions = new Float32Array(PARTICLES * 3);
    const vel: [number, number, number][] = [];
    for (let i = 0; i < PARTICLES; i++) {
      const angle = (i / PARTICLES) * Math.PI * 2;
      const speed = 0.8 + Math.random() * 0.6;
      vel.push([Math.cos(angle) * speed, Math.sin(angle) * speed, (Math.random() - 0.5) * speed]);
      positions[i * 3] = 0;
      positions[i * 3 + 1] = 0;
      positions[i * 3 + 2] = 0;
    }
    const geo = new BufferGeometry();
    geo.setAttribute("position", new BufferAttribute(positions, 3));
    return { geometry: geo, velocities: vel };
  }, []);

  useFrame((_, delta) => {
    t.current += delta;
    const progress = Math.min(t.current / DURATION, 1);
    const pos = ref.current?.geometry.attributes.position as BufferAttribute | undefined;
    if (pos) {
      for (let i = 0; i < PARTICLES; i++) {
        const [vx, vy, vz] = velocities[i];
        pos.setXYZ(i, vx * progress, vy * progress, vz * progress);
      }
      pos.needsUpdate = true;
    }
    if (ref.current) {
      (ref.current.material as PointsMaterial).opacity = 1 - progress;
    }
    if (progress >= 1) onDone();
  });

  return (
    <points ref={ref} position={position} geometry={geometry}>
      <pointsMaterial color={color} size={0.14} transparent opacity={1} depthWrite={false} />
    </points>
  );
}
