package com.strangequark.vpnservice.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
public class WireGuardControlUtility {
    @Value("${wireguard.control.url}")
    private String wireGuardControlUrl;
    @Value("${wireguard.control.token}")
    private String wireGuardControlToken;
    @Autowired
    private RestTemplate restTemplate;

    public WireGuardPeer createPeer(String vpnAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-WIREGUARD-CONTROL-TOKEN", wireGuardControlToken);
        String requestBody = "{\"vpnAddress\":\"" + vpnAddress + "\"}";
        headers.setContentLength(requestBody.getBytes(StandardCharsets.UTF_8).length);
        return restTemplate.postForObject(wireGuardControlUrl + "/create-peer",
                new HttpEntity<>(requestBody, headers), WireGuardPeer.class);
    }

    public void removePeer(String publicKey, String vpnAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-WIREGUARD-CONTROL-TOKEN", wireGuardControlToken);
        String requestBody = "{\"publicKey\":\"" + publicKey + "\",\"vpnAddress\":\"" + vpnAddress + "\"}";
        headers.setContentLength(requestBody.getBytes(StandardCharsets.UTF_8).length);
        restTemplate.exchange(wireGuardControlUrl + "/remove-peer", HttpMethod.POST,
                new HttpEntity<>(requestBody, headers), Void.class);
    }
}
