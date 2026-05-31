package com.globallearning.booking.service;

import com.globallearning.booking.dto.AddSessionRequest;
import com.globallearning.booking.dto.AddSessionsRequest;
import com.globallearning.booking.dto.SessionResponse;
import com.globallearning.booking.entity.Offering;
import com.globallearning.booking.entity.Session;
import com.globallearning.booking.enums.OfferingStatus;
import com.globallearning.booking.exception.InvalidInputException;
import com.globallearning.booking.exception.ResourceNotFoundException;
import com.globallearning.booking.repository.OfferingRepository;
import com.globallearning.booking.repository.SessionRepository;
import com.globallearning.booking.util.SessionTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static java.awt.SystemColor.info;

@Slf4j
@Service
public class SessionService {

    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;

    public SessionService(OfferingRepository offeringRepository, SessionRepository sessionRepository) {
        this.offeringRepository = offeringRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Adds one or more sessions to an existing offering.
     *
     * Business flow:
     * 1. Check whether the offering exists using offerId.
     * 2. Use the offering timezone to calculate session times(starting and ending times).
     * 3. Convert session start/end times into UTC before saving.
     * 4. Validate each session timing.
     * 5. Check that new sessions do not overlap with each other.
     * 6. Check that the teacher does not already have future sessions overlapping with new sessions.
     * 7. Save all sessions.
     * 8. Mark the offering as ACTIVE after sessions are added.
     *
     * @param offeringId offering id for which sessions need to be added
     * @param request request containing one or more session timings
     * @return saved session list
     */
    @Transactional
    public List<SessionResponse>  addSessions(Long offeringId, AddSessionsRequest request) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Offering not found with id: " + offeringId,
                        "Offering not found"
                ));

        log.info(offering.getTeacher().getId().toString());

        ZoneId offeringZoneId = ZoneId.of(offering.getTimezone());
        Instant now = Instant.now();

        List<Session> newSessions = buildAndValidateSessions(offering, request, offeringZoneId, now);

        validateTeacherSessionOverlap(
                offering.getTeacher().getId(),
                newSessions,
                now
        );

        List<Session> savedSessions = sessionRepository.saveAll(newSessions);

        offering.setStatus(OfferingStatus.ACTIVE);
        offeringRepository.save(offering);

        return savedSessions.stream()
                .map(session -> SessionResponse.builder()
                        .sessionId(session.getId())
                        .offeringId(offering.getId())
                        .teacherId(offering.getTeacher().getId())
                        .startTime(LocalDateTime.ofInstant(session.getStartTimeUtc(), offeringZoneId))
                        .endTime(LocalDateTime.ofInstant(session.getEndTimeUtc(), offeringZoneId))
                        .build())
                .toList();
    }

    private List<Session> buildAndValidateSessions(
            Offering offering,
            AddSessionsRequest request,
            ZoneId offeringZoneId,
            Instant now
    ) {
        List<Session> newSessions = new ArrayList<>();

        for (AddSessionRequest sessionRequest : request.getSessions()) {
            Instant startTimeUtc = sessionRequest.getStartTime()
                    .atZone(offeringZoneId)
                    .toInstant();

            Instant endTimeUtc = sessionRequest.getEndTime()
                    .atZone(offeringZoneId)
                    .toInstant();

            validateSessionTime(startTimeUtc, endTimeUtc, now);

            Session session = Session.builder()
                    .offering(offering)
                    .teacher(offering.getTeacher())
                    .startTimeUtc(startTimeUtc)
                    .endTimeUtc(endTimeUtc)
                    .createdAt(LocalDateTime.now())
                    .build();

            newSessions.add(session);
        }

        if (SessionTimeUtil.hasOverlap(newSessions)) {
            throw new InvalidInputException(
                    "New sessions overlap with each other",
                    "Sessions should not overlap with each other"
            );
        }

        return newSessions;
    }

    private void validateSessionTime(Instant startTimeUtc, Instant endTimeUtc, Instant now) {
        if (!endTimeUtc.isAfter(startTimeUtc)) {
            throw new InvalidInputException(
                    "Session end time must be after start time",
                    "Session end time must be after start time"
            );
        }

        if (startTimeUtc.isBefore(now)) {
            throw new InvalidInputException(
                    "Session start time is in the past",
                    "Session timing should not be in the past"
            );
        }
    }

    private void validateTeacherSessionOverlap(
            Long teacherId,
            List<Session> newSessions,
            Instant now
    ) {
        List<Session> existingFutureSessions =
                sessionRepository.findFutureSessionsByTeacherId(teacherId, now);

        if (SessionTimeUtil.hasOverlap(existingFutureSessions, newSessions)) {
            throw new InvalidInputException(
                    "Teacher has overlapping sessions",
                    "Teacher has another session during this time"
            );
        }
    }
}