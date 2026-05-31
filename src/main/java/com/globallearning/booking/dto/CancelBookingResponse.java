package com.globallearning.booking.dto;

import com.globallearning.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CancelBookingResponse {

    private Long bookingId;
    private Long parentId;
    private Long offeringId;
    private BookingStatus status;
    private String message;
    private Instant bookedAt;
}