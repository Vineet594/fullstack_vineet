package com.securecms.service;

import com.securecms.dto.request.LoginRequest;
import com.securecms.dto.request.RefreshTokenRequest;
import com.securecms.dto.request.RegisterRequest;
import com.securecms.dto.response.LoginResponse;
import com.securecms.dto.response.UserResponse;
import com.securecms.entity.RefreshToken;
import com.securecms.entity.Role;
import com.securecms.entity.User;
import com.securecms.exception.InvalidTokenException;
import com.securecms.exception.UserAlreadyExistsException;
import com.securecms.repository.RefreshTokenRepository;
import com.securecms.repository.RoleRepository;
import com.securecms.repository.UserRepository;
import com.securecms.security.JwtService;
import com.securecms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EncryptionService encryptionService;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found. Please run data initializer."));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .encryptedPhone(encryptionService.encrypt(request.getPhone()))
                .role(userRole)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        // Persist refresh token
        saveRefreshToken(principal.getId(), refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        User user = storedToken.getUser();
        UserPrincipal principal = new UserPrincipal(user);

        String newAccessToken = jwtService.generateAccessToken(principal);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(requestToken) // keep same refresh token (or rotate if you prefer)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            refreshTokenRepository.deleteByUser(user);
        });
    }

    private void saveRefreshToken(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove old tokens for this user (single active refresh token policy)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(encryptionService.decrypt(user.getEncryptedPhone()))
                .role(user.getRole().getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
