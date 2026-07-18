/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.service;

import java.util.Map;

/**
 * 数据大屏服务接口
 * 提供实时统计数据，用于大屏展示
 */
public interface DashboardService {

    /**
     * 获取数据大屏统计信息
     * @return 包含各项统计指标的数据Map
     */
    Map<String, Object> getStats();
}
