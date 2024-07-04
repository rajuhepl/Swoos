package com.example.swoos.configure;

import com.example.swoos.dto.UserDTO;

public class UserContextHolder {

    private static final ThreadLocal<UserDTO> USER_CONTEXT = new ThreadLocal<>();

    public static void setUserDto(UserDTO userContextDTO) {
        USER_CONTEXT.set(userContextDTO);
    }

    public static UserDTO getUserDto() {
        return USER_CONTEXT.get();
    }

    public static void clear() {
        USER_CONTEXT.remove();
    }
}