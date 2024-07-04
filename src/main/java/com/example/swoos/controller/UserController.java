package com.example.swoos.controller;

import com.example.swoos.dto.ColumnDto;
import com.example.swoos.dto.PasswordUpdateDTO;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.response.UserSignUpRequest;
import com.example.swoos.service.UserService;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/userSignup")
    public SuccessResponse<Object> userSignup(@RequestBody UserSignUpRequest userSignUpRequest) throws Exception {
        return userService.userSignup(userSignUpRequest);
    }

    @GetMapping("/getUserById")
    public SuccessResponse<Object> getUserById(@RequestParam(value = "id") String id) {
        return userService.getUserById(id);
    }

    @GetMapping("/getAllUser")
    public PageResponse<Object> getAllUser(@RequestParam(value = "pageNo") int pageNo){
        return userService.getAllUser(pageNo);
    }

    @PostMapping("/updatepassword")
    public String upDatePassword(@RequestBody PasswordUpdateDTO pass) throws Exception {
        return userService.updatePassword(pass);
    }

    @PostMapping("/column")
    public String addColumn(@RequestBody ColumnDto columnDto){
    return userService.addColumn(columnDto);
    }

    @GetMapping("/columns")
    public ColumnDto getAllColumns(){
        return userService.getAllColumns();
    }
}
