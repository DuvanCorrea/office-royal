package com.officewars.infrastructure.web;

import com.officewars.application.port.GameEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Adaptador STOMP del puerto GameEventPublisher. Notifica cambios a /topic/room/{code}. */
@Component
public class StompGameEventPublisher implements GameEventPublisher {

    private final SimpMessagingTemplate messaging;

    public StompGameEventPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @Override
    public void notifyRoomChanged(String code) {
        messaging.convertAndSend("/topic/room/" + code, Map.of("type", "UPDATE"));
    }
}
