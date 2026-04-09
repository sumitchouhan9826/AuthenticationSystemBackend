package com.myAuth.authenticationSystem.dtos;

import com.myAuth.authenticationSystem.entities.Provider;
import com.myAuth.authenticationSystem.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String email;
    private String name;
    private Provider provider;
    private Set<String> roles;
    private String image;
    private Boolean enabled;
    private String password;

    public static void setRoles(Role role) {
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }





}