package com.LMS.Learning_Management_System.controller;


import com.LMS.Learning_Management_System.entity.Student;
import com.LMS.Learning_Management_System.service.NotificationsService;
import com.LMS.Learning_Management_System.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentService studentService;
    private final NotificationsService notificationsService;
    public StudentController(StudentService studentService, NotificationsService notificationsService) {
        this.studentService = studentService;
        this.notificationsService = notificationsService;
    }
    @PutMapping("/update_profile/{studentId}")
    public ResponseEntity<String> updateUser(@PathVariable int studentId,
                           @RequestBody Student student,
                           HttpServletRequest request
    ) {
        try {
            studentService.save(studentId, student, request);
            return ResponseEntity.ok("Student updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/allnotifications/{userId}")
    public List<String> getAllNotifications(@PathVariable int userId, HttpServletRequest request)  {
        return notificationsService.getAllNotifications(userId, request);
    }

    @GetMapping("/unreadnotifications/{userId}")
    public List<String> getUnreadNotifications(@PathVariable int userId , HttpServletRequest request) {
        return notificationsService.getAllUnreadNotifications(userId , request);
    }
}
