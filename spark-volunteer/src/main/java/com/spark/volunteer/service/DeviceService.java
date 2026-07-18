package com.spark.volunteer.service;

import com.spark.volunteer.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 设备服务接口
 */
public interface DeviceService {

    // 创建设备
    Device createDevice(Device device);

    // 根据ID获取设备
    Device getDeviceById(Long id);

    // 根据设备ID获取设备
    Device getDeviceByDeviceId(String deviceId);

    // 分页获取所有设备
    Page<Device> getAllDevices(Pageable pageable);

    // 获取所有设备
    List<Device> getAllDevices();

    // 分页获取用户的设备
    Page<Device> getDevicesByUserId(Long userId, Pageable pageable);

    // 根据用户ID获取设备
    List<Device> getDevicesByUserId(Long userId);

    // 更新设备
    Device updateDevice(Long id, Device device);

    // 删除设备
    void deleteDevice(Long id);

    // 绑定设备到用户
    Device bindDeviceToUser(String deviceId, Long userId);

    // 解除设备绑定
    Device unbindDeviceFromUser(String deviceId);

    // 激活设备
    Device activateDevice(String deviceId);

    // 停用设备
    Device deactivateDevice(String deviceId);

    // 更新设备在线状态
    Device updateDeviceOnlineStatus(String deviceId);
}
