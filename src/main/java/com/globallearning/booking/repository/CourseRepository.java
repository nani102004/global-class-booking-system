package com.globallearning.booking.repository;

import com.globallearning.booking.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}