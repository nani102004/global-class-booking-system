package com.globallearning.booking.entity;

import com.globallearning.booking.enums.OfferingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offerings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Teacher/offering timezone
    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferingStatus status;

    // Many Offerings can belong to one Course.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Many Offerings can be created by one Teacher.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    // One Offering can have multiple Sessions.
    @Builder.Default
    @OneToMany(mappedBy = "offering", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Session> sessions = new ArrayList<>();

    // One Offering can have multiple Bookings.
    @Builder.Default
    @OneToMany(mappedBy = "offering")
    private List<Booking> bookings = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;
}