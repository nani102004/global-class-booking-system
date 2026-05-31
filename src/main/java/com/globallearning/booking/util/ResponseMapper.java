package com.globallearning.booking.util;


import com.globallearning.booking.dto.ParentSessionResponse;
import com.globallearning.booking.dto.TeacherSessionResponse;
import com.globallearning.booking.entity.Session;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    /**
     * Converts session entities into parent session response DTOs.
     *
     * Business flow:
     * 1. Sort sessions by start time.
     * 2. Convert UTC session timings into the parent timezone.
     * 3. Build parent session response DTOs.
     *
     * @param sessions list of sessions stored in UTC
     * @param zoneId parent timezone in which session timings should be displayed
     * @return list of parent session responses
     */
    public static List<ParentSessionResponse> toParentSessionResponses(
            List<Session> sessions,
            ZoneId zoneId
    ) {
        return sessions.stream()
                .sorted(Comparator.comparing(Session::getStartTimeUtc))
                .map(session -> ParentSessionResponse.builder()
                        .sessionId(session.getId())
                        .startTime(LocalDateTime.ofInstant(session.getStartTimeUtc(), zoneId))
                        .endTime(LocalDateTime.ofInstant(session.getEndTimeUtc(), zoneId))
                        .build())
                .toList();
    }

    /**
     * Converts session entities into teacher session response DTOs.
     *
     * Business flow:
     * 1. Keep only upcoming sessions.
     * 2. Sort sessions by start time.
     * 3. Convert UTC session timings into the given timezone.
     * 4. Build teacher session response DTOs.
     *
     * @param sessions list of sessions stored in UTC
     * @param zoneId timezone in which session timings should be displayed
     * @param now current UTC time used to filter upcoming sessions
     * @return list of teacher session responses
     */
    public static List<TeacherSessionResponse> toTeacherSessionResponses(
            List<Session> sessions,
            ZoneId zoneId,
            Instant now
    ) {
        return sessions.stream()
                .filter(session -> session.getEndTimeUtc().isAfter(now))
                .sorted(Comparator.comparing(Session::getStartTimeUtc))
                .map(session -> TeacherSessionResponse.builder()
                        .sessionId(session.getId())
                        .startTime(LocalDateTime.ofInstant(session.getStartTimeUtc(), zoneId))
                        .endTime(LocalDateTime.ofInstant(session.getEndTimeUtc(), zoneId))
                        .build())
                .toList();
    }

}