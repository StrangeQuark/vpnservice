package com.strangequark.vpnservice.device;

import java.time.LocalDateTime;
import java.util.UUID;

public class VpnDeviceResponse {
    private UUID id;
    private String deviceName;
    private String vpnAddress;
    private LocalDateTime createdAt;

    public VpnDeviceResponse(VpnDevice vpnDevice) {
        id = vpnDevice.getId();
        deviceName = vpnDevice.getDeviceName();
        vpnAddress = vpnDevice.getVpnAddress();
        createdAt = vpnDevice.getCreatedAt();
    }

    public UUID getId() { return id; }
    public String getDeviceName() { return deviceName; }
    public String getVpnAddress() { return vpnAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
