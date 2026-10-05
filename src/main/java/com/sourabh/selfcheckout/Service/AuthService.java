package com.sourabh.selfcheckout.Service;

import com.sourabh.selfcheckout.Dto.AuthResponse;
import com.sourabh.selfcheckout.Dto.LoginRequest;
import com.sourabh.selfcheckout.Dto.RegisterRequest;
import com.sourabh.selfcheckout.Entity.Customer;
import com.sourabh.selfcheckout.Entity.User;
import com.sourabh.selfcheckout.Repo.CustomerRepository;
import com.sourabh.selfcheckout.Repo.UserRepository;
import com.sourabh.selfcheckout.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ========================
    // REGISTER
    // ========================
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(User.Role.USER);
        user.setActive(true);

        userRepository.save(user);

        customerRepository.findByEmail(user.getEmail()).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setFullName(user.getFullName());
            customer.setEmail(user.getEmail());
            customer.setPhone(user.getPhoneNumber());
            customer.setCreatedAt(LocalDateTime.now());
            return customerRepository.save(customer);
        });

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userId(String.valueOf(user.getId()))
                .message("User registered successfully")
                .build();
    }

    // ========================
    // LOGIN
    // ========================
    public AuthResponse login(LoginRequest request) {

        // This throws BadCredentialsException if wrong password,
        // or DisabledException if isEnabled() = false.
        // Both are now handled correctly in GlobalExceptionHandler.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userId(String.valueOf(user.getId()))
                .message("Login successful")
                .build();
    }
}
