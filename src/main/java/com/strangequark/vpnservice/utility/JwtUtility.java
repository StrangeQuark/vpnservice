package com.strangequark.vpnservice.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

@Service
public class JwtUtility {
    @Value("${JWT_PUBLIC_KEY}")
    private String JWT_PUBLIC_KEY;
    @Value("${JWT_ISSUER}")
    private String JWT_ISSUER;

    public String extractId() {
        Claims claims = getClaims(getToken());
        if(!"USER".equals(claims.get("principalType", String.class)))
            throw new RuntimeException("VPN device actions require a user token");
        return claims.get("principalId", String.class);
    }

    private String getToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes == null)
            throw new RuntimeException("No request context available");
        HttpServletRequest request = attributes.getRequest();
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer "))
            return header.substring(7);
        if(request.getCookies() != null) {
            for(Cookie cookie : request.getCookies())
                if(cookie.getName().equals("access_token") && !cookie.getValue().isBlank())
                    return cookie.getValue();
        }
        throw new RuntimeException("Missing or invalid Authorization header and access_token cookie");
    }

    private Claims getClaims(String token) {
        try {
            Key key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Decoders.BASE64.decode(JWT_PUBLIC_KEY)));
            Claims claims = Jwts.parserBuilder().setSigningKey(key).requireIssuer(JWT_ISSUER).build()
                    .parseClaimsJws(token).getBody();
            if(!"ACCESS".equals(claims.get("tokenType", String.class)))
                throw new RuntimeException("JWT token type is invalid");
            if(claims.getId() == null || claims.get("principalId", String.class) == null)
                throw new RuntimeException("JWT is missing required claims");
            return claims;
        } catch(Exception ex) {
            throw new RuntimeException("Failed to validate JWT", ex);
        }
    }
}
