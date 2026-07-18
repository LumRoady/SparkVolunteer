package com.spark.volunteer.exception;

/**
 * 资源未找到异常
 * 用于处理请求的资源不存在的情况
 */
public class NotFoundException extends BusinessException {

    /**
     * 构造函数
     * @param message 异常信息
     */
    public NotFoundException(String message) {
        super(404, message);
    }

    /**
     * 构造函数 - 指定资源类型
     * @param resourceType 资源类型（如"用户"、"任务"）
     * @param id 资源ID
     */
    public NotFoundException(String resourceType, Object id) {
        super(404, resourceType + "不存在，ID: " + id);
    }
}
