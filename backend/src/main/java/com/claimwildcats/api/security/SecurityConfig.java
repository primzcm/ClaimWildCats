package com.claimwildcats.api.security;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<FirebaseAuth> firebaseAuthProvider)
            throws Exception {
        FirebaseAuth firebaseAuth = firebaseAuthProvider.getIfAvailable();
        if (firebaseAuth != null) {
            http.addFilterBefore(new FirebaseAuthenticationFilter(firebaseAuth), UsernamePasswordAuthenticationFilter.class);
        }

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/items/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/items/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/items/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/claims/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/claims/**").authenticated()
                        .anyRequest()
                        .permitAll());

        return http.build();
    }
}
