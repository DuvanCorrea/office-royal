import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";
import { useSession } from "../state/store";
import { HowToPlay } from "./HowToPlay";
import type { RoomSummary } from "../api/types";

export function JoinScreen() {
  const setSession = useSession((s) => s.setSession);
  const [nickname, setNickname] = useState("");
  const [code, setCode] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [showCode, setShowCode] = useState(false);
  const [showHelp, setShowHelp] = useState(false);

  const { data: rooms = [] } = useQuery({
    queryKey: ["rooms"],
    queryFn: api.listRooms,
    refetchInterval: 3000,
  });

  async function enter(roomCode: string) {
    if (!nickname.trim()) return setError("Escribe tu nickname");
    setBusy(true);
    setError(null);
    try {
      const joined = await api.joinRoom(roomCode, nickname.trim());
      setSession({
        code: roomCode,
        playerId: joined.playerId,
        nickname: joined.nickname,
        color: joined.color,
      });
    } catch (e) {
      setError((e as Error).message);
      setBusy(false);
    }
  }

  async function createAndEnter() {
    if (!nickname.trim()) return setError("Escribe tu nickname");
    setBusy(true);
    setError(null);
    try {
      const room = await api.createRoom();
      await enter(room.code);
    } catch (e) {
      setError((e as Error).message);
      setBusy(false);
    }
  }

  return (
    <div className="home">
      <div className="home-card card">
        <div className="home-brand">
          Office <span>Wars</span> <span className="home-emoji">🎯</span>
        </div>
        <p className="home-tag">Batalla de oficina por turnos. Escóndete y encuentra a los demás.</p>

        <label className="field">
          <span>Tu nickname</span>
          <input value={nickname} onChange={(e) => setNickname(e.target.value)} placeholder="Ej. Carlos" maxLength={14} />
        </label>

        <div className="servers">
          <div className="servers-head">
            <span>🎮 Servidores</span>
            <span className="pill">{rooms.length}</span>
          </div>
          <div className="server-list">
            {rooms.length === 0 && <div className="server-empty">No hay salas abiertas — ¡crea una!</div>}
            {rooms.map((r: RoomSummary) => (
              <button
                key={r.code}
                className="server"
                disabled={busy || r.players >= r.maxPlayers}
                onClick={() => enter(r.code)}
              >
                <span className="server-name">{r.name}</span>
                <span className="server-meta">
                  {r.players}/{r.maxPlayers} <span className="server-go">▶</span>
                </span>
              </button>
            ))}
          </div>
        </div>

        <button className="btn primary big" disabled={busy} onClick={createAndEnter}>
          ▶ Crear sala
        </button>

        <div className="home-actions">
          <button className="btn" onClick={() => setShowCode((v) => !v)}>
            {showCode ? "✕ Cerrar" : "# Unirse por código"}
          </button>
          <button className="btn" onClick={() => setShowHelp(true)}>
            ¿Cómo jugar?
          </button>
        </div>

        {showCode && (
          <div className="code-row">
            <input
              value={code}
              onChange={(e) => setCode(e.target.value.toUpperCase())}
              placeholder="AB34JK"
              maxLength={6}
              className="code-input"
            />
            <button className="btn primary" disabled={busy} onClick={() => enter(code.trim())}>
              Entrar
            </button>
          </div>
        )}

        {error && <div className="error">{error}</div>}
      </div>

      {showHelp && <HowToPlay onClose={() => setShowHelp(false)} />}
    </div>
  );
}
