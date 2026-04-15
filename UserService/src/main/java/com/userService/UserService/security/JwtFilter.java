package com.userService.UserService.security;


import com.userService.UserService.config.JwtProvider;
import com.userService.UserService.config.JwtValidator;
import com.userService.UserService.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final JwtValidator jwtValidator;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtProvider jwtProvider,
                     JwtValidator jwtValidator,
                     CustomUserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.jwtValidator = jwtValidator;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        // Extract token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            email = jwtProvider.getEmailFromToken(token);
        }

        // Validate and set authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtValidator.validateToken(token)) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
