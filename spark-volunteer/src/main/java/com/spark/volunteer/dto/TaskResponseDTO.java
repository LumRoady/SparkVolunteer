package com.spark.volunteer.dto;

import com.spark.volunteer.entity.Task;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * 任务响应DTO类
 * 用于前后端数据传输，解决字段名称不匹配问题
 */
@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String content; // 任务详情
    private String type;    // 任务类型（emergency/life_service/consultation/normal）
    private String status;  // 当前状态（PENDING/ACCEPTED/COMPLETED）
    private String location; // 位置信息
    private Date createTime; // 创建时间
    private String contactPhone; // 联系电话（用于弹窗显示）
    private Long requesterId; // 请求者ID（用于加载历史记录）
    private Long receiverId; // 志愿者ID（接单者）
    
    /**
     * 从Task实体转换为DTO
     */
    public static TaskResponseDTO fromTask(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setContent(task.getContent());
        dto.setType(task.getType());
        dto.setStatus(mapStatusToString(task.getStatus()));
        dto.setLocation(task.getLocation());
        dto.setCreateTime(task.getCreateTime());
        dto.setRequesterId(task.getRequesterId());
        dto.setReceiverId(task.getReceiverId());
        dto.setContactPhone("暂无");
        
        return dto;
    }
    
    /**
     * 将数字状态转换为字符串
     */
    private static String mapStatusToString(Integer status) {
        switch (status) {
            case 0:
                return "PENDING";
            case 1:
                return "ACCEPTED";
            case 2:
                return "COMPLETED";
            case 3:
                return "CANCELLED";
            default:
                return "UNKNOWN";
        }
    }
}