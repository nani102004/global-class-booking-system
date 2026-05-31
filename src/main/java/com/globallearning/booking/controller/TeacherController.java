package com.globallearning.booking.controller;


import com.globallearning.booking.dto.*;
import com.globallearning.booking.entity.Offering;
import com.globallearning.booking.entity.Session;
import com.globallearning.booking.entity.Teacher;
import com.globallearning.booking.service.OfferingService;
import com.globallearning.booking.service.SessionService;
import com.globallearning.booking.service.TeacherService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final OfferingService offeringService;
    private final SessionService sessionService;

    public TeacherController(TeacherService teacherService, OfferingService offeringService, SessionService sessionService) {
        this.teacherService = teacherService;
        this.offeringService = offeringService;
        this.sessionService = sessionService;
    }

    // Creates a new teacher with name, email, and timezone details.
    @PostMapping
    public ResponseEntity<Teacher> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request
    ) {
        Teacher teacher = teacherService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(teacher);
    }

    /**
     Creates a new offering for a teacher and course.
     The offering is created in DRAFT status until sessions are added.
     */
    @PostMapping("/offerings")
    public ResponseEntity<OfferingResponse>  createOffering(
            @Valid @RequestBody CreateOfferingRequest request
    )
    {
        OfferingResponse response = offeringService.createOffering(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     Creates a new offering for a teacher and course.
     The offering is created in DRAFT status until sessions are added.
     */
    @PostMapping("/offerings/{offeringId}/sessions")
    public ResponseEntity<List<SessionResponse>> addSessions(
            @PathVariable Long offeringId,
            @Valid @RequestBody AddSessionsRequest request) {

        List<SessionResponse> sessions =
                sessionService.addSessions(offeringId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(sessions);
    }

    /*
     Returns upcoming offerings along with corresponding sessions for a teacher.
     Session timings are converted and displayed in the teacher's timezone.
     */
    @GetMapping("/{teacherId}/offerings")
    public ResponseEntity<List<TeacherOfferingResponse>> getTeacherUpcomingOfferings(
            @PathVariable Long teacherId) {

        List<TeacherOfferingResponse> offerings =
                offeringService.getTeacherUpcomingOfferings(teacherId);

        return ResponseEntity.ok(offerings);
    }

}