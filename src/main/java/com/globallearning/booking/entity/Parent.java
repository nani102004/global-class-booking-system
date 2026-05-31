package com.globallearning.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Example: Asia/Kolkata
    @Column(nullable = false)
    private String timezone;

//    One Parent can have multiple Bookings.
    @Builder.Default
    @OneToMany(mappedBy = "parent")
    private List<Booking> bookings = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
