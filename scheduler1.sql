-- Create new offering whose first session has already started
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
           4,
           'Scheduler In Progress Test Batch',
           'UTC',
           10,
           'ACTIVE'
       );

SET @in_progress_offering_id = LAST_INSERT_ID();

INSERT INTO sessions (
    offering_id,
    teacher_id,
    start_time_utc,
    end_time_utc
)
VALUES
    (@in_progress_offering_id, 4, UTC_TIMESTAMP() - INTERVAL 1 MINUTE, UTC_TIMESTAMP() + INTERVAL 59 MINUTE),
    (@in_progress_offering_id, 4, UTC_TIMESTAMP() + INTERVAL 7 DAY, UTC_TIMESTAMP() + INTERVAL 7 DAY + INTERVAL 1 HOUR);