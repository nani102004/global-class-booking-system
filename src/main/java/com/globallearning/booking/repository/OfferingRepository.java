package com.globallearning.booking.repository;

import com.globallearning.booking.entity.Offering;
import com.globallearning.booking.enums.OfferingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {
    List<Offering> findByTeacherId(Long teacherId);

    List<Offering> findByStatus(OfferingStatus status);

    @Query("""
        SELECT DISTINCT o
        FROM Offering o
        JOIN o.sessions s
        WHERE o.status IN :statuses
        GROUP BY o
        HAVING MIN(s.startTimeUtc) <= :now
    """)
    List<Offering> findOfferingsToMarkInProgress(
            @Param("statuses") List<OfferingStatus> statuses,
            @Param("now") Instant now
    );

    @Query("""
        SELECT DISTINCT o
        FROM Offering o
        JOIN o.sessions s
        WHERE o.status = :status
        GROUP BY o
        HAVING MAX(s.endTimeUtc) <= :now
    """)
    List<Offering> findOfferingsToMarkCompleted(
            @Param("status") OfferingStatus status,
            @Param("now") Instant now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Offering o WHERE o.id = :offeringId")
    Optional<Offering> findByIdForUpdate(@Param("offeringId") Long offeringId);
}
