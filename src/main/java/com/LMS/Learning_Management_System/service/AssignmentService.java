package com.LMS.Learning_Management_System.service;

import com.LMS.Learning_Management_System.dto.AssignmentDto;
import com.LMS.Learning_Management_System.entity.*;
import com.LMS.Learning_Management_System.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, SubmissionRepository submissionRepository,
                             CourseRepository courseRepository, StudentRepository studentRepository,
                             EnrollmentRepository enrollmentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public void uploadAssignment(AssignmentDto assignment, HttpServletRequest request) {
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("You are not logged in");
        }

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(()-> new IllegalArgumentException("Course not found"));

        Student student = studentRepository.findById(loggedInInstructor.getUserId())
                .orElseThrow(()-> new IllegalArgumentException("You're not a student"));

        Boolean isExist = enrollmentRepository.existsByStudentAndCourse(student, course);
        if (!isExist) {
            throw new IllegalArgumentException("You're not enrolled in this course");
        }


        List<Submission> submissions = submissionRepository.findByStudentId(student);

        for (Submission s : submissions) {
            if (s.getAssignmentId().getAssignmentId() == assignment.getAssignmentId()){
                throw new IllegalArgumentException("You've already submitted this assignment");
            }
        }
        Assignment assignment1 = new Assignment();
        assignment1.setAssignmentId(assignment.getAssignmentId());
        assignment1.setDescription(assignment.getAssignmentDescription());
        assignment1.setCourseID(course);
        assignment1.setDueDate(new Date());
        assignment1.setTitle(assignment.getAssignmentTitle());

        Submission submission = new Submission();
        submission.setAssignmentId(assignment1);
        submission.setStudentId(student);
        submissionRepository.save(submission);
    }


    public void gradeAssignment(int studentID, int assigID, float grade, HttpServletRequest request ) {
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("You are not logged in");
        }

        Assignment assignment = assignmentRepository.findById(assigID)
                .orElseThrow(()-> new IllegalArgumentException("Assignment not found"));


        if (loggedInInstructor.getUserId() != assignment.getCourseID().getInstructorId().getUserAccountId()){
            throw new IllegalArgumentException("You're not the instructor of this course");
        }


        Student student = studentRepository.findById(studentID)
                .orElseThrow(()-> new IllegalArgumentException("Student not found"));

        List<Submission> submission = submissionRepository.findByStudentId(student);


        if (submission.isEmpty()) {
            throw new IllegalArgumentException("Student has no submissions");
        }

        for (Submission s : submission) {
            if (s.getAssignmentId().getAssignmentId() == assignment.getAssignmentId()) {
                s.setGrade(grade);
                submissionRepository.save(s);
                return;
            }
        }
        throw new IllegalArgumentException("Student didn't submit this assignment");

    }

    public void saveAssignmentFeedback(int studentID, int assigID, String feedback, HttpServletRequest request) {
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("You are not logged in");
        }


        Assignment assignment = assignmentRepository.findById(assigID)
                .orElseThrow(()-> new IllegalArgumentException("Assignment not found"));


        if (loggedInInstructor.getUserId() != assignment.getCourseID().getInstructorId().getUserAccountId()){
            throw new IllegalArgumentException("You're not the instructor of this course");
        }


        Student student = studentRepository.findById(studentID)
                .orElseThrow(()-> new IllegalArgumentException("Student not found"));

        List<Submission> submission = submissionRepository.findByStudentId(student);


        if (submission.isEmpty()) {
            throw new IllegalArgumentException("Student has no submissions");
        }

        for (Submission s : submission) {
            if (s.getAssignmentId().getAssignmentId() == assignment.getAssignmentId()) {
                s.setFeedback(feedback);
                submissionRepository.save(s);
                return;
            }
        }
        throw new IllegalArgumentException("Student didn't submit this assignment");
    }

    public String getFeedback(int assigID, HttpServletRequest request) {
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("You are not logged in");
        }
        Assignment assignment = assignmentRepository.findById(assigID)
                .orElseThrow(()-> new IllegalArgumentException("Assignment not found"));


        Student student = studentRepository.findById(loggedInInstructor.getUserId())
                .orElseThrow(()-> new IllegalArgumentException("You're not a student"));

        Boolean isExist = enrollmentRepository.existsByStudentAndCourse(student, assignment.getCourseID());
        if (!isExist) {
            throw new IllegalArgumentException("You're not enrolled in this course");
        }

        List<Submission> submission = submissionRepository.findByStudentId(student);


        if (submission.isEmpty()) {
            throw new IllegalArgumentException("Student has no submissions");
        }

        String feedback = "";

        for (Submission s : submission) {
            if (s.getAssignmentId().getAssignmentId() == assignment.getAssignmentId()) {
                if (s.getFeedback() == null){
                    feedback =  "There is no feedback yet";
                    break;

            }
                else {
                    feedback =  s.getFeedback();
                    break;
                }
        }
        throw new IllegalArgumentException("Student didn't submit this assignment");
    }
        return feedback;
    }

    public List <String> assignmentSubmissions (int assignmentId, HttpServletRequest request)
    {
        if (assignmentRepository.existsById(assignmentId))
        {
            Assignment assignment = assignmentRepository.findById(assignmentId).get();
            List <Submission> assignmentSubmissions = submissionRepository.findAllByAssignmentId(assignment);
            Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
            int instructorId = assignment.getCourseID().getInstructorId().getUserAccountId();

            if (loggedInInstructor == null)
            {
                throw new IllegalArgumentException("No logged in user is found.");
            }
            else if (loggedInInstructor.getUserTypeId() == null || loggedInInstructor.getUserTypeId().getUserTypeId() != 3)
            {
                throw new IllegalArgumentException("Logged-in user is not an instructor.");
            }
            else if (instructorId != loggedInInstructor.getUserId())
            {
                throw new IllegalArgumentException("Logged-in instructor does not have access for this assignment submissions.");
            }

            List <String> submissions = new ArrayList<>();
            for (Submission submission : assignmentSubmissions)
            {
                Student student = submission.getStudentId();
                String studentSubmission = student.getUserAccountId() + ": " + submission.getGrade();
                submissions.add(studentSubmission);
            }
            return submissions;
        }
        else
        {
            throw new IllegalArgumentException("Assignment with ID " + assignmentId + " not found.");
        }
    }

    public void addAssignment(AssignmentDto assignment, HttpServletRequest request) {
        Users loggedInInstructor = (Users) request.getSession().getAttribute("user");
        if (loggedInInstructor == null) {
            throw new IllegalArgumentException("No user is logged in.");
        }
        if (loggedInInstructor.getUserTypeId() == null || loggedInInstructor.getUserTypeId().getUserTypeId() != 3) {
            throw new IllegalArgumentException("Logged-in user is not an instructor.");
        }
        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("No such CourseId"));

        int ids = course.getInstructorId().getUserAccountId();
        if (loggedInInstructor.getUserId() != ids) {
            throw new IllegalArgumentException("You are not the Instructor of this course");
        }
        boolean exist = assignmentRepository.existsById(assignment.getAssignmentId());
        if (exist) {
            throw new IllegalArgumentException("Assignment already exists");
        }
        Assignment assignment1=new Assignment();
        assignment1.setDescription(assignment.getAssignmentDescription());
        assignment1.setTitle(assignment.getAssignmentTitle());
        assignment1.setDueDate(new Date());
        assignment1.setCourseID(course);

        assignmentRepository.save(assignment1);
    }
}
