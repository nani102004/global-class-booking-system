DROP DATABASE IF EXISTS global_class_booking;

CREATE DATABASE global_class_booking;

USE global_class_booking;

CREATE TABLE teachers (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          email VARCHAR(150) NOT NULL UNIQUE,
                          timezone VARCHAR(100) NOT NULL,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE parents (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         timezone VARCHAR(100) NOT NULL,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         title VARCHAR(150) NOT NULL,
                         description TEXT,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE offerings (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           course_id BIGINT NOT NULL,
                           teacher_id BIGINT NOT NULL,
                           title VARCHAR(150) NOT NULL,
                           timezone VARCHAR(100) NOT NULL,
                           capacity INT NOT NULL,

                           status ENUM (
        'DRAFT',
        'ACTIVE',
        'CLOSED',
        'IN_PROGRESS',
        'COMPLETED',
        'CANCELLED'
    ) NOT NULL,

                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_offering_course
                               FOREIGN KEY (course_id)
                                   REFERENCES courses(id),

                           CONSTRAINT fk_offering_teacher
                               FOREIGN KEY (teacher_id)
                                   REFERENCES teachers(id)
);

CREATE TABLE sessions (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          offering_id BIGINT NOT NULL,
                          teacher_id BIGINT NOT NULL,
                          start_time_utc DATETIME NOT NULL,
                          end_time_utc DATETIME NOT NULL,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_session_offering
                              FOREIGN KEY (offering_id)
                                  REFERENCES offerings(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_session_teacher
                              FOREIGN KEY (teacher_id)
                                  REFERENCES teachers(id)
);

CREATE TABLE bookings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          parent_id BIGINT NOT NULL,
                          offering_id BIGINT NOT NULL,

                          status ENUM (
        'CONFIRMED',
        'FAILED',
        'CANCELLED'
    ) NOT NULL,

                          booked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_booking_parent
                              FOREIGN KEY (parent_id)
                                  REFERENCES parents(id),

                          CONSTRAINT fk_booking_offering
                              FOREIGN KEY (offering_id)
                                  REFERENCES offerings(id)
);

CREATE INDEX idx_offerings_status
    ON offerings(status);

CREATE INDEX idx_sessions_offering
    ON sessions(offering_id);

CREATE INDEX idx_sessions_time
    ON sessions(start_time_utc, end_time_utc);

CREATE INDEX idx_bookings_parent
    ON bookings(parent_id);

CREATE INDEX idx_bookings_offering
    ON bookings(offering_id);

CREATE INDEX idx_bookings_status
    ON bookings(status);


-- =========================
-- TEACHERS
-- =========================
INSERT INTO teachers (id, name, email, timezone) VALUES
                                                     (1, 'John Smith', 'john.teacher@example.com', 'America/New_York'),
                                                     (2, 'Priya Sharma', 'priya.teacher@example.com', 'Asia/Kolkata'),
                                                     (3, 'Emma Wilson', 'emma.teacher@example.com', 'Europe/London'),
                                                     (4, 'Carlos Diaz', 'carlos.teacher@example.com', 'America/Los_Angeles');

-- =========================
-- PARENTS
-- =========================
INSERT INTO parents (id, name, email, timezone) VALUES
                                                    (1, 'Ankita Parent', 'ankita.parent@example.com', 'Asia/Kolkata'),
                                                    (2, 'Rahul Parent', 'rahul.parent@example.com', 'Asia/Kolkata'),
                                                    (3, 'David Parent', 'david.parent@example.com', 'America/Los_Angeles'),
                                                    (4, 'Sophia Parent', 'sophia.parent@example.com', 'Europe/London');

-- =========================
-- COURSES
-- =========================
INSERT INTO courses (id, title, description) VALUES
                                                 (1, 'Python Coding', 'Beginner-friendly Python programming course'),
                                                 (2, 'Minecraft Coding', 'Coding concepts using Minecraft projects'),
                                                 (3, 'Roblox Game Design', 'Game design and scripting course'),
                                                 (4, 'Art Drawing Class', 'Creative drawing and sketching sessions'),
                                                 (5, 'Public Speaking', 'Communication and confidence-building course'),
                                                 (6, 'Math Olympiad', 'Problem solving and logical reasoning course');

-- =========================
-- OFFERINGS
-- =========================
INSERT INTO offerings
(id, course_id, teacher_id, title, timezone, capacity, status)
VALUES
-- DRAFT: no sessions
(1, 1, 1, 'Python Draft Batch', 'America/New_York', 20, 'DRAFT'),

-- ACTIVE: future sessions and seats available
(2, 2, 1, 'Minecraft Saturday Batch', 'America/New_York', 20, 'ACTIVE'),

-- ACTIVE: overlaps with Offering 2, useful for conflict test
(3, 3, 1, 'Roblox Overlap Batch', 'America/New_York', 20, 'ACTIVE'),

-- ACTIVE: valid non-conflicting offering
(4, 4, 2, 'Art Weekday Evening Batch', 'Asia/Kolkata', 15, 'ACTIVE'),

-- CLOSED: capacity full
(5, 4, 2, 'Art Limited Seat Batch', 'Asia/Kolkata', 1, 'CLOSED'),

-- IN_PROGRESS: first sessions completed/started, future session remaining
(6, 5, 3, 'Public Speaking In Progress Batch', 'Europe/London', 10, 'IN_PROGRESS'),

-- COMPLETED: all sessions completed
(7, 1, 2, 'Python Completed Summer Camp', 'Asia/Kolkata', 10, 'COMPLETED'),

-- CANCELLED: teacher cancelled
(8, 2, 1, 'Minecraft Cancelled Batch', 'America/New_York', 10, 'CANCELLED'),

-- ACTIVE: boundary test, starts exactly after another session ends
(9, 6, 4, 'Math Boundary Batch', 'America/Los_Angeles', 5, 'ACTIVE'),

-- ACTIVE: useful for successful booking by another parent
(10, 6, 3, 'Math Evening Batch', 'Europe/London', 3, 'ACTIVE');



-- =========================
-- SESSIONS
-- All timings are UTC
-- =========================

-- Offering 1: DRAFT
-- No sessions inserted.

-- Offering 2: ACTIVE
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
                                                                                 (2, 1, '2026-07-04 22:00:00', '2026-07-04 23:00:00'),
                                                                                 (2, 1, '2026-07-11 22:00:00', '2026-07-11 23:00:00'),
                                                                                 (2, 1, '2026-07-18 22:00:00', '2026-07-18 23:00:00');

-- Offering 3: ACTIVE
-- Changed timings to avoid overlap with Offering 2 for teacher_id = 1
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
                                                                                 (3, 1, '2026-07-04 23:30:00', '2026-07-05 00:30:00'),
                                                                                 (3, 1, '2026-07-11 23:30:00', '2026-07-12 00:30:00'),
                                                                                 (3, 1, '2026-07-18 23:30:00', '2026-07-19 00:30:00');

-- Offering 4: ACTIVE non-conflicting
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
                                                                                 (4, 2, '2026-07-07 11:30:00', '2026-07-07 12:30:00'),
                                                                                 (4, 2, '2026-07-08 11:30:00', '2026-07-08 12:30:00'),
                                                                                 (4, 2, '2026-07-09 11:30:00', '2026-07-09 12:30:00');

-- Offering 5: CLOSED because capacity = 1 and already booked
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
                                                                                 (5, 2, '2026-07-12 10:00:00', '2026-07-12 11:00:00'),
                                                                                 (5, 2, '2026-07-13 10:00:00', '2026-07-13 11:00:00'),
                                                                                 (5, 2, '2026-07-14 10:00:00', '2026-07-14 11:00:00');

-- Offering 6: IN_PROGRESS
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
                                                                                 (6, 3, '2026-05-20 17:00:00', '2026-05-20 18:00:00'),
                                                                                 (6, 3, '2026-05-27 17:00:00', '2026-05-27 18:00:00'),
                                                                                 (6, 3, '2026-06-03 17:00:00', '2026-06-03 18:00:00');

-- Offering 7: COMPLETED
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
                                                                                 (7, 2, '2026-04-01 11:30:00', '2026-04-01 12:30:00'),
                                                                                 (7, 2, '2026-04-02 11:30:00', '2026-04-02 12:30:00'),
                                                                                 (7, 2, '2026-04-03 11:30:00', '2026-04-03 12:30:00');

-- Offering 8: CANCELLED but future session exists
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
    (8, 1, '2026-07-05 22:00:00', '2026-07-05 23:00:00');

-- Offering 9: boundary test
-- Starts exactly when another session ends, so it should NOT conflict.
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
    (9, 4, '2026-07-04 23:00:00', '2026-07-05 00:00:00');

-- Offering 10: another valid active offering
INSERT INTO sessions (offering_id, teacher_id, start_time_utc, end_time_utc) VALUES
                                                                                 (10, 3, '2026-07-20 15:00:00', '2026-07-20 16:00:00'),
                                                                                 (10, 3, '2026-07-21 15:00:00', '2026-07-21 16:00:00');


-- =========================
-- BOOKINGS
-- No failure_reason column now
-- =========================
INSERT INTO bookings
(parent_id, offering_id, status, booked_at)
VALUES
-- Parent 1 confirmed Offering 2
-- Used to test duplicate booking and overlap with Offering 3
(1, 2, 'CONFIRMED', '2026-06-01 10:00:00'),

-- Parent 1 failed Offering 3
-- Service should throw: Session time overlaps with an existing confirmed booking
(1, 3, 'FAILED', '2026-06-01 10:05:00'),

-- Parent 1 cancelled Offering 4
-- Used to test cancellation history
(1, 4, 'CANCELLED', '2026-06-02 10:00:00'),

-- Parent 2 confirmed Offering 5
-- Capacity = 1, so Offering 5 remains CLOSED
(2, 5, 'CONFIRMED', '2026-06-03 10:00:00'),

-- Parent 3 failed Offering 5
-- Service should throw: Offering capacity is full
(3, 5, 'FAILED', '2026-06-03 10:10:00'),

-- Parent 2 failed Offering 6
-- Service should throw: Offering has already started
(2, 6, 'FAILED', '2026-06-04 10:00:00'),

-- Parent 2 failed Offering 7
-- Service should throw: Offering is already completed
(2, 7, 'FAILED', '2026-06-04 10:05:00'),

-- Parent 2 failed Offering 8
-- Service should throw: Offering is cancelled by teacher
(2, 8, 'FAILED', '2026-06-04 10:10:00'),

-- Parent 1 failed duplicate booking of Offering 2
-- Service should throw: Candidate already registered for this offering
(1, 2, 'FAILED', '2026-06-05 10:00:00'),

-- Parent 3 confirmed Offering 10
-- Normal successful booking
(3, 10, 'CONFIRMED', '2026-06-06 10:00:00');


-- select * from sessions where offering_id = 2 ;

-- Make sure parent 1 already has Offering 2 booked
INSERT INTO bookings (parent_id, offering_id, status)
VALUES (1, 2, 'CONFIRMED');

-- Add new offering that overlaps with Offering 2 sessions
INSERT INTO offerings (
    course_id,
    teacher_id,
    title,
    timezone,
    capacity,
    status
)
VALUES (
           3,
           4,
           'Roblox Conflict Booking Batch',
           'America/New_York',
           10,
           'ACTIVE'
       );

-- Assume new offering id becomes 11
INSERT INTO sessions (
    offering_id,
    teacher_id,
    start_time_utc,
    end_time_utc
)
VALUES
    (11, 4, '2026-07-11 22:30:00', '2026-07-11 23:30:00');


INSERT INTO offerings (
    course_id,
    teacher_id,
    title,
    timezone,
    capacity,
    status
)
VALUES (
           1,
           1,
           'Python Draft No Sessions Batch',
           'America/New_York',
           10,
           'DRAFT'
       );
