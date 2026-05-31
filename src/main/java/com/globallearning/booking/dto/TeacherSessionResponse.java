package com.globallearning.booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TeacherSessionResponse {

    private Long sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}