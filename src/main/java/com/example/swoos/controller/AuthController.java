package com.example.swoos.controller;

import com.example.swoos.dto.TokenDTO;
import com.example.swoos.model.User;
import com.example.swoos.request.LoginRequest;
import com.example.swoos.response.AuthResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.Authservice;
import com.example.swoos.util.JWTUtils;
import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class AuthController {
    @Autowired
    Authservice authService;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JWTUtils jwtUtils;

    @PostMapping("/login")
    public SuccessResponse login(@RequestBody LoginRequest loginRequest, HttpSession session) throws Exception {
        if (loginRequest == null) {
            throw new Exception("Bad Request");
        }
        AuthResponse response = authenticate(loginRequest.getEmail(), loginRequest.getPassword(),session,false);
        return authService.login(response);
    }

    public AuthResponse authenticate(String username, String password, HttpSession session,Boolean refreshFlag) throws Exception {
        String token = null;
        String refreshToken = null;

        try {
            if(!refreshFlag) {
                final Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(username, password));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                if (Objects.nonNull(authentication)) {
                    User user = authService.getUser(username);
                    token = jwtUtils.generateToken(user, session);
                    refreshToken = jwtUtils.refreshToken(token, user);
                }
            }
            User user = authService.getUser(username);
            token = jwtUtils.generateToken(user, session);
            refreshToken = jwtUtils.refreshToken(token, user);
        } catch (DisabledException e) {
            throw new CustomValidationException(ErrorCode.CAP_1001);
        } catch (BadCredentialsException e) {
            throw new CustomValidationException(ErrorCode.CAP_1016);
        }

        return new AuthResponse(token, refreshToken, jwtUtils.getEmail(token));
    }
    @PostMapping("/refresh")
    public SuccessResponse refreshToken(@RequestBody TokenDTO token, HttpSession session) throws Exception {

        String email = jwtUtils.getEmail(token.getRefreshToken());
        if (email == null) {
            throw new CustomValidationException(ErrorCode.CAP_1016);
        }
        User user = authService.getUser(email);
        AuthResponse response = authenticate(user.getEmail(), user.getPassword(), session,true);
        return  authService.login(response);
    }
}

