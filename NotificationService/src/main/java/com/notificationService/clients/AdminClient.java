package com.notificationService.clients;

import com.notificationService.DTO.ApiResponse;
import com.notificationService.DTO.HodDetailsResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "admin-service", path = "/admin")
public interface AdminClient {

    // UPDATE: Now expects ApiResponse wrapper
    @GetMapping("/departments/{departmentId}/hod")
    ApiResponse<HodDetailsResponse> getHodDetails(@PathVariable("departmentId") Long departmentId);
}