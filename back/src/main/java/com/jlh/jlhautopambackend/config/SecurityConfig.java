package com.jlh.jlhautopambackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration ac) throws Exception {
        return ac.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ Règles d’API claires (admin vs client) + CORS + JSON sur 401/403
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // public
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/auth/verify-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/promotions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()

                        // client
                        .requestMatchers(HttpMethod.POST, "/api/auth/resend-verification").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/me/change-password").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/demandes/mes-demandes").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET,  "/api/demandes/mes-demandes/stats").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET,  "/api/demandes/mes-demandes/prochain-rdv").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET,  "/api/demandes/mes-demandes/prochain-rdv.ics").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/demandes").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/demandes/current").hasRole("CLIENT")

                        // ✅ ICS chemin correct (sous /api/demandes)
                        .requestMatchers(HttpMethod.GET, "/api/demandes/rendezvous/*/ics").hasAnyRole("CLIENT","ADMIN")

                        // demandes-services : seuls les clients ajoutent/modifient/suppriment
                        .requestMatchers(HttpMethod.POST,   "/api/demandes-services").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT,    "/api/demandes-services/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/demandes-services/**").hasRole("CLIENT")
                        // (optionnel) lister/voir demandes-services → ADMIN
                        .requestMatchers(HttpMethod.GET,    "/api/demandes-services/**").hasRole("ADMIN")

                        // admin
                        .requestMatchers("/api/administrateurs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/promotions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/promotions/**").hasRole("ADMIN")
                        // ✅ restreindre les opé admin *par méthode* sur services
                        .requestMatchers(HttpMethod.POST,   "/api/services/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/services/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasRole("ADMIN")

                        // le reste doit être authentifié
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Forbidden\"}");
                        })
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ CORS : autorise localhost:4200 et renvoie bien les headers sur la preflight
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // IMPORTANT : origins explicites quand allowCredentials=true
        cfg.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:63342"
        ));
        // Alternative souple si besoin : cfg.setAllowedOriginPatterns(List.of("http://localhost:*"));

        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        cfg.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"
        ));
        cfg.setExposedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L); // 1h de cache pour la preflight

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        // Applique à toute l’API
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
