package com.clinic.management.config;

import com.clinic.management.security.CustomOAuth2UserService;
import com.clinic.management.security.CustomUserDetailsService;
import com.clinic.management.security.JwtAuthenticationEntryPoint;
import com.clinic.management.security.JwtAuthenticationFilter;
import com.clinic.management.security.OAuth2FailureHandler;
import com.clinic.management.security.OAuth2SuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the CORS wiring in SecurityConfig — no Spring context / DB
 * required. Guards against regressing to a single hard-coded dev origin
 * (the original bug that blocked the deployed frontend).
 */
class SecurityConfigCorsTest {

    private SecurityConfig newConfig(String allowedOrigins) {
        SecurityConfig config = new SecurityConfig(
                mock(CustomUserDetailsService.class),
                mock(JwtAuthenticationEntryPoint.class),
                mock(JwtAuthenticationFilter.class),
                mock(CustomOAuth2UserService.class),
                mock(OAuth2SuccessHandler.class),
                mock(OAuth2FailureHandler.class));
        ReflectionTestUtils.setField(config, "allowedOrigins", allowedOrigins);
        return config;
    }

    @Test
    void allowsConfiguredProductionAndLocalOrigins() {
        SecurityConfig config = newConfig(
                "https://medicine-health.vercel.app,http://localhost:5173");
        CorsConfigurationSource source = config.corsConfigurationSource();

        CorsConfiguration deployedFrontend = source.getCorsConfiguration(
                requestFrom("https://medicine-health.vercel.app", "/api/auth/login"));
        assertThat(deployedFrontend).isNotNull();
        assertThat(deployedFrontend.getAllowedOrigins())
                .contains("https://medicine-health.vercel.app", "http://localhost:5173");
        assertThat(deployedFrontend.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertThat(deployedFrontend.getAllowedHeaders()).contains("*");
        assertThat(deployedFrontend.getAllowCredentials()).isTrue();
    }

    @Test
    void rejectsUnknownOrigin() {
        SecurityConfig config = newConfig(
                "https://medicine-health.vercel.app,http://localhost:5173");
        CorsConfigurationSource source = config.corsConfigurationSource();

        CorsConfiguration cfg = source.getCorsConfiguration(
                requestFrom("https://evil.example.com", "/api/auth/login"));
        assertThat(cfg.checkOrigin("https://evil.example.com")).isNull();
    }

    private MockHttpServletRequest requestFrom(String origin, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", path);
        request.addHeader("Origin", origin);
        return request;
    }
}
