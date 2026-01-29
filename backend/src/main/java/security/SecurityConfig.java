// Summary: Security: /api/teacher/** braucht JWT; /api/student/** bleibt anonym.
package security;

import jakarta.servlet.FilterChain;
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

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/student/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/teacher/**").authenticated()
                .anyRequest().permitAll()
        );

        http.addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(frame -> frame.disable())); // H2 console
        return http.build();
    }

    static class JwtFilter extends OncePerRequestFilter {
        private final JwtService jwtService;

        JwtFilter(JwtService jwtService) { this.jwtService = jwtService; }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
            try {
                String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (auth != null && auth.startsWith("Bearer ")) {
                    String token = auth.substring(7);
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
                // Log the exception to help debug token parsing/auth issues during development
                e.printStackTrace();
                try { response.sendError(401, "Unauthorized"); } catch (Exception ignored) {}
            }
        }
    }
}