package com.globallearning.booking.service;

import com.globallearning.booking.dto.CreateTeacherRequest;
import com.globallearning.booking.entity.Teacher;
import com.globallearning.booking.repository.TeacherRepository;
import com.globallearning.booking.util.TimezoneValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    /**
     * Creates a new teacher after validating the provided timezone.
     * Business flow:
     * 1. Validate that the timezone is a valid IANA timezone.(like Asia/Kolkata)
     * 2. Build a Teacher entity from the request data.
     * 3. Save the teacher details in the database.
     *
     * @param request teacher creation request containing name, email, and timezone
     * @return saved Teacher entity
     */
    public Teacher createTeacher(CreateTeacherRequest request) {
        // Validate timezone before saving so invalid values are not stored in DB.
        TimezoneValidator.validateAndGetZoneId(request.getTimezone());

        // Build Teacher entity using request data.
        Teacher teacher = Teacher.builder()
                .name(request.getName())
                .email(request.getEmail())
                .timezone(request.getTimezone())
                .createdAt(LocalDateTime.now())
                .build();

        return teacherRepository.save(teacher);
    }
}