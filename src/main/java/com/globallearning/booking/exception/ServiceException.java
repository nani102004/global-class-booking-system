package com.globallearning.booking.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    /*
     * message:
     * Internal technical message intended for logs and debugging
     *
     * userFriendlyMessage:
     * A polished, user-facing message that can be safely displayed
     * to parents/candidates through the API response without exposing
     * internal implementation details or sensitive information.(like ids and other sensitive data)
     */
    private final String userFriendlyMessage;

    public ServiceException(String message, String userFriendlyMessage) {
        super(message);
        this.userFriendlyMessage = userFriendlyMessage;
    }
}