package com.example.swoos.response;



import com.example.swoos.model.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AuthResponse implements Serializable {
    public AuthResponse(String token, String refreshToken, User user) {
        this.token = token;
        this.user = user;
        this.refreshToken = refreshToken;
    }

    private static final long serialVersionUID = 8286210631647330695L;

    private User user;

    private String token;

    private String refreshToken;
}

