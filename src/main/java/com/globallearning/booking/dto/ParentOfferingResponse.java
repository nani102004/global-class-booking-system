package com.globallearning.booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParentOfferingResponse {

    private Long offeringId;
    private String offeringTitle;
    private Long courseId;
    private String courseTitle;
    private String teacherName;
    private Integer remainingSeats;
    private List<ParentSessionResponse> sessions;
}