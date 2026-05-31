package com.globallearning.booking.exception;


/*
Invalid timezone
Invalid session time
End time before start time
Invalid request data
 */
public class InvalidInputException extends ServiceException {

    public InvalidInputException(String message, String userFriendlyMessage) {
        super(message, userFriendlyMessage);
    }
}