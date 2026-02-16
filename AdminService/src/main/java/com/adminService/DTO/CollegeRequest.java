package com.adminService.DTO;


import lombok.Data;

@Data
public class CollegeRequest {
    private String name;
    private String location;
    private String adminEmail; // Email for the new College Admin
}
