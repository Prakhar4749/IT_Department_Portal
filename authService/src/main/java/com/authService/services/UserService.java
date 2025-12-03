package com.authService.services;


import com.authService.entities.UserInfo;
import com.authService.jwtSecurity.JwtService;
import com.authService.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    @Autowired
    private JwtService jwtService;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.encoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));


        return new UserInfoDetails(user);
    }


    public Boolean checkUserExist(String email){
        return userRepo.existsById(email);
    }
    // Add any additional methods for registering or managing users
    public String addUser(UserInfo userInfo) {
        if(checkUserExist(userInfo.getEmail())){
            return "user is already exist with same email id!";
        }
        // Encrypt password before saving
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        userRepo.save(userInfo);
        return "User added successfully!";
    }
    public String generateToken(String email){
        return jwtService.generateToken(email);
    }
    public boolean validateToken(String token) {

        return jwtService.validateToken(token);
    }


}
