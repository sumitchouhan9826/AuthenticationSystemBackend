package com.myAuth.authenticationSystem.dtos;

public record TokenResponse(
        String AccesToken,
        String RefreshToken,
        long expiresIn,
        String tokenType,
        UserDto user
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn,  UserDto user) {
        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
    }

}
