package com.strangequark.vpnservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Value("${JWT_PUBLIC_KEY}")
    private String JWT_PUBLIC_KEY;
    @Value("${JWT_ISSUER}")
    private String JWT_ISSUER;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws jakarta.servlet.ServletException, java.io.IOException {
        String token = getToken(request);
        if(token != null) {
            try {
                Claims claims = getClaims(token);
                List<SimpleGrantedAuthority> authorizations = new ArrayList<>();
                List<String> authorizationNames = claims.get("authorizations", List.class);
                if(authorizationNames != null)
                    for(String authorizationName : authorizationNames)
                        authorizations.add(new SimpleGrantedAuthority(authorizationName));
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        claims.get("principalId", String.class), null, authorizations));
            } catch(Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private Claims getClaims(String token) throws Exception {
        Key key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Decoders.BASE64.decode(JWT_PUBLIC_KEY)));
        Claims claims = Jwts.parserBuilder().setSigningKey(key).requireIssuer(JWT_ISSUER).build().parseClaimsJws(token).getBody();
        if(!"ACCESS".equals(claims.get("tokenType", String.class)))
            throw new RuntimeException("JWT token type is invalid");
        return claims;
    }

    private String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7);
        if(request.getCookies() != null)
            for(Cookie cookie : request.getCookies())
                if(cookie.getName().equals("access_token") && !cookie.getValue().isBlank())
                    return cookie.getValue();
        return null;
    }
}
