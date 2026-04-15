package com.userService.UserService.security;


import com.userService.UserService.config.JwtProvider;
import com.userService.UserService.config.JwtValidator;
import com.userService.UserService.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            // ✅ ADD THIS FIRST
            if (!jwtValidator.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }




            // 🔥 STEP 1: Parse token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtProvider.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();

            // 🔥 STEP 2: Extract roles from token
            List<String> roles = (List<String>) claims.get("roles");

            // 🔥 STEP 3: Convert roles to authorities
            List<SimpleGrantedAuthority> authorities =
                    roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            // 🔥 STEP 4: Create authentication object
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            System.out.println("Invalid JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
