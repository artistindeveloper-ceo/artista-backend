package com.artist_in.app.service;

import com.artist_in.app.dto.auth.AuthResponse;
import com.artist_in.app.dto.auth.LoginRequest;
import com.artist_in.app.dto.auth.RegisterRequest;


public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshTokenValue);

    void logout(String refreshTokenValue);


}