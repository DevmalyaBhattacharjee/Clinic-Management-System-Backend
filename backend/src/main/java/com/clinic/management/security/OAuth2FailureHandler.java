package com.clinic.management.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2FailureHandler
 *
 * Handles Google OAuth2 failures:
 *   - User canceled the Google consent screen
 *   - Google returned an error
 *   - State parameter mismatch (CSRF)
 *
 * Redirects to React login page with an error query parameter so the UI
 * can display a user-friendly message.
 */
@Component
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest  request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("OAuth2 authentication failed: {}", exception.getMessage());

        String msg = URLEncoder.encode(
            "Google sign-in failed. Please try again or use email/password.",
            StandardCharsets.UTF_8);

        response.sendRedirect(frontendUrl + "/login?oauth_error=" + msg);
    }
}
