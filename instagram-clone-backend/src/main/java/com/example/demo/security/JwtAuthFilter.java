package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        // read header
        String authHeader = request.getHeader("Authorization");
        // check header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // remove "Bearer " to get the actual token.
        String token = authHeader.substring(7);

        if (jwtUtil.validateToken(token)) {
            // extract the username from the token
            String username = jwtUtil.extractUsername(token);
            // find user info in the db
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            //create Auth
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            // attach additional req info
            authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );
            // add auth to the security context
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authToken);
        }
        // always allow the req to proceed
        filterChain.doFilter(request, response);
    }
}
