package com.spark.volunteer.service.impl;

import com.spark.volunteer.common.WebSocketMessage;
import com.spark.volunteer.config.WebSocketMessageBridge;
import com.spark.volunteer.entity.Device;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.repository.DeviceRepository;
import com.spark.volunteer.repository.TaskRepository;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.RedisCacheService;
import com.spark.volunteer.service.TaskService;
import com.spark.volunteer.service.VolunteerGrowthService;
import com.spark.volunteer.util.TaskStatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private VolunteerGrowthService volunteerGrowthService;

    @Autowired
    private WebSocketMessageBridge messageBridge;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CachePut(value = "task", key = "#result.id")
    public Task createTask(Task task) {
        // 验证任务数据
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new BusinessException("任务标题不能为空");
        }
        if (task.getContent() == null || task.getContent().trim().isEmpty()) {
            throw new BusinessException("任务描述不能为空");
        }
        if (task.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 验证用户是否存在
        userRepository.findById(task.getUserId())
                .orElseThrow(() -> new NotFoundException("用户", task.getUserId()));

        // 设置初始状态
        task.setStatus(0);

        Task savedTask = taskRepository.save(task);

        // 写入Redis缓存：待接单任务加入ZSET
        if (savedTask.getId() != null && savedTask.getCreateTime() != null) {
            redisCacheService.addPendingTask(savedTask.getId(), savedTask.getCreateTime().getTime());
            redisCacheService.cacheTask(savedTask, savedTask.getId());
        }

        // 发送WebSocket通知
        messageBridge.broadcast(new WebSocketMessage("TASK_CREATED", savedTask));

        return savedTask;
    }

    @Override
    public Task createTaskByDevice(String deviceId, Integer button) {
        // 根据设备ID查找设备
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new NotFoundException("设备", deviceId);
        }

        // 获取绑定的用户
        User user = userRepository.findById(device.getUserId())
                .orElseThrow(() -> new NotFoundException("设备未绑定用户"));

        // 创建任务
        Task task = new Task();
        task.setUserId(user.getId());
        task.setDeviceId(deviceId);
        task.setStatus(0);
        task.setLocation(user.getAddress());

        // 根据按钮类型设置任务类型和内容
        switch (button) {
            case 1: // SOS紧急求助
                task.setType("sos");
                task.setTitle("紧急求助");
                task.setContent("老人按下SOS紧急求助按钮");
                break;
            case 2: // 生活服务
                task.setType("life_service");
                task.setTitle("生活服务");
                task.setContent("老人需要生活服务帮助");
                break;
            case 3: // 日常咨询
                task.setType("consultation");
                task.setTitle("日常咨询");
                task.setContent("老人需要日常咨询服务");
                break;
            default:
                throw new BusinessException("无效的按钮类型");
        }

        Task savedTask = taskRepository.save(task);

        // 【核心】按社区精准推送：设备→老人→社区→同社区志愿者
        String community = user.getCommunity();
        if (community != null && !community.isEmpty()) {
            // 查找同社区在线志愿者
            java.util.List<User> nearbyVolunteers = userRepository.findByRoleAndCommunity("VOLUNTEER", community);
            java.util.List<Long> volunteerIds = new java.util.ArrayList<>();
            for (User v : nearbyVolunteers) {
                volunteerIds.add(v.getId());
            }
            if (!volunteerIds.isEmpty()) {
                messageBridge.sendToUsers(volunteerIds,
                    new WebSocketMessage("TASK_CREATED", savedTask));
                logger.info("精准推送: 设备{} 老人社区[{}] → {}名志愿者", deviceId, community, volunteerIds.size());
            } else {
                // 同社区无在线志愿者，降级为全量广播
                messageBridge.broadcast(new WebSocketMessage("TASK_CREATED", savedTask));
                logger.info("降级广播: 社区[{}]无在线志愿者，全量推送", community);
            }
        } else {
            // 老人无社区信息，全量广播
            messageBridge.broadcast(new WebSocketMessage("TASK_CREATED", savedTask));
        }

        return savedTask;
    }

    @Override
    public List<Task> getTasks(Integer status, String type) {
        if (status != null && type != null) {
            return taskRepository.findByStatusAndType(status, type);
        } else if (status != null) {
            return taskRepository.findByStatus(status);
        } else if (type != null) {
            return taskRepository.findByType(type);
        } else {
            return taskRepository.findAll();
        }
    }

    @Override
    public Page<Task> getTasksPage(Integer status, String type, Pageable pageable) {
        if (status != null && type != null) {
            return taskRepository.findByStatusAndType(status, type, pageable);
        } else if (status != null) {
            return taskRepository.findByStatus(status, pageable);
        } else if (type != null) {
            return taskRepository.findByType(type, pageable);
        } else {
            return taskRepository.findAll(pageable);
        }
    }

    @Override
    @Cacheable(value = "task", key = "#id", unless = "#result == null")
    public Task getTaskById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("无效的任务ID");
        }
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("任务", id));
    }

    @Override
    public Task updateTask(Long id, Task task) {
        Task existingTask = getTaskById(id);

        // 只更新允许修改的字段
        if (task.getTitle() != null) {
            existingTask.setTitle(task.getTitle());
        }
        if (task.getContent() != null) {
            existingTask.setContent(task.getContent());
        }
        if (task.getType() != null) {
            existingTask.setType(task.getType());
        }
        if (task.getLocation() != null) {
            existingTask.setLocation(task.getLocation());
        }
        if (task.getLatitude() != null) {
            existingTask.setLatitude(task.getLatitude());
        }
        if (task.getLongitude() != null) {
            existingTask.setLongitude(task.getLongitude());
        }

        return taskRepository.save(existingTask);
    }

    @Override
    @CacheEvict(value = "task", key = "#id")
    public Task updateTaskStatus(Long id, Integer status, Long volunteerId) {
        Task task = getTaskById(id);

        // 使用状态管理器处理状态转换
        try {
            // 如果是接单操作，必须验证志愿者ID和志愿者是否存在
            if (status == TaskStatusManager.STATUS_ACCEPTED) {
                if (volunteerId == null) {
                    throw new BusinessException("志愿者ID不能为空");
                }
                userRepository.findById(volunteerId)
                        .orElseThrow(() -> new NotFoundException("志愿者", volunteerId));
            }

            // 执行状态转换
            TaskStatusManager.applyStatusTransition(task, status, volunteerId);

            Task updatedTask = taskRepository.save(task);

            // 发送WebSocket通知
            messageBridge.broadcast(new WebSocketMessage("TASK_STATUS_CHANGED", updatedTask));

            return updatedTask;

        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "task", key = "#id")
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task acceptTask(Long taskId, Long volunteerId) {
        // 验证志愿者ID不能为空
        if (volunteerId == null) {
            throw new BusinessException("志愿者ID不能为空");
        }

        // 验证志愿者是否存在
        userRepository.findById(volunteerId)
                .orElseThrow(() -> new NotFoundException("志愿者", volunteerId));

        // 防止自己接自己的任务
        Task preCheck = getTaskById(taskId);
        if (volunteerId.equals(preCheck.getUserId())) {
            throw new BusinessException("不能接受自己发布的任务");
        }

        // 原子接单：仅当 status=0 时更新，消除竞态条件
        int affected = taskRepository.acceptTaskAtomically(taskId, volunteerId);
        if (affected == 0) {
            throw new BusinessException("任务已被其他人接单或状态已变更");
        }

        // 重新加载更新后的任务并清除缓存
        redisCacheService.evictTaskCache(taskId);
        return getTaskById(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task completeTask(Long taskId) {
        Task task = getTaskById(taskId);

        // 使用状态管理器处理完成操作
        try {
            TaskStatusManager.applyStatusTransition(task, TaskStatusManager.STATUS_COMPLETED, null);
            Task saved = taskRepository.save(task);
            // Redis：任务完成，清理缓存
            redisCacheService.evictTaskCache(taskId);
            // 按任务类型结算积分 + 更新志愿者完成数
            if (saved.getReceiverId() != null) {
                settlePoints(saved);
                // 异步检查志愿者成就
                volunteerGrowthService.checkAchievements(saved.getReceiverId());
            }
            return saved;
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task cancelTask(Long taskId) {
        Task task = getTaskById(taskId);

        // 使用状态管理器处理取消操作
        try {
            TaskStatusManager.applyStatusTransition(task, TaskStatusManager.STATUS_CANCELLED, null);
            Task saved = taskRepository.save(task);
            // Redis：任务取消，清理缓存
            redisCacheService.evictTaskCache(taskId);
            return saved;
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public List<Task> getMyTasks(String role, Long userId) {
        if ("elderly".equalsIgnoreCase(role)) {
            // 老人查看自己发布的任务
            return taskRepository.findByUserId(userId);
        } else if ("volunteer".equalsIgnoreCase(role)) {
            // 志愿者查看自己接的任务
            return taskRepository.findByReceiverId(userId);
        } else {
            throw new BusinessException("无效的角色类型");
        }
    }

    @Override
    public Page<Task> getTasksPageByUserId(Long userId, Pageable pageable) {
        return taskRepository.findByUserId(userId, pageable);
    }

    @Override
    @Cacheable(value = "task", key = "'taskNo:' + #taskNo", unless = "#result == null")
    public Task getTaskByTaskNo(Integer taskNo) {
        if (taskNo == null || taskNo <= 0) {
            throw new BusinessException("无效的任务编号");
        }
        Task task = taskRepository.findByTaskNo(taskNo);
        if (task == null) {
            throw new NotFoundException("任务", taskNo);
        }
        return task;
    }

    @Override
    public Task getTaskDetail(Integer taskNo) {
        return getTaskByTaskNo(taskNo);
    }

    @Override
    public List<Task> getTasksByIds(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return taskRepository.findAllById(taskIds);
    }

    /**
     * 按任务类型结算积分
     * 紧急求助(sos)=50分、生活服务(life_service)=30分、日常咨询(consultation)=20分
     */
    private void settlePoints(Task task) {
        int points;
        switch (task.getType()) {
            case "sos":      points = 50; break;
            case "life_service": points = 30; break;
            case "consultation": points = 20; break;
            default:         points = 10;
        }
        // 原子更新积分和完成数，消除竞态条件
        userRepository.addPointsAtomically(task.getReceiverId(), points);
        userRepository.addCompletedTasksAtomically(task.getReceiverId(), 1);
        logger.info("任务 {} 完成，志愿者 {} 获得 {} 积分",
                task.getId(), task.getReceiverId(), points);
    }
}
