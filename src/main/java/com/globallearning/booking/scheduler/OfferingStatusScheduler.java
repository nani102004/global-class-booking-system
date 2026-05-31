package com.globallearning.booking.scheduler;

import com.globallearning.booking.entity.Offering;
import com.globallearning.booking.enums.OfferingStatus;
import com.globallearning.booking.repository.OfferingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class OfferingStatusScheduler {

    private final OfferingRepository offeringRepository;

    public OfferingStatusScheduler(OfferingRepository offeringRepository) {
        this.offeringRepository = offeringRepository;
    }

    /**
     * Marks offerings as IN_PROGRESS when their first session has started.
     *
     * Business rule:
     * - ACTIVE offering means sessions are available for booking.
     * - CLOSED offering means sessions are available but capacity is full.
     * - Once the first session start time is reached, both ACTIVE and CLOSED offerings
     *   should move to IN_PROGRESS.
     *
     * Scheduler behavior:
     * - Runs every minute.
     * - Finds offerings whose first session start time is less than or equal to current UTC time.
     * - Updates those offerings to IN_PROGRESS.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void markOfferingsInProgress() {
        Instant now = Instant.now();

        List<Offering> offerings = offeringRepository.findOfferingsToMarkInProgress(
                List.of(OfferingStatus.ACTIVE, OfferingStatus.CLOSED),
                now
        );

        for (Offering offering : offerings) {
            offering.setStatus(OfferingStatus.IN_PROGRESS);
        }

        if (!offerings.isEmpty()) {
            offeringRepository.saveAll(offerings);
            log.info("Marked {} offerings as IN_PROGRESS", offerings.size());
        }
    }

    /**
     * Marks offerings as COMPLETED when their last session has ended.
     *
     * Business rule:
     * - IN_PROGRESS offering means the first session has started.
     * - Once all sessions are completed, the offering should move to COMPLETED.
     *
     * Scheduler behavior:
     * - Runs every minute at the 30th second.
     * - Finds IN_PROGRESS offerings whose last session end time is less than or equal to current UTC time.
     * - Updates those offerings to COMPLETED.
     */
    @Scheduled(cron = "30 * * * * *")
    @Transactional
    public void markOfferingsCompleted() {
        Instant now = Instant.now();

        List<Offering> offerings = offeringRepository.findOfferingsToMarkCompleted(
                OfferingStatus.IN_PROGRESS,
                now
        );

        for (Offering offering : offerings) {
            offering.setStatus(OfferingStatus.COMPLETED);
        }

        if (!offerings.isEmpty()) {
            offeringRepository.saveAll(offerings);
            log.info("Marked {} offerings as COMPLETED", offerings.size());
        }
    }
}