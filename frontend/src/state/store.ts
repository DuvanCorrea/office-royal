import { create } from "zustand";
import { persist } from "zustand/middleware";

interface Session {
  code: string;
  playerId: string;
  nickname: string;
  color: string;
}

interface SessionStore {
  session: Session | null;
  setSession: (s: Session) => void;
  clearSession: () => void;
}

/**
 * Persistida en localStorage: sobrevive a un refresh de página para poder retomar la misma
 * partida (ver App.tsx, que valida esta sesión contra el backend antes de confiar en ella).
 */
export const useSession = create<SessionStore>()(
  persist(
    (set) => ({
      session: null,
      setSession: (session) => set({ session }),
      clearSession: () => set({ session: null }),
    }),
    { name: "ow-session" }
  )
);
