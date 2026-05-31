package com.globallearning.booking.controller;


import com.globallearning.booking.dto.BookingResponse;
import com.globallearning.booking.dto.CancelBookingResponse;
import com.globallearning.booking.dto.CreateParentRequest;
import com.globallearning.booking.dto.ParentOfferingResponse;
import com.globallearning.booking.entity.Parent;
import com.globallearning.booking.service.BookingService;
import com.globallearning.booking.service.ParentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parents")
public class ParentController {

    private final ParentService parentService;
    private final BookingService bookingService;

    public ParentController(ParentService parentService, BookingService bookingService) {
        this.parentService = parentService;
        this.bookingService = bookingService;
    }

    // Creates a new parent/student profile with name, email, and timezone.
    @PostMapping
    public ResponseEntity<Parent> createParent(
            @Valid @RequestBody CreateParentRequest request) {

        Parent parent = parentService.createParent(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(parent);
    }

    // Returns offerings that are available for the parent to book.
    // It excludes already booked offerings and offerings that conflict with parent's confirmed bookings.
    // Session timings are converted to the parent's timezone.
    @GetMapping("/{parentId}/offerings/available")
    public ResponseEntity<List<ParentOfferingResponse>> getAvailableOfferings(
            @PathVariable Long parentId) {

        List<ParentOfferingResponse> offerings =
                parentService.getAvailableOfferings(parentId);

        return ResponseEntity.ok(offerings);
    }

    // Books the complete offering for the parent.
    // Booking succeeds only if offering is active, capacity is available,
    // offering has not started, parent has not already booked it,
    // and there is no session conflict with existing confirmed bookings.
    @PostMapping("/{parentId}/offerings/{offeringId}/bookings")
    public ResponseEntity<BookingResponse> bookOffering(
            @PathVariable Long parentId,
            @PathVariable Long offeringId) {

        BookingResponse bookingResponse =
                bookingService.bookOffering(parentId, offeringId);

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingResponse);
    }

    // Returns all booking records of a parent.
    // This includes confirmed, failed, and cancelled bookings.
    // Session timings are converted to the parent's timezone.
    @GetMapping("/{parentId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookings(
            @PathVariable Long parentId) {

        List<BookingResponse> bookings = bookingService.getBookings(parentId);

        return ResponseEntity.ok(bookings);
    }

    // Cancels a confirmed booking for the parent.
    // Only bookings in CONFIRMED status can be cancelled.
    @PutMapping("/{parentId}/bookings/{bookingId}/cancel")
    public ResponseEntity<CancelBookingResponse> cancelBooking(
            @PathVariable Long parentId,
            @PathVariable Long bookingId) {

        CancelBookingResponse response = bookingService.cancelBooking(parentId, bookingId);
        return ResponseEntity.ok(response);
    }


}