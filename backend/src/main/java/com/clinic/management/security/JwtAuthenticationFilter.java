package com.clinic.management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthenticationFilter
 *
 * Validates JWT tokens on every incoming request EXCEPT OPTIONS requests
 * (CORS preflight) and the paths listed in SKIP_PATHS.  The skip list is
 * critical for OAuth2: if the filter runs on /oauth2/authorization/google or
 * /login/oauth2/code/google it will find no Bearer token and block the flow
 * before Spring Security's OAuth2 machinery even starts, producing a
 * spurious 401.
 *
 * shouldNotFilter() is called by the Servlet container BEFORE doFilterInternal,
 * so skipped paths bypass this filter completely.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil                  jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /**
     * Paths that must bypass JWT validation entirely.
     *
     * /api/auth/**         — login, forgot-password, reset-password (all public)
     * /api/patient/register— public self-registration
     * /oauth2/**           — OAuth2 authorization initiation
     * /login/**            — Spring Security's OAuth2 callback handler (/login/oauth2/code/*)
     * /swagger-ui/**       — Swagger UI assets
     * /v3/api-docs/**      — OpenAPI JSON
     * /swagger-ui.html     — Swagger redirect
     */
    private static final List<String> SKIP_PATHS = List.of(
        "/api/auth/**",
        "/api/patient/register",
        "/oauth2/**",
        "/login/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // CORS preflight requests never carry a JWT — let them pass through untouched
        // so the CORS filter/headers are applied instead of a spurious 401/403.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        return SKIP_PATHS.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {
        try {
            String jwt = extractJwt(request);

            if (StringUtils.hasText(jwt)) {
                String username = jwtUtil.extractUsername(jwt);

                if (StringUtils.hasText(username) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                        authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
