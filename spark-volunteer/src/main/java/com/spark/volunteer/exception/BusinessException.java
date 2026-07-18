package com.spark.volunteer.exception;

/**
 * 业务异常基类
 * 用于处理业务逻辑中的异常情况
 */
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 构造函数
     * @param message 异常信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 构造函数
     * @param code 错误码
     * @param message 异常信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数
     * @param message 异常信息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
}
