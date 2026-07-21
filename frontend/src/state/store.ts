import { create } from "zustand";

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

export const useSession = create<SessionStore>((set) => ({
  session: null,
  setSession: (session) => set({ session }),
  clearSession: () => set({ session: null }),
}));
