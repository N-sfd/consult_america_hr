package com.consultamerica.hr.auth.service;

import com.consultamerica.hr.auth.dto.LoginRequest;
import com.consultamerica.hr.auth.dto.LoginResponse;
import com.consultamerica.hr.auth.dto.RegisterRequest;
import com.consultamerica.hr.auth.entity.PasswordResetToken;
import com.consultamerica.hr.auth.entity.Role;
import com.consultamerica.hr.auth.entity.User;
import com.consultamerica.hr.auth.repository.PasswordResetTokenRepository;
import com.consultamerica.hr.auth.repository.RoleRepository;
import com.consultamerica.hr.auth.repository.UserRepository;
import com.consultamerica.hr.common.exception.DuplicateEmailException;
import com.consultamerica.hr.common.exception.ResourceNotFoundException;
import com.consultamerica.hr.mail.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                        PasswordResetTokenRepository resetTokenRepository, PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException("An account with this email already exists");
        }
        Role role = roleRepository.findByNameIgnoreCase(request.role().name())
            .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + request.role().name()));

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user = userRepository.save(user);

        return toResponse(user);
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);
    }

    public void forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUser(user);
            resetToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            resetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordReset(user.getEmail(), resetLink);
        });
        // Always return success regardless of whether the email exists, to avoid account enumeration.
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }

    private LoginResponse toResponse(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        return new LoginResponse(user.getId(), user.getEmail(), user.getName(), roleName);
    }
}
