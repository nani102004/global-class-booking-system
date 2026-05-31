-- Create new offering whose all sessions are already completed
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
           'Scheduler Completed Test Batch',
           'UTC',
           10,
           'IN_PROGRESS'
       );

SET @completed_offering_id = LAST_INSERT_ID();

INSERT INTO sessions (
    offering_id,
    teacher_id,
    start_time_utc,
    end_time_utc
)
VALUES
    (@completed_offering_id, 4, UTC_TIMESTAMP() - INTERVAL 2 HOUR, UTC_TIMESTAMP() - INTERVAL 1 HOUR),
    (@completed_offering_id, 4, UTC_TIMESTAMP() - INTERVAL 90 MINUTE, UTC_TIMESTAMP() - INTERVAL 30 MINUTE);
