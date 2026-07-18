package com.spark.volunteer.exception;

/**
 * 未授权异常
 * 用于处理用户未登录或权限不足的情况
 */
public class UnauthorizedException extends BusinessException {

    /**
     * 构造函数
     * @param message 异常信息
     */
    public UnauthorizedException(String message) {
        super(401, message);
    }
}
