package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    RegisterResponse register(RegisterRequest userRequest);
}
