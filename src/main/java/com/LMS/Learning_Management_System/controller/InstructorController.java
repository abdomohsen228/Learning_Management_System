package com.LMS.Learning_Management_System.controller;
import com.LMS.Learning_Management_System.entity.Instructor;
import com.LMS.Learning_Management_System.service.InstructorService;
import com.LMS.Learning_Management_System.service.NotificationsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor")
public class InstructorController {
    private final InstructorService instructorService;
    private final NotificationsService notificationsService;

    public InstructorController(InstructorService instructorService, NotificationsService notificationsService) {
        this.instructorService = instructorService;
        this.notificationsService = notificationsService;
    }
    @PutMapping("/update_profile/{instructorId}")
    public ResponseEntity<String> updateUser(@PathVariable int instructorId,
                                             @RequestBody Instructor instructor,
                                             HttpServletRequest request
    ) {
        try {
            instructorService.save(instructorId, instructor, request);
            return ResponseEntity.ok("Instructor updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/notifications/{userId}")
    public List<String> getAllNotifications(@PathVariable int userId ,HttpServletRequest request) {
        return notificationsService.getAllNotifications(userId ,request);
    }

    @GetMapping("/unreadnotifications/{userId}")
    public List<String> getUnreadNotifications(@PathVariable int userId ,HttpServletRequest request) {
        return notificationsService.getAllUnreadNotifications(userId, request);
    }
}
