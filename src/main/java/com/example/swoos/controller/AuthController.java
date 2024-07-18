package com.example.swoos.controller;

import com.example.swoos.dto.TokenDTO;
import com.example.swoos.model.User;
import com.example.swoos.request.LoginRequest;
import com.example.swoos.response.AuthResponse;
import com.example.swoos.response.LoginResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.Authservice;
import com.example.swoos.util.JWTUtils;
import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private Authservice authService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpSession session) throws CustomValidationException {
        if (loginRequest == null) {
            throw new CustomValidationException(ErrorCode.CAP_1016);
        }
        AuthResponse response = authenticate(loginRequest.getEmail(), loginRequest.getPassword(),session,false);
        return ResponseEntity.ok(authService.login(response));
    }

    public AuthResponse authenticate(String username, String password, HttpSession session,boolean refreshFlag) throws CustomValidationException {
        String token = null;
        String refreshToken = null;
        User user = null;
        try {
            if(!refreshFlag) {
                final Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(username, password));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                if (Objects.nonNull(authentication)) {
                    user = authService.getUser(username);
                    token = jwtUtils.generateToken(user, session);
                    refreshToken = jwtUtils.refreshToken(token, user);
                }
            }else{
                user = authService.getUser(username);
                token = jwtUtils.generateToken(user, session);
                refreshToken = jwtUtils.refreshToken(token, user);
            }

        } catch (DisabledException e) {
            throw new CustomValidationException(ErrorCode.CAP_1001);
        } catch (BadCredentialsException e) {
            throw new CustomValidationException(ErrorCode.CAP_1016);
        }

        return new AuthResponse(token, refreshToken, user);
    }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody TokenDTO token, HttpSession session) throws Exception {

        String email = jwtUtils.getEmail(token.getRefreshToken());
        if (email == null) {
            throw new CustomValidationException(ErrorCode.CAP_1016);
        }
        User user = authService.getUser(email);
        AuthResponse response = authenticate(user.getEmail(), user.getPassword(), session,true);
        return  ResponseEntity.ok(authService.login(response));
    }
}

