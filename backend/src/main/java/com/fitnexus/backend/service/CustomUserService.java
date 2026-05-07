package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Users> users = userRepository.findByEmail(email);

        if (users == null) {
            throw new UsernameNotFoundException("Users not found");
        }

        return new org.springframework.security.core.userdetails.User(
                users.get().getEmail(),
                users.get().getPassword(),
                Collections.emptyList() // later we add roles
        );
    }
}
