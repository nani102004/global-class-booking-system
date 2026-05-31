package com.globallearning.booking.dto;

import com.globallearning.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class BookingResponse {

    private Long bookingId;
    private Long parentId;
    private Long offeringId;
    private String offeringTitle;
    private String courseTitle;
    private BookingStatus status;
    private String message;
    private Instant bookedAt;
    private List<ParentSessionResponse> sessions;
}