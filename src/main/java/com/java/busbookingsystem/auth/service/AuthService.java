package com.java.busbookingsystem.auth.service;

import com.java.busbookingsystem.auth.helper.JwtService;
import com.java.busbookingsystem.auth.helper.UserInfoService;
import com.java.busbookingsystem.auth.model.AuthRequest;
import com.java.busbookingsystem.utils.exception.GlobalExceptionWrapper;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
@Autowired
    private UserInfoService userInfoService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Authenticates the user provided credentials.
     *
     * @param authRequest The user provided credentials.
     * @return The token on validating the user.
     */
    public HashMap<String, String> authenticate(@NonNull AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            return generateTokens(authRequest.getEmail());
        } else {
            throw new GlobalExceptionWrapper.BadRequestException("Invalid Credentials.");
        }
    }

    public Map<String, String> refreshToken(String refreshToken) {
        try {
            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);
            if (username == null) {
                throw new GlobalExceptionWrapper.BadRequestException("Invalid Refresh Token.");
            }

            // Load user details
            UserDetails userDetails = userInfoService.loadUserByUsername(username);

            // Validate the refresh token
            if (jwtService.validateToken(refreshToken, userDetails)) {
                // Generate new tokens
                return generateTokens(username);
            } else {
                throw new GlobalExceptionWrapper.BadRequestException("Invalid Refresh Token.");
            }
        } catch (Exception e) {
            throw new GlobalExceptionWrapper.BadRequestException("Invalid Refresh Token: " + e.getMessage());
        }
    }

    /**
     * Additional method to check if the token is a refresh token
     *
     * @param token JWT token to check
     * @return boolean indicating if it's a refresh token
     */
    private boolean isRefreshToken(String token) {
        try {
            Date expiration = jwtService.extractExpiration(token);
            return expiration.getTime() == jwtService.getRefreshTokenExpiration();
        } catch (Exception e) {
            return false;
        }
    }

    private HashMap<String, String> generateTokens(String username) {
        HashMap<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", jwtService.generateToken(username));
        tokenMap.put("refreshToken", jwtService.generateRefreshToken(username));
        return tokenMap;
    }

}
