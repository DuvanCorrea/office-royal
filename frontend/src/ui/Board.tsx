import { useState } from "react";
import type { OpponentOffice, OwnOffice, ShotOutcome } from "../api/types";
import { objectEmoji, objectLabel } from "../game/objects";

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

/** Texto del tooltip: qué pasó en la celda y quién lo hizo. */
function tipFor(outcome: ShotOutcome, objectType: string | null, who: string): string {
  const obj = objectType ? objectLabel(objectType) : "";
  switch (outcome) {
    case "MISS":
      return `${who} disparó aquí — agua`;
    case "OBJECT_HIT":
      return `${who} dañó ${obj}`;
    case "OBJECT_DESTROYED":
      return `${who} destruyó ${obj}`;
    case "AVATAR_HIT":
      return `${who} golpeó al avatar aquí`;
    case "AVATAR_ELIMINATED":
      return `${who} eliminó al jugador aquí`;
  }
}

/** Tu oficina, totalmente visible: avatar, objetos y los disparos recibidos. */
export function OwnBoard({
  office,
  color,
  nameOf,
}: {
  office: OwnOffice;
  color: string;
  nameOf: (id: string | null) => string;
}) {
  const objByCell = new Map(office.objects.map((o) => [`${o.x},${o.y}`, o]));
  const shotByCell = new Map(office.shots.map((s) => [`${s.x},${s.y}`, s]));

  const cells = [];
  for (let y = 0; y < office.height; y++) {
    for (let x = 0; x < office.width; x++) {
      const key = `${x},${y}`;
      const isAvatar = office.avatar?.x === x && office.avatar?.y === y;
      const obj = objByCell.get(key);
      const shot = shotByCell.get(key);
      const tip = shot ? tipFor(shot.outcome, shot.objectType ?? obj?.type ?? null, nameOf(shot.byPlayerId)) : undefined;
      cells.push(
        <div key={key} className={`cell ${shot ? "cell-shot" : ""}`} data-tip={tip}>
          {isAvatar && (
            <span className="avatar-chip" style={{ background: color }}>
              🧑
            </span>
          )}
          {!isAvatar && obj && (
            <span className={`piece ${obj.destroyed ? "destroyed" : ""}`}>
              {objectEmoji(obj.type)}
            </span>
          )}
          {shot && <span className="shot-overlay">{shot.outcome === "MISS" ? "•" : "✕"}</span>}
        </div>
      );
    }
  }

  return (
    <div className="board" style={{ gridTemplateColumns: `repeat(${office.width}, 1fr)` }}>
      {cells}
    </div>
  );
}

/** Oficina de un rival: oculta salvo las celdas ya disparadas. */
export function OpponentBoard({
  office,
  canShoot,
  onShoot,
  nameOf,
}: {
  office: OpponentOffice;
  canShoot: boolean;
  onShoot: (x: number, y: number, origin: { x: number; y: number }) => void;
  nameOf: (id: string | null) => string;
}) {
  const [pending, setPending] = useState<string | null>(null);
  const revealedByCell = new Map(office.revealed.map((r) => [`${r.x},${r.y}`, r]));

  const cells = [];
  for (let y = 0; y < office.height; y++) {
    for (let x = 0; x < office.width; x++) {
      const key = `${x},${y}`;
      const r = revealedByCell.get(key);
      if (r) {
        const hit = r.outcome !== "MISS";
        cells.push(
          <div
            key={key}
            className={`cell revealed ${hit ? "hit" : "miss"} pop`}
            data-tip={tipFor(r.outcome, r.objectType, nameOf(r.byPlayerId))}
          >
            <span className="piece">{outcomeMark(r.outcome, r.objectType)}</span>
          </div>
        );
      } else {
        const isPending = pending === key;
        cells.push(
          <button
            key={key}
            className={`cell hidden ${canShoot ? "shootable" : ""} ${isPending ? "pending" : ""}`}
            disabled={!canShoot}
            onClick={(e) => {
              const rect = e.currentTarget.getBoundingClientRect();
              setPending(key);
              onShoot(x, y, { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 });
            }}
          >
            {isPending ? "◎" : ""}
          </button>
        );
      }
    }
  }

  return (
    <div className="board" style={{ gridTemplateColumns: `repeat(${office.width}, 1fr)` }}>
      {cells}
    </div>
  );
}
