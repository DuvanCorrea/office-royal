package com.officewars.application.port;

/** Puerto de notificación en tiempo real. La implementación (STOMP/WebSocket) vive en infrastructure. */
public interface GameEventPublisher {

    /** Notifica a todos los suscriptores de la sala que su estado cambió (deben refrescar). */
    void notifyRoomChanged(String code);
}
