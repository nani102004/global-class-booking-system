package com.globallearning.booking.repository;

import com.globallearning.booking.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("""
        SELECT s
        FROM Session s
        WHERE s.teacher.id = :teacherId
          AND s.endTimeUtc > :now
    """)
    List<Session> findFutureSessionsByTeacherId(
            @Param("teacherId") Long teacherId,
            @Param("now") Instant now
    );
}