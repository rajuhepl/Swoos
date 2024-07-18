package com.example.swoos.service;


import com.example.swoos.dto.ColumnDto;
import com.example.swoos.dto.PasswordUpdateDTO;
import com.example.swoos.dto.UserDTO;
import com.example.swoos.dto.UserResponseDTO;
import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.UserSignUpRequest;

import java.util.List;

public interface UserService {

    String userSignup(UserSignUpRequest userSignUpRequest) throws CustomValidationException;

    UserDTO getUserById(String id) throws CustomValidationException;

    PageResponse<List<UserResponseDTO>> getAllUser(Integer pageNo) throws CustomValidationException;

    String updatePassword(PasswordUpdateDTO pass) throws Exception;

    String addColumn(ColumnDto columnDto);

    ColumnDto getAllColumns();
}

