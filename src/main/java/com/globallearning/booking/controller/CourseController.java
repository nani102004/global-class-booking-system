package com.globallearning.booking.controller;

import com.globallearning.booking.dto.CreateCourseRequest;
import com.globallearning.booking.entity.Course;
import com.globallearning.booking.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {

        Course course = courseService.createCourse(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }
}