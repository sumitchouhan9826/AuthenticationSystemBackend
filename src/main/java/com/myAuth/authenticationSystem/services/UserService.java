package com.myAuth.authenticationSystem.services;

import com.myAuth.authenticationSystem.dtos.RegisterUserDto;
import com.myAuth.authenticationSystem.dtos.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto createUser(UserDto dto);

    UserDto getUserByEmail(String email);

    UserDto getUserById(String userId);

    UserDto updateUser( UserDto userDto, String userId);

    void deleteUser(String userId);

    List<UserDto> getAllUsers();
}