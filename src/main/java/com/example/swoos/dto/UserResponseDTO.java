package com.example.swoos.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {

    private long id;
    private String username;
    private String email;
    private String applicationRole;
    private String createdAt;

}

