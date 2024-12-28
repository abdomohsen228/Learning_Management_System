package com.LMS.Learning_Management_System.service;

import com.LMS.Learning_Management_System.entity.Notifications;
import com.LMS.Learning_Management_System.entity.Users;
import com.LMS.Learning_Management_System.entity.UsersType;
import com.LMS.Learning_Management_System.repository.NotificationsRepository;
import com.LMS.Learning_Management_System.repository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationsRepository notificationsRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private NotificationsService notificationsService;

    private UsersType instructorType;
    private Users instructorUser;
    private Users studentUser;
    private UsersType studentType;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        studentType = new UsersType();
        studentType.setUserTypeId(2);

        instructorUser = new Users();
        instructorUser.setUserId(1);
        instructorUser.setUserTypeId(instructorType);

        studentUser = new Users();
        studentUser.setUserId(2);
        studentUser.setUserTypeId(studentType);

        instructorType = new UsersType();
        instructorType.setUserTypeId(3);
    }

    @Test
    void getAllNotificationsForValidUserTest() {

        Users user = new Users(2, "instructor@example.com", "password", new Date(), instructorType);
        Notifications notification1 = new Notifications(1, user, "Message 1", new Date());
        notification1.setRead(false);
        Notifications notification2 = new Notifications(2, user, "Message 2", new Date());
        notification2.setRead(true);

        List<Notifications> notificationsList = List.of(notification1, notification2);
        when(notificationsRepository.findAll()).thenReturn(notificationsList);

        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(studentUser);

        List<String> result = notificationsService.getAllNotifications(2, request);

        assertEquals(2, result.size());
        assertTrue(result.contains("Message 1"));
        assertTrue(result.contains("Message 2"));
        verify(notificationsRepository, times(2)).save(any(Notifications.class));
    }

    @Test
    void getAllNotificationsForInvalidUserTest() {

        Users user = new Users(2, "instructor@example.com", "password", new Date(), instructorType);
        Notifications notification = new Notifications(1, user, "Message", new Date());
        List<Notifications> notificationsList = List.of(notification);

        when(notificationsRepository.findAll()).thenReturn(notificationsList);
        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(studentUser);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationsService.getAllNotifications(999, request);
        });

//        assertEquals("Logged-in instructor does not have access for this course.", exception.getMessage());
//        List<String> result = notificationsService.getAllNotifications(999, request);
//
        assertEquals("ID mismatch. Please provide the correct ID.", exception.getMessage());
        verify(notificationsRepository, never()).save(any(Notifications.class));
    }

    @Test
    void getAllNotificationsForUserWithNoNotifications_Test() {

        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(studentUser);
        when(notificationsRepository.findAll()).thenReturn(List.of());
        List<String> result = notificationsService.getAllNotifications(2 ,request );

        assertTrue(result.isEmpty());
        verify(notificationsRepository, never()).save(any(Notifications.class));
    }

    @Test
    void getAllUnreadNotificationsForValidUser_Test() {

        Users user = new Users(2, "instructor@example.com", "password", new Date(), instructorType);
        Notifications notification1 = new Notifications(1, user, "Message 1", new Date());
        notification1.setRead(false);
        Notifications notification2 = new Notifications(2, user, "Message 2", new Date());
        notification2.setRead(true);
        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(studentUser);

        List<Notifications> notificationsList = List.of(notification1, notification2);
        when(notificationsRepository.findAll()).thenReturn(notificationsList);

        List<String> result = notificationsService.getAllUnreadNotifications(2, request);

        assertEquals(1, result.size());
        assertTrue(result.contains("Message 1"));
        verify(notificationsRepository, times(1)).save(notification1);
    }

    @Test
    void getAllUnreadNotificationsForInvalidUserTest() {

        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(studentUser);
        Users validUser = new Users(2, "instructor@example.com", "password", new Date(), instructorType);
        Notifications notification = new Notifications(1, validUser, "Message", new Date());
        notification.setRead(false);
        List<Notifications> notificationsList = List.of(notification);

        when(notificationsRepository.findAll()).thenReturn(notificationsList);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationsService.getAllUnreadNotifications(999, request);
        });

//        assertEquals("Logged-in instructor does not have access for this course.", exception.getMessage());
//        List<String> result = notificationsService.getAllNotifications(999, request);
//
        assertEquals("ID mismatch. Please provide the correct ID.", exception.getMessage());
        verify(notificationsRepository, never()).save(any(Notifications.class));

    }



    @Test
    void getAllUnreadNotificationsForUserWithNoUnreadNotifications_Test() {
        Users user = new Users(2, "user1@example.com", "password", new Date(), instructorType);
        Notifications notification1 = new Notifications(1, user, "Message", new Date());
        notification1.setRead(true);
        HttpSession mockSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(studentUser);


        List<Notifications> notificationsList = List.of(notification1);

        when(notificationsRepository.findAll()).thenReturn(notificationsList);
        List<String> result = notificationsService.getAllUnreadNotifications(2,request);

        assertTrue(result.isEmpty());
        verify(notificationsRepository, never()).save(any(Notifications.class));
    }

    @Test
    void sendNotificationForValidUser_Test() {

        Users user = new Users(1, "instructor@example.com", "password", new Date(), instructorType);
        when(usersRepository.findById(1)).thenReturn(Optional.of(user));

        notificationsService.sendNotification("Message", 1);

        ArgumentCaptor<Notifications> captor = ArgumentCaptor.forClass(Notifications.class);
        verify(notificationsRepository).save(captor.capture());

        Notifications savedNotification = captor.getValue();
        assertEquals("Message", savedNotification.getMessage());
        assertEquals(user, savedNotification.getUserId());
        assertFalse(savedNotification.isRead());
    }

    @Test
    void sendNotificationForInvalidUser_Test() {

        when(usersRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationsService.sendNotification("Message", 1));

        verify(notificationsRepository, never()).save(any(Notifications.class));
    }
}
