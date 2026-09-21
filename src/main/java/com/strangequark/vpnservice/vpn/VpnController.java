package com.strangequark.vpnservice.vpn;

import com.strangequark.vpnservice.device.VpnDeviceRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vpn")
public class VpnController {
    private final VpnService vpnService;

    public VpnController(VpnService vpnService) {
        this.vpnService = vpnService;
    }

    @PostMapping("/create-device")
    public ResponseEntity<?> createDevice(@RequestBody VpnDeviceRequest vpnDeviceRequest) { return vpnService.createDevice(vpnDeviceRequest); }
    @GetMapping("/get-devices")
    public ResponseEntity<?> getDevices() { return vpnService.getDevices(); }
    @DeleteMapping("/revoke-device")
    public ResponseEntity<?> revokeDevice(@RequestBody VpnDeviceRequest vpnDeviceRequest) { return vpnService.revokeDevice(vpnDeviceRequest); }
    @PostMapping("/rotate-device")
    public ResponseEntity<?> rotateDevice(@RequestBody VpnDeviceRequest vpnDeviceRequest) { return vpnService.rotateDevice(vpnDeviceRequest); }
    @GetMapping("/get-all-devices")
    public ResponseEntity<?> getAllDevices() { return vpnService.getAllDevices(); }
    @PostMapping("/revoke-user-devices")
    public ResponseEntity<?> revokeUserDevices(@RequestBody VpnDeviceRequest vpnDeviceRequest) { return vpnService.revokeUserDevices(vpnDeviceRequest); }
}
