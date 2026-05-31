package com.globallearning.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

//    One Course can have multiple Offerings.
    @Builder.Default
    @OneToMany(mappedBy = "course")
    private List<Offering> offerings = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
