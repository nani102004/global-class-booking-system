package com.globallearning.booking.util;

import com.globallearning.booking.exception.InvalidInputException;

import java.time.ZoneId;

public final class TimezoneValidator {

    private TimezoneValidator() {
    }

    /**
     * Validates the given timezone and returns its ZoneId.
     *
     * Business use:
     * - Ensures only valid IANA timezone values are accepted.
     * - Prevents invalid timezone values from being stored in DB.
     *
     * Example valid values:
     * - Asia/Kolkata
     * - America/New_York
     * - Europe/London
     *
     * @param timezone timezone string received from request
     * @return ZoneId object for the valid timezone
     */
    public static ZoneId validateAndGetZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (Exception ex) {
            throw new InvalidInputException(
                    "Invalid timezone provided: " + timezone,
                    "Invalid timezone"
            );
        }
    }
}