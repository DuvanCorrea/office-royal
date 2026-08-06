import { useRef } from "react";
import { useFrame } from "@react-three/fiber";
import { Vector3 } from "three";
import type { Group } from "three";

const DURATION = 0.45;

/** Proyectil que viaja de tu tablero al del rival dibujando un arco; al llegar dispara onArrive. */
export function ShotArc({
  from,
  to,
  onArrive,
}: {
  from: [number, number, number];
  to: [number, number, number];
  onArrive: () => void;
}) {
  const ref = useRef<Group>(null);
  const t = useRef(0);
  const arrived = useRef(false);
  const start = useRef(new Vector3(...from));
  const end = useRef(new Vector3(...to));

  useFrame((_, delta) => {
    if (arrived.current) return;
    t.current += delta;
    const progress = Math.min(t.current / DURATION, 1);
    const arcHeight = 1.6 * Math.sin(progress * Math.PI);
    if (ref.current) {
      ref.current.position.lerpVectors(start.current, end.current, progress);
      ref.current.position.y += arcHeight;
    }
    if (progress >= 1) {
      arrived.current = true;
      onArrive();
    }
  });

  return (
    <group ref={ref}>
      <mesh>
        <sphereGeometry args={[0.12, 12, 12]} />
        <meshStandardMaterial color="#ffdd55" emissive="#ff9f0a" emissiveIntensity={0.8} />
      </mesh>
    </group>
  );
}
