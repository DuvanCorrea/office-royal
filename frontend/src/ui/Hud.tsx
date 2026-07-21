import type { RoomState } from "../api/types";

interface Props {
  state: RoomState;
  playerId: string;
}

const STATUS_LABEL: Record<string, string> = {
  WAITING: "En espera",
  PREPARING: "Preparando",
  RUNNING: "En curso",
  FINISHED: "Terminada",
  ARCHIVED: "Archivada",
};

export function Hud({ state, playerId }: Props) {
  return (
    <div className="hud">
      <div className="hud-head">
        <span className={`status status-${state.status.toLowerCase()}`}>
          {STATUS_LABEL[state.status] ?? state.status}
        </span>
        {state.status === "RUNNING" && (
          <span className="turn">
            {state.currentPlayerId === playerId ? "¡Es tu turno!" : "Turno del rival"}
          </span>
        )}
      </div>

      <ul className="players">
        {state.players.map((p) => {
          const isTurn = p.id === state.currentPlayerId && state.status === "RUNNING";
          return (
            <li key={p.id} className={`player ${isTurn ? "is-turn" : ""} ${p.status === "ELIMINATED" ? "dead" : ""}`}>
              <span className="dot" style={{ background: p.color }} />
              <span className="pname">
                {p.nickname}
                {p.id === playerId && <em> (tú)</em>}
              </span>
              <span className="lives">
                {p.status === "ELIMINATED"
                  ? "💀"
                  : "❤".repeat(p.lives) || "—"}
              </span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
