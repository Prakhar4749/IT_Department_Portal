package com.adminService.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String code;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    // Existing: The ID of the user in Auth Service
    @Column(name = "admin_user_id")
    private Long adminUserId;

    // NEW FIELD: Store the email here so Kafka Connect can pick it up!
    @Column(nullable = false)
    private String adminEmail;
}