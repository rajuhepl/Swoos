package com.example.swoos.response;



import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AuthResponse implements Serializable {
    public AuthResponse(String token, String refreshToken, String user) {
        this.token = token;
        this.user = user;
        this.refreshToken = refreshToken;
    }

    private static final long serialVersionUID = 8286210631647330695L;

    private String user;

    private String token;

    private String refreshToken;
}

