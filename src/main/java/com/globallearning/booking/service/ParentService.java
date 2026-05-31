package com.globallearning.booking.service;

import com.globallearning.booking.dto.CreateParentRequest;
import com.globallearning.booking.dto.ParentOfferingResponse;
import com.globallearning.booking.entity.Booking;
import com.globallearning.booking.entity.Offering;
import com.globallearning.booking.entity.Parent;
import com.globallearning.booking.entity.Session;
import com.globallearning.booking.enums.BookingStatus;
import com.globallearning.booking.enums.OfferingStatus;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParentService {

    private final ParentRepository parentRepository;
    private final OfferingRepository offeringRepository;
    private final BookingRepository bookingRepository;

    public ParentService(ParentRepository parentRepository, OfferingRepository offeringRepository, BookingRepository bookingRepository) {
        this.parentRepository = parentRepository;
        this.offeringRepository = offeringRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Creates a new parent/student profile after validating the provided timezone.
     *
     * Business flow:
     * 1. Validate that the timezone is a valid IANA timezone.
     * 2. Build Parent entity using request data.
     * 3. Save parent details in the database.
     *
     * @param request parent creation request containing name, email, and timezone
     * @return saved Parent entity
     */
    public Parent createParent(CreateParentRequest request) {
        TimezoneValidator.validateAndGetZoneId(request.getTimezone());

        Parent parent = Parent.builder()
                .name(request.getName())
                .email(request.getEmail())
                .timezone(request.getTimezone())
                .createdAt(LocalDateTime.now())
                .build();

        return parentRepository.save(parent);
    }

    /**
     * Returns all offerings that are available for a parent to book.
     *
     * Business flow:
     * 1. Validate that the parent exists.
     * 2. Use parent timezone to display session timings.
     * 3. Fetch parent's confirmed bookings.
     * 4. Exclude offerings already booked by the parent.
     * 5. Exclude offerings that conflict with parent's confirmed future sessions.
     * 6. Return only ACTIVE offerings that are safe for the parent to book.
     *
     * @param parentId parent id for whom available offerings need to be fetched
     * @return list of available offerings with sessions shown in parent timezone
     */
    @Transactional(readOnly = true)
    public List<ParentOfferingResponse> getAvailableOfferings(Long parentId) {
        // Check whether the parent exists before fetching available offerings.
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent not found with id: " + parentId,
                        "Parent not found"
                ));

        ZoneId parentZoneId = ZoneId.of(parent.getTimezone());

        // Check whether the parent exists before fetching available offerings.
        List<Booking> confirmedBookings =
                bookingRepository.findByParentIdAndStatus(parentId, BookingStatus.CONFIRMED);

        // extracting offeringIds from bookings
        Set<Long> alreadyBookedOfferingIds = confirmedBookings.stream()
                .map(booking -> booking.getOffering().getId())
                .collect(Collectors.toSet());

        // extracting sessions which are in future
        List<Session> confirmedFutureSessions = confirmedBookings.stream()
                .flatMap(booking -> booking.getOffering().getSessions().stream())
                .filter(session -> session.getEndTimeUtc().isAfter(Instant.now()))
                .toList();

        // remove offerings which are already booked, remove offering sessions which are overlapping
        return offeringRepository.findByStatus(OfferingStatus.ACTIVE)
                .stream()
                .filter(offering -> !alreadyBookedOfferingIds.contains(offering.getId()))
                .filter(offering -> !SessionTimeUtil.hasOverlap(
                    confirmedFutureSessions,
                    offering.getSessions())
                )
                .map(offering -> mapToParentOfferingResponse(offering, parentZoneId))
                .toList();
    }


    private ParentOfferingResponse mapToParentOfferingResponse(
            Offering offering,
            ZoneId parentZoneId
    ) {
        long confirmedBookings = bookingRepository.countByOfferingIdAndStatus(
                offering.getId(),
                BookingStatus.CONFIRMED
        );

        int remainingSeats = offering.getCapacity() - (int) confirmedBookings;

        return ParentOfferingResponse.builder()
                .offeringId(offering.getId())
                .offeringTitle(offering.getTitle())
                .courseId(offering.getCourse().getId())
                .courseTitle(offering.getCourse().getTitle())
                .teacherName(offering.getTeacher().getName())
                .remainingSeats(remainingSeats)
                .sessions(ResponseMapper.toParentSessionResponses(
                        offering.getSessions(),
                        parentZoneId
                ))
                .build();
    }
}