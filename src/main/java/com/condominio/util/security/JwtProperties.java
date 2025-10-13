package com.condominio.util.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtProperties {

    @Value("${JWT_SECRET_KEY}")
    private String secret;
    @Value("${JWT_EXPIRATION_TIME}")
    private long expiration;
    @Value("${JWT_REFRESH_EXPIRATION}")
    private long refreshExpiration;

}
