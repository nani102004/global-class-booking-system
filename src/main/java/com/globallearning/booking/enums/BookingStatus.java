package com.globallearning.booking.enums;

public enum BookingStatus {

    // Booking is successful.
    // Conditions:
    // 1. Offering has all sessions in the future.(Offering has not started.)
    // 2. Capacity is available.
    // 3. Candidate has not already booked the same offering.
    // 4. No session conflict with candidate's existing confirmed bookings.
    CONFIRMED,

    // Booking attempt failed due to validation/business rule.
    // Possible reasons:
    // - Session time overlaps with existing booking.
    // - Offering capacity is full.
    // - Offering already started.
    // - Offering already completed.
    // - Offering canceled by teacher.
    // - Candidate already registered for same offering.
    FAILED,

    // Booking was confirmed earlier but later canceled by candidate.
    CANCELLED
}