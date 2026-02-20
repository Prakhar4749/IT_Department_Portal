package com.adminService.feign;

import com.adminService.DTO.ApiResponse;
import com.adminService.DTO.SignupRequest;

import com.adminService.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

// "auth-service" must match the name in Eureka
// Added a configuration class to handle abrupt external failures elegantly
@FeignClient(name = "auth-service", configuration = FeignClientConfig.class)
public interface AuthClient {

    @PostMapping("/auth/internal/create-admin")
    ApiResponse<Long> createAdminUser(
            @RequestHeader("x-gateway-secret") String secret,
            @RequestBody SignupRequest request
    );
}