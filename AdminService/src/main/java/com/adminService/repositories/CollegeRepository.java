package com.adminService.repositories;

import com.adminService.entities.College;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {
     College findByName(String name);
    boolean existsByName(String name);
}
