import type { CreatedRoom, Joined, Placement, RoomState, RoomSummary } from "./types";

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    let message = `Error ${res.status}`;
    try {
      const body = await res.json();
      if (body?.error) message = body.error;
    } catch {
      /* respuesta sin cuerpo JSON */
    }
    throw new Error(message);
  }
  return res.json() as Promise<T>;
}

export const api = {
  listRooms: () => request<RoomSummary[]>("/api/rooms"),

  createRoom: (name?: string, listed = true) =>
    request<CreatedRoom>("/api/rooms", {
      method: "POST",
      body: JSON.stringify({ name, listed }),
    }),

  joinRoom: (code: string, nickname: string) =>
    request<Joined>(`/api/rooms/${code}/join`, {
      method: "POST",
      body: JSON.stringify({ nickname }),
    }),

  startGame: (code: string, playerId: string) =>
    request<RoomState>(`/api/rooms/${code}/start?playerId=${playerId}`, { method: "POST" }),

  arrange: (code: string, playerId: string, avatarX: number, avatarY: number, objects: Placement[]) =>
    request<RoomState>(`/api/rooms/${code}/arrange`, {
      method: "POST",
      body: JSON.stringify({ playerId, avatarX, avatarY, objects }),
    }),

  autoArrange: (code: string, playerId: string) =>
    request<RoomState>(`/api/rooms/${code}/auto-arrange?playerId=${playerId}`, { method: "POST" }),

  ready: (code: string, playerId: string) =>
    request<RoomState>(`/api/rooms/${code}/ready?playerId=${playerId}`, { method: "POST" }),

  shoot: (code: string, playerId: string, targetId: string, x: number, y: number) =>
    request<RoomState>(`/api/rooms/${code}/shot`, {
      method: "POST",
      body: JSON.stringify({ playerId, targetId, x, y }),
    }),

  leave: (code: string, playerId: string) =>
    fetch(`/api/rooms/${code}/leave?playerId=${playerId}`, { method: "POST" }).catch(() => {}),

  heartbeat: (code: string, playerId: string) =>
    fetch(`/api/rooms/${code}/heartbeat?playerId=${playerId}`, { method: "POST" }).catch(() => {}),

  getState: (code: string, playerId: string) =>
    request<RoomState>(`/api/rooms/${code}/state?playerId=${playerId}`),
};
