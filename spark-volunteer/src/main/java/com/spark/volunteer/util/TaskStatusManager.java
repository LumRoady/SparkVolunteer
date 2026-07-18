package com.spark.volunteer.util;

import com.spark.volunteer.entity.Task;
import java.util.Date;

/**
 * 任务状态管理器
 * 基于状态机模式的任务生命周期管理，是本系统的核心专利技术之一
 * 
 * 本类实现了四态任务状态机（PENDING→ACCEPTED→COMPLETED/CANCELLED），
 * 提供状态转换验证与执行功能，有效防止非法操作和状态异常。
 */
public class TaskStatusManager {

    /**
     * 任务状态：待接单
     * 初始状态，任务已发布但尚未被志愿者承接
     */
    public static final int STATUS_PENDING = 0;
    
    /**
     * 任务状态：已接单
     * 志愿者已承接任务，正在处理中
     */
    public static final int STATUS_ACCEPTED = 1;
    
    /**
     * 任务状态：已完成
     * 志愿者已完成任务，任务结束
     */
    public static final int STATUS_COMPLETED = 2;
    
    /**
     * 任务状态：已取消
     * 任务已被取消，不再继续处理
     */
    public static final int STATUS_CANCELLED = 3;

    /**
     * 验证状态转换的合法性
     */
    public static void validateStatusTransition(Task task, Integer newStatus, Long volunteerId) {
        Integer currentStatus = task.getStatus();

        switch (newStatus) {
            case STATUS_PENDING:
                validateResetToPending(currentStatus);
                break;
            case STATUS_ACCEPTED:
                validateToAccepted(currentStatus, volunteerId);
                break;
            case STATUS_COMPLETED:
                validateToCompleted(currentStatus);
                break;
            case STATUS_CANCELLED:
                validateToCancelled(currentStatus);
                break;
            default:
                throw new IllegalArgumentException("无效的任务状态: " + newStatus);
        }
    }

    /**
     * 验证重置为待接单状态
     */
    private static void validateResetToPending(Integer currentStatus) {
        // 允许从任何状态重置为待接单
        // 但已完成的任务不应该被重置
        if (currentStatus == STATUS_COMPLETED) {
            throw new IllegalStateException("已完成的任务不能重置为待接单状态");
        }
    }

    /**
     * 验证转换为已接单状态
     */
    private static void validateToAccepted(Integer currentStatus, Long volunteerId) {
        if (currentStatus != STATUS_PENDING) {
            throw new IllegalStateException("只有待接单状态的任务才能接单");
        }
        // 志愿者ID验证由调用方负责
    }

    /**
     * 验证转换为已完成状态
     */
    private static void validateToCompleted(Integer currentStatus) {
        if (currentStatus != STATUS_ACCEPTED) {
            throw new IllegalStateException("只有已接单状态的任务才能完成");
        }
    }

    /**
     * 验证转换为已取消状态
     */
    private static void validateToCancelled(Integer currentStatus) {
        // 待接单和已接单的任务都可以取消
        if (currentStatus != STATUS_PENDING && currentStatus != STATUS_ACCEPTED) {
            throw new IllegalStateException("只有待接单或已接单状态的任务才能取消");
        }
    }

    /**
     * 执行状态转换
     */
    public static void applyStatusTransition(Task task, Integer newStatus, Long volunteerId) {
        validateStatusTransition(task, newStatus, volunteerId);

        switch (newStatus) {
            case STATUS_PENDING:
                resetToPending(task);
                break;
            case STATUS_ACCEPTED:
                setToAccepted(task, volunteerId);
                break;
            case STATUS_COMPLETED:
                setToCompleted(task);
                break;
            case STATUS_CANCELLED:
                setToCancelled(task);
                break;
        }

        task.setStatus(newStatus);
    }

    /**
     * 重置为待接单状态
     */
    private static void resetToPending(Task task) {
        task.setReceiverId(null);
        task.setAcceptTime(null);
        task.setFinishTime(null);
    }

    /**
     * 设置为已接单状态
     */
    private static void setToAccepted(Task task, Long volunteerId) {
        task.setReceiverId(volunteerId);
        task.setAcceptTime(new Date());
        task.setFinishTime(null);
    }

    /**
     * 设置为已完成状态
     */
    private static void setToCompleted(Task task) {
        task.setFinishTime(new Date());
    }

    /**
     * 设置为已取消状态
     */
    private static void setToCancelled(Task task) {
        // 取消任务时保留接单信息，但清空完成时间
        task.setFinishTime(null);
    }

    /**
     * 检查任务是否已超时
     */
    public static boolean isTaskTimeout(Task task, long timeoutMinutes) {
        if (task.getStatus() == STATUS_ACCEPTED && task.getAcceptTime() != null) {
            long elapsedMinutes = (new Date().getTime() - task.getAcceptTime().getTime()) / (1000 * 60);
            return elapsedMinutes > timeoutMinutes;
        }
        return false;
    }

    /**
     * 获取状态的中文描述
     */
    public static String getStatusDescription(Integer status) {
        switch (status) {
            case STATUS_PENDING:
                return "待接单";
            case STATUS_ACCEPTED:
                return "已接单";
            case STATUS_COMPLETED:
                return "已完成";
            case STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知状态";
        }
    }
}