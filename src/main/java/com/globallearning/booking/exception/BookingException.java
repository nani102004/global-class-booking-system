package com.globallearning.booking.exception;

/*
Offering already started
Offering capacity full
Session time conflict
Candidate already registered
Offering not active
 */
public class BookingException extends ServiceException {

    public BookingException(String message, String userFriendlyMessage) {
        super(message, userFriendlyMessage);
    }
}