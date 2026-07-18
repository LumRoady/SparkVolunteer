package com.spark.volunteer;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.controller.EmergencyController;
import com.spark.volunteer.dto.TaskResponseDTO;
import com.spark.volunteer.dto.request.CreateTaskRequest;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.service.TaskService;
import com.spark.volunteer.service.WechatNotifyService;
import com.spark.volunteer.config.WebSocketMessageBridge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private WechatNotifyService wechatNotifyService;

    @Mock
    private WebSocketMessageBridge messageBridge;

    @InjectMocks
    private EmergencyController emergencyController;

    @BeforeEach
    void setUp() {
        // @ExtendWith(MockitoExtension.class) 自动初始化，无需手动 openMocks
    }

    @Test
    void testCreateEmergencyTask() {
        // 使用 CreateTaskRequest 而非 Task（匹配控制器方法签名）
        CreateTaskRequest request = new CreateTaskRequest();
        request.setUserId(1L);
        request.setTitle("紧急求助");
        request.setContent("老人紧急求助");
        request.setType("sos");

        // 模拟服务层返回
        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setUserId(1L);
        savedTask.setTitle("紧急求助");
        savedTask.setType("sos");
        savedTask.setStatus(0);

        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        // 调用控制器方法
        Result<TaskResponseDTO> result = emergencyController.createEmergencyTask(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("sos", result.getData().getType());
    }

    @Test
    void testButtonHelpRed() {
        Task mockTask = new Task();
        mockTask.setId(1L);
        mockTask.setUserId(1L);
        mockTask.setTitle("紧急求助");
        mockTask.setType("sos");
        mockTask.setStatus(0);

        when(taskService.createTask(any(Task.class))).thenReturn(mockTask);

        Result<TaskResponseDTO> result = emergencyController.buttonHelp("red", 1L, "device_001");

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testButtonHelpGreen() {
        Task mockTask = new Task();
        mockTask.setId(1L);
        mockTask.setUserId(1L);
        mockTask.setTitle("日常咨询");
        mockTask.setType("consultation");
        mockTask.setStatus(0);

        when(taskService.createTask(any(Task.class))).thenReturn(mockTask);

        Result<TaskResponseDTO> result = emergencyController.buttonHelp("green", 1L, "device_001");

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testButtonHelpYellow() {
        Task mockTask = new Task();
        mockTask.setId(1L);
        mockTask.setUserId(1L);
        mockTask.setTitle("生活求助");
        mockTask.setType("life_service");
        mockTask.setStatus(0);

        when(taskService.createTask(any(Task.class))).thenReturn(mockTask);

        Result<TaskResponseDTO> result = emergencyController.buttonHelp("yellow", 1L, "device_001");

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }
}
