import type { PlayerView } from "../api/types";

interface Props {
  players: PlayerView[];
  currentPlayerId: string | null;
  you: string | null;
}

export function Scoreboard({ players, currentPlayerId, you }: Props) {
  const sorted = [...players].sort((a, b) => b.score - a.score);
  return (
    <div className="scoreboard card">
      <div className="panel-title">Jugadores</div>
      <ul>
        {sorted.map((p) => {
          const isTurn = p.id === currentPlayerId;
          return (
            <li key={p.id} className={`${isTurn ? "is-turn" : ""} ${p.status === "ELIMINATED" ? "dead" : ""}`}>
              <span className="dot" style={{ background: p.color }} />
              <span className="pname">
                {p.nickname}
                {p.id === you && <em> (tú)</em>}
              </span>
              <span className="pscore">{p.score}★</span>
              <span className="plives">
                {p.status === "ELIMINATED" ? "💀" : "❤".repeat(p.lives) || "—"}
              </span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
