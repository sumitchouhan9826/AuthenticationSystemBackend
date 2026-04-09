package com.myAuth.authenticationSystem.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {
    private String email;
    private String name;
    private String password;
    private String image;
}