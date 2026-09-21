package com.strangequark.vpnservice.device;

import java.util.UUID;

public class VpnConfigurationResponse extends VpnDeviceResponse {
    private final String configuration;
    private final String qrCode;

    public VpnConfigurationResponse(VpnDevice vpnDevice, String configuration, String qrCode) {
        super(vpnDevice);
        this.configuration = configuration;
        this.qrCode = qrCode;
    }

    public String getConfiguration() { return configuration; }
    public String getQrCode() { return qrCode; }
}
