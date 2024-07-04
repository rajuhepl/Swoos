package com.example.swoos.service.serviceimpl;

import com.example.swoos.dto.UserDTO;
import com.example.swoos.model.User;
import com.example.swoos.repository.UserRepository;
import com.example.swoos.response.AuthResponse;
import com.example.swoos.response.LoginResponse;
import com.example.swoos.response.SuccessResponse;
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
    public SuccessResponse login(AuthResponse response) throws Exception {
        SuccessResponse successResponse = null;
        try {
            successResponse = new SuccessResponse();
            if (response != null) {
                Optional<User> users = userRepository.findByEmail(response.getUser());
                if (users.isEmpty()) {
                    throw new Exception("INVALID_CREDENTIALS");
                }
                ModelMapper modelMapper = new ModelMapper();
                successResponse.setData(new LoginResponse(modelMapper.map(users.get(), UserDTO.class), response.getToken(),
                        response.getRefreshToken()));

            } else {
                throw new Exception("Login Failed.!");
            }
            return successResponse;
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            throw new Exception("Login Failed.!");
        }
    }

    @Override
    public User getUser(String username) throws Exception {
        Optional<User> users= userRepository.findByEmail(username);
        if(users.isEmpty()){
            throw new Exception("INVALID_CREDENTIALS");
        }
        return users.get();
    }

//    @Override
//    public SuccessResponse refreshToken(TokenDTO token, HttpSession session) throws Exception {
//        SuccessResponse successResponse = null;
//        try {
//            successResponse = new SuccessResponse();
//            if (response != null) {
//                Optional<Users> users = userRepository.findByEmail(response.getUser());
//                if (users.isEmpty()) {
//                    throw new Exception("INVALID_CREDENTIALS");
//                }
//                ModelMapper modelMapper = new ModelMapper();
//                successResponse.setData(new LoginResponse(modelMapper.map(users.get(), Users.class), response.getToken(),
//                        response.getRefreshToken()));
//
//            } else {
//                throw new Exception("Login Failed.!");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            e.getMessage();
//        }
//        return successResponse;
//    }
}

