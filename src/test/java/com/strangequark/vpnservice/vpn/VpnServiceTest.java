package com.strangequark.vpnservice.vpn;

import com.strangequark.vpnservice.device.*;
import com.strangequark.vpnservice.utility.JwtUtility;
import com.strangequark.vpnservice.utility.QrCodeUtility;
import com.strangequark.vpnservice.utility.WireGuardControlUtility;
import com.strangequark.vpnservice.utility.WireGuardPeer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VpnServiceTest {
    @Mock
    private VpnDeviceRepository vpnDeviceRepository;
    @Mock
    private JwtUtility jwtUtility;
    @Mock
    private WireGuardControlUtility wireGuardControlUtility;
    @Mock
    private QrCodeUtility qrCodeUtility;
    private VpnService vpnService;

    @BeforeEach
    public void setup() {
        vpnService = new VpnService(vpnDeviceRepository);
        ReflectionTestUtils.setField(vpnService, "jwtUtility", jwtUtility);
        ReflectionTestUtils.setField(vpnService, "wireGuardControlUtility", wireGuardControlUtility);
        ReflectionTestUtils.setField(vpnService, "qrCodeUtility", qrCodeUtility);
        ReflectionTestUtils.setField(vpnService, "authserviceIntegration", false);
        ReflectionTestUtils.setField(vpnService, "vpnEndpoint", "vpn.example.com:51820");
        ReflectionTestUtils.setField(vpnService, "vpnClientDns", "1.1.1.1");
        ReflectionTestUtils.setField(vpnService, "vpnClientAllowedIps", "10.8.0.0/24");
        ReflectionTestUtils.setField(vpnService, "vpnAddressPrefix", "10.8.0");
    }

    @Test
    public void createDeviceReturnsOneTimeConfiguration() {
        VpnDeviceRequest request = new VpnDeviceRequest();
        request.setDeviceName("laptop");
        WireGuardPeer peer = new WireGuardPeer();
        peer.setPrivateKey("private-key");
        peer.setPublicKey("public-key");
        peer.setServerPublicKey("server-key");
        when(vpnDeviceRepository.findAll()).thenReturn(List.of());
        when(wireGuardControlUtility.createPeer("10.8.0.2")).thenReturn(peer);
        when(qrCodeUtility.createQrCode(anyString())).thenReturn("data:image/png;base64,test-qr-code");
        when(vpnDeviceRepository.save(any(VpnDevice.class))).thenAnswer(invocation -> {
            VpnDevice vpnDevice = invocation.getArgument(0);
            vpnDevice.setId(UUID.randomUUID());
            return vpnDevice;
        });

        ResponseEntity<?> response = vpnService.createDevice(request);

        assertEquals(200, response.getStatusCode().value());
        VpnConfigurationResponse body = (VpnConfigurationResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.getConfiguration().contains("PrivateKey = private-key"));
        assertTrue(body.getConfiguration().contains("Address = 10.8.0.2/32"));
        assertTrue(body.getConfiguration().contains("[Interface]\nPrivateKey"));
        assertFalse(body.getConfiguration().contains("\\n"));
        assertEquals("data:image/png;base64,test-qr-code", body.getQrCode());
        verify(wireGuardControlUtility).createPeer("10.8.0.2");
    }

    @Test
    public void revokeDeviceRemovesWireGuardPeerAndDevice() {
        UUID deviceId = UUID.randomUUID();
        VpnDevice vpnDevice = new VpnDevice(null, "laptop", "public-key", "10.8.0.2");
        vpnDevice.setId(deviceId);
        VpnDeviceRequest request = new VpnDeviceRequest();
        request.setDeviceId(deviceId);
        when(vpnDeviceRepository.findById(deviceId)).thenReturn(java.util.Optional.of(vpnDevice));

        ResponseEntity<?> response = vpnService.revokeDevice(request);

        assertEquals(200, response.getStatusCode().value());
        verify(wireGuardControlUtility).removePeer("public-key", "10.8.0.2");
        verify(vpnDeviceRepository).delete(vpnDevice);
    }
}
