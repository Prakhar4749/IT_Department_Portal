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
    private String name; // e.g., "Computer Science"

    private String code; // e.g., "CSE"

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    // ID of the DEPT_ADMIN user in Auth Service
    @Column(nullable = false)
    private Long adminUserId;
}