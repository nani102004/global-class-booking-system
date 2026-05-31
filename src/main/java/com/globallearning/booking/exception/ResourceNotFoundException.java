package com.globallearning.booking.exception;

/*
Course not found
Teacher not found
Offering not found
Parent not found
 */
public class ResourceNotFoundException extends ServiceException {

    public ResourceNotFoundException(String message, String userFriendlyMessage) {
        super(message, userFriendlyMessage);
    }
}