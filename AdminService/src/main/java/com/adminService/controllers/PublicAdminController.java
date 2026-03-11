package com.adminService.controllers;

import com.adminService.DTO.ApiResponse;
import com.adminService.entities.College;
import com.adminService.entities.Department;
import com.adminService.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/public")
@RequiredArgsConstructor
public class PublicAdminController {

    private final AdminService adminService;

    @GetMapping("/colleges")
    public ResponseEntity<ApiResponse<List<College>>> getAllColleges() {
        List<College> colleges = adminService.getAllColleges();
        return ResponseEntity.ok(ApiResponse.success(colleges, "Colleges fetched successfully."));
    }

    @GetMapping("/departments/{collegeId}")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartmentsByCollege(@PathVariable Long collegeId) {
        List<Department> departments = adminService.getDepartmentsByCollege(collegeId);
        return ResponseEntity.ok(ApiResponse.success(departments, "Departments fetched successfully."));
    }
}
