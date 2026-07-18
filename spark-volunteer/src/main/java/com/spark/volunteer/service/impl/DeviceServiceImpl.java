package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.Device;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.repository.DeviceRepository;
import com.spark.volunteer.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 设备服务实现类
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    @CachePut(value = "device", key = "#result.id")
    public Device createDevice(Device device) {
        // 检查设备ID是否已存在
        Device existingDevice = deviceRepository.findByDeviceId(device.getDeviceId());
        if (existingDevice != null) {
            throw new BusinessException("设备ID已存在");
        }
        return deviceRepository.save(device);
    }

    @Override
    @Cacheable(value = "device", key = "#id", unless = "#result == null")
    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备", id));
    }

    @Override
    @Cacheable(value = "device", key = "'deviceId:' + #deviceId", unless = "#result == null")
    public Device getDeviceByDeviceId(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new NotFoundException("设备", deviceId);
        }
        return device;
    }

    @Override
    public Page<Device> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable);
    }

    @Override
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Override
    public Page<Device> getDevicesByUserId(Long userId, Pageable pageable) {
        return deviceRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<Device> getDevicesByUserId(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    @Override
    @CacheEvict(value = "device", key = "#id")
    public Device updateDevice(Long id, Device device) {
        Device existingDevice = getDeviceById(id);

        if (device.getName() != null) {
            existingDevice.setName(device.getName());
        }
        if (device.getDeviceType() != null) {
            existingDevice.setDeviceType(device.getDeviceType());
        }
        if (device.getUserId() != null) {
            existingDevice.setUserId(device.getUserId());
        }
        if (device.getStatus() != null) {
            existingDevice.setStatus(device.getStatus());
        }

        return deviceRepository.save(existingDevice);
    }

    @Override
    @CacheEvict(value = "device", key = "#id")
    public void deleteDevice(Long id) {
        Device device = getDeviceById(id);
        deviceRepository.delete(device);
    }

    @Override
    @CacheEvict(value = "device", key = "'deviceId:' + #deviceId")
    public Device bindDeviceToUser(String deviceId, Long userId) {
        Device device = getDeviceByDeviceId(deviceId);
        device.setUserId(userId);
        device.setStatus("ACTIVE");
        return deviceRepository.save(device);
    }

    @Override
    @CacheEvict(value = "device", key = "'deviceId:' + #deviceId")
    public Device unbindDeviceFromUser(String deviceId) {
        Device device = getDeviceByDeviceId(deviceId);
        device.setUserId(null);
        device.setStatus("INACTIVE");
        return deviceRepository.save(device);
    }

    @Override
    @CacheEvict(value = "device", key = "'deviceId:' + #deviceId")
    public Device activateDevice(String deviceId) {
        Device device = getDeviceByDeviceId(deviceId);
        device.setStatus("ACTIVE");
        return deviceRepository.save(device);
    }

    @Override
    @CacheEvict(value = "device", key = "'deviceId:' + #deviceId")
    public Device deactivateDevice(String deviceId) {
        Device device = getDeviceByDeviceId(deviceId);
        device.setStatus("INACTIVE");
        return deviceRepository.save(device);
    }

    @Override
    public Device updateDeviceOnlineStatus(String deviceId) {
        Device device = getDeviceByDeviceId(deviceId);
        device.setLastOnlineTime(new Date());
        return deviceRepository.save(device);
    }
}
