package com.strangequark.vpnservice.vpn;

import com.strangequark.vpnservice.device.*;
import com.strangequark.vpnservice.utility.JwtUtility;
import com.strangequark.vpnservice.utility.QrCodeUtility;
import com.strangequark.vpnservice.utility.WireGuardControlUtility;
import com.strangequark.vpnservice.utility.WireGuardPeer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.*;

@Service
public class VpnService {
    @Value("${authservice.integration}")
    private boolean authserviceIntegration;
    @Value("${vpn.endpoint}")
    private String vpnEndpoint;
    @Value("${vpn.client.allowed-ips}")
    private String vpnClientAllowedIps;
    @Value("${vpn.network.prefix}")
    private String vpnNetworkPrefix;

    private final VpnDeviceRepository vpnDeviceRepository;
    @Autowired
    JwtUtility jwtUtility;
    @Autowired
    WireGuardControlUtility wireGuardControlUtility;
    @Autowired
    QrCodeUtility qrCodeUtility;

    public VpnService(VpnDeviceRepository vpnDeviceRepository) {
        this.vpnDeviceRepository = vpnDeviceRepository;
    }

    public ResponseEntity<?> createDevice(VpnDeviceRequest vpnDeviceRequest) {
        try {
            if(vpnDeviceRequest.getDeviceName() == null || vpnDeviceRequest.getDeviceName().isBlank())
                throw new RuntimeException("Device name is required");

            String vpnAddress = getAvailableVpnAddress();
            WireGuardPeer wireGuardPeer = wireGuardControlUtility.createPeer(vpnAddress);
            VpnDevice vpnDevice = vpnDeviceRepository.save(new VpnDevice(
                    getRequestingUserId(), vpnDeviceRequest.getDeviceName(), wireGuardPeer.getPublicKey(), vpnAddress));
            String configuration = getConfiguration(wireGuardPeer, vpnAddress);
            return ResponseEntity.ok(new VpnConfigurationResponse(vpnDevice, configuration,
                    qrCodeUtility.createQrCode(configuration)));
        } catch(RestClientException ex) {
            return ResponseEntity.status(502).body("WireGuard control service is unavailable");
        } catch(Exception ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }

    public ResponseEntity<?> getDevices() {
        if(authserviceIntegration)
            return ResponseEntity.ok(getDeviceResponses(vpnDeviceRepository.findAllByUserId(getRequestingUserId())));
        return ResponseEntity.ok(getDeviceResponses(vpnDeviceRepository.findAllByUserIdIsNull()));
    }

    public ResponseEntity<?> revokeDevice(VpnDeviceRequest vpnDeviceRequest) {
        try {
            VpnDevice vpnDevice = getVpnDevice(vpnDeviceRequest.getDeviceId());
            validateDeviceAccess(vpnDevice);
            removeVpnDevice(vpnDevice);
            return ResponseEntity.ok("VPN device revoked");
        } catch(RestClientException ex) {
            return ResponseEntity.status(502).body("WireGuard control service is unavailable");
        } catch(Exception ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }

    public ResponseEntity<?> revokeAdminDevice(VpnDeviceRequest vpnDeviceRequest) {
        try {
            removeVpnDevice(getVpnDevice(vpnDeviceRequest.getDeviceId()));
            return ResponseEntity.ok("VPN device revoked");
        } catch(RestClientException ex) {
            return ResponseEntity.status(502).body("WireGuard control service is unavailable");
        } catch(Exception ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }

    public ResponseEntity<?> rotateDevice(VpnDeviceRequest vpnDeviceRequest) {
        try {
            VpnDevice vpnDevice = getVpnDevice(vpnDeviceRequest.getDeviceId());
            validateDeviceAccess(vpnDevice);
            wireGuardControlUtility.removePeer(vpnDevice.getPublicKey(), vpnDevice.getVpnAddress());
            WireGuardPeer wireGuardPeer = wireGuardControlUtility.createPeer(vpnDevice.getVpnAddress());
            vpnDevice.setPublicKey(wireGuardPeer.getPublicKey());
            vpnDevice = vpnDeviceRepository.save(vpnDevice);
            String configuration = getConfiguration(wireGuardPeer, vpnDevice.getVpnAddress());
            return ResponseEntity.ok(new VpnConfigurationResponse(vpnDevice, configuration,
                    qrCodeUtility.createQrCode(configuration)));
        } catch(RestClientException ex) {
            return ResponseEntity.status(502).body("WireGuard control service is unavailable");
        } catch(Exception ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }

    public ResponseEntity<?> getAllDevices() {
        return ResponseEntity.ok(getDeviceResponses(vpnDeviceRepository.findAll()));
    }

    public ResponseEntity<?> revokeUserDevices(VpnDeviceRequest vpnDeviceRequest) {
        try {
            if(vpnDeviceRequest.getUserId() == null)
                throw new RuntimeException("User ID is required");
            for(VpnDevice vpnDevice : vpnDeviceRepository.findAllByUserId(vpnDeviceRequest.getUserId()))
                removeVpnDevice(vpnDevice);
            return ResponseEntity.ok("User VPN devices revoked");
        } catch(RestClientException ex) {
            return ResponseEntity.status(502).body("WireGuard control service is unavailable");
        } catch(Exception ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }

    private UUID getRequestingUserId() {
        if(authserviceIntegration)
            return UUID.fromString(jwtUtility.extractId());
        return null;
    }

    private VpnDevice getVpnDevice(UUID deviceId) {
        if(deviceId == null)
            throw new RuntimeException("Device ID is required");
        return vpnDeviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("VPN device not found"));
    }

    private void validateDeviceAccess(VpnDevice vpnDevice) {
        if(authserviceIntegration && !vpnDevice.getUserId().equals(getRequestingUserId()))
            throw new RuntimeException("VPN device does not belong to requesting user");
    }

    private void removeVpnDevice(VpnDevice vpnDevice) {
        wireGuardControlUtility.removePeer(vpnDevice.getPublicKey(), vpnDevice.getVpnAddress());
        vpnDeviceRepository.delete(vpnDevice);
    }

    private String getAvailableVpnAddress() {
        Set<String> usedAddresses = new HashSet<>();
        for(VpnDevice vpnDevice : vpnDeviceRepository.findAll())
            usedAddresses.add(vpnDevice.getVpnAddress());
        for(int i = 2; i < 255; i++) {
            String vpnAddress = vpnNetworkPrefix + "." + i;
            if(!usedAddresses.contains(vpnAddress))
                return vpnAddress;
        }
        throw new RuntimeException("No VPN addresses are available");
    }

    private List<VpnDeviceResponse> getDeviceResponses(List<VpnDevice> vpnDevices) {
        List<VpnDeviceResponse> responses = new ArrayList<>();
        for(VpnDevice vpnDevice : vpnDevices)
            responses.add(new VpnDeviceResponse(vpnDevice));
        return responses;
    }

    private String getConfiguration(WireGuardPeer wireGuardPeer, String vpnAddress) {
        return "[Interface]\n" +
                "PrivateKey = " + wireGuardPeer.getPrivateKey() + "\n" +
                "Address = " + vpnAddress + "/32\n" +
                "DNS = " + vpnNetworkPrefix + ".1\n\n" +
                "[Peer]\n" +
                "PublicKey = " + wireGuardPeer.getServerPublicKey() + "\n" +
                "Endpoint = " + vpnEndpoint + "\n" +
                "AllowedIPs = " + getVpnClientAllowedIps() + "\n" +
                "PersistentKeepalive = 25";
    }

    private String getVpnClientAllowedIps() {
        if(vpnClientAllowedIps.isBlank())
            return vpnNetworkPrefix + ".0/24";
        return vpnClientAllowedIps;
    }
}
