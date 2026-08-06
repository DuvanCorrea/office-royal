import { useRef, useState } from "react";
import { api } from "../api/client";
import { BoardScene, type ShotEffectRequest } from "../three/BoardScene";
import { ShotFly, type Fly } from "./ShotFly";
import { useTurnAlert } from "./useTurnAlert";
import type { RoomState } from "../api/types";

interface Props {
  state: RoomState;
  code: string;
  playerId: string;
  applyState: (s: RoomState) => void;
}

export function Battle({ state, code, playerId, applyState }: Props) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [flies, setFlies] = useState<Fly[]>([]);
  const [shotEffect, setShotEffect] = useState<ShotEffectRequest | null>(null);
  const flyId = useRef(0);
  const shotSeq = useRef(0);

  const you = state.players.find((p) => p.id === playerId);

  const isYourTurn = state.status === "RUNNING" && state.currentPlayerId === playerId;
  useTurnAlert(isYourTurn);

  async function shoot(targetId: string, x: number, y: number, origin: { x: number; y: number }) {
    setBusy(true);
    setError(null);
    const maxSeq = state.feed.reduce((m, f) => Math.max(m, f.seq), 0);
    try {
      const fresh = await api.shoot(code, playerId, targetId, x, y);
      applyState(fresh);

      // Toma el resultado de mi disparo (nueva entrada del feed) y lánzalo hacia el Historial.
      const newer = fresh.feed.filter((f) => f.seq > maxSeq);
      const entry = newer[newer.length - 1];
      if (entry) {
        const id = ++flyId.current;
        setFlies((fs) => [...fs, { id, message: entry.message, outcome: entry.type, from: origin }]);
      }

      // Dispara el arco/impacto 3D hacia la celda exacta que revelaste.
      const revealed = fresh.opponents.find((o) => o.id === targetId)?.office.revealed.find(
        (r) => r.x === x && r.y === y
      );
      if (revealed) {
        setShotEffect({
          seq: ++shotSeq.current,
          targetId,
          x,
          y,
          outcome: revealed.outcome,
          objectType: revealed.objectType,
        });
      }
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  const winner = state.winnerId ? state.players.find((p) => p.id === state.winnerId) : null;
  const currentName = state.players.find((p) => p.id === state.currentPlayerId)?.nickname;

  const opponentsWithShoot = state.opponents.map((o) => ({
    ...o,
    canShoot: isYourTurn && !busy && o.status === "ALIVE",
  }));

  return (
    <div className="battle">
      {state.status === "RUNNING" && (
        <div key={state.currentPlayerId} className={`turn-banner ${isYourTurn ? "your-turn" : ""}`}>
          {isYourTurn ? "🎯 ¡Es tu turno! Elige un rival y dispara" : `⏳ Turno de ${currentName}`}
        </div>
      )}

      {state.yourOffice && (
        <BoardScene
          you={{ color: you?.color ?? "#888" }}
          yourOffice={state.yourOffice}
          opponents={opponentsWithShoot}
          onShoot={shoot}
          shotEffect={shotEffect}
        />
      )}

      {flies.map((f) => (
        <ShotFly key={f.id} fly={f} onDone={(id) => setFlies((fs) => fs.filter((x) => x.id !== id))} />
      ))}

      {error && <div className="toast error">{error}</div>}

      {state.status === "FINISHED" && (
        <div className="modal-backdrop">
          <div className="modal card win-modal pop">
            <div className="win-emoji">{winner?.id === playerId ? "🏆" : "🎮"}</div>
            <h2>{winner ? `${winner.nickname} ganó` : "Fin de la partida"}</h2>
            {winner?.id === playerId ? <p>¡Felicidades, sobreviviste!</p> : <p>¡Buena partida!</p>}
          </div>
        </div>
      )}
    </div>
  );
}
