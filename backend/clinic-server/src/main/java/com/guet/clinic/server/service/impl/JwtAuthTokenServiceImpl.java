package com.guet.clinic.server.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.server.service.AuthTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtAuthTokenServiceImpl implements AuthTokenService {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    @Value("${clinic.auth.jwt-secret}")
    private String jwtSecret;

    @Value("${clinic.auth.token-expire-hours:12}")
    private long tokenExpireHours;

    @Value("${clinic.auth.issuer:clinic-server}")
    private String issuer;

    public JwtAuthTokenServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String createToken(UserAccount userAccount) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + tokenExpireHours * 3600;

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", issuer);
        claims.put("sub", userAccount.getUsername());
        claims.put("iat", issuedAt);
        claims.put("exp", expiresAt);
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("userId", userAccount.getId());
        claims.put("username", userAccount.getUsername());
        claims.put("name", userAccount.getName());
        claims.put("phone", userAccount.getPhone());
        claims.put("roleName", userAccount.getRoleName());
        claims.put("permissions", userAccount.getPermissions());
        claims.put("enabled", userAccount.getEnabled());

        String content = encodeJson(Map.of("alg", "HS256", "typ", "JWT")) + "." + encodeJson(claims);
        return content + "." + URL_ENCODER.encodeToString(sign(content));
    }

    @Override
    public boolean validateToken(String token) {
        return parseToken(token, true) != null;
    }

    @Override
    public UserAccount getUser(String token) {
        ParsedToken parsedToken = parseToken(token, true);
        if (parsedToken == null) {
            return null;
        }

        Map<String, Object> claims = parsedToken.claims();
        UserAccount user = new UserAccount();
        user.setId(longValue(claims.get("userId")));
        user.setUsername(stringValue(claims.get("username")));
        user.setName(stringValue(claims.get("name")));
        user.setPhone(stringValue(claims.get("phone")));
        user.setRoleName(stringValue(claims.get("roleName")));
        user.setPermissions(stringValue(claims.get("permissions")));
        user.setEnabled(booleanValue(claims.get("enabled")));
        return user;
    }

    @Override
    public void invalidate(String token) {
        ParsedToken parsedToken = parseToken(token, false);
        if (parsedToken != null) {
            revokedTokens.put(parsedToken.tokenId(), parsedToken.expiresAt());
        }
    }

    private ParsedToken parseToken(String token, boolean checkRevoked) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            cleanExpiredRevocations();
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String content = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(content), URL_DECODER.decode(parts[2]))) {
                return null;
            }

            Map<String, Object> header = decodeJson(parts[0]);
            if (!"HS256".equals(header.get("alg")) || !"JWT".equals(header.get("typ"))) {
                return null;
            }

            Map<String, Object> claims = decodeJson(parts[1]);
            Long expiresAt = longValue(claims.get("exp"));
            String tokenId = stringValue(claims.get("jti"));
            if (!issuer.equals(claims.get("iss"))
                    || expiresAt == null
                    || expiresAt <= Instant.now().getEpochSecond()
                    || tokenId == null
                    || (checkRevoked && revokedTokens.containsKey(tokenId))) {
                return null;
            }
            return new ParsedToken(claims, tokenId, expiresAt);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT serialization failed", exception);
        }
    }

    private Map<String, Object> decodeJson(String value) throws Exception {
        return objectMapper.readValue(URL_DECODER.decode(value), MAP_TYPE);
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT signing failed", exception);
        }
    }

    private void cleanExpiredRevocations() {
        long now = Instant.now().getEpochSecond();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    private Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Boolean booleanValue(Object value) {
        return value instanceof Boolean booleanValue ? booleanValue : Boolean.valueOf(stringValue(value));
    }

    private record ParsedToken(Map<String, Object> claims, String tokenId, long expiresAt) {}
}
