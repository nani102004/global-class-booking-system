package com.globallearning.booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionResponse {

    private Long sessionId;
    private Long offeringId;
    private Long teacherId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}