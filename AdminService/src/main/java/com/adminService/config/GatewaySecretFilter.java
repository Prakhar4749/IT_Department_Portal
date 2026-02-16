package com.adminService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GatewaySecretFilter extends OncePerRequestFilter {

    @Value("${gateway.secret}")
    private String expectedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check for the secret header from API Gateway
        String requestSecret = request.getHeader("x-gateway-secret");

        if (requestSecret == null || !requestSecret.equals(expectedSecret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied: You must use the API Gateway.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}