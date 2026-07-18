/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.common;

import lombok.Data;
import org.slf4j.MDC;

/**
 * 统一API响应格式
 */
@Data
public class Result<T> {
    private Integer code;    // 状态码
    private String message;  // 消息
    private T data;          // 数据
    private Long timestamp;  // 时间戳
    private String traceId;  // 链路追踪ID（从 MDC 获取）

    // 成功，无数据
    public static <T> Result<T> success() {
        return success(null);
    }

    // 成功，有数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    // 失败
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    // 默认失败
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}