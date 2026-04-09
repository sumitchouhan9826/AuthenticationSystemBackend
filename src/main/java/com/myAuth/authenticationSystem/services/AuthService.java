package com.myAuth.authenticationSystem.services;

import com.myAuth.authenticationSystem.dtos.UserDto;



public interface AuthService {
    UserDto registerUser(UserDto userDto);
}
