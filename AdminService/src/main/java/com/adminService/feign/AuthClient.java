package com.adminService.feign;


import com.adminService.DTO.SignupRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

// "auth-service" must match the name in Eureka
@FeignClient(name = "auth-service")
public interface AuthClient {

    @PostMapping("/auth/internal/create-admin")
    Long createAdminUser(
            @RequestHeader("x-gateway-secret") String secret, // Pass the handshake secret
            @RequestBody SignupRequest request
    );
}