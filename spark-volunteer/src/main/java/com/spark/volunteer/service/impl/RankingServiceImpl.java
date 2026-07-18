package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Map<String, Object>> getRanking(String type) {
        List<User> volunteers = userRepository.findByRole("VOLUNTEER");
        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = 0; i < volunteers.size(); i++) {
            User u = volunteers.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", i + 1);
            item.put("userId", u.getId());
            item.put("nickname", u.getNickname() != null ? u.getNickname() : u.getName());
            item.put("avatar", u.getAvatar());
            item.put("community", u.getCommunity());
            item.put("completedTasks", u.getCompletedTasks() != null ? u.getCompletedTasks() : 0);
            item.put("points", u.getPoints() != null ? u.getPoints() : 0);
            item.put("level", getLevel(u.getPoints()));
            list.add(item);
        }

        switch (type) {
            case "accept":
                list.sort((a, b) -> Integer.compare(
                    (int) b.get("completedTasks"), (int) a.get("completedTasks")));
                break;
            case "points":
            case "rating":
            default:
                list.sort((a, b) -> Integer.compare(
                    (int) b.get("points"), (int) a.get("points")));
                break;
        }

        // 重新分配排名
        for (int i = 0; i < list.size(); i++) {
            list.get(i).put("rank", i + 1);
        }

        // 返回前20名
        return list.stream().limit(20).collect(Collectors.toList());
    }

    private int getLevel(Integer points) {
        int p = points != null ? points : 0;
        if (p >= 1000) return 4;
        if (p >= 300) return 3;
        if (p >= 100) return 2;
        return 1;
    }
}
