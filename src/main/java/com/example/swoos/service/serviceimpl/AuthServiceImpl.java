package com.example.swoos.service.serviceimpl;

import com.example.swoos.dto.UserDTO;
import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.exception.ErrorCode;
import com.example.swoos.model.User;
import com.example.swoos.repository.UserRepository;
import com.example.swoos.response.AuthResponse;
import com.example.swoos.response.LoginResponse;
import com.example.swoos.service.Authservice;
import com.example.swoos.util.JWTUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements Authservice {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JWTUtils jwtUtils;
    @Override
    public LoginResponse login(AuthResponse response) throws CustomValidationException {
        try {
            if (response != null) {
                User users = response.getUser();
                ModelMapper modelMapper = new ModelMapper();
                return new LoginResponse(modelMapper.map(users, UserDTO.class), response.getToken(),
                        response.getRefreshToken());
            } else {
                throw new CustomValidationException(ErrorCode.CAP_1017);
            }
        } catch (Exception e) {
            throw new CustomValidationException(ErrorCode.CAP_1017);
        }
    }

    @Override
    public User getUser(String username) throws CustomValidationException {
        Optional<User> users= userRepository.findByEmail(username);
        if(users.isEmpty()){
            throw new CustomValidationException(ErrorCode.CAP_1016);
        }
        return users.get();
    }

}

