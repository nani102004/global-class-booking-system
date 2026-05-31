package com.globallearning.booking.dto;


import com.globallearning.booking.enums.OfferingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeacherOfferingResponse {

    private Long offeringId;
    private String offeringTitle;
    private Long courseId;
    private String courseTitle;
    private OfferingStatus status;
    private String timezone;
    private Integer capacity;
    private Integer remainingSeats;
    private List<TeacherSessionResponse> sessions;
}