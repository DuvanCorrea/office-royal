import { useState } from "react";
import type { OpponentOffice, OwnOffice, ShotOutcome } from "../api/types";
import { objectEmoji } from "../game/objects";

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

/** Tu oficina, totalmente visible: avatar, objetos y los disparos recibidos. */
export function OwnBoard({ office, color }: { office: OwnOffice; color: string }) {
  const objByCell = new Map(office.objects.map((o) => [`${o.x},${o.y}`, o]));
  const shotByCell = new Map(office.shots.map((s) => [`${s.x},${s.y}`, s]));

  const cells = [];
  for (let y = 0; y < office.height; y++) {
    for (let x = 0; x < office.width; x++) {
      const key = `${x},${y}`;
      const isAvatar = office.avatar?.x === x && office.avatar?.y === y;
      const obj = objByCell.get(key);
      const shot = shotByCell.get(key);
      cells.push(
        <div key={key} className={`cell ${shot ? "cell-shot" : ""}`}>
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
}: {
  office: OpponentOffice;
  canShoot: boolean;
  onShoot: (x: number, y: number, origin: { x: number; y: number }) => void;
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
          <div key={key} className={`cell revealed ${hit ? "hit" : "miss"} pop`}>
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
              const r = e.currentTarget.getBoundingClientRect();
              setPending(key);
              onShoot(x, y, { x: r.left + r.width / 2, y: r.top + r.height / 2 });
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
