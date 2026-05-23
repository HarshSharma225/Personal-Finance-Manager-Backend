package com.syfe.finance.service;

import com.syfe.finance.dto.request.LoginRequest;
import com.syfe.finance.dto.request.RegisterRequest;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.DuplicateResourceException;
import com.syfe.finance.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("+1234567890");
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        User saved = new User();
        saved.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        Map<String, Object> result = authService.register(registerRequest);

        assertEquals("User registered successfully", result.get("message"));
        assertEquals(1L, result.get("userId"));
    }

    @Test
    void register_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(httpRequest.getSession(true)).thenReturn(session);

        Map<String, String> result = authService.login(loginRequest, httpRequest);
        assertEquals("Login successful", result.get("message"));
    }

    @Test
    void logout_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);

        Map<String, String> result = authService.logout(request);
        assertEquals("Logout successful", result.get("message"));
        verify(session).invalidate();
    }

    @Test
    void logout_noSession_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        Map<String, String> result = authService.logout(request);
        assertEquals("Logout successful", result.get("message"));
    }
}
