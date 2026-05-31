package com.globallearning.booking.service;

import com.globallearning.booking.dto.CreateCourseRequest;
import com.globallearning.booking.entity.Course;
import com.globallearning.booking.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CourseService {
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    private final CourseRepository courseRepository;

    public Course createCourse(CreateCourseRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        return courseRepository.save(course);
    }
}