package com.yoga.portal.model;

/**
 * Booking lifecycle: REQUESTED -> CONFIRMED, REQUESTED -> CANCELLED, CONFIRMED -> CANCELLED.
 */
public enum BookingStatus {
    REQUESTED,
    CONFIRMED,
    CANCELLED
}
