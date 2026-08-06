import { create } from "zustand";
import { persist } from "zustand/middleware";

interface PreferencesStore {
  lastNickname: string;
  lastRoomCode: string;
  setLastNickname: (nickname: string) => void;
  setLastRoomCode: (code: string) => void;
}

/**
 * Preferencias de "ingreso rápido": separadas de la sesión activa (state/store.ts) a propósito,
 * para que sobrevivan a salir de una sala (clearSession) y precarguen el nickname en JoinScreen.
 */
export const usePreferences = create<PreferencesStore>()(
  persist(
    (set) => ({
      lastNickname: "",
      lastRoomCode: "",
      setLastNickname: (lastNickname) => set({ lastNickname }),
      setLastRoomCode: (lastRoomCode) => set({ lastRoomCode }),
    }),
    { name: "ow-prefs" }
  )
);
