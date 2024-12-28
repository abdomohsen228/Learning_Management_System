package com.LMS.Learning_Management_System.service;


import com.LMS.Learning_Management_System.dto.AssignmentDto;
import com.LMS.Learning_Management_System.entity.*;
import com.LMS.Learning_Management_System.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AssignmentService assignmentService;

    private Users instructorUser;
    private Users instructorUser2;
    private Users studentUser;
    private Course course;
    private Enrollment enrollment;
    private UsersType instructorType;
    private UsersType studentType;
    private Assignment assignment;
    private Submission submission;
    private Student student;
    private AssignmentDto assignmentDto;
    private Instructor instructor;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        studentType = new UsersType();
        studentType.setUserTypeId(2);

        instructorType = new UsersType();
        instructorType.setUserTypeId(3);

        studentUser = new Users();
        studentUser.setUserId(1);
        studentUser.setUserTypeId(studentType);

        instructorUser = new Users();
        instructorUser.setUserId(2);
        instructorUser.setUserTypeId(instructorType);

        instructorUser2 = new Users();
        instructorUser2.setUserId(3);
        instructorUser2.setUserTypeId(instructorType);

        student = new Student();
        student.setUserId(studentUser);

        instructor = new Instructor();
        instructor.setUserId(instructorUser);
        instructor.setUserAccountId(2);

        course = new Course();
        course.setCourseId(1);
        course.setCourseName("Test Course");
        course.setInstructorId(instructor);

        assignment = new Assignment();
        assignment.setAssignmentId(1);
        assignment.setCourseID(course);

        assignmentDto = new AssignmentDto();
        assignmentDto.setAssignmentId(assignment.getAssignmentId());
        assignmentDto.setCourseId(assignment.getCourseID().getCourseId());

        enrollment = new Enrollment();
        enrollment.setEnrollmentId(1);
        enrollment.setCourse(course);
        enrollment.setStudent(student);

        submission = new Submission();
        submission.setSubmissionId(1);
        submission.setStudentId(student);
        submission.setAssignmentId(assignment);

    }


    @Test
    public void UploadAssignment_UserNotLoggedIn() {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.uploadAssignment(assignmentDto, request);
        });

        assertEquals("You are not logged in", exception.getMessage());
}

    @Test
    public void uploadAssignment_NotStudent(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);

        when(courseRepository.findById(1)).thenReturn(Optional.of(course));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.uploadAssignment(assignmentDto, request);
        });

        assertEquals("You're not a student", exception.getMessage());


    }

    @Test
    public void uploadAssignment_CourseNotFound(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(studentUser);

        when(courseRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.uploadAssignment(assignmentDto, request);
        });

        assertEquals("Course not found", exception.getMessage());

    }


    @Test
    public void uploadAssignment_NotEnrolled(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(studentUser);

        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentAndCourse(student,course)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.uploadAssignment(assignmentDto, request);
        });

        assertEquals("You're not enrolled in this course", exception.getMessage());

    }

    @Test
    public void uploadAssignment_AlreadySubmitted(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(studentUser);

        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByStudentAndCourse(student,course)).thenReturn(true);
        List<Submission> submissions = new ArrayList<>();
        submissions.add(submission);
        when(submissionRepository.findByStudentId(student)).thenReturn(submissions);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.uploadAssignment(assignmentDto, request);
        });

        assertEquals("You've already submitted this assignment", exception.getMessage());

    }

    @Test
    public void gradeAssignment_UserNotLoggedIn(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.gradeAssignment(5,1,100, request);
        });

        assertEquals("You are not logged in", exception.getMessage());

    }


    @Test
    public void gradeAssignment_AssignmentNotFound(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);

        when(assignmentRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.gradeAssignment(1,6,100, request);
        });

        assertEquals("Assignment not found", exception.getMessage());

    }

    @Test
    public void gradeAssignment_NotTheInstructor(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser2);

        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.gradeAssignment(1,1,100, request);
        });

        assertEquals("You're not the instructor of this course", exception.getMessage());

    }


    @Test
    public void gradeAssignment_StudentNotFound(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);


        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.empty());
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.gradeAssignment(1,1,50, request);
        });

        assertEquals("Student not found", exception.getMessage());

    }

    @Test
    public void gradeAssignment_StudentHasNoSubmissions(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);


        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(submissionRepository.findByStudentId(student)).thenReturn(new ArrayList<>());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.gradeAssignment(1,1,50, request);
        });

        assertEquals("Student has no submissions", exception.getMessage());

    }

    @Test
    public void gradeAssignment_StudentDidntSubmit(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);


        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));

        Submission unrelatedSubmission = new Submission();
        Assignment unrelatedAssignment = new Assignment();
        unrelatedAssignment.setAssignmentId(2);
        unrelatedSubmission.setAssignmentId(unrelatedAssignment);
        unrelatedSubmission.setStudentId(student);

        List<Submission> submissions = new ArrayList<>();
        submissions.add(unrelatedSubmission);

        when(submissionRepository.findByStudentId(student)).thenReturn(submissions);


        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.gradeAssignment(1,1,50, request);
        });

        assertEquals("Student didn't submit this assignment", exception.getMessage());

    }



    @Test
    public void saveAssignmentFeedback_UserNotLoggedIn(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.saveAssignmentFeedback(5,1,"done", request);
        });

        assertEquals("You are not logged in", exception.getMessage());

    }

    @Test
    public void saveAssignmentFeedback_AssignmentNotFound(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);

        when(assignmentRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.saveAssignmentFeedback(1,6,"done", request);
        });

        assertEquals("Assignment not found", exception.getMessage());

    }

    @Test
    public void saveAssignmentFeedback_NotTheInstructor(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser2);

        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.saveAssignmentFeedback(1,1,"done", request);
        });

        assertEquals("You're not the instructor of this course", exception.getMessage());

    }


    @Test
    public void saveAssignmentFeedback_StudentNotFound(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);


        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.empty());
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.saveAssignmentFeedback(1,1,"done", request);
        });

        assertEquals("Student not found", exception.getMessage());

    }


    @Test
    public void saveAssignmentFeedback_StudentHasNoSubmissions(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);


        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(submissionRepository.findByStudentId(student)).thenReturn(new ArrayList<>());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.saveAssignmentFeedback(1,1,"done", request);
        });

        assertEquals("Student has no submissions", exception.getMessage());

    }

    @Test
    public void saveAssignmentFeedback_StudentDidntSubmit(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);


        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));

        Submission unrelatedSubmission = new Submission();
        Assignment unrelatedAssignment = new Assignment();
        unrelatedAssignment.setAssignmentId(2);
        unrelatedSubmission.setAssignmentId(unrelatedAssignment);
        unrelatedSubmission.setStudentId(student);

        List<Submission> submissions = new ArrayList<>();
        submissions.add(unrelatedSubmission);

        when(submissionRepository.findByStudentId(student)).thenReturn(submissions);


        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.saveAssignmentFeedback(1,1,"done", request);
        });

        assertEquals("Student didn't submit this assignment", exception.getMessage());

    }

    @Test
    public void getFeedback_UserNotLoggedIn(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.getFeedback(1, request);
        });

        assertEquals("You are not logged in", exception.getMessage());

    }

    @Test
    public void getFeedback_AssignmentNotFound(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);

        when(assignmentRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.getFeedback(1, request);
        });

        assertEquals("Assignment not found", exception.getMessage());

    }

    @Test
    public void getFeedback_NotStudent(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(instructorUser);

        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));


        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.getFeedback(1, request);
        });

        assertEquals("You're not a student", exception.getMessage());
    }

    @Test
    public void getFeedback_NotEnrolled(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(studentUser);

        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByStudentAndCourse(student,course)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.getFeedback(1, request);
        });

        assertEquals("You're not enrolled in this course", exception.getMessage());

    }

    @Test
    public void getFeedback_StudentHasNoSubmissions(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(studentUser);


        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentAndCourse(student,course)).thenReturn(true);
        when(submissionRepository.findByStudentId(student)).thenReturn(new ArrayList<>());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.getFeedback(1, request);
        });

        assertEquals("Student has no submissions", exception.getMessage());

    }

    @Test
    public void getFeedback_StudentDidntSubmit(){
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(studentUser);


        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentAndCourse(student,course)).thenReturn(true);


        Submission unrelatedSubmission = new Submission();
        Assignment unrelatedAssignment = new Assignment();
        unrelatedAssignment.setAssignmentId(2);
        unrelatedSubmission.setAssignmentId(unrelatedAssignment);
        unrelatedSubmission.setStudentId(student);

        List<Submission> submissions = new ArrayList<>();
        submissions.add(unrelatedSubmission);

        when(submissionRepository.findByStudentId(student)).thenReturn(submissions);


        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.getFeedback(1, request);
        });

        assertEquals("Student didn't submit this assignment", exception.getMessage());

    }

    @Test
    void testAssignmentSubmissions_noLoggedInUser()
    {
        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);

        when(mockSession.getAttribute("user")).thenReturn(null);
        when(assignmentRepository.existsById(1)).thenReturn(true);
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findAllByAssignmentId(assignment)).thenReturn(List.of(submission));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
        {
            assignmentService.assignmentSubmissions(1, request);
        });

        assertEquals("No logged in user is found.", exception.getMessage());
    }

    @Test
    void testAssignmentSubmissions_notInstructor()
    {
        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);

        when(mockSession.getAttribute("user")).thenReturn(studentUser);
        when(assignmentRepository.existsById(1)).thenReturn(true);
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findAllByAssignmentId(assignment)).thenReturn(List.of(submission));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
        {
            assignmentService.assignmentSubmissions(1, request);
        });

        assertEquals("Logged-in user is not an instructor.", exception.getMessage());
    }

    @Test
    void testAssignmentSubmissions_notAssignmentInstructor()
    {
        Users inValidInstructorUser = new Users();
        inValidInstructorUser.setUserId(3);
        inValidInstructorUser.setUserTypeId(instructorType);

        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);

        when(mockSession.getAttribute("user")).thenReturn(inValidInstructorUser);
        when(assignmentRepository.existsById(1)).thenReturn(true);
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findAllByAssignmentId(assignment)).thenReturn(List.of(submission));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
        {
            assignmentService.assignmentSubmissions(1, request);
        });

        assertEquals("Logged-in instructor does not have access for this assignment submissions.", exception.getMessage());
    }

    @Test
    void testAssignmentSubmissions_assignmentNotFound()
    {
        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);

        when(mockSession.getAttribute("user")).thenReturn(instructorUser);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
        {
            assignmentService.assignmentSubmissions(2, request);
        });

        assertEquals("Assignment with ID 2 not found.", exception.getMessage());
    }

    @Test
    void testAssignmentSubmissions()
    {
        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);

        when(mockSession.getAttribute("user")).thenReturn(instructorUser);
        when(assignmentRepository.existsById(1)).thenReturn(true);
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findAllByAssignmentId(assignment)).thenReturn(List.of(submission));

        List <String> quizGrades = assignmentService.assignmentSubmissions(1, request);

        assertEquals(1, quizGrades.size());
    }
}
