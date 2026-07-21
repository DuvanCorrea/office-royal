package com.officewars.domain.core;

/** Línea del feed global en tiempo real. */
public record FeedEntry(long seq, String type, String message, long timestamp) {
}
