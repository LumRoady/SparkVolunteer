package com.spark.volunteer.repository;

import com.spark.volunteer.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备仓库接口
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    // 根据设备ID查询设备
    Device findByDeviceId(String deviceId);

    // 根据用户ID查询设备
    List<Device> findByUserId(Long userId);
    org.springframework.data.domain.Page<Device> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    // 根据设备ID和状态查询设备
    Device findByDeviceIdAndStatus(String deviceId, String status);
}
