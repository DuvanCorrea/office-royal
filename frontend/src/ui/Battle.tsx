import { useState } from "react";
import { api } from "../api/client";
import { OpponentBoard, OwnBoard } from "./Board";
import type { RoomState } from "../api/types";

interface Props {
  state: RoomState;
  code: string;
  playerId: string;
  applyState: (s: RoomState) => void;
}

export function Battle({ state, code, playerId, applyState }: Props) {
  const [selected, setSelected] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const you = state.players.find((p) => p.id === playerId);
  const aliveOpponents = state.opponents.filter((o) => o.status === "ALIVE");
  const selId =
    selected && state.opponents.some((o) => o.id === selected && o.status === "ALIVE")
      ? selected
      : aliveOpponents[0]?.id ?? null;
  const target = state.opponents.find((o) => o.id === selId) ?? null;

  const isYourTurn = state.status === "RUNNING" && state.currentPlayerId === playerId;
  const canShoot = isYourTurn && !busy && !!target;

  async function shoot(x: number, y: number) {
    if (!selId) return;
    setBusy(true);
    setError(null);
    try {
      const fresh = await api.shoot(code, playerId, selId, x, y);
      applyState(fresh);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  const winner = state.winnerId ? state.players.find((p) => p.id === state.winnerId) : null;
  const currentName = state.players.find((p) => p.id === state.currentPlayerId)?.nickname;

  return (
    <div className="battle">
      {state.status === "RUNNING" && (
        <div className={`turn-banner ${isYourTurn ? "your-turn" : ""}`}>
          {isYourTurn ? "🎯 ¡Es tu turno! Elige un rival y dispara" : `⏳ Turno de ${currentName}`}
        </div>
      )}

      <div className="boards">
        <div className="board-col">
          <div className="board-head">
            <span className="avatar-chip sm" style={{ background: you?.color }}>
              🧑
            </span>
            Tu oficina
          </div>
          {state.yourOffice && <OwnBoard office={state.yourOffice} color={you?.color ?? "#888"} />}
        </div>

        <div className="board-col">
          <div className="opp-tabs">
            {state.opponents.map((o) => (
              <button
                key={o.id}
                className={`opp-tab ${o.id === selId ? "active" : ""} ${o.status === "ELIMINATED" ? "dead" : ""}`}
                disabled={o.status === "ELIMINATED"}
                onClick={() => setSelected(o.id)}
                style={{ borderColor: o.id === selId ? o.color : undefined }}
              >
                <span className="dot" style={{ background: o.color }} />
                {o.nickname}
                <span className="mini-lives">{"❤".repeat(o.lives) || "💀"}</span>
              </button>
            ))}
          </div>
          {target ? (
            <OpponentBoard office={target.office} canShoot={canShoot} onShoot={shoot} />
          ) : (
            <div className="board-empty">No quedan rivales</div>
          )}
        </div>
      </div>

      {error && <div className="toast error">{error}</div>}

      {state.status === "FINISHED" && (
        <div className="modal-backdrop">
          <div className="modal card win-modal">
            <div className="win-emoji">{winner?.id === playerId ? "🏆" : "🎮"}</div>
            <h2>{winner ? `${winner.nickname} ganó` : "Fin de la partida"}</h2>
            {winner?.id === playerId ? <p>¡Felicidades, sobreviviste!</p> : <p>¡Buena partida!</p>}
          </div>
        </div>
      )}
    </div>
  );
}
