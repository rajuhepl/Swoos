package com.example.swoos.configure;

import com.example.swoos.model.User;
import com.example.swoos.service.Authservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private Authservice authService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;

        try {
            user = authService.getUser(username);
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(e.getMessage()));
        }
        List<GrantedAuthority> listRole = new ArrayList<GrantedAuthority>();

        return new org.springframework.security.core.userdetails.User
                (user.getUsername(), user.getPassword(), listRole);
    }

}
