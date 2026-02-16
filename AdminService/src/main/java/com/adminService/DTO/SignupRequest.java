package com.adminService.DTO;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String role; // String here is fine, Auth service converts to Enum
    // We don't need collegeId/deptId here because the Admin IS the creator
}
