package com.condominio.util.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtProperties {

    @Value("${JWT_SECRET_KEY}")
    private String secret;
    @Value("${JWT_EXPIRATION_TIME}")
    private long expiration;

    public String getSecret() { return secret; }

    public long getExpiration() { return expiration; }
}
