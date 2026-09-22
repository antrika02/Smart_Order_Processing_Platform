package com.antrika.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login"
                        ).permitAll()

                        // Authenticated users can read products
                        .requestMatchers(
                                HttpMethod.GET,
                                "/products",
                                "/products/**"
                        ).authenticated()

                        // Only ADMIN can create products
                        .requestMatchers(
                                HttpMethod.POST,
                                "/products",
                                "/products/**"
                        ).hasRole("ADMIN")

                        // Only ADMIN can update products
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/products/**"
                        ).hasRole("ADMIN")

                        // Only ADMIN can delete products
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/products/**"
                        ).hasRole("ADMIN")

                        // Authenticated users can create orders
                        .requestMatchers(
                                HttpMethod.POST,
                                "/orders"
                        ).authenticated()

                        // Authenticated users can view their orders
                        .requestMatchers(
                                HttpMethod.GET,
                                "/orders",
                                "/orders/**"
                        ).authenticated()

                        // Authenticated users can cancel their own orders
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/orders/*/cancel"
                        ).authenticated()

                        // Only ADMIN can change order status
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/orders/*/status"
                        ).hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}