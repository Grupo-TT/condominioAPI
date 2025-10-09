package com.condominio.util.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthFilter)  throws Exception{
        return httpSecurity
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/login", "/auth/**", "/error").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(form -> Customizer.withDefaults())
                .logout(LogoutConfigurer::permitAll)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userDetailsService)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConfig) throws Exception{
        return authConfig.getAuthenticationManager();
    }

}
