package com.LMS.Learning_Management_System.service;

import com.LMS.Learning_Management_System.dto.LessonDto;
import com.LMS.Learning_Management_System.dto.StudentDto;
import com.LMS.Learning_Management_System.entity.*;
import com.LMS.Learning_Management_System.service.LessonService;
import com.LMS.Learning_Management_System.repository.CourseRepository;
import com.LMS.Learning_Management_System.repository.EnrollmentRepository;
import com.LMS.Learning_Management_System.repository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private final NotificationsService notificationsService;


    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository, CourseRepository courseRepository, NotificationsService notificationsService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.notificationsService = notificationsService;
    }

    public void enrollInCourse(Enrollment enrollmentRequest, HttpServletRequest request) {
        Users loggedInUser = (Users) request.getSession().getAttribute("user");
        if (loggedInUser == null) {
            throw new IllegalArgumentException("No user is logged in.");
        }

        if (loggedInUser.getUserId()!=enrollmentRequest.getStudent().getUserAccountId()) {
            throw new IllegalArgumentException("Student ID mismatch. Please provide the correct ID.");
        }

        Student student = studentRepository.findById(enrollmentRequest.getStudent().getUserAccountId())
                .orElseThrow(() -> new IllegalArgumentException("No student found with the given ID."));

        int courseId = enrollmentRequest.getCourse().getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No course found with the given ID: " + courseId));

        boolean isEnrolled = enrollmentRepository.existsByStudentAndCourse(student, course);
        if (isEnrolled) {
            throw new IllegalArgumentException("Student is already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(new java.util.Date());

        enrollmentRepository.save(enrollment);
        int stdId = enrollmentRequest.getStudent().getUserAccountId();
        notificationsService.sendNotification("Student with id "+ stdId +" Enrolled ", course.getInstructorId().getUserAccountId());
    }

    public List<StudentDto> viewEnrolledStudents(int courseId, HttpServletRequest request){
        Course course = check_course_before_logic(courseId , request);
        List<Enrollment>enrollments = enrollmentRepository.findByCourse(course);
        List<Student> students = new ArrayList<Student>();
        for (Enrollment enrollment : enrollments) {
            students.add(enrollment.getStudent());
        }
        return convertToDtoList(students);
    }

    private List<StudentDto> convertToDtoList(List<Student> students) {
        return students.stream()
                .map(student -> new StudentDto(
                        student.getUserAccountId(),
                        student.getFirstName(),
                        student.getLastName()
                ))
                .collect(Collectors.toList());
    }

    public void removeEnrolledStudent(int courseId, int studentId, HttpServletRequest request){
        Course course = check_course_logic(courseId , request);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No student found with the given ID."));
        boolean isEnrolled = enrollmentRepository.existsByStudentAndCourse(student, course);
        if(!isEnrolled){
            throw new IllegalArgumentException("This student is not enrolled in this course");
        }
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student,course);
        enrollmentRepository.deleteById(enrollment.getEnrollmentId());
    }


    private Course check_course_before_logic(int courseId, HttpServletRequest request)
    {
        //Both Admin and Instructor can access
        Users loggedInUser = (Users) request.getSession().getAttribute("user");
        if (loggedInUser == null) {
            throw new IllegalArgumentException("No user is logged in.");
        }
        if (loggedInUser.getUserTypeId() == null || loggedInUser.getUserTypeId().getUserTypeId() == 2) {
            throw new IllegalArgumentException("Logged-in user is not an Instructor or Admin.");
        }
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No course found with the given ID: " + courseId));

        if (loggedInUser.getUserTypeId().getUserTypeId() == 3 &&
                existingCourse.getInstructorId().getUserAccountId() != loggedInUser.getUserId()) {
            throw new IllegalArgumentException("You are not the Instructor of this course");
        }
        return existingCourse;
    }

    private Course check_course_logic(int courseId, HttpServletRequest request)
    {
        //only Instructor can access
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
            throw new IllegalArgumentException("You are not the Instructor of this course.");
        }
        return existingCourse;
    }
}