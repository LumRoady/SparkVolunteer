package com.spark.volunteer.service;

import java.util.List;
import java.util.Map;

/**
 * 排行榜服务接口
 */
public interface RankingService {

    /**
     * 获取排行榜
     * @param type 排行类型: accept(接单榜), points(积分榜), rating(好评榜)
     * @return Top20 排行数据
     */
    List<Map<String, Object>> getRanking(String type);
}
