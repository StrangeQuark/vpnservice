package com.strangequark.vpnservice.device;

import java.util.UUID;

public class VpnDeviceRequest {
    private UUID deviceId;
    private String deviceName;
    private UUID userId;

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
