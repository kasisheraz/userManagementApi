package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.LoginRequest;
import com.fincore.usermgmt.dto.LoginResponse;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.UserStatus;
import com.fincore.usermgmt.repository.UserRepository;
import com.fincore.usermgmt.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${security.max-login-attempts}")
    private int maxLoginAttempts;

    @Value("${security.account-lock-duration}")
    private int lockDuration;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new LockedException("Account is not active");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("Account is locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        resetFailedAttempts(user);
        String token = jwtUtil.generateToken(user.getUsername());

        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole().getName());
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusSeconds(lockDuration));
            user.setStatus(UserStatus.LOCKED);
        }
        
        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
