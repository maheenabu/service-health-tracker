package com.goodleap.healthtracker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> WHITELIST = List.of(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        for (String pattern : WHITELIST) {
            if (PATH_MATCHER.match(pattern, path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            reject(response, "Missing or invalid Authorization header");
            return;
        }

        String token = auth.substring(7).trim();
        try {

            Jws<Claims> jws = TokenFactory.parser().parseClaimsJws(token);

            Claims body = jws.getBody();
            Object isAdminClaim = body.get("isAdmin");
            boolean isAdmin = (isAdminClaim instanceof Boolean && (Boolean) isAdminClaim)
                    || (isAdminClaim instanceof String && "true".equalsIgnoreCase((String) isAdminClaim));

            if (!isAdmin) { reject(response, "Admin privileges required"); return; }

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            var authentication = new UsernamePasswordAuthenticationToken(body.getSubject(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try { filterChain.doFilter(request, response); }
            finally { SecurityContextHolder.clearContext(); }

        } catch (Exception e) {
            reject(response, "Invalid token: " + e.getMessage());
        }
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getWriter(), Map.of("error", message));
    }
}
