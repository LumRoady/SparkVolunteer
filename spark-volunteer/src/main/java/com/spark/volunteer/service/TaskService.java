package com.spark.volunteer.service;

import com.spark.volunteer.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TaskService {

    // 创建任务
    Task createTask(Task task);
    
    // 根据设备创建任务（硬件上报）
    Task createTaskByDevice(String deviceId, Integer button);

    // 获取任务列表（支持按状态、类型筛选）
    List<Task> getTasks(Integer status, String type);
    
    // 分页获取任务列表
    Page<Task> getTasksPage(Integer status, String type, Pageable pageable);

    // 根据ID获取任务
    Task getTaskById(Long id);

    // 更新任务
    Task updateTask(Long id, Task task);
    
    // 更新任务状态
    Task updateTaskStatus(Long id, Integer status, Long volunteerId);

    // 删除任务
    void deleteTask(Long id);

    // 志愿者接单
    Task acceptTask(Long taskId, Long volunteerId);

    // 完成任务
    Task completeTask(Long taskId);

    // 取消任务
    Task cancelTask(Long taskId);

    // 获取我的任务
    List<Task> getMyTasks(String role, Long userId);
    
    // 根据用户ID分页获取任务列表
    Page<Task> getTasksPageByUserId(Long userId, Pageable pageable);
    
    // 根据task_no获取任务
    Task getTaskByTaskNo(Integer taskNo);
    
    // 根据task_no获取任务详情
    Task getTaskDetail(Integer taskNo);
    
    // 根据任务ID列表获取任务
    List<Task> getTasksByIds(List<Long> taskIds);
}