package com.example.swoos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
   // private String password;
    private String dob;
    private String mobileNumber;
    private MasterRoleDTO applicationRole;
    private boolean isActive;
    private boolean deleteFlag;
    private String createdAt;
    private int createdBy;
    private String updatedAt;
    private int updatedBy;

}
