package com.globallearning.booking.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOfferingRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    @NotNull(message = "teacherId is required")
    private Long teacherId;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "timezone is required")
    private String timezone;

    @NotNull(message = "capacity is required")
    private Integer capacity;
}