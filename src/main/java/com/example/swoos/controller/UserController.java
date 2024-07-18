package com.example.swoos.controller;

import com.example.swoos.dto.ColumnDto;
import com.example.swoos.dto.PasswordUpdateDTO;
import com.example.swoos.dto.UserDTO;
import com.example.swoos.dto.UserResponseDTO;
import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.response.UserSignUpRequest;
import com.example.swoos.service.UserService;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/userSignup")
    public ResponseEntity<String> userSignup(@RequestBody UserSignUpRequest userSignUpRequest) throws Exception {
        return ResponseEntity.ok(userService.userSignup(userSignUpRequest));
    }

    @GetMapping("/getUserById")
    public ResponseEntity<UserDTO> getUserById(@RequestParam(value = "id") String id) throws CustomValidationException {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/getAllUser")
    public PageResponse<List<UserResponseDTO>> getAllUser(@RequestParam(value = "pageNo") int pageNo) throws CustomValidationException {
        return userService.getAllUser(pageNo);
    }

    @PostMapping("/updatepassword")
    public ResponseEntity<String> upDatePassword(@RequestBody PasswordUpdateDTO pass) throws Exception {
        return ResponseEntity.ok(userService.updatePassword(pass));
    }

    @PostMapping("/column")
    public ResponseEntity<String> addColumn(@RequestBody ColumnDto columnDto){
        return ResponseEntity.ok(userService.addColumn(columnDto));
    }

    @GetMapping("/columns")
    public ResponseEntity<ColumnDto> getAllColumns(){
        return ResponseEntity.ok(userService.getAllColumns());
    }
}
