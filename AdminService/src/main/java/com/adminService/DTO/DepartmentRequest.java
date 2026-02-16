package com.adminService.DTO;

import lombok.Data;

@Data
public class DepartmentRequest {
    private String name;
    private String code;
    private Long collegeId;
    private String adminEmail; // Email for the new Dept Admin
}
