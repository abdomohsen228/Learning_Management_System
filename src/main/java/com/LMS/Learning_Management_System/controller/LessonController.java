package com.LMS.Learning_Management_System.controller;

import com.LMS.Learning_Management_System.dto.LessonDto;
import com.LMS.Learning_Management_System.entity.Course;
import com.LMS.Learning_Management_System.entity.Lesson;
import com.LMS.Learning_Management_System.service.LessonService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/lesson")
public class LessonController {
    @Autowired
    private final LessonService lessonService;
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }
    @PostMapping("/add_lesson")
    public ResponseEntity<String> addLesson(@RequestBody Lesson lesson , HttpServletRequest request){
        try {
            lessonService.addLesson(lesson , request);
            return ResponseEntity.ok("Lesson added successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/get_all_lessons/{courseId}")
    public ResponseEntity<?> getAllLessons(@PathVariable int courseId , HttpServletRequest request) {
        try {
            List<LessonDto> lessons = lessonService.getLessonsByCourseId(courseId , request);
            return ResponseEntity.ok(lessons);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/lesson_id/{lessonId}")
    public ResponseEntity<?> getLessonById(@PathVariable int lessonId , HttpServletRequest request) {
        try {
            LessonDto lesson = lessonService.getLessonById(lessonId, request);
            return ResponseEntity.ok(lesson);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/lesson_id/{lessonId}")
    public ResponseEntity<String> updateLesson(
            @PathVariable int lessonId,
            @RequestBody Lesson updatedLesson,
            HttpServletRequest request) {
        try {
            lessonService.updateLesson(lessonId, updatedLesson, request);
            return ResponseEntity.ok("Lesson updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/delete/lesson_id/{lessonId}/course_id/{courseId}")
    public ResponseEntity<String> deleteLesson(@PathVariable int lessonId , @PathVariable int courseId,HttpServletRequest request) {
        try {
            lessonService.deleteLesson(lessonId, courseId , request);
            return ResponseEntity.ok("Lesson deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/studnet_enter_lesson/course_id/{courseId}/lesson_id/{lessonId}/otp/{otp}")
    public ResponseEntity<?> StudentEnterLesson(@PathVariable int courseId ,
                                                @PathVariable int lessonId ,
                                                @PathVariable String otp ,
                                                HttpServletRequest request) {
        try{
            lessonService.StudentEnterLesson( courseId , lessonId  , otp,request);
            return ResponseEntity.ok("Student entered lesson successfully.");
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/attendances/{lessonId}")
    public ResponseEntity <List <String>> trackLessonAttendances (@PathVariable int lessonId, HttpServletRequest request)
    {
        try
        {
            List <String> submissions = lessonService.lessonAttendance(lessonId, request);
            return ResponseEntity.ok(submissions);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body(Collections.singletonList(e.getMessage()));
        }
    }
}
