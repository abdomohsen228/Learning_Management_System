package com.LMS.Learning_Management_System.controller;

import com.LMS.Learning_Management_System.dto.AssignmentDto;
import com.LMS.Learning_Management_System.entity.Assignment;
import com.LMS.Learning_Management_System.entity.Course;
import com.LMS.Learning_Management_System.entity.Instructor;
import com.LMS.Learning_Management_System.service.AssignmentService;
import com.LMS.Learning_Management_System.service.CourseService;
import com.LMS.Learning_Management_System.service.NotificationsService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class AssignmentControllerTest {


    @Mock
    private AssignmentService assignmentService;


    @Mock
    private NotificationsService notificationsService;


    @InjectMocks
    private AssigmentController assigmentController;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
    }

    @Test
    void uploadAssignmentTest() {
        AssignmentDto assignmentDto = new AssignmentDto();
        assignmentDto.setAssignmentId(1);
        assignmentDto.setCourseId(1);

        HttpServletRequest request = mock(HttpServletRequest.class);

        doNothing().when(assignmentService).uploadAssignment(any(AssignmentDto.class), any(HttpServletRequest.class));

        ResponseEntity<String> response = assigmentController.uploadAssignment(assignmentDto, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Assignment uploaded successfully.", response.getBody());

        verify(assignmentService, times(1)).uploadAssignment(eq(assignmentDto), eq(request));
    }


    @Test
    void gradeAssignmentTest() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        doNothing().when(assignmentService).gradeAssignment(anyInt(), anyInt(), anyFloat(), any(HttpServletRequest.class));
        doNothing().when(notificationsService).sendNotification(anyString(), anyInt());

        ResponseEntity<String> response = assigmentController.gradeAssignment(1,1, 100, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Assignment has been graded successfully.", response.getBody());

        verify(assignmentService, times(1)).gradeAssignment(eq(1),eq(1), eq(100.0f), eq(request));
        verify(notificationsService, times(1)).sendNotification(eq("Assignment 1 grade is uploaded"),eq(1));

    }

    @Test
    void saveAssignmentFeedbackTest() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        doNothing().when(assignmentService).saveAssignmentFeedback(anyInt(), anyInt(), anyString(), any(HttpServletRequest.class));

        ResponseEntity<String> response = assigmentController.saveAssignmentFeedback(1, 1, "done", request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Assignment feedback is saved successfully.", response.getBody());

        verify(assignmentService, times(1)).saveAssignmentFeedback(eq(1), eq(1), eq("done"), eq(request));
    }


    @Test
    void getFeedbackTest() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(assignmentService.getFeedback(anyInt(), any(HttpServletRequest.class)))
                .thenReturn("This is a feedback message");

        ResponseEntity<String> response = assigmentController.getFeedback(1, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("This is a feedback message", response.getBody());

        verify(assignmentService, times(1)).getFeedback(eq(1), eq(request));
    }

    @Test
    void testTrackAssignmentSubmissions()
    {
        List<String> ans = new ArrayList<String>();

        when(assignmentService.assignmentSubmissions(eq(1), eq(request))).thenReturn(ans);

        ResponseEntity <?> response = assigmentController.trackAssignmentSubmissions(1, request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ans, response.getBody());
        verify(assignmentService, times(1)).assignmentSubmissions(eq(1), eq(request));
    }
}
