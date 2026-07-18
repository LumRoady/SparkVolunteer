/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.dto.MapTaskResponseDTO;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 地图控制器
 * 处理地图相关的HTTP请求（地址解析、附近任务等）
 */
@RestController
@RequestMapping("/api/map")
public class MapController {

    private static final Logger logger = LoggerFactory.getLogger(MapController.class);

    @Autowired
    private TaskService taskService;

    @Value("${map.api.key:}")
    private String mapApiKey;

    @Value("${map.api.provider:amap}")
    private String mapProvider;

    /**
     * 地址解析接口：将地址转换为坐标
     * GET /api/map/geocode?address=北京市朝阳区
     */
    @GetMapping("/geocode")
    public Result<Map<String, Object>> geocode(@RequestParam String address) {
        Map<String, Object> result = new HashMap<>();
        result.put("address", address);

        // 尝试调用真实地图 API，未配置时回退桩数据
        Map<String, Double> location = resolveGeocode(address);
        result.put("location", location);
        result.put("status", "OK");
        return Result.success(result);
    }

    /**
     * 坐标解析接口：将坐标转换为地址
     * GET /api/map/reverse-geocode?latitude=39.9042&longitude=116.4074
     */
    @GetMapping("/reverse-geocode")
    public Result<Map<String, Object>> reverseGeocode(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Double> location = new HashMap<>();
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        result.put("location", location);

        // 尝试调用真实地图 API，未配置时回退桩数据
        String resolvedAddress = resolveReverseGeocode(latitude, longitude);
        result.put("address", resolvedAddress);
        result.put("status", "OK");
        return Result.success(result);
    }

    /**
     * 附近任务接口：根据用户位置获取附近的任务
     * GET /api/map/nearby-tasks?latitude=39.9042&longitude=116.4074&radius=1000
     */
    @GetMapping("/nearby-tasks")
    public Result<List<MapTaskResponseDTO>> getNearbyTasks(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "1000") int radius) {
        List<Task> pendingTasks = taskService.getTasks(0, null);

        // 筛选附近任务
        List<MapTaskResponseDTO> nearbyTasks = new ArrayList<>();
        for (Task task : pendingTasks) {
            if (task.getLatitude() != null && task.getLongitude() != null) {
                double distance = Math.sqrt(
                        Math.pow(task.getLatitude() - latitude, 2) +
                        Math.pow(task.getLongitude() - longitude, 2)
                ) * 111000;
                if (distance <= radius) {
                    nearbyTasks.add(MapTaskResponseDTO.fromTask(task));
                }
            }
        }

        return Result.success(nearbyTasks);
    }

    // ==================== 内部方法：真实 API 调用 + 回退 ====================

    /**
     * 地址解析（地址 → 坐标）
     * 未配置 API Key 时回退为北京默认坐标
     */
    private Map<String, Double> resolveGeocode(String address) {
        Map<String, Double> location = new HashMap<>();
        if (mapApiKey != null && !mapApiKey.isEmpty()) {
            try {
                RestTemplate rt = new RestTemplate();
                String url;
                if ("baidu".equalsIgnoreCase(mapProvider)) {
                    url = String.format("https://api.map.baidu.com/geocoding/v3/?address=%s&output=json&ak=%s", address, mapApiKey);
                } else {
                    url = String.format("https://restapi.amap.com/v3/geocode/geo?address=%s&output=json&key=%s", address, mapApiKey);
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> resp = rt.getForObject(url, Map.class);
                if (resp != null) {
                    // 高德地图解析
                    Object geocodes = resp.get("geocodes");
                    if (geocodes instanceof List && !((List<?>) geocodes).isEmpty()) {
                        Object loc = ((List<Map<String, Object>>) geocodes).get(0).get("location");
                        if (loc instanceof String) {
                            String[] parts = ((String) loc).split(",");
                            location.put("longitude", Double.parseDouble(parts[0]));
                            location.put("latitude", Double.parseDouble(parts[1]));
                            return location;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("地图 API 调用失败，使用回退坐标: {}", e.getMessage());
            }
        }
        // 回退：北京默认坐标
        location.put("latitude", 39.9042);
        location.put("longitude", 116.4074);
        return location;
    }

    /**
     * 逆地址解析（坐标 → 地址）
     * 未配置 API Key 时回退为“未知地址”
     */
    private String resolveReverseGeocode(double latitude, double longitude) {
        if (mapApiKey != null && !mapApiKey.isEmpty()) {
            try {
                RestTemplate rt = new RestTemplate();
                String url;
                if ("baidu".equalsIgnoreCase(mapProvider)) {
                    url = String.format("https://api.map.baidu.com/reverse_geocoding/v3/?location=%s,%s&output=json&ak=%s", latitude, longitude, mapApiKey);
                } else {
                    url = String.format("https://restapi.amap.com/v3/geocode/regeo?location=%s,%s&output=json&key=%s", longitude, latitude, mapApiKey);
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> resp = rt.getForObject(url, Map.class);
                if (resp != null) {
                    Object regeocode = resp.get("regeocode");
                    if (regeocode instanceof Map) {
                        Object formatted = ((Map<String, Object>) regeocode).get("formatted_address");
                        if (formatted instanceof String) {
                            return (String) formatted;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("逆地址解析失败，使用回退: {}", e.getMessage());
            }
        }
        return "未知地址";
    }

    /**
     * 计算两点间距离（米），基于简化平面近似
     */
    public static double calculateDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lng1 - lng2, 2)) * 111000;
    }
}
