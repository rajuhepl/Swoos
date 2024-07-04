package com.example.swoos.service;


import com.example.swoos.model.User;
import com.example.swoos.response.AuthResponse;
import com.example.swoos.response.SuccessResponse;

public interface Authservice {
    SuccessResponse login(AuthResponse response) throws Exception;

    User getUser(String username) throws Exception;

//    SuccessResponse refreshToken(TokenDTO token, HttpSession session) throws Exception;

}