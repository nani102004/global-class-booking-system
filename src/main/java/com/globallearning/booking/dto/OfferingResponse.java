package com.globallearning.booking.dto;


import com.globallearning.booking.enums.OfferingStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OfferingResponse {

    private Long offeringId;
    private Long courseId;
    private String courseTitle;
    private Long teacherId;
    private String teacherName;
    private String title;
    private String timezone;
    private Integer capacity;
    private OfferingStatus status;
}