package security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/assets/**").permitAll()
                .requestMatchers("/student/**").permitAll()
                .requestMatchers("/api/student/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers("/teacher/login.html").permitAll()
                .requestMatchers("/teacher/**").authenticated()

                .requestMatchers("/api/teacher/**").authenticated()
                .anyRequest().permitAll()
        );

        http.addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    static class JwtFilter extends OncePerRequestFilter {
        private final JwtService jwtService;

        JwtFilter(JwtService jwtService) { this.jwtService = jwtService; }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {

            String path = request.getRequestURI();

            try {
                String token = extractToken(request);
                if (token != null) {
                    long teacherId = jwtService.parseTeacherId(token);

                    AbstractAuthenticationToken authentication = new AbstractAuthenticationToken(
                            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
                    ) {
                        @Override public Object getCredentials() { return token; }
                        @Override public Object getPrincipal() { return teacherId; }
                    };
                    authentication.setAuthenticated(true);

                    org.springframework.security.core.context.SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

                chain.doFilter(request, response);

            } catch (Exception e) {
                // nur teacher paths hart blocken
                if (path.startsWith("/api/teacher") || (path.startsWith("/teacher") && !path.equals("/teacher/login.html"))) {
                    response.sendError(401, "Unauthorized");
                } else {
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                    chain.doFilter(request, response);
                }
            }
        }

        private String extractToken(HttpServletRequest request) {
            // 1) Header
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && auth.startsWith("Bearer ")) {
                return auth.substring(7).trim();
            }

            // 2) Cookie (optional)
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("LC_TOKEN".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                        return c.getValue().trim();
                    }
                }
            }
            return null;
        }
    }
}