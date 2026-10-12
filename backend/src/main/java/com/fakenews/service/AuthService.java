package com.fakenews.service;

import com.fakenews.dto.AuthResponse;
import com.fakenews.dto.LoginRequest;
import com.fakenews.dto.RegisterRequest;
import com.fakenews.dto.UserSummaryResponse;
import com.fakenews.exception.BadRequestException;
import com.fakenews.model.User;
import com.fakenews.repository.UserRepository;
import com.fakenews.security.JwtTokenProvider;
import com.fakenews.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with email " + normalizedEmail + " already exists");
        }

        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                "ROLE_USER"
        );

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                tokenProvider.getExpirationMs()
        );
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(principal.getId(), principal.getEmail(), "ROLE_USER");

        return new AuthResponse(
                token,
                principal.getId(),
                principal.getName(),
                principal.getEmail(),
                "ROLE_USER",
                tokenProvider.getExpirationMs()
        );
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
