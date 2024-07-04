package com.example.swoos.response;

import com.example.swoos.dto.UserDTO;
import lombok.Getter;

@Getter
public class LoginResponse {
    private UserDTO users;

    private String token;

    private String refreshToken;

    public LoginResponse(UserDTO users, String token, String refreshToken) {
        this.users = users;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
