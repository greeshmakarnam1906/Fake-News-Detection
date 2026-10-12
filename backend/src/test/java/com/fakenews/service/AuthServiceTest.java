package com.fakenews.service;

import com.fakenews.dto.AuthResponse;
import com.fakenews.dto.LoginRequest;
import com.fakenews.dto.RegisterRequest;
import com.fakenews.exception.BadRequestException;
import com.fakenews.model.User;
import com.fakenews.repository.UserRepository;
import com.fakenews.security.JwtTokenProvider;
import com.fakenews.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider, authenticationManager);
    }

    @Test
    @DisplayName("Should successfully register new user and hash password with BCrypt")
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest("Alice Smith", "alice@example.com", "secret123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(101L);
            return u;
        });
        when(tokenProvider.generateToken(101L, "alice@example.com", "ROLE_USER")).thenReturn("mock-jwt-token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals("alice@example.com", res.getEmail());
        assertEquals("Alice Smith", res.getName());

        verify(userRepository).save(argThat(user ->
                passwordEncoder.matches("secret123", user.getPasswordHash())
        ));
    }

    @Test
    @DisplayName("Should throw BadRequestException if email already registered")
    void testRegisterDuplicateEmail() {
        RegisterRequest req = new RegisterRequest("Bob", "bob@example.com", "secret123");
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("bob@example.com", "secret123");
        User user = new User("Bob", "bob@example.com", "hash", "ROLE_USER");
        user.setId(202L);
        UserPrincipal principal = UserPrincipal.create(user);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(tokenProvider.generateToken(202L, "bob@example.com", "ROLE_USER")).thenReturn("login-jwt-token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("login-jwt-token", res.getToken());
        assertEquals("bob@example.com", res.getEmail());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on authentication failure")
    void testLoginFailure() {
        LoginRequest req = new LoginRequest("bob@example.com", "wrongpassword");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }
}
