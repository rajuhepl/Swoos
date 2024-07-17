package com.example.swoos.service;


import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.model.User;
import com.example.swoos.response.AuthResponse;
import com.example.swoos.response.LoginResponse;
import com.example.swoos.response.SuccessResponse;

public interface Authservice {
    LoginResponse login(AuthResponse response) throws CustomValidationException;

    User getUser(String username) throws CustomValidationException;

//    SuccessResponse refreshToken(TokenDTO token, HttpSession session) throws Exception;

}