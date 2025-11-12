// src/main/java/com/finance/api/config/SecurityConfig.java
package com.finance.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.finance.api.auth.application.JwtAuthFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()

                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET,  "/auth/session").permitAll()

                .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/forgot-password/confirm").permitAll()

                .requestMatchers(HttpMethod.GET, "/users/by-email").permitAll()

                .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()

                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .clearAuthentication(true)
                .invalidateHttpSession(false) // stateless
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)) 
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}