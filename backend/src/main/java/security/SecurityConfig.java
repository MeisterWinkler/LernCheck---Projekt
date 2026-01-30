// Summary: Security: /api/teacher/** braucht JWT; /api/student/** bleibt anonym.
package security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/student/**").permitAll()
                .requestMatchers("/assets/**").permitAll()
                .requestMatchers("/student/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/teacher/**").authenticated()
                .anyRequest().permitAll()
        );

        http.addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        // H2 console
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

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
                String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (auth != null && auth.startsWith("Bearer ")) {
                    String token = auth.substring(7).trim();

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
                // Nur API-Teacher Endpunkte wirklich blocken.
                // Sonst zerstörst du dir permitAll-Requests durch einen JWT-Parsing Fehler.
                if (path.startsWith("/api/teacher")) {
                    response.sendError(401, "Unauthorized");
                } else {
                    // Für alles andere: kein Auth setzen und Request normal weiterlaufen lassen
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                    chain.doFilter(request, response);
                }
            }
        }
    }
}