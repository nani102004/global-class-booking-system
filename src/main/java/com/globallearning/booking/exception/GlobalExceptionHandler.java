package com.globallearning.booking.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Global exception handler for the application.
 *
 * This class catches exceptions thrown from controllers/services and converts
 * them into consistent API error responses.
 *
 * Internal exception messages are logged for developers, while polished
 * user-friendly messages are returned to the candidate/parent through the API.
 *
 * This avoids exposing internal technical details and keeps error handling
 * centralized across the application.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
        log.error("Invalid input: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, getUserMessage(ex, "Invalid request data"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, getUserMessage(ex, "Requested resource not found"));
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ErrorResponse> handleBookingException(BookingException ex) {
        log.error("Booking failed: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, getUserMessage(ex, "Booking could not be completed"));
    }

    //Handles validation failures for request DTOs annotated with @Valid.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = Optional.ofNullable(ex)
                .map(BindException::getBindingResult)
                .map(Errors::getFieldErrors)
                .stream()
                .flatMap(List::stream)
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.error("Validation failed: {}", errors, ex);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                String.join(", ", errors)
        );
    }

    //Handles invalid or unreadable JSON request bodies.(Wrong date-time format)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJsonException(HttpMessageNotReadableException ex) {
        log.error("Invalid JSON request: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request body");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    }

    /*
     * Returns the safe message that should be shown to the user.
     * If no user-friendly message is provided, a default polished message is used.
     */
    private String getUserMessage(ServiceException ex, String defaultMessage) {
        return Optional.ofNullable(ex.getUserFriendlyMessage())
                .filter(StringUtils::isNotBlank)
                .orElse(defaultMessage);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}