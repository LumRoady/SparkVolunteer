package com.spark.volunteer.util;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public class PageUtil {

    /**
     * 封装分页结果
     */
    public static <T> Map<String, Object> buildPageResult(Page<T> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("data", page.getContent());
        result.put("currentPage", page.getNumber());
        result.put("totalPages", page.getTotalPages());
        result.put("totalElements", page.getTotalElements());
        result.put("size", page.getSize());
        result.put("hasNext", page.hasNext());
        result.put("hasPrevious", page.hasPrevious());
        return result;
    }
}