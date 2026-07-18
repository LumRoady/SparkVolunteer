package com.spark.volunteer.service;

import com.spark.volunteer.entity.Checkin;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.repository.CheckinRepository;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.impl.CheckinServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckinServiceImplTest {

    @Mock
    private CheckinRepository checkinRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private CheckinServiceImpl checkinService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setRole("VOLUNTEER");
        testUser.setPoints(100);
        testUser.setCheckinStreak(5);
        testUser.setIsDeleted(0);
    }

    @Test
    void checkin_userNotFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> checkinService.checkin(999L));
        verify(checkinRepository, never()).save(any());
    }

    @Test
    void checkin_alreadyCheckedIn_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(redisCacheService.hasCheckedInToday(eq(1L), anyInt())).thenReturn(true);

        assertThrows(BusinessException.class, () -> checkinService.checkin(1L));
        verify(checkinRepository, never()).save(any());
    }

    @Test
    void checkin_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(redisCacheService.hasCheckedInToday(eq(1L), anyInt())).thenReturn(false);
        when(checkinRepository.save(any(Checkin.class))).thenAnswer(invocation -> {
            Checkin c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(redisCacheService.getCheckinStreak(eq(1L), anyInt())).thenReturn(6L);

        Checkin result = checkinService.checkin(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("SUCCESS", result.getStatus());
        verify(redisCacheService).markCheckin(eq(1L), anyInt());
        verify(userRepository).addPointsAtomically(eq(1L), eq(10));
    }

    @Test
    void hasCheckedInToday_true() {
        when(checkinRepository.findByUserIdAndCheckinDate(eq(1L), any(Date.class)))
                .thenReturn(new Checkin());

        boolean result = checkinService.hasCheckedInToday(1L);

        assertTrue(result);
    }

    @Test
    void hasCheckedInToday_false() {
        when(checkinRepository.findByUserIdAndCheckinDate(eq(1L), any(Date.class)))
                .thenReturn(null);

        boolean result = checkinService.hasCheckedInToday(1L);

        assertFalse(result);
    }

    @Test
    void getCheckinStreak_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        int streak = checkinService.getCheckinStreak(1L);

        assertEquals(5, streak);
    }

    @Test
    void getCheckinStreak_userNotFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> checkinService.getCheckinStreak(999L));
    }

    @Test
    void getTotalCheckins_success() {
        when(checkinRepository.countByUserId(1L)).thenReturn(30L);

        long total = checkinService.getTotalCheckins(1L);

        assertEquals(30L, total);
    }
}
