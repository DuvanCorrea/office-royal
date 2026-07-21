import { useState } from "react";
import { api } from "../api/client";
import { objectEmoji, objectLabel } from "../game/objects";
import type { OwnOffice, RoomState } from "../api/types";

interface Piece {
  key: string;
  type: string;
  x: number | null;
  y: number | null;
}

interface Layout {
  avatar: { x: number; y: number } | null;
  pieces: Piece[];
}

function fromOffice(office: OwnOffice | null): Layout {
  if (!office) return { avatar: null, pieces: [] };
  return {
    avatar: office.avatar ? { x: office.avatar.x, y: office.avatar.y } : null,
    pieces: office.objects.map((o) => ({ key: o.id, type: o.type, x: o.x, y: o.y })),
  };
}

interface Props {
  state: RoomState;
  code: string;
  playerId: string;
  applyState: (s: RoomState) => void;
}

type Drag = { kind: "avatar" } | { kind: "piece"; key: string };

export function Preparation({ state, code, playerId, applyState }: Props) {
  const [layout, setLayout] = useState<Layout>(() => fromOffice(state.yourOffice));
  const [drag, setDrag] = useState<Drag | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const me = state.players.find((p) => p.id === playerId);
  const iAmReady = me?.ready ?? false;
  const w = state.officeWidth;
  const h = state.officeHeight;

  const occupant = (x: number, y: number): "avatar" | Piece | null => {
    if (layout.avatar && layout.avatar.x === x && layout.avatar.y === y) return "avatar";
    return layout.pieces.find((p) => p.x === x && p.y === y) ?? null;
  };

  function dropOnCell(x: number, y: number) {
    if (!drag) return;
    const occ = occupant(x, y);
    if (occ && !(drag.kind === "piece" && occ !== "avatar" && occ.key === drag.key)) return;
    if (drag.kind === "avatar") {
      setLayout((l) => ({ ...l, avatar: { x, y } }));
    } else {
      setLayout((l) => ({
        ...l,
        pieces: l.pieces.map((p) => (p.key === drag.key ? { ...p, x, y } : p)),
      }));
    }
    setDrag(null);
  }

  function dropOnTray() {
    if (drag?.kind === "piece") {
      setLayout((l) => ({
        ...l,
        pieces: l.pieces.map((p) => (p.key === drag.key ? { ...p, x: null, y: null } : p)),
      }));
    }
    setDrag(null);
  }

  async function autoArrange() {
    setBusy(true);
    setError(null);
    try {
      const fresh = await api.autoArrange(code, playerId);
      applyState(fresh);
      setLayout(fromOffice(fresh.yourOffice));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  async function ready() {
    if (!layout.avatar) return setError("Coloca tu avatar antes de estar listo");
    setBusy(true);
    setError(null);
    try {
      const placed = layout.pieces
        .filter((p) => p.x !== null && p.y !== null)
        .map((p) => ({ type: p.type, x: p.x as number, y: p.y as number }));
      await api.arrange(code, playerId, layout.avatar.x, layout.avatar.y, placed);
      const fresh = await api.ready(code, playerId);
      applyState(fresh);
    } catch (e) {
      setError((e as Error).message);
      setBusy(false);
    }
  }

  const tray = layout.pieces.filter((p) => p.x === null || p.y === null);

  const cells = [];
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const occ = occupant(x, y);
      cells.push(
        <div
          key={`${x},${y}`}
          className="cell drop"
          onDragOver={(e) => e.preventDefault()}
          onDrop={() => dropOnCell(x, y)}
        >
          {occ === "avatar" && (
            <span
              className="avatar-chip"
              draggable
              onDragStart={() => setDrag({ kind: "avatar" })}
              style={{ background: me?.color }}
            >
              🧑
            </span>
          )}
          {occ && occ !== "avatar" && (
            <span
              className="piece"
              draggable
              onDragStart={() => setDrag({ kind: "piece", key: occ.key })}
            >
              {objectEmoji(occ.type)}
            </span>
          )}
        </div>
      );
    }
  }

  if (iAmReady) {
    return (
      <div className="prep-wait card">
        <h2>¡Listo! 🎉</h2>
        <p>Esperando a los demás jugadores…</p>
        <ul className="ready-list">
          {state.players.map((p) => (
            <li key={p.id}>
              <span className="dot" style={{ background: p.color }} />
              {p.nickname} {p.ready ? "✅" : "⏳"}
            </li>
          ))}
        </ul>
      </div>
    );
  }

  return (
    <div className="prep">
      <div className="prep-main card">
        <h2>Arma tu oficina</h2>
        <p className="hint">
          Arrastra tu <strong>avatar</strong> 🧑 y tus objetos a las celdas. Escóndete bien: los
          rivales dispararán a ciegas.
        </p>
        <div className="board" style={{ gridTemplateColumns: `repeat(${w}, 1fr)` }}>
          {cells}
        </div>
      </div>

      <div className="prep-side">
        <div
          className="tray card"
          onDragOver={(e) => e.preventDefault()}
          onDrop={dropOnTray}
        >
          <div className="tray-title">Piezas</div>
          {!layout.avatar && (
            <span
              className="avatar-chip big"
              draggable
              onDragStart={() => setDrag({ kind: "avatar" })}
              style={{ background: me?.color }}
              title="Tu avatar"
            >
              🧑
            </span>
          )}
          {tray.map((p) => (
            <span
              key={p.key}
              className="piece big"
              draggable
              onDragStart={() => setDrag({ kind: "piece", key: p.key })}
              title={objectLabel(p.type)}
            >
              {objectEmoji(p.type)}
            </span>
          ))}
          {layout.avatar && tray.length === 0 && <span className="tray-empty">Todo colocado ✓</span>}
        </div>

        <button className="btn" disabled={busy} onClick={autoArrange}>
          🎲 Ordenar automáticamente
        </button>
        <button className="btn primary" disabled={busy} onClick={ready}>
          ✓ Estoy listo
        </button>
        {error && <div className="error">{error}</div>}

        <ul className="ready-list card">
          {state.players.map((p) => (
            <li key={p.id}>
              <span className="dot" style={{ background: p.color }} />
              {p.nickname} {p.ready ? "✅" : "⏳"}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
