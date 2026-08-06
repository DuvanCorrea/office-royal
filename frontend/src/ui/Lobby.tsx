import { useState } from "react";
import { api } from "../api/client";
import type { RoomState } from "../api/types";

interface Props {
  state: RoomState;
  code: string;
  playerId: string;
  applyState: (s: RoomState) => void;
}

export function Lobby({ state, code, playerId, applyState }: Props) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function start() {
    setBusy(true);
    setError(null);
    try {
      const fresh = await api.startGame(code, playerId);
      applyState(fresh);
    } catch (e) {
      setError((e as Error).message);
      setBusy(false);
    }
  }

  return (
    <div className="lobby card">
      <h2>Sala de espera</h2>
      <p className="hint">
        Comparte el código <strong className="code-badge">{code}</strong> para que se unan.
      </p>
      <ul className="lobby-players">
        {state.players.map((p) => (
          <li key={p.id} className="pop">
            <span className="avatar-chip" style={{ background: p.color }}>
              🧑
            </span>
            <span>{p.nickname}</span>
            {p.id === playerId && <em> (tú)</em>}
          </li>
        ))}
      </ul>
      <button
        className="btn primary big"
        disabled={busy || state.players.length < 2}
        onClick={start}
      >
        {state.players.length < 2 ? "Faltan jugadores (mín. 2)" : "▶ Empezar preparación"}
      </button>
      {error && <div className="error">{error}</div>}
    </div>
  );
}
