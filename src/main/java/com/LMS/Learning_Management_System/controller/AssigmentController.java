package com.LMS.Learning_Management_System.controller;

import com.LMS.Learning_Management_System.dto.AssignmentDto;
import com.LMS.Learning_Management_System.dto.GetFeedbackDto;
import com.LMS.Learning_Management_System.dto.GradeAssignmentDto;
import com.LMS.Learning_Management_System.dto.SaveAssignmentDto;
import com.LMS.Learning_Management_System.entity.Assignment;
import com.LMS.Learning_Management_System.entity.Course;
import com.LMS.Learning_Management_System.entity.Student;
import com.LMS.Learning_Management_System.entity.Submission;
import com.LMS.Learning_Management_System.service.AssignmentService;
import com.LMS.Learning_Management_System.service.NotificationsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/assignment")
public class AssigmentController {
    private final AssignmentService assignmentService;
    private final NotificationsService notificationsService;

    public AssigmentController(AssignmentService assignmentService, NotificationsService notificationsService) {
        this.assignmentService = assignmentService;
        this.notificationsService = notificationsService;
    }
    @PostMapping("/add_assignment")
    public ResponseEntity<String> addAssignment(@RequestBody AssignmentDto assignment , HttpServletRequest request)

    {
        try {
            assignmentService.addAssignment(assignment , request);
            return ResponseEntity.ok("Assignment created successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/uploadAssignment")
    public ResponseEntity<String> uploadAssignment(@RequestBody AssignmentDto assignment, HttpServletRequest request){
        try {
            assignmentService.uploadAssignment(assignment, request);
            return ResponseEntity.ok("Assignment uploaded successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/gradeAssignment")
    public ResponseEntity<String> gradeAssignment(@RequestBody GradeAssignmentDto gradeAssignmentDto, HttpServletRequest request){
        try {
            assignmentService.gradeAssignment(gradeAssignmentDto.getStudentId(), gradeAssignmentDto.getAssignmentId(), gradeAssignmentDto.getGrade(), request);
            String message = "Assignment "+gradeAssignmentDto.getAssignmentId()+" grade is uploaded";
            notificationsService.sendNotification(message, gradeAssignmentDto.getStudentId());
            return ResponseEntity.ok("Assignment has been graded successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/saveAssignmentFeedback")
    public ResponseEntity<String> saveAssignmentFeedback(@RequestBody SaveAssignmentDto saveAssignmentDto, HttpServletRequest request ){
        try {
            assignmentService.saveAssignmentFeedback(saveAssignmentDto.getStudentId(), saveAssignmentDto.getAssignmentId(), saveAssignmentDto.getFeedback(), request);
            return ResponseEntity.ok("Assignment feedback is saved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getFeedback")
    public ResponseEntity<String> getFeedback(@RequestBody GetFeedbackDto getFeedbackDto, HttpServletRequest request){
        try {
            return ResponseEntity.ok(assignmentService.getFeedback(getFeedbackDto.getAssignmentId(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/submissions/{assignmentId}")
    public ResponseEntity <List <String>> trackAssignmentSubmissions (@PathVariable int assignmentId, HttpServletRequest request)
    {
        try
        {
            List <String> submissions = assignmentService.assignmentSubmissions(assignmentId, request);
            return ResponseEntity.ok(submissions);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body(Collections.singletonList(e.getMessage()));
        }
    }
}
