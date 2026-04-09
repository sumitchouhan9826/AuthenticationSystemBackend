package com.myAuth.authenticationSystem.services.impl;

import com.myAuth.authenticationSystem.dtos.UserDto;
import com.myAuth.authenticationSystem.repositories.RoleRepository;
import com.myAuth.authenticationSystem.services.AuthService;
import com.myAuth.authenticationSystem.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{
     private final UserService userService;
     private final PasswordEncoder passwordEncoder;


    @Override
    public UserDto registerUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));


        return userService.createUser(userDto);

    }
}
