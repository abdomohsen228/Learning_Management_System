package com.LMS.Learning_Management_System.service;

import com.LMS.Learning_Management_System.dto.CourseDto;
import com.LMS.Learning_Management_System.entity.*;
import com.LMS.Learning_Management_System.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;


    public CourseService(InstructorRepository instructorRepository, CourseRepository courseRepository, LessonRepository lessonRepository, LessonRepository lessonRepository1) {
        this.instructorRepository = instructorRepository;
        this.courseRepository = courseRepository;

        this.lessonRepository = lessonRepository1;
    }
    public void addCourse(Course course , HttpServletRequest request , int instructorId){
        // auth
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("No user is logged in.");
        }
        if (loggedInInstructor.getUserTypeId() == null || loggedInInstructor.getUserTypeId().getUserTypeId() != 3) {
            throw new IllegalArgumentException("Logged-in user is not an instructor.");
        }
        if(instructorId != loggedInInstructor.getUserId()){
            throw new IllegalArgumentException("Logged-in user is an another instructor.");
        }
        //

        if (courseRepository.findByCourseName(course.getCourseName()) != null) {
            throw new IllegalArgumentException("This CourseName already exist");
        }
        course.setCreationDate(new Date(System.currentTimeMillis()));
        if (course.getInstructorId() == null|| course.getInstructorId().getUserAccountId()==0) {
            throw new IllegalArgumentException("InstructorId cannot be null");
        }
        Instructor instructor = instructorRepository.findById(course.getInstructorId().getUserAccountId())
                .orElseThrow(() -> new IllegalArgumentException("No such Instructor"));
        course.setInstructorId(instructor);
        courseRepository.save(course);
    }
    public List<CourseDto> getAllCourses(HttpServletRequest request) {
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("No user is logged in.");
        }

        List<Course> courses = courseRepository.findAll();

        return convertToCourseDtoList(courses);

    }

    public CourseDto getCourseById(int id ,HttpServletRequest request) {
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("No user is logged in.");
        }
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No course found with the given ID: " + id));

        return new CourseDto(
                course.getCourseId(),
                course.getCourseName(),
                course.getInstructorId().getUserAccountId(),
                course.getDescription(),
                course.getDuration()
        );
    }
    public void updateCourse(int courseId, Course updatedCourse, HttpServletRequest request) {

        Course existingCourse = check_before_logic(courseId , request);
        existingCourse.setCourseName(updatedCourse.getCourseName());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setDuration(updatedCourse.getDuration());

        courseRepository.save(existingCourse);
    }
    public void deleteCourse(int courseId, HttpServletRequest request) {
        Course existingCourse = check_before_logic(courseId , request);
        courseRepository.delete(existingCourse);
    }




    private Course check_before_logic(int courseId, HttpServletRequest request)
    {
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("No user is logged in.");
        }
        if (loggedInInstructor.getUserTypeId() == null || loggedInInstructor.getUserTypeId().getUserTypeId() != 3) {
            throw new IllegalArgumentException("Logged-in user is not an instructor.");
        }
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No course found with the given ID: " + courseId));

        if (existingCourse.getInstructorId() == null ||
                existingCourse.getInstructorId().getUserAccountId() != loggedInInstructor.getUserId()) {
            throw new IllegalArgumentException("You are not authorized to update or delete this course.");
        }
        return existingCourse;
    }
    private List<CourseDto> convertToCourseDtoList(List<Course> courses) {
        return courses.stream()
                .map(course -> new CourseDto(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getInstructorId().getUserAccountId(),  // Assuming instructor is a related entity
                        course.getDescription(),
                        course.getDuration()
                ))
                .collect(Collectors.toList());
    }


}
