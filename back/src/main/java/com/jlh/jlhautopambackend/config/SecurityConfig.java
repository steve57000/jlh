package com.jlh.jlhautopambackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactive CSRF (nécessaire pour les POST/PUT depuis JS externes)
                .csrf(AbstractHttpConfigurer::disable)

                // Règles d'autorisation
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()   // ouvre  l'API REST
                        .requestMatchers("/api/demandes").permitAll()
                        .anyRequest().authenticated()             // protège le reste
                )

                // Active l'authentification HTTP Basic (ou remplacez par formLogin si vous préférez)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
