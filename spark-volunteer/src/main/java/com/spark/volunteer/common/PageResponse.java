/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一分页响应格式
 * 将 Spring Data Page 转换为前端友好的 DTO
 *
 * @param <T> 数据项类型
 */
@Data
public class PageResponse<T> {

    /** 当前页数据 */
    private List<T> content;

    /** 当前页码（0-based） */
    private int page;

    /** 每页大小 */
    private int size;

    /** 总记录数 */
    private long totalElements;

    /** 总页数 */
    private int totalPages;

    /** 是否为第一页 */
    private boolean first;

    /** 是否为最后一页 */
    private boolean last;

    /**
     * 从 Spring Data Page 构建 PageResponse（不做类型转换）
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }

    /**
     * 从 Spring Data Page 构建 PageResponse（做类型转换）
     */
    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> converter) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent().stream().map(converter).collect(Collectors.toList()));
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }

    /**
     * 从 List 手动构建分页响应（用于内存分页场景）
     */
    public static <T> PageResponse<T> fromList(List<T> allItems, int page, int size) {
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, allItems.size());

        PageResponse<T> response = new PageResponse<>();
        if (fromIndex >= allItems.size()) {
            response.setContent(java.util.Collections.emptyList());
        } else {
            response.setContent(allItems.subList(fromIndex, toIndex));
        }
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(allItems.size());
        response.setTotalPages((int) Math.ceil((double) allItems.size() / size));
        response.setFirst(page == 0);
        response.setLast(fromIndex + size >= allItems.size());
        return response;
    }
}
