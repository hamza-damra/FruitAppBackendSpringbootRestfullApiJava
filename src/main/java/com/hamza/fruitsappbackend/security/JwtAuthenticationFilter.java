package com.hamza.fruitsappbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.fruitsappbackend.exception.CustomErrorResponse;
import com.hamza.fruitsappbackend.exception.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.info("Request URI: " + requestURI);

        if (isInvalidUrl(requestURI)) {
            logger.warning("Invalid URL accessed: " + requestURI);
            CustomErrorResponse errorResponse = new CustomErrorResponse("Invalid URL: The requested endpoint does not exist.", HttpServletResponse.SC_NOT_FOUND);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return;
        }

        if (isUnsecuredEndpoint(requestURI)) {
            logger.info("Unsecured endpoint accessed: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = getJwtTokenFromRequest(request);
            logger.info("JWT token from request: " + token);

            if (!StringUtils.hasText(token)) {
                logger.warning("No token provided for secured endpoint: " + requestURI);
                CustomErrorResponse errorResponse = new CustomErrorResponse("Token is required. Please add a token.", HttpServletResponse.SC_BAD_REQUEST);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
                return;
            }

            if (jwtTokenProvider.validateToken(token)) {
                String userName = jwtTokenProvider.getUserNameFromToken(token);
                logger.info("Username from token: " + userName);

                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("User authenticated: " + userName);
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            logger.warning("JWT authentication failed: " + ex.getMessage());
            CustomErrorResponse errorResponse = new CustomErrorResponse("Unauthorized: Invalid JWT token. Please log in again.", HttpServletResponse.SC_UNAUTHORIZED);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        }
    }


    private boolean isInvalidUrl(String requestURI) {
        return false;
//        return !requestURI.startsWith("/api/v1/users/") &&
//                !requestURI.startsWith("/api/v1/roles/") &&
//                !requestURI.startsWith("/api/v1/orders") &&
//                !requestURI.startsWith("/api/carts/") &&
//                !requestURI.startsWith("/api/v1/products/");
    }

    private boolean isUnsecuredEndpoint(String requestURI) {
        return requestURI.startsWith("/api/v1/users/") || requestURI.startsWith("/api/v1/roles/") || requestURI.startsWith("/api/v1/orders");
    }

    private String getJwtTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
