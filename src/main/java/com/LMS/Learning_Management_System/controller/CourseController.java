package com.LMS.Learning_Management_System.controller;

import com.LMS.Learning_Management_System.dto.CourseDto;
import com.LMS.Learning_Management_System.entity.Course;
import com.LMS.Learning_Management_System.entity.Lesson;
import com.LMS.Learning_Management_System.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {
    private final CourseService courseService;
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }
    @PostMapping("/add_course")
    public ResponseEntity<String> addCourse(@RequestBody Course course ,HttpServletRequest request)

    {
        try {
            courseService.addCourse(course , request , course.getInstructorId().getUserAccountId());
            return ResponseEntity.ok("Course created successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/course_id/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable int id, HttpServletRequest request) {
        try {
            CourseDto courseDTO = courseService.getCourseById(id , request);
            return ResponseEntity.ok(courseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/all_courses")
    public ResponseEntity<?> getAllCourses(HttpServletRequest request) {
        try {
            List<CourseDto> courseDTOList = courseService.getAllCourses(request);
            return ResponseEntity.ok(courseDTOList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


//    HttpServletRequest request allow to retrieve the logged-in user from the session inside the service method.
    @PutMapping("/update/course_id/{courseId}")
    public ResponseEntity<String> updateCourse(
            @PathVariable int courseId,
            @RequestBody Course updatedCourse,
            HttpServletRequest request) {
        try {
            courseService.sendNotificationsToEnrolledStudents(courseId,request);
            courseService.updateCourse(courseId, updatedCourse, request);
            return ResponseEntity.ok("Course updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/delete/course_id/{courseId}")
    public ResponseEntity<String> deleteCourse(@PathVariable int courseId,HttpServletRequest request) {
        try {
            courseService.deleteCourse(courseId , request);
            return ResponseEntity.ok("Course deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/upload_media/{courseId}")
    public ResponseEntity<String> uploadMedia(@PathVariable int courseId,
                                              @RequestParam("file") MultipartFile file,
                                              HttpServletRequest request) {
        try {
            courseService.uploadMediaFile(courseId, file, request);
            return ResponseEntity.ok("File uploaded successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

}
// ResponseEntity<?> is a flexible way to represent HTTP responses with different types of body content in Spring controllers