import { useRef } from "react";
import { useFrame } from "@react-three/fiber";
import type { Mesh, MeshBasicMaterial } from "three";

const DURATION = 0.5;

/** Anillo que se expande y se desvanece — resultado de un disparo fallado (agua). */
export function Splash({
  position,
  onDone,
}: {
  position: [number, number, number];
  onDone: () => void;
}) {
  const ref = useRef<Mesh>(null);
  const t = useRef(0);

  useFrame((_, delta) => {
    t.current += delta;
    const progress = Math.min(t.current / DURATION, 1);
    if (ref.current) {
      const scale = 0.15 + progress * 0.55;
      ref.current.scale.setScalar(scale);
      const material = ref.current.material as MeshBasicMaterial;
      material.opacity = 1 - progress;
    }
    if (progress >= 1) onDone();
  });

  return (
    <mesh ref={ref} position={position} rotation={[-Math.PI / 2, 0, 0]}>
      <ringGeometry args={[0.55, 0.7, 32]} />
      <meshBasicMaterial color="#1cb0f6" transparent opacity={1} depthWrite={false} />
    </mesh>
  );
}
