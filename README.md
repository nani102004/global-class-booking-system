# Global Class Offering Booking System

## Project Overview

Global Class Booking System is a backend service for a live-learning platform where teachers create online class offerings and parents book those offerings for students.

Each course can have multiple offerings, and each offering contains multiple sessions. Teachers may create offerings in their own timezone, while parents can view session timings in their local timezone.

The system handles:
  - Teacher, parent, and course creation
  - Offering creation
  - Adding sessions to offerings
  - Viewing teacher upcoming offerings
  - Viewing available offerings for parents
  - Booking complete offerings
  - Cancelling bookings
  - Session conflict detection
  - Capacity handling
  - Timezone conversion
  - Offering status updates using schedulers (ACTIVE to INPROGRESS AND INPROGRESS to COMPLETED)

---

## Tech Stack Used

| Component  | Technology                  |
| ---------- | --------------------------- |
| Language   | Java 17                     |
| Framework  | Spring Boot                 |
| Database   | MySQL                       |
| ORM        | Spring Data JPA / Hibernate |
| Build Tool | Maven                       |
| Scheduling | Spring Scheduler            |

---

## Setup Instructions

```bash
git clone <repository>

cd project

mvn clean install

mvn spring-boot:run
```

Application URL: http://localhost:8080/{endpoints}

---

## API Documentation

### Course APIs

| Method | Endpoint   | Description     |
| ------ | ---------- | --------------- |
| POST   | `/courses` | Create a course |

### Teacher APIs

| Method | Endpoint                                    | Description                    |
| ------ | ------------------------------------------- | ------------------------------ |
| POST   | `/teachers`                                 | Create teacher                 |
| POST   | `/teachers/offerings`                       | Create offering                |
| POST   | `/teachers/offerings/{offeringId}/sessions` | Add sessions to offering       |
| GET    | `/teachers/{teacherId}/offerings`           | Get teacher upcoming offerings |

### Parent APIs

| Method | Endpoint                                              | Description             |
| ------ | ----------------------------------------------------- | ----------------------- |
| POST   | `/parents`                                            | Create parent           |
| GET    | `/parents/{parentId}/offerings/available`             | Get available offerings |
| POST   | `/parents/{parentId}/offerings/{offeringId}/bookings` | Book an offering        |
| GET    | `/parents/{parentId}/bookings`                        | Get parent bookings     |
| PUT    | `/parents/{parentId}/bookings/{bookingId}/cancel`     | Cancel booking          |

---

## Database Schema Overview

| Entity     | Main Fields                                                                                               |
| ---------- | --------------------------------------------------------------------------------------------------------- |
| `Course`   | `id`, `title`, `description`, `offerings`, `createdAt`                                                    |
| `Teacher`  | `id`, `name`, `email`, `timezone`, `offerings`, `sessions`, `createdAt`                                   |
| `Parent`   | `id`, `name`, `email`, `timezone`, `bookings`, `createdAt`                                                |
| `Offering` | `id`, `title`, `timezone`, `capacity`, `status`, `course`, `teacher`, `sessions`, `bookings`, `createdAt` |
| `Session`  | `id`, `startTimeUtc`, `endTimeUtc`, `offering`, `teacher`, `createdAt`                                    |
| `Booking`  | `id`, `status`, `bookedAt`, `parent`, `offering`                                                          |

### Offering Status Rules

| Status          | Conditions                                                                                                                                 |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| **DRAFT**       | Offering has **no sessions** created.                                                                                                      |
| **ACTIVE**      | Offering has at least one session, the **first session start time is in the future**, and **available seats > 0**.                         |
| **CLOSED**      | Offering has at least one session, the **first session start time is in the future**, and **available seats = 0** (capacity fully booked). |
| **IN_PROGRESS** | The **first session has already started or completed**, and **at least one future session still exists**.                                  |
| **COMPLETED**   | **All sessions have ended** (every session `end_time_utc` is in the past).                                                                 |
| **CANCELLED**   | The teacher has manually cancelled/stopped the offering.                                                                                   |

---

### Booking Status Rules

| Status        | Conditions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| ------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **CONFIRMED** | Booking is successful only if **all** of the following validations pass: <br><br>1. No future session of the requested offering overlaps with any future session of the parent's already confirmed offerings.<br>2. The offering has **not started yet** (first session start time is in the future).<br>3. The offering is **not at full capacity**.<br>4. The parent has **not already registered** for the same offering.<br>5. The offering status is neither **CANCELLED**, **COMPLETED**, nor **DRAFT**. |
| **FAILED**    | Any of the following validations fail: <br><br>• Session conflict with an existing confirmed booking.<br>• Offering has already started.<br>• Offering capacity is full.<br>• Parent already registered for the offering.<br>• Offering is cancelled.<br>• Offering is completed.<br>• Offering is in draft state (no sessions).                                                                                                                                                                               |
| **CANCELLED** | Booking was previously confirmed but later cancelled by the parent.                                                                                                                                                                                                                                                                                                                                                                                                                                            |

---

### Assumptions Made

| Assumption                                                     | Description                                                                                                                                              |
| -------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Parent books the entire offering**                           | A parent enrolls in an offering as a whole; individual session-level booking is not supported.                                                           |
| **Booking allowed only before the offering starts**            | A booking can be created only if no session of the offering has started yet (i.e., the first session start time is in the future).                       |
| **Same parent cannot book the same offering twice**            | A parent can have at most one active booking for a given offering. Duplicate registrations are not allowed.                                              |
| **All sessions in an offering are taught by a single teacher** | Every session belonging to an offering must be assigned to the same teacher as the offering.                                                             |
| **Teacher overlap validation is enforced**                     | When creating or updating sessions, the system validates that the teacher does not already have another overlapping session during the same time period. |
| **Teacher ID is stored per session**                           | Each session stores a `teacher_id` to support teacher availability and overlap validation.                                                               |
| **Parent attends all sessions in an offering**                 | Once booked, the parent/student is considered enrolled in every session of that offering.                                                                |
| **Offering timezone is immutable**                             | The offering timezone represents the timezone in which the batch was created and cannot be changed after creation.                                       |
| **Teacher timezone may change**                                | A teacher's timezone can be updated later due to relocation or profile updates.                                                                          |
| **Offering timezone is independent of teacher timezone**       | Even if a teacher changes timezone, the offering continues to use its original timezone to avoid impacting existing schedules and enrolled parents.      |
| **Session timestamps are stored in UTC**                       | All session start and end times are persisted in UTC for consistent scheduling across time zones.                                                        |
| **Teacher cancellation overrides offering state**              | If a teacher explicitly cancels an offering, the offering status becomes `CANCELLED` regardless of session dates.                                        |
| **Status is derived from sessions and bookings**               | Offering statuses such as `ACTIVE`, `CLOSED`, `IN_PROGRESS`, and `COMPLETED` are determined based on session schedules and booking capacity.             |


---

### Reference TimeZones

| Timezone            | Offset |
| ------------------- | ------ |
| UTC                 | +00:00 |
| America/New_York    | -04:00 |
| America/Chicago     | -05:00 |
| America/Denver      | -06:00 |
| America/Los_Angeles | -07:00 |
| America/Phoenix     | -07:00 |
| Europe/London       | +01:00 |
| Europe/Paris        | +02:00 |
| Europe/Berlin       | +02:00 |
| Europe/Madrid       | +02:00 |
| Europe/Rome         | +02:00 |
| Asia/Dubai          | +04:00 |
| Asia/Kolkata        | +05:30 |
| Asia/Dhaka          | +06:00 |
| Asia/Bangkok        | +07:00 |
| Asia/Singapore      | +08:00 |
| Asia/Hong_Kong      | +08:00 |
| Asia/Shanghai       | +08:00 |
| Asia/Tokyo          | +09:00 |
| Australia/Perth     | +08:00 |
| Australia/Sydney    | +10:00 |
| Pacific/Auckland    | +12:00 |

## Concurrency Handling Approach

The system supports multiple users trying to book offerings at the same time.

To safely handle multiple parents booking the same offering simultaneously, the system uses a pessimistic lock on the offering row during the booking process.

### Scenarios Handled

- Multiple parents booking the same offering at the same time.
- Parent trying to book the same offering multiple times.
- Parent trying to book overlapping offerings.

### Booking Validation

Before confirming a booking, the system checks:

1. Offering exists.
2. Offering has not started.
3. Seats are available.
4. Parent has not already booked the offering.
5. No session conflicts with the parent's existing bookings.

### Result

This ensures:

- No overbooking beyond capacity.
- No duplicate bookings for the same offering.
- No overlapping bookings for the same parent.
- Data consistency during concurrent requests.

---

## Timezone Handling Approach

The system supports teachers and parents from different timezones.

### Approach

- Teacher provides timezone while creating an offering.
- Sessions are created using the offering timezone.
- All session times are converted and stored in UTC.
- Session conflict checks are performed using UTC timestamps.
- While viewing sessions, UTC timestamps are converted to the parent's or teacher's local timezone.
- Offering timezone is stored permanently to preserve the original schedule even if the teacher changes timezone later.

### Example

| Event | Time |
|--------|--------|
| Teacher Timezone | Asia/Kolkata |
| Teacher Creates Session | 06:00 PM IST |
| Stored in Database | 12:30 PM UTC |
| Parent Timezone | America/New_York |
| Parent Sees Session | 08:30 AM EDT |

### Benefits

- Single source of truth using UTC.
- Accurate timezone conversion for teachers and parents.
- Simplified session conflict detection.
- Consistent scheduling across different countries and timezones.

---

## Booking Conflict Handling

Parents book an entire offering, which automatically includes all sessions belonging to that offering.

Before confirming a booking, the system checks every future session of the selected offering against all future sessions from the parent's existing confirmed bookings.

| Validation                                    | Result              |
| --------------------------------------------- | ------------------- |
| No overlapping sessions found                 | Booking is allowed  |
| Any session overlaps with an existing booking | Booking is rejected |

### Example

Parent has already booked:

| Offering         | Session Time               |
| ---------------- | -------------------------- |
| Minecraft Coding | June 7, 5:00 PM – 6:00 PM  |
| Minecraft Coding | June 14, 5:00 PM – 6:00 PM |
| Minecraft Coding | June 21, 5:00 PM – 6:00 PM |

Parent attempts to book:

| Offering           | Session Time               |
| ------------------ | -------------------------- |
| Roblox Game Design | June 14, 5:30 PM – 6:30 PM |

Result: Booking is rejected because the session overlaps with an already booked session on June 14.

### Overlap Check

All overlap validations are performed using UTC timestamps, ensuring accurate conflict detection regardless of teacher or parent timezone.

---

## Seed and Scheduler Test SQL Files

The ZIP contains SQL files for testing:

- `data.sql` - creates schema and inserts base seed data.
- `scheduler1` - creates an offering that should move from ACTIVE to IN_PROGRESS.
- `scheduler2` - creates an offering that should move from IN_PROGRESS to COMPLETED.

## Steps to Run Locally

### 1. Create Database

```sql
CREATE DATABASE global_class_booking;
````

### 2. Configure Environment Variables

Update the database configuration in :

```text
src/main/resources/application.properties
```

Configure:

```properties
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
```

### 3. Execute SQL Scripts

Run the provided SQL files:

* `data.sql` – Creates schema and inserts seed data.
* `scheduler1.sql` – Used to test ACTIVE → IN_PROGRESS scheduler.
* `scheduler2.sql` – Used to test IN_PROGRESS → COMPLETED scheduler.

### 4. Start Application

```bash
mvn spring-boot:run
```

Application will start on:

```text
http://localhost:8080
```

### 5. Test APIs

A Postman collection is included with the submission.

The collection contains:

* Teacher APIs
* Parent APIs
* Course APIs
* Successful booking scenarios
* Failed booking scenarios
* Conflict detection scenarios
* Cancellation scenarios
* Scheduler testing scenarios

Import the Postman collection and execute the requests to test the application.

```
