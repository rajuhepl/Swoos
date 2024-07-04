package com.example.swoos.service;


import com.example.swoos.dto.ColumnDto;
import com.example.swoos.dto.PasswordUpdateDTO;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.response.UserSignUpRequest;

public interface UserService {

    SuccessResponse<Object> userSignup(UserSignUpRequest userSignUpRequest);

    SuccessResponse<Object> getUserById(String id);

    PageResponse<Object> getAllUser(Integer pageNo);

    String updatePassword(PasswordUpdateDTO pass) throws Exception;

    String addColumn(ColumnDto columnDto);

   ColumnDto getAllColumns();
}

