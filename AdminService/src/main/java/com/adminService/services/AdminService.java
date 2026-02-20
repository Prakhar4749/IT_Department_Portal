package com.adminService.services;

import com.adminService.DTO.ApiResponse;
import com.adminService.DTO.CollegeRequest;
import com.adminService.DTO.DepartmentRequest;
import com.adminService.DTO.SignupRequest;
import com.adminService.entities.College;
import com.adminService.entities.Department;
import com.adminService.exceptions.DuplicateResourceException;
import com.adminService.exceptions.ExternalServiceException;
import com.adminService.exceptions.ResourceNotFoundException;
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

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public List<Department> getDepartmentsByCollege(Long collegeId) {
        return departmentRepository.findByCollegeId(collegeId);
    }

    @Transactional
    public College addCollege(CollegeRequest request) {
        if (collegeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("College with the name '" + request.getName() + "' already exists.");
        }

        String tempPassword = RandomStringUtils.randomAlphanumeric(8);

        SignupRequest userRequest = new SignupRequest();
        userRequest.setEmail(request.getAdminEmail());
        userRequest.setPassword(tempPassword);
        userRequest.setRole("COLLEGE_ADMIN");

        Long adminUserId;

        // No try-catch needed! If this fails, the ErrorDecoder automatically
        // throws DuplicateResourceException or ExternalServiceException,
        // which the GlobalExceptionHandler catches and formats for the frontend.
        ApiResponse<Long> authResponse = authClient.createAdminUser(gatewaySecret, userRequest);

        // Extract the actual ID from the wrapper
        adminUserId = authResponse.getData();

        College college = College.builder()
                .name(request.getName())
                .location(request.getLocation())
                .adminUserId(adminUserId)
                .build();

        return collegeRepository.save(college);
    }

    @Transactional
    public Department addDepartment(DepartmentRequest request) {
        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException("College not found with ID: " + request.getCollegeId()));

        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .college(college)
                .adminEmail(request.getAdminEmail())
                .build();

        department = departmentRepository.save(department);
        Long newDeptId = department.getId();

        String tempPassword = RandomStringUtils.randomAlphanumeric(8);

        SignupRequest userRequest = new SignupRequest();
        userRequest.setEmail(request.getAdminEmail());
        userRequest.setPassword(tempPassword);
        userRequest.setRole("DEPT_ADMIN");
        userRequest.setCollegeId(college.getId());
        userRequest.setDepartmentId(newDeptId);

        Long adminUserId;

        // No try-catch needed! If this fails, the ErrorDecoder automatically
        // throws DuplicateResourceException or ExternalServiceException,
        // which the GlobalExceptionHandler catches and formats for the frontend.
        ApiResponse<Long> authResponse = authClient.createAdminUser(gatewaySecret, userRequest);

        // Extract the actual ID from the wrapper
        adminUserId = authResponse.getData();

        department.setAdminUserId(adminUserId);
        return departmentRepository.save(department);
    }
}