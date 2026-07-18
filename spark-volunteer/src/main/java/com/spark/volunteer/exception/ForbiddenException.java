package com.spark.volunteer.exception;

/**
 * 禁止访问异常
 * 用于处理用户权限不足的情况
 */
public class ForbiddenException extends BusinessException {

    /**
     * 构造函数
     * @param message 异常信息
     */
    public ForbiddenException(String message) {
        super(403, message);
    }
}
