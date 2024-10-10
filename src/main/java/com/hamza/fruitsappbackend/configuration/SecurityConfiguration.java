package com.hamza.fruitsappbackend.configuration;

import com.hamza.fruitsappbackend.security.JwtAuthenticationEntryPoint;
import com.hamza.fruitsappbackend.security.JwtAuthenticationFilter;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfiguration(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtTokenProvider jwtTokenProvider) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
                .csrf().disable() // Disable CSRF as we're using JWT
                .cors() // Enable CORS
                .and()
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless sessions (JWT)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/users/**", "/api/v1/roles/**", "/api/v1/forgotPassword/**").permitAll() // Public endpoints
                        .requestMatchers("/api/v1/auth/**", "/api/v1/products/**").permitAll() // Allow authentication requests without JWT
                        .requestMatchers("/api/v1/**").permitAll() // Allow all OPTIONS requests
                        .anyRequest().authenticated()) // All other requests require authentication
                .addFilterBefore(jwtAuthenticationFilter(userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    // Add CORS filter if not using Spring Boot's @CrossOrigin annotation
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Allow any origin for development
        config.addAllowedHeader("*"); // Allow any header
        config.addAllowedMethod("*"); // Allow any method (GET, POST, etc.)
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
