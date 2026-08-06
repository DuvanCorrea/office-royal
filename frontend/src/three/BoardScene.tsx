import { useEffect, useMemo, useState } from "react";
import { Canvas, useThree } from "@react-three/fiber";
import { OrthographicCamera } from "@react-three/drei";
import type { OrthographicCamera as ThreeOrthographicCamera } from "three";
import { OwnBoardGroup, OpponentBoardGroup, cellPosition } from "./BoardGroup";
import { ShotArc } from "./effects/ShotArc";
import { Explosion } from "./effects/Explosion";
import { Splash } from "./effects/Splash";
import type { OwnOffice, OpponentView } from "../api/types";

const OPP_MAX_COLUMNS = 3;
const BOARD_GAP = 1.3;

/** Último disparo que hiciste, para animar el arco/impacto en la escena 3D. */
export interface ShotEffectRequest {
  seq: number;
  targetId: string;
  x: number;
  y: number;
  outcome: string;
  objectType: string | null;
}

interface ActiveEffect {
  id: number;
  phase: "arc" | "impact";
  from: [number, number, number];
  to: [number, number, number];
  outcome: string;
}

interface Props {
  you: { color: string };
  yourOffice: OwnOffice;
  opponents: (OpponentView & { canShoot: boolean })[];
  onShoot: (targetId: string, x: number, y: number, origin: { x: number; y: number }) => void;
  shotEffect: ShotEffectRequest | null;
}

/** Ajusta el zoom de la cámara ortográfica para que todo el contenido entre en el canvas. */
function FitCamera({ contentWidth, contentHeight }: { contentWidth: number; contentHeight: number }) {
  const camera = useThree((s) => s.camera) as ThreeOrthographicCamera;
  const size = useThree((s) => s.size);

  useEffect(() => {
    const margin = 1.15;
    const zoomX = size.width / (contentWidth * margin);
    const zoomY = size.height / (contentHeight * margin);
    camera.zoom = Math.max(4, Math.min(zoomX, zoomY));
    camera.updateProjectionMatrix();
  }, [camera, size.width, size.height, contentWidth, contentHeight]);

  return null;
}

export function BoardScene({ you, yourOffice, opponents, onShoot, shotEffect }: Props) {
  const [effects, setEffects] = useState<ActiveEffect[]>([]);
  const nextEffectId = useMemo(() => ({ current: 0 }), []);

  const layout = useMemo(() => {
    const boardW = yourOffice.width;
    const boardH = yourOffice.height;
    const unitX = boardW + BOARD_GAP;
    const unitY = boardH + BOARD_GAP;
    const columns = Math.min(OPP_MAX_COLUMNS, Math.max(1, Math.ceil(Math.sqrt(opponents.length))));
    const rows = Math.max(1, Math.ceil(opponents.length / columns));

    const ownPosition: [number, number, number] = [-(unitX / 2 + unitX * 0.6), 0, 0];
    const oppOriginX = unitX / 2 + unitX * 0.6;

    const oppPositions = new Map<string, [number, number, number]>();
    opponents.forEach((o, i) => {
      const col = i % columns;
      const row = Math.floor(i / columns);
      const rowCenterOffset = ((rows - 1) / 2) * unitY;
      oppPositions.set(o.id, [oppOriginX + col * unitX, rowCenterOffset - row * unitY, 0]);
    });

    const oppCols = Math.min(columns, opponents.length) || 1;
    const contentWidth = unitX * 0.6 * 2 + unitX + oppCols * unitX;
    const contentHeight = Math.max(unitY, rows * unitY);

    return { ownPosition, oppPositions, contentWidth, contentHeight };
  }, [yourOffice.width, yourOffice.height, opponents.length]);

  // Cuando Battle.tsx registra un disparo tuyo, lanza el arco proyectil hacia el tablero rival.
  useEffect(() => {
    if (!shotEffect) return;
    const target = layout.oppPositions.get(shotEffect.targetId);
    const targetOffice = opponents.find((o) => o.id === shotEffect.targetId)?.office;
    if (!target || !targetOffice) return;

    const [lx, ly] = cellPosition(shotEffect.x, shotEffect.y, targetOffice.width, targetOffice.height);
    const to: [number, number, number] = [target[0] + lx, target[1] + ly, target[2] + 0.3];
    const id = ++nextEffectId.current;
    setEffects((fx) => [
      ...fx,
      { id, phase: "arc", from: [...layout.ownPosition] as [number, number, number], to, outcome: shotEffect.outcome },
    ]);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shotEffect?.seq]);

  function onArcArrive(id: number) {
    setEffects((fx) => fx.map((e) => (e.id === id ? { ...e, phase: "impact" } : e)));
  }

  function onImpactDone(id: number) {
    setEffects((fx) => fx.filter((e) => e.id !== id));
  }

  return (
    <div className="board3d-wrap">
      <Canvas dpr={[1, 2]} style={{ flex: 1, minHeight: 0 }}>
        <OrthographicCamera makeDefault position={[0, 0, 20]} near={0.1} far={200} />
        <FitCamera contentWidth={layout.contentWidth} contentHeight={layout.contentHeight} />
        <ambientLight intensity={0.9} />
        <directionalLight position={[4, 6, 8]} intensity={0.6} />

        <OwnBoardGroup office={yourOffice} color={you.color} label="Tu oficina" position={layout.ownPosition} />

        {opponents.map((o) => {
          const pos = layout.oppPositions.get(o.id)!;
          return (
            <OpponentBoardGroup
              key={o.id}
              office={o.office}
              color={o.status === "ELIMINATED" ? "#8a8f9c" : o.color}
              label={o.status === "ELIMINATED" ? `☠️ ${o.nickname}` : o.nickname}
              position={pos}
              canShoot={o.canShoot}
              onShoot={(x, y, origin) => onShoot(o.id, x, y, origin)}
            />
          );
        })}

        {effects.map((e) =>
          e.phase === "arc" ? (
            <ShotArc key={e.id} from={e.from} to={e.to} onArrive={() => onArcArrive(e.id)} />
          ) : e.outcome === "MISS" ? (
            <Splash key={e.id} position={e.to} onDone={() => onImpactDone(e.id)} />
          ) : (
            <Explosion
              key={e.id}
              position={e.to}
              color={e.outcome === "AVATAR_ELIMINATED" ? "#ff4b4b" : "#ff9f0a"}
              onDone={() => onImpactDone(e.id)}
            />
          )
        )}
      </Canvas>
    </div>
  );
}
