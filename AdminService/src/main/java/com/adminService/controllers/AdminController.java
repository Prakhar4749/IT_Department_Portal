package com.adminService.controllers;

import com.adminService.DTO.ApiResponse;
import com.adminService.DTO.CollegeRequest;
import com.adminService.DTO.DepartmentRequest;
import com.adminService.DTO.HodResponse;

import com.adminService.entities.College;
import com.adminService.entities.Department;
import com.adminService.exceptions.ResourceNotFoundException;

import com.adminService.exceptions.UnauthorizedAccessException;
import com.adminService.repositories.DepartmentRepository;
import com.adminService.services.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DepartmentRepository departmentRepository;

    // --- ADD OPERATIONS (Protected) ---

    @PostMapping("/add-college")
    public ResponseEntity<ApiResponse<College>> addCollege(
            @RequestHeader("loggedInUserRole") String role,
            @Valid @RequestBody CollegeRequest request) {

        if (!"SUPER_ADMIN".equals(role)) {
            throw new UnauthorizedAccessException("Access Denied: Only SUPER_ADMIN can add colleges.");
        }

        College createdCollege = adminService.addCollege(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdCollege, "College added successfully."));
    }

    @PostMapping("/add-department")
    public ResponseEntity<ApiResponse<Department>> addDepartment(
            @RequestHeader("loggedInUserRole") String role,
            @Valid @RequestBody DepartmentRequest request) {

        if (!"SUPER_ADMIN".equals(role) && !"COLLEGE_ADMIN".equals(role)) {
            throw new UnauthorizedAccessException("Access Denied: Insufficient permissions to add a department.");
        }

        Department createdDepartment = adminService.addDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdDepartment, "Department added successfully."));
    }

    // --- GET OPERATIONS (For Frontend Dropdowns) ---

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

    @GetMapping("/departments/{id}/hod")
    public ResponseEntity<ApiResponse<HodResponse>> getHodDetails(@PathVariable Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        HodResponse response = new HodResponse(dept.getAdminUserId(), dept.getAdminEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "HOD details fetched successfully."));
    }
}