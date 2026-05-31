package com.globallearning.booking.repository;

import com.globallearning.booking.entity.Booking;
import com.globallearning.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    boolean existsByParentIdAndOfferingIdAndStatus(
            Long parentId,
            Long offeringId,
            BookingStatus status
    );

    List<Booking> findByParentIdAndStatus(
            Long parentId,
            BookingStatus status
    );

    long countByOfferingIdAndStatus(
            Long offeringId,
            BookingStatus status
    );

    List<Booking> findByParentId(Long parentId);

    Optional<Booking> findByIdAndParentId(Long bookingId, Long parentId);
}