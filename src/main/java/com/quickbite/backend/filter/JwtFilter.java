package com.quickbite.backend.filter;

import com.quickbite.backend.A2_service.JwtService;
import com.quickbite.backend.A3_repo.UserRepo;
import com.quickbite.backend.custom_exception.AccountDeactivatedException;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.principal.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    // extract token from header
    // validate token (automatic by parseSignedClaims that checks SIGNATURE and EXPIRY
    // if valid then extract username
    // add to SecurityContextHolder for further layers to access it.

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = null;
            String userId = null;

            try {
                userId = jwtService.extractAllClaims(token).getSubject();
            } catch (SignatureException e) {

                String jsonString = "{\"message\" : \"Cannot trust the user.Please try to login again\", \"time\" : \"" + LocalDateTime.now() + "\"}";

                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write(jsonString);

                return;

            } catch (ExpiredJwtException e) {
                String jsonString = "{\"message\" : \"Session expired! Please login again\", \"time\" : \"" + LocalDateTime.now() + "\"}";

                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write(jsonString);

                return;
            } catch (Exception e) {
                System.out.println("exception in filter");
                throw new RuntimeException(e);
            }

            try {
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    AppUser user = userRepo.findByUserId(Integer.valueOf(userId)).orElseThrow(() -> new ResourceNotFoundException("{\"message\" : \"User not found.Please register\", \"time\" : \"" + LocalDateTime.now() + "\"}"));

                    if (user.getActive()) {
                        UserPrincipal userDetails = new UserPrincipal(user);
                        Authentication authObj = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authObj);
                    } else {
                        throw new AccountDeactivatedException("\"message\" : \"You account is deactivated, please register again with same email-id to activate again.\", \"time\" : \"" + LocalDateTime.now() + "\"");
                    }
                }
            } catch (AccountDeactivatedException | ResourceNotFoundException e) {
                Throwable cause = e.getCause();
                if (cause instanceof AccountDeactivatedException) {
                    response.setStatus(409);
                } else {
                    response.setStatus(403);
                }
                response.setContentType("application/json");
                response.getWriter().write(e.getMessage());
                System.out.println("error");
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
