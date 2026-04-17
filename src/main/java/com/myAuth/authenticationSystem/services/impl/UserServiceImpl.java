package com.myAuth.authenticationSystem.services.impl;

import com.myAuth.authenticationSystem.config.AppConstants;
import com.myAuth.authenticationSystem.dtos.UserDto;
import com.myAuth.authenticationSystem.entities.Provider;
import com.myAuth.authenticationSystem.entities.Role;
import com.myAuth.authenticationSystem.entities.User;
import com.myAuth.authenticationSystem.exceptions.ResourceNotFoundException;
import com.myAuth.authenticationSystem.helpers.UserHelper;
import com.myAuth.authenticationSystem.repositories.RoleRepository;
import com.myAuth.authenticationSystem.repositories.UserRepository;
import com.myAuth.authenticationSystem.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;


    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if(userDto.getEmail()== null || userDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is required");
        }
        if(userRepository.existsByEmail((userDto.getEmail()))){
            throw new IllegalArgumentException("Email already exists");
        }
        User user = modelMapper.map(userDto , User.class);
        user.setProvider(userDto.getProvider() !=null ? userDto.getProvider() : Provider.LOCAL);
        user.setEnabled(true);

        Role role = roleRepository.findByName(AppConstants.GUEST_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        
        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser , UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
       User user =  userRepository
               .findByEmail(email)
               .orElseThrow(()->new ResourceNotFoundException("User not found with given email id "));

        return modelMapper.map(user , UserDto.class);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(() -> new ResourceNotFoundException("User not found with given Id"));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User existingUser = userRepository.findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given Id"));
        if(userDto.getName()!= null) existingUser.setName(userDto.getName());
        if(userDto.getImage()!= null) existingUser.setImage(userDto.getImage());
        if(userDto.getProvider()!= null) existingUser.setProvider(userDto.getProvider());
        if(userDto.getPassword()!= null) existingUser.setPassword(userDto.getPassword());
        existingUser.setEnabled(userDto.isEnabled());
        existingUser.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);

    }

    @Override
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uId).orElseThrow(() -> new ResourceNotFoundException("User not found with given Id"));
        userRepository.delete(user);

    }

    @Override
    @Transactional
    public List<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}
