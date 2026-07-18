package com.spark.volunteer.dto;

import lombok.Data;

/**
 * 任务视图对象
 * 用于对外展示，使用task_no作为ID
 */
@Data
public class TaskVO {
    private Integer id;          // 对外展示的序号（1,2,3...）
    private String taskCode;     // 原始ID（如"3DEV001"）
    private String title;        // 任务标题
    private String location;     // 位置信息
    private Integer status;      // 任务状态
    private String content;      // 任务详情
    private String type;         // 任务类型
}
