import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

/**
 * Se suscribe a /topic/room/{code}. Cada notificación (STATE_UPDATE) dispara onUpdate,
 * que el frontend usa para refrescar el estado desde REST. Devuelve una función de cierre.
 */
export function subscribeRoom(code: string, onUpdate: () => void): () => void {
  const client = new Client({
    webSocketFactory: () => new SockJS("/ws"),
    reconnectDelay: 3000,
    onConnect: () => {
      client.subscribe(`/topic/room/${code}`, () => onUpdate());
    },
  });
  client.activate();

  return () => {
    client.deactivate();
  };
}
