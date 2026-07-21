package com.officewars.application;

/** Error de regla de juego o de solicitud inválida (se traduce a HTTP 400/404 en la web). */
public class GameException extends RuntimeException {

    public GameException(String message) {
        super(message);
    }
}
