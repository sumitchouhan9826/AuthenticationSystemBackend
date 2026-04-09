package com.myAuth.authenticationSystem.dtos;

import java.time.OffsetDateTime;

public record ApiError(
        int status,
        String message,
        String path,
        String error,
        OffsetDateTime timestamp
) {
        public static ApiError of(int status, String error, String message, String path) {
            return new ApiError(status,error, message, path,  OffsetDateTime.now());
        }
        public static ApiError of(int status,String error, String message, String path,boolean notDateTime) {
            return new ApiError(status,error,  message, path, null);
        }
}
