package com.notificationService.controller;


import com.notificationService.DTO.ApiResponse;
import com.notificationService.entities.Notification;
import com.notificationService.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(
            @RequestHeader("loggedInUserId") Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        List<Notification> notifications = notificationService.getUserNotifications(userId, unreadOnly);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications fetched successfully."));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read."));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@RequestHeader("loggedInUserId") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read."));
    }
}