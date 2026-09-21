package com.strangequark.vpnservice.device;

import com.strangequark.vpnservice.utility.LocalDateTimeEncryptDecryptConverter;
import com.strangequark.vpnservice.utility.StringEncryptDecryptConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vpn_devices")
public class VpnDevice {
    public VpnDevice() {
    }

    public VpnDevice(UUID userId, String deviceName, String publicKey, String vpnAddress) {
        this.userId = userId;
        this.deviceName = deviceName;
        this.publicKey = publicKey;
        this.vpnAddress = vpnAddress;
    }

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    @Column(nullable = false)
    @Convert(converter = StringEncryptDecryptConverter.class)
    private String deviceName;

    @Column(nullable = false, unique = true)
    @Convert(converter = StringEncryptDecryptConverter.class)
    private String publicKey;

    @Column(nullable = false, unique = true)
    @Convert(converter = StringEncryptDecryptConverter.class)
    private String vpnAddress;

    @Column(nullable = false, updatable = false)
    @Convert(converter = LocalDateTimeEncryptDecryptConverter.class)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getVpnAddress() { return vpnAddress; }
    public void setVpnAddress(String vpnAddress) { this.vpnAddress = vpnAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
