import { useState } from "react";
import { Html } from "@react-three/drei";
import { objectEmoji } from "../game/objects";
import { EmojiSprite } from "./EmojiSprite";
import type { OwnOffice, OpponentOffice, ShotOutcome } from "../api/types";

const CELL = 1;
const GAP = 0.08;

function cellPosition(x: number, y: number, width: number, height: number): [number, number, number] {
  return [x - (width - 1) / 2, (height - 1) / 2 - y, 0];
}

function outcomeMark(outcome: ShotOutcome, objectType: string | null): string {
  switch (outcome) {
    case "MISS":
      return "💧";
    case "OBJECT_HIT":
      return objectType ? objectEmoji(objectType) : "💢";
    case "OBJECT_DESTROYED":
      return "💥";
    case "AVATAR_HIT":
      return "💢";
    case "AVATAR_ELIMINATED":
      return "🎯";
  }
}

function Tile({
  x,
  y,
  width,
  height,
  color,
  onClick,
  hoverColor,
}: {
  x: number;
  y: number;
  width: number;
  height: number;
  color: string;
  onClick?: (origin: { x: number; y: number }) => void;
  hoverColor?: string;
}) {
  const [hovered, setHovered] = useState(false);
  return (
    <mesh
      position={cellPosition(x, y, width, height)}
      onClick={
        onClick
          ? (e) => {
              e.stopPropagation();
              onClick({ x: e.nativeEvent.clientX, y: e.nativeEvent.clientY });
            }
          : undefined
      }
      onPointerOver={onClick ? () => setHovered(true) : undefined}
      onPointerOut={onClick ? () => setHovered(false) : undefined}
    >
      <boxGeometry args={[CELL - GAP, CELL - GAP, 0.14]} />
      <meshStandardMaterial color={hovered && hoverColor ? hoverColor : color} />
    </mesh>
  );
}

function EmojiAt({
  x,
  y,
  width,
  height,
  emoji,
  size = 0.6,
  opacity = 1,
  tint,
}: {
  x: number;
  y: number;
  width: number;
  height: number;
  emoji: string;
  size?: number;
  opacity?: number;
  tint?: string;
}) {
  const [px, py] = cellPosition(x, y, width, height);
  return (
    <group position={[px, py, 0.18]}>
      {tint && (
        <mesh>
          <circleGeometry args={[size * 0.55, 24]} />
          <meshBasicMaterial color={tint} />
        </mesh>
      )}
      <EmojiSprite emoji={emoji} size={size} position={[0, 0, 0.02]} opacity={opacity} />
    </group>
  );
}

/**
 * Nombre del dueño flotando sobre el tablero, vía drei/Html en modo de proyección simple
 * (sin `distanceFactor`, que solo tiene sentido con cámara en perspectiva y con la
 * ortográfica produce una escala completamente rota).
 */
function BoardLabel({ text, height }: { text: string; height: number }) {
  return (
    <Html position={[0, (height - 1) / 2 + 0.85, 0]} center zIndexRange={[10, 0]}>
      <div className="board3d-label">{text}</div>
    </Html>
  );
}

/** Placa de color detrás del tablero: identifica de quién es de un vistazo. */
function BoardBacking({ width, height, color }: { width: number; height: number; color: string }) {
  return (
    <mesh position={[0, 0, -0.1]}>
      <planeGeometry args={[width + 0.35, height + 0.35]} />
      <meshBasicMaterial color={color} transparent opacity={0.35} />
    </mesh>
  );
}

/** Tu oficina en la escena 3D: totalmente visible, con avatar, objetos y disparos recibidos. */
export function OwnBoardGroup({
  office,
  color,
  label,
  position,
}: {
  office: OwnOffice;
  color: string;
  label: string;
  position: [number, number, number];
}) {
  const { width, height } = office;
  const shotByCell = new Map(office.shots.map((s) => [`${s.x},${s.y}`, s]));

  const tiles = [];
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const shot = shotByCell.get(`${x},${y}`);
      tiles.push(
        <Tile key={`${x},${y}`} x={x} y={y} width={width} height={height} color={shot ? "#f3c9ca" : "#e9edfb"} />
      );
    }
  }

  return (
    <group position={position}>
      <BoardBacking width={width} height={height} color={color} />
      <BoardLabel text={label} height={height} />
      {tiles}
      {office.avatar && (
        <EmojiAt x={office.avatar.x} y={office.avatar.y} width={width} height={height} emoji="🧑" tint={color} />
      )}
      {office.objects.map((o) => (
        <EmojiAt
          key={o.id}
          x={o.x}
          y={o.y}
          width={width}
          height={height}
          emoji={objectEmoji(o.type)}
          opacity={o.destroyed ? 0.4 : 1}
        />
      ))}
    </group>
  );
}

/** Oficina de un rival en la escena 3D: oculta salvo las celdas ya disparadas por alguien. */
export function OpponentBoardGroup({
  office,
  color,
  label,
  position,
  canShoot,
  onShoot,
}: {
  office: OpponentOffice;
  color: string;
  label: string;
  position: [number, number, number];
  canShoot: boolean;
  onShoot: (x: number, y: number, origin: { x: number; y: number }) => void;
}) {
  const { width, height } = office;
  const revealedByCell = new Map(office.revealed.map((r) => [`${r.x},${r.y}`, r]));

  const tiles = [];
  const marks = [];
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const r = revealedByCell.get(`${x},${y}`);
      if (r) {
        const hit = r.outcome !== "MISS";
        tiles.push(
          <Tile key={`${x},${y}`} x={x} y={y} width={width} height={height} color={hit ? "#ffdfe0" : "#d7e9ff"} />
        );
        marks.push(
          <EmojiAt
            key={`m-${x},${y}`}
            x={x}
            y={y}
            width={width}
            height={height}
            emoji={outcomeMark(r.outcome, r.objectType)}
            size={0.45}
          />
        );
      } else {
        tiles.push(
          <Tile
            key={`${x},${y}`}
            x={x}
            y={y}
            width={width}
            height={height}
            color="#dbe0f3"
            hoverColor={canShoot ? "#cdd6ff" : undefined}
            onClick={canShoot ? (origin) => onShoot(x, y, origin) : undefined}
          />
        );
      }
    }
  }

  return (
    <group position={position}>
      <BoardBacking width={width} height={height} color={color} />
      <BoardLabel text={label} height={height} />
      {tiles}
      {marks}
    </group>
  );
}

export { cellPosition };
