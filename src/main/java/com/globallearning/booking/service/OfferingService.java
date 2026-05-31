package com.globallearning.booking.service;

import com.globallearning.booking.dto.CreateOfferingRequest;
import com.globallearning.booking.dto.OfferingResponse;
import com.globallearning.booking.dto.TeacherOfferingResponse;
import com.globallearning.booking.dto.TeacherSessionResponse;
import com.globallearning.booking.entity.Course;
import com.globallearning.booking.entity.Offering;
import com.globallearning.booking.entity.Teacher;
import com.globallearning.booking.enums.BookingStatus;
import com.globallearning.booking.enums.OfferingStatus;
import com.globallearning.booking.exception.InvalidInputException;
import com.globallearning.booking.exception.ResourceNotFoundException;
import com.globallearning.booking.repository.BookingRepository;
import com.globallearning.booking.repository.CourseRepository;
import com.globallearning.booking.repository.OfferingRepository;
import com.globallearning.booking.repository.TeacherRepository;
import com.globallearning.booking.util.ResponseMapper;
import com.globallearning.booking.util.TimezoneValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class OfferingService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final OfferingRepository offeringRepository;
    private final BookingRepository bookingRepository;

    public OfferingService(CourseRepository courseRepository, TeacherRepository teacherRepository, OfferingRepository offeringRepository, BookingRepository bookingRepository) {
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.offeringRepository = offeringRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Creates a new offering for a given course and teacher.
     *
     * Business flow:
     * 1. Validate that the course exists.
     * 2. Validate that the teacher exists.
     * 3. Validate offering timezone.
     * 4. Validate offering capacity.
     * 5. Create offering in DRAFT status because sessions are not added yet.
     * 6. Save offering in the database.
     *
     * @param request offering creation request containing courseId, teacherId, title, timezone, and capacity
     * @return saved Offering entity
     */
    public OfferingResponse createOffering(CreateOfferingRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with id: " + request.getCourseId(),
                        "Course not found"
                ));

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teacher not found with id: " + request.getTeacherId(),
                        "Teacher not found"
                ));

        TimezoneValidator.validateAndGetZoneId(request.getTimezone());
        validateCapacity(request.getCapacity());

        Offering offering = Offering.builder()
                .course(course)
                .teacher(teacher)
                .title(request.getTitle())
                .timezone(request.getTimezone())
                .capacity(request.getCapacity())
                .status(OfferingStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        Offering savedOffering = offeringRepository.save(offering);

        return OfferingResponse.builder()
                .offeringId(savedOffering.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .teacherId(teacher.getId())
                .teacherName(teacher.getName())
                .title(savedOffering.getTitle())
                .timezone(savedOffering.getTimezone())
                .capacity(savedOffering.getCapacity())
                .status(savedOffering.getStatus())
                .build();
    }

    /**
     * Returns all upcoming offerings for a teacher.
     *
     * Business flow:
     * 1. Validate that the teacher exists.
     * 2. Read teacher timezone for displaying session timings.
     * 3. Fetch all offerings created by the teacher.
     * 4. Convert each offering's future sessions into teacher timezone.
     * 5. Exclude offerings that do not have any upcoming sessions.
     *
     * @param teacherId teacher id whose offerings need to be fetched
     * @return list of teacher offerings with upcoming sessions
     */
    public List<TeacherOfferingResponse> getTeacherUpcomingOfferings(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teacher not found with id: " + teacherId,
                        "Teacher not found"
                ));

        ZoneId teacherZoneId = ZoneId.of(teacher.getTimezone());
        Instant now = Instant.now();

        return offeringRepository.findByTeacherId(teacherId)
                .stream()
                .map(offering -> mapToTeacherOfferingResponse(offering, teacherZoneId, now))
                .filter(response -> !response.getSessions().isEmpty())
                .toList();
    }

    /**
     * Converts Offering entity into TeacherOfferingResponse.
     *
     * Business flow:
     * 1. Convert upcoming session timings from UTC to teacher timezone.
     * 2. Count confirmed bookings for this offering.
     * 3. Calculate remaining seats.
     * 4. Build teacher offering response.
     *
     * @param offering offering entity
     * @param teacherZoneId teacher timezone
     * @param now current UTC time
     * @return teacher offering response
     */
    private TeacherOfferingResponse mapToTeacherOfferingResponse(
            Offering offering,
            ZoneId teacherZoneId,
            Instant now
    ) {
        List<TeacherSessionResponse> upcomingSessions =  ResponseMapper.toTeacherSessionResponses(
                offering.getSessions(),
                teacherZoneId,
                now
        );

        long confirmedBookings = bookingRepository.countByOfferingIdAndStatus(
                offering.getId(),
                BookingStatus.CONFIRMED
        );

        int remainingSeats = offering.getCapacity() - (int) confirmedBookings;

        return TeacherOfferingResponse.builder()
                .offeringId(offering.getId())
                .offeringTitle(offering.getTitle())
                .courseId(offering.getCourse().getId())
                .courseTitle(offering.getCourse().getTitle())
                .status(offering.getStatus())
                .timezone(teacherZoneId.getId())
                .capacity(offering.getCapacity())
                .remainingSeats(remainingSeats)
                .sessions(upcomingSessions)
                .build();
    }

    private void validateCapacity(Integer capacity) {
        if (capacity <= 0) {
            throw new InvalidInputException(
                    "Invalid capacity provided: " + capacity,
                    "Capacity must be greater than 0"
            );
        }
    }
}