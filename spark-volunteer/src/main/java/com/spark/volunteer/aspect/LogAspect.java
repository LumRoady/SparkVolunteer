/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 日志切面类
 * 用于记录Controller层方法的执行情况
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 环绕通知：监控Controller层所有方法的执行
     */
    @Around("execution(* com.spark.volunteer.controller..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        logger.info("开始执行 {}.{}", className, methodName);

        Object result;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            logger.info("{}.{} 执行完成，耗时: {} ms", className, methodName, (endTime - startTime));
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("{}.{} 执行出错，耗时: {} ms, 错误信息：{}", className, methodName, (endTime - startTime), e.getMessage());
            throw e;
        }
    }
}