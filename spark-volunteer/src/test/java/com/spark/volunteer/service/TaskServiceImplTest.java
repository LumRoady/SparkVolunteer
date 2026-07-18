package com.spark.volunteer.service;

import com.spark.volunteer.entity.Task;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.repository.DeviceRepository;
import com.spark.volunteer.repository.TaskRepository;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.config.WebSocketMessageBridge;
import com.spark.volunteer.service.impl.TaskServiceImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private VolunteerGrowthService volunteerGrowthService;

    @Mock
    private WebSocketMessageBridge messageBridge;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task testTask;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setRole("ELDERLY");
        testUser.setIsDeleted(0);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setUserId(1L);
        testTask.setTitle("测试任务");
        testTask.setContent("测试任务描述");
        testTask.setType("life_service");
        testTask.setStatus(0);
        testTask.setCreateTime(new Date());
    }

    @Test
    void createTask_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreateTime(new Date());
            return saved;
        });

        Task result = taskService.createTask(testTask);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        verify(taskRepository).save(any(Task.class));
        verify(redisCacheService).addPendingTask(eq(1L), anyLong());
        verify(redisCacheService).cacheTask(any(), eq(1L));
    }

    @Test
    void createTask_emptyTitle_throwsException() {
        testTask.setTitle("");

        assertThrows(BusinessException.class, () -> taskService.createTask(testTask));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_nullTitle_throwsException() {
        testTask.setTitle(null);

        assertThrows(BusinessException.class, () -> taskService.createTask(testTask));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void acceptTask_success() {
        // 任务状态为待接单(status=0)
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        User volunteer = new User();
        volunteer.setId(2L);
        volunteer.setRole("VOLUNTEER");
        when(userRepository.findById(2L)).thenReturn(Optional.of(volunteer));

        // 原子接单返回成功（影响1行）
        when(taskRepository.acceptTaskAtomically(1L, 2L)).thenReturn(1);

        // 接单后重新加载任务（返回已接单状态）
        Task acceptedTask = new Task();
        acceptedTask.setId(1L);
        acceptedTask.setUserId(1L);
        acceptedTask.setTitle("测试任务");
        acceptedTask.setContent("测试任务描述");
        acceptedTask.setType("life_service");
        acceptedTask.setStatus(1);
        acceptedTask.setReceiverId(2L);
        acceptedTask.setCreateTime(new Date());
        // findById 被调用两次：第一次是 getTaskById（接单前检查），第二次是接单后重新加载
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask)).thenReturn(Optional.of(acceptedTask));

        Task result = taskService.acceptTask(1L, 2L);

        assertNotNull(result);
        assertEquals(1, result.getStatus());
        assertEquals(2L, result.getReceiverId());
        verify(redisCacheService).evictTaskCache(1L);
    }

    @Test
    void acceptTask_alreadyAccepted_throwsException() {
        // 任务已被接单(status=1)，再次接单应抛异常
        testTask.setStatus(1);
        testTask.setReceiverId(2L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(3L)).thenReturn(Optional.of(testUser));

        assertThrows(BusinessException.class, () -> taskService.acceptTask(1L, 3L));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void completeTask_success() {
        // 任务已接单(status=1)
        testTask.setStatus(1);
        testTask.setReceiverId(2L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.completeTask(1L);

        assertNotNull(result);
        assertEquals(2, result.getStatus());
        assertNotNull(result.getFinishTime());
        verify(redisCacheService).evictTaskCache(1L);
        verify(volunteerGrowthService).checkAchievements(2L);
    }

    @Test
    void cancelTask_success() {
        // 任务待接单(status=0)
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.cancelTask(1L);

        assertNotNull(result);
        assertEquals(3, result.getStatus());
        verify(redisCacheService).evictTaskCache(1L);
    }

    @Test
    void getTaskById_notFound_throwsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.getTaskById(999L));
    }
}
