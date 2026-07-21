import { useEffect, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../api/client";
import { subscribeRoom } from "../api/ws";
import { useSession } from "../state/store";
import { Lobby } from "./Lobby";
import { Preparation } from "./Preparation";
import { Battle } from "./Battle";
import { Scoreboard } from "./Scoreboard";
import { Feed } from "./Feed";
import { HowToPlay } from "./HowToPlay";
import { ThemeToggle } from "./ThemeToggle";
import type { RoomState } from "../api/types";

export function GameScreen() {
  const session = useSession((s) => s.session)!;
  const clearSession = useSession((s) => s.clearSession);
  const queryClient = useQueryClient();
  const { code, playerId } = session;
  const queryKey = ["state", code, playerId];
  const [showHelp, setShowHelp] = useState(false);

  const { data: state } = useQuery({
    queryKey,
    queryFn: () => api.getState(code, playerId),
    refetchInterval: 15000,
  });

  const applyState = (fresh: RoomState) => queryClient.setQueryData(queryKey, fresh);

  useEffect(() => {
    const unsubscribe = subscribeRoom(code, () => {
      queryClient.invalidateQueries({ queryKey });
    });
    return unsubscribe;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [code, playerId]);

  if (!state) return <div className="loading">Cargando sala…</div>;

  const showSidebar = state.status === "RUNNING" || state.status === "FINISHED";

  return (
    <div className="game">
      <header className="topbar">
        <div className="brand">
          Office <span>Wars</span>
        </div>
        <button
          className="room-chip"
          onClick={() => navigator.clipboard?.writeText(code)}
          title="Copiar código"
        >
          Sala <strong>{code}</strong> · copiar
        </button>
        <button className="btn ghost" onClick={() => setShowHelp(true)}>
          ¿Cómo jugar?
        </button>
        <ThemeToggle />
        <button
          className="btn ghost"
          onClick={async () => {
            await api.leave(code, playerId); // libera la sala (se borra si queda vacía)
            clearSession();
          }}
        >
          Salir
        </button>
      </header>

      <div className="stage">
        <main className="content">
          {state.status === "WAITING" && (
            <Lobby state={state} code={code} playerId={playerId} applyState={applyState} />
          )}
          {state.status === "PREPARING" && (
            <Preparation state={state} code={code} playerId={playerId} applyState={applyState} />
          )}
          {(state.status === "RUNNING" || state.status === "FINISHED") && (
            <Battle state={state} code={code} playerId={playerId} applyState={applyState} />
          )}
        </main>

        {showSidebar && (
          <aside className="sidebar">
            <Scoreboard players={state.players} currentPlayerId={state.currentPlayerId} you={playerId} />
            <Feed feed={state.feed} />
          </aside>
        )}
      </div>

      {showHelp && <HowToPlay onClose={() => setShowHelp(false)} />}
    </div>
  );
}
