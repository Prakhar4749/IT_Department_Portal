package com.adminService.repositories;

import com.adminService.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
     Department findByName(String name);
    List<Department> findByCollegeId(Long collegeId);
}
