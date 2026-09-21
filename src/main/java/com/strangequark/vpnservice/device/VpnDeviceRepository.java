package com.strangequark.vpnservice.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VpnDeviceRepository extends JpaRepository<VpnDevice, UUID> {
    List<VpnDevice> findAllByUserId(UUID userId);
    List<VpnDevice> findAllByUserIdIsNull();
}
