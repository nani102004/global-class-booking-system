package com.globallearning.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Example: Asia/Kolkata, America/New_York
    @Column(nullable = false)
    private String timezone;

    // One Teacher can create multiple Offerings.
    @Builder.Default
    @OneToMany(mappedBy = "teacher")
    private List<Offering> offerings = new ArrayList<>();

    // One Teacher can have multiple Sessions.
    @Builder.Default
    @OneToMany(mappedBy = "teacher")
    private List<Session> sessions = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;
}