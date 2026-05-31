package com.globallearning.booking.util;

import com.globallearning.booking.entity.Session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SessionTimeUtil {

    private SessionTimeUtil() {
    }

    /**
     * Checks whether the given sessions overlap with each other.
     *
     * Boundary rule:
     * - 10:00 to 11:00 and 11:00 to 12:00 is allowed.
     * - 10:00 to 11:00 and 10:59 to 12:00 is considered overlap.
     *
     * @param sessions list of sessions to check
     * @return true if any overlap exists, otherwise false
     */
    public static boolean hasOverlap(List<Session> sessions) {
        List<Session> sortedSessions = new ArrayList<>(sessions);
        sortedSessions.sort(Comparator.comparing(Session::getStartTimeUtc));

        for (int i = 1; i < sortedSessions.size(); i++) {
            Session previous = sortedSessions.get(i - 1);
            Session current = sortedSessions.get(i);

            if (previous.getEndTimeUtc().isAfter(current.getStartTimeUtc())) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasOverlap(List<Session> existingSessions, List<Session> newSessions) {
        List<Session> allSessions = new ArrayList<>();
        allSessions.addAll(existingSessions);
        allSessions.addAll(newSessions);
        return hasOverlap(allSessions);
    }
}