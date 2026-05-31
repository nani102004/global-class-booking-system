package com.globallearning.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class AddSessionsRequest {

    @NotEmpty(message = "sessions are required")
    private List<@Valid AddSessionRequest> sessions;
}