package com.antrika.backend.security;

import com.antrika.backend.entity.User;
import com.antrika.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            log.warn(
                    "Rejected invalid JWT: method={}, uri={}",
                    request.getMethod(),
                    request.getRequestURI()
            );

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token"
            );
            return;
        }

        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            log.warn("JWT user not found: email={}", email);

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "User not found"
            );
            return;
        }

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(authority)
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        log.info(
                "JWT authenticated: email={}, role={}, authority={}, method={}, uri={}",
                email,
                user.getRole(),
                authority.getAuthority(),
                request.getMethod(),
                request.getRequestURI()
        );

        log.info(
                "SecurityContext: authenticated={}, authorities={}",
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .isAuthenticated(),
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getAuthorities()
        );

        filterChain.doFilter(request, response);
    }
}