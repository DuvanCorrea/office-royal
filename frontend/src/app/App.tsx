import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useSession } from "../state/store";
import { JoinScreen } from "../ui/JoinScreen";
import { GameScreen } from "../ui/GameScreen";

type Status = "checking" | "valid" | "invalid";

/**
 * Al montar, si hay una sesión persistida (state/store.ts) intenta retomarla validándola contra
 * el backend antes de confiar en ella — así un refresh de página vuelve a la misma partida en
 * vez de caer siempre a JoinScreen. GET /state no da 404 si el playerId ya no está en la sala
 * (solo si la sala en sí desapareció), así que la validación real es comprobar que el jugador
 * siga en state.players.
 */
export function App() {
  const session = useSession((s) => s.session);
  const clearSession = useSession((s) => s.clearSession);
  const [status, setStatus] = useState<Status>(session ? "checking" : "invalid");

  useEffect(() => {
    if (!session) {
      setStatus("invalid");
      return;
    }
    let cancelled = false;
    setStatus("checking");
    api
      .getState(session.code, session.playerId)
      .then((state) => {
        if (cancelled) return;
        const stillIn = state.players.some((p) => p.id === session.playerId);
        if (!stillIn) {
          clearSession();
          setStatus("invalid");
          return;
        }
        setStatus("valid");
      })
      .catch(() => {
        if (cancelled) return;
        clearSession();
        setStatus("invalid");
      });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session?.code, session?.playerId]);

  if (status === "checking") return <div className="loading">Reconectando…</div>;
  return <div className="app">{status === "valid" ? <GameScreen /> : <JoinScreen />}</div>;
}
