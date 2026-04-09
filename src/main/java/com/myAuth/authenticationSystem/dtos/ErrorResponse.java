package com.myAuth.authenticationSystem.dtos;


import org.springframework.http.HttpStatus;

public record ErrorResponse (
    String message,
    HttpStatus status
){

}