package com.myAuth.authenticationSystem.dtos;

public record LoginRequest(
        String email,
        String password
) {
}
