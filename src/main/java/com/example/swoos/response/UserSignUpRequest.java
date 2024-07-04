package com.example.swoos.response;

import com.example.swoos.dto.MasterRoleDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignUpRequest {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String dob;
    private String mobileNumber;
    private MasterRoleDTO applicationRole;
    private boolean isActive;
}

