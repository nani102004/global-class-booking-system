package com.globallearning.booking.enums;

public enum OfferingStatus {

    // No sessions are added for the offering yet.
    DRAFT,

    // Sessions exist, first session is in the future,
    // and seats are still available.
    ACTIVE,

    // Sessions exist and are in the future,
    // but all seats are already booked.
    CLOSED,

    // First session has already started/completed,
    // and at least one future session is still remaining.
    // Late booking is not allowed.
    IN_PROGRESS,

    // All session end times are in the past.
    // Candidate cannot book this offering.
    COMPLETED,

    // Teacher manually canceled the offering.
    // Even future sessions cannot be booked.
    CANCELLED
}