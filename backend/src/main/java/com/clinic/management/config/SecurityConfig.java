package com.clinic.management.config;

import com.clinic.management.security.CustomOAuth2UserService;
import com.clinic.management.security.CustomUserDetailsService;
import com.clinic.management.security.CustomUserDetailsService;
import com.clinic.management.security.JwtAuthenticationEntryPoint;
import com.clinic.management.security.JwtAuthenticationFilter;
import com.clinic.management.security.OAuth2FailureHandler;
import com.clinic.management.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig — combines JWT and Google OAuth2 authentication.
 *
 * Authentication flows:
 *
 *   A. JWT (REST API):
 *      Client sends  Authorization: Bearer <token>
 *      → JwtAuthenticationFilter validates token
 *      → SecurityContext populated
 *      → Request reaches controller
 *
 *   B. Google OAuth2 (browser):
 *      Browser → /oauth2/authorization/google
 *      → Google consent screen
 *      → Google → /login/oauth2/code/google
 *      → CustomOAuth2UserService (loads/creates user)
 *      → OAuth2SuccessHandler (generates JWT, redirects to React)
 *      → React stores JWT, uses it for subsequent API calls (flow A)
 *
 * Session policy: IF_REQUIRED
 *   OAuth2 requires a short-lived HTTP session to store the "state" parameter
 *   used for CSRF protection during the authorization code flow.  After the
 *   redirect the session is no longer needed — React takes over with JWT.
 *   All API endpoints remain effectively stateless.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService   userDetailsService;
    private final JwtAuthenticationEntryPoint jwtEntryPoint;
    private final JwtAuthenticationFilter    jwtFilter;
    private final CustomOAuth2UserService    oAuth2UserService;
    private final OAuth2SuccessHandler       oAuth2SuccessHandler;
    private final OAuth2FailureHandler       oAuth2FailureHandler;

    // Comma-separated list of allowed frontend origins.
    // Override in production via the CORS_ALLOWED_ORIGINS env var on Railway.
    @Value("${app.cors.allowed-origins:https://medicine-health.vercel.app,http://localhost:5173}")
    private String allowedOrigins;

    // ── Beans ──────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // Deployed Vercel frontend + local Vite dev server.
        // Explicit origin list (not "*") is required because allowCredentials is true.
        cfg.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);                    // needed for OAuth2 cookies / JWT header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // ── Security filter chain ──────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled for REST APIs.
                // OAuth2 CSRF is protected via the "state" parameter (built into Spring).
                .csrf(AbstractHttpConfigurer::disable)

                // Apply CORS config above
                .cors(Customizer.withDefaults())

                // 401 handler for REST endpoints with no/invalid JWT
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint))

                // IF_REQUIRED: sessions are created only when needed (OAuth2 state storage).
                // REST API calls never create a session — they use JWT only.
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authenticationProvider(authenticationProvider())

                // ── URL authorization ──────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // CORS preflight — browsers send OPTIONS with no Authorization header,
                        // so it must be permitted for every path (including role-protected ones)
                        // or the preflight itself gets a 401/403 before the real request is sent.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Public REST auth endpoints (login, forgot-password, reset-password)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Patient self-registration — public by design
                        .requestMatchers(HttpMethod.POST, "/api/patient/register").permitAll()

                        // OAuth2 authorization initiation + callback — Spring manages these
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()

                        // Role-scoped API endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/patient/**").hasRole("PATIENT")
                        .requestMatchers("/api/receptionist/**").hasRole("RECEPTIONIST")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // ── OAuth2 login configuration ─────────────────────────────────
                .oauth2Login(oauth -> oauth
                        // CustomOAuth2UserService: loads or creates the User entity
                        .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
                        // OAuth2SuccessHandler: generates JWT and redirects to React
                        .successHandler(oAuth2SuccessHandler)
                        // OAuth2FailureHandler: redirects to React login with error param
                        .failureHandler(oAuth2FailureHandler)
                )

                // JWT filter runs BEFORE UsernamePasswordAuthenticationFilter.
                // JwtAuthenticationFilter.shouldNotFilter() skips /oauth2/** and /login/**
                // so OAuth2 paths are never touched by the JWT filter.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}