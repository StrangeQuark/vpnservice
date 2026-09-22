package com.strangequark.vpnservice.device;

import java.time.LocalDateTime;
import java.util.UUID;

public class VpnDeviceResponse {
    private UUID id;
    private UUID userId;
    private String deviceName;
    private String vpnAddress;
    private LocalDateTime createdAt;

    public VpnDeviceResponse(VpnDevice vpnDevice) {
        id = vpnDevice.getId();
        userId = vpnDevice.getUserId();
        deviceName = vpnDevice.getDeviceName();
        vpnAddress = vpnDevice.getVpnAddress();
        createdAt = vpnDevice.getCreatedAt();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getDeviceName() { return deviceName; }
    public String getVpnAddress() { return vpnAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
