package com.adminService.services;

import com.adminService.DTO.CollegeRequest;
import com.adminService.DTO.DepartmentRequest;
import com.adminService.DTO.SignupRequest;
import com.adminService.entities.College;
import com.adminService.entities.Department;
import com.adminService.feign.AuthClient;
import com.adminService.repositories.CollegeRepository;
import com.adminService.repositories.DepartmentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthClient authClient;

    @Value("${gateway.secret}")
    private String gatewaySecret;

    // ==========================================
    //              READ OPERATIONS
    // ==========================================

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public List<Department> getDepartmentsByCollege(Long collegeId) {
        return departmentRepository.findByCollegeId(collegeId);
    }

    // ==========================================
    //             WRITE OPERATIONS
    // ==========================================

    /**
     * Creates a College AND a corresponding Admin User in Auth Service.
     */
    @Transactional
    public College addCollege(CollegeRequest request) {
        // 1. Validation
        if (collegeRepository.existsByName(request.getName())) {
            throw new RuntimeException("College with this name already exists.");
        }

        // 2. Generate Random Password (8 chars)
        String tempPassword = RandomStringUtils.randomAlphanumeric(8);

        // 3. Prepare User Request for Auth Service
        SignupRequest userRequest = new SignupRequest();
        userRequest.setEmail(request.getAdminEmail());
        userRequest.setPassword(tempPassword);
        userRequest.setRole("COLLEGE_ADMIN");

        Long adminUserId;
        try {
            // 4. Call Auth Service (Synchronous)
            // This creates the user, sets force-reset flag, and triggers email notification
            adminUserId = authClient.createAdminUser(gatewaySecret, userRequest);

        } catch (FeignException e) {
            // Handle duplicate email error from Auth Service
            if(e.status() == 500 || e.status() == 400) {
                throw new RuntimeException("Failed to create Admin: Email '" + request.getAdminEmail() + "' likely already exists.");
            }
            throw new RuntimeException("External System Error: " + e.getMessage());
        }

        // 5. Save College locally
        College college = College.builder()
                .name(request.getName())
                .location(request.getLocation())
                .adminUserId(adminUserId)
                .build();

        return collegeRepository.save(college);
    }

    /**
     * Creates a Department AND a corresponding Admin User in Auth Service.
     */
    @Transactional
    public Department addDepartment(DepartmentRequest request) {
        // 1. Verify College Exists
        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new RuntimeException("College not found with ID: " + request.getCollegeId()));

        // 2. Generate Random Password
        String tempPassword = RandomStringUtils.randomAlphanumeric(8);

        // 3. Create Dept Admin in Auth Service
        SignupRequest userRequest = new SignupRequest();
        userRequest.setEmail(request.getAdminEmail());
        userRequest.setPassword(tempPassword);
        userRequest.setRole("DEPT_ADMIN");

        Long adminUserId;
        try {
            adminUserId = authClient.createAdminUser(gatewaySecret, userRequest);
        } catch (FeignException e) {
            throw new RuntimeException("Failed to create Admin: Email '" + request.getAdminEmail() + "' likely already exists.");
        }

        // 4. Save Department locally
        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .college(college)
                .adminUserId(adminUserId)
                .build();

        return departmentRepository.save(department);
    }
}