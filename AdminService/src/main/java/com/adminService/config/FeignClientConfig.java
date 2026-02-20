package com.adminService.config;

import com.adminService.exceptions.DuplicateResourceException;
import com.adminService.exceptions.ExternalServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FeignClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            int status = response.status();

            // If Auth Service says bad request or conflict, assume duplicate user
            if (status == 400 || status == 409 || status == 500) {
                return new DuplicateResourceException("Registration failed: Email is likely already in use.");
            }

            // Fallback for abrupt Auth Service behavior (down, timeout, etc.)
            return new ExternalServiceException("Auth Service communication failed with status: " + status);
        }
    }
}