package com.globallearning.booking.service;

import com.globallearning.booking.dto.BookingResponse;
import com.globallearning.booking.dto.CancelBookingResponse;
import com.globallearning.booking.entity.Booking;
import com.globallearning.booking.entity.Parent;
import com.globallearning.booking.entity.Offering;
import com.globallearning.booking.entity.Session;
import com.globallearning.booking.enums.BookingStatus;
import com.globallearning.booking.enums.OfferingStatus;
import com.globallearning.booking.exception.BookingException;
import com.globallearning.booking.exception.ResourceNotFoundException;
import com.globallearning.booking.repository.BookingRepository;
import com.globallearning.booking.repository.OfferingRepository;
import com.globallearning.booking.repository.ParentRepository;

import com.globallearning.booking.util.ResponseMapper;
import com.globallearning.booking.util.SessionTimeUtil;
import com.globallearning.booking.util.TimezoneValidator;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.time.Instant;

import java.time.ZoneId;
import java.util.List;

@Service
public class BookingService {

    private final ParentRepository parentRepository;
    private final OfferingRepository offeringRepository;
    private final BookingRepository bookingRepository;

    public BookingService(ParentRepository parentRepository, OfferingRepository offeringRepository, BookingRepository bookingRepository) {
        this.parentRepository = parentRepository;
        this.offeringRepository = offeringRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Books an offering for a parent.
     *
     * Business flow:
     * 1. Validate that the parent exists.
     * 2. Validate that the offering exists.
     * 3. Validate all booking rules.
     * 4. If validation fails, save booking attempt as FAILED and throw BookingException.
     * 5. If validation passes, save booking as CONFIRMED.
     * 6. If confirmed bookings reach capacity, mark offering as CLOSED.
     * 7. Return booking response with sessions in parent's timezone.
     *
     * @param parentId parent who is booking the offering
     * @param offeringId offering to be booked
     * @return confirmed booking response
     */
    @Transactional
    public BookingResponse bookOffering(Long parentId, Long offeringId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent not found with id: " + parentId,
                        "Parent not found"
                ));

        Offering offering = offeringRepository.findByIdForUpdate(offeringId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Offering not found with id: " + offeringId,
                        "Offering not found"
                ));

        String failureMessage = validateBooking(parent, offering);

        if (failureMessage != null) {
            saveBooking(parent, offering, BookingStatus.FAILED);

            throw new BookingException(
                    "Booking failed for parentId: " + parentId
                            + ", offeringId: " + offeringId
                            + ". Reason: " + failureMessage,
                    failureMessage
            );
        }

        Booking confirmedBooking = saveBooking(parent, offering, BookingStatus.CONFIRMED);
        updateOfferingStatusIfCapacityFull(offering);

        ZoneId parentZoneId = TimezoneValidator.validateAndGetZoneId(confirmedBooking.getParent().getTimezone());
        return mapToResponse(confirmedBooking, "Booking confirmed successfully", parentZoneId);
    }

    /**
     * Returns all bookings of a parent.
     *
     * Business flow:
     * 1. Validate that the parent exists.
     * 2. Fetch all booking records of the parent.
     * 3. Return confirmed, failed, and cancelled bookings.
     * 4. Convert session timings into parent's timezone.
     *
     * @param parentId parent whose bookings need to be fetched
     * @return list of booking responses
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent not found with id: " + parentId,
                        "Parent not found"
                ));

        ZoneId parentZoneId = ZoneId.of(parent.getTimezone());

        return bookingRepository.findByParentId(parentId)
                .stream()
                .map(booking -> mapToResponse(
                        booking,
                        getBookingMessage(booking),
                        parentZoneId
                ))
                .toList();
    }

    /**
     * Cancels a confirmed booking for a parent.
     *
     * Business flow:
     * 1. Validate that booking belongs to the parent.
     * 2. Allow cancellation only for CONFIRMED bookings.
     * 3. Change booking status to CANCELLED.
     * 4. If the offering was CLOSED due to full capacity, reopen it as ACTIVE.
     * 5. Save updated booking.
     * 6. Return simple cancellation response.
     *
     * @param parentId parent requesting cancellation
     * @param bookingId booking to be cancelled
     * @return cancellation response
     */
    @Transactional
    public CancelBookingResponse cancelBooking(Long parentId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndParentId(bookingId, parentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId + " for parent id: " + parentId,
                        "Booking not found"
                ));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException(
                    "Only confirmed bookings can be cancelled. Booking id: " + bookingId,
                    "Only confirmed bookings can be cancelled"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Offering offering = booking.getOffering();

        if (offering.getStatus() == OfferingStatus.CLOSED) {
            offering.setStatus(OfferingStatus.ACTIVE);
            offeringRepository.save(offering);
        }

        Booking savedBooking = bookingRepository.save(booking);

        return CancelBookingResponse.builder()
                .bookingId(savedBooking.getId())
                .parentId(savedBooking.getParent().getId())
                .offeringId(savedBooking.getOffering().getId())
                .status(savedBooking.getStatus())
                .message("Booking cancelled successfully")
                .bookedAt(savedBooking.getBookedAt())
                .build();
    }

    /**
     * Validates whether a parent can book an offering.
     *
     * Validation rules:
     * 1. Cancelled offerings cannot be booked.
     * 2. Completed offerings cannot be booked.
     * 3. In-progress offerings cannot be booked.
     * 4. Draft offerings cannot be booked.
     * 5. Closed offerings cannot be booked.
     * 6. Parent cannot book same offering twice.
     * 7. Offering capacity should be available.
     * 8. Offering sessions should not conflict with parent's confirmed future sessions.
     *
     * @param parent parent requesting booking
     * @param offering offering to be booked
     * @return failure message if validation fails, otherwise null
     */
    private String validateBooking(Parent parent, Offering offering) {
        if (offering.getStatus() == OfferingStatus.CANCELLED) {
            return "Offering is cancelled by teacher";
        }

        if (offering.getStatus() == OfferingStatus.COMPLETED) {
            return "Offering is already completed";
        }

        if (offering.getStatus() == OfferingStatus.IN_PROGRESS) {
            return "Offering has already started";
        }

        if (offering.getStatus() == OfferingStatus.DRAFT) {
            return "Offering does not have sessions yet";
        }

        if (offering.getStatus() == OfferingStatus.CLOSED) {
            return "Offering capacity is full";
        }

        if (bookingRepository.existsByParentIdAndOfferingIdAndStatus(
                parent.getId(),
                offering.getId(),
                BookingStatus.CONFIRMED
        )) {
            return "Candidate already registered for this offering";
        }

        long confirmedBookings = bookingRepository.countByOfferingIdAndStatus(
                offering.getId(),
                BookingStatus.CONFIRMED
        );

        if (confirmedBookings >= offering.getCapacity()) {
            return "Offering capacity is full";
        }

        if (hasSessionConflict(parent.getId(), offering,Instant.now())) {
            return "Session time overlaps with an existing confirmed booking";
        }

        return null;
    }

    /**
     * Checks whether the new offering conflicts with parent's existing confirmed future sessions.
     *
     * @param parentId parent id
     * @param newOffering offering being booked
     * @param now current UTC time
     * @return true if any overlap exists, otherwise false
     */
    private boolean hasSessionConflict(Long parentId, Offering newOffering,Instant now) {
        List<Booking> confirmedBookings = bookingRepository.findByParentIdAndStatus(
                parentId,
                BookingStatus.CONFIRMED
        );


        List<Session> existingFutureSessions = confirmedBookings.stream()
                .flatMap(booking -> booking.getOffering().getSessions().stream())
                .filter(session -> session.getEndTimeUtc().isAfter(now))
                .toList();

        return SessionTimeUtil.hasOverlap(existingFutureSessions, newOffering.getSessions());
    }

    /**
     * Saves a booking record with the given status.
     *
     * Used for both successful and failed booking attempts.
     *
     * @param parent parent associated with booking
     * @param offering offering associated with booking
     * @param status booking status
     * @return saved booking entity
     */
    private Booking saveBooking(
            Parent parent,
            Offering offering,
            BookingStatus status
    ) {
        Booking booking = Booking.builder()
                .parent(parent)
                .offering(offering)
                .status(status)
                .bookedAt(Instant.now())
                .build();

        return bookingRepository.save(booking);
    }

    /**
     * Updates offering status to CLOSED if confirmed bookings reach capacity.
     *
     * Business rule:
     * ACTIVE offering becomes CLOSED once all seats are filled.
     *
     * @param offering offering whose capacity needs to be checked
     */
    private void updateOfferingStatusIfCapacityFull(Offering offering) {
        long confirmedBookings = bookingRepository.countByOfferingIdAndStatus(
                offering.getId(),
                BookingStatus.CONFIRMED
        );

        if (confirmedBookings >= offering.getCapacity()) {
            offering.setStatus(OfferingStatus.CLOSED);
            offeringRepository.save(offering);
        }
    }

    /**
     * Updates offering status to CLOSED if confirmed bookings reach capacity.
     *
     * Business rule:
     * ACTIVE offering becomes CLOSED once all seats are filled.
     *
     * @param offering offering whose capacity needs to be checked
     */
    private BookingResponse mapToResponse(
            Booking booking,
            String message,
            ZoneId parentZoneId
    ) {
        Offering offering = booking.getOffering();

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .parentId(booking.getParent().getId())
                .offeringId(offering.getId())
                .offeringTitle(offering.getTitle())
                .courseTitle(offering.getCourse().getTitle())
                .status(booking.getStatus())
                .message(message)
                .bookedAt(booking.getBookedAt())
                .sessions(ResponseMapper.toParentSessionResponses(offering.getSessions(), parentZoneId))
                .build();
    }

    /**
     * Returns a user-friendly message based on booking status.
     *
     * @param booking booking entity
     * @return message for booking status
     */
    private String getBookingMessage(Booking booking) {
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return "Booking confirmed successfully";
        }

        if (booking.getStatus() == BookingStatus.FAILED) {
            return "Booking failed";
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return "Booking cancelled by candidate";
        }

        return "Booking status unavailable";
    }
}