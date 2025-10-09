package com.condominio.util.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtProperties {

    @Value("${JWT_SECRET_KEY}")
    private String secret;
    @Value("${JWT_EXPIRATION_TIME}")
    private long expiration;
    @Value("${JWT_REFRESH_EXPIRATION_TIME}")
    private long refreshExpiration;

    public String getSecret() { return secret; }

    public long getExpiration() { return expiration; }

    public long getRefreshExpiration() { return refreshExpiration; }
}
