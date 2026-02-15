package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.LoginRequest;
import com.diiexe.pcsalessystem.dto.LoginResponse;
import com.diiexe.pcsalessystem.dto.RegisterRequest;
import com.diiexe.pcsalessystem.dto.UserDTO;
import com.diiexe.pcsalessystem.entity.User;
import com.diiexe.pcsalessystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        // Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng!"));

        // Kiểm tra password với BCrypt
        // Hỗ trợ cả plain text (old users) và hashed password (new users)
        boolean passwordMatch;
        if (user.getPassword().startsWith("$2a$")) {
            // Password đã hash - dùng BCrypt verify
            passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } else {
            // Plain text password (old users) - so sánh trực tiếp
            passwordMatch = request.getPassword().equals(user.getPassword());
        }

        if (!passwordMatch) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng!");
        }

        // Tạo token đơn giản (Trong production nên dùng JWT)
        String token = generateSimpleToken(user);

        // Tạo UserDTO
        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );

        return new LoginResponse(token, userDTO);
    }
    
    public LoginResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        // Tạo user mới
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        
        // Hash password với BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);
        
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        
        // Note: Avatar will be set to default byte[] later when user uploads
        // For now, leave it null (user can upload later)
        
        // Set default role
        user.setRole("USER");
        user.setLocked(false);
        
        // Lưu vào database
        User savedUser = userRepository.save(user);
        
        // Generate token
        String token = generateSimpleToken(savedUser);
        
        // Return user data + token
        UserDTO userDTO = new UserDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole()
        );
        
        return new LoginResponse(token, userDTO);
    }

    private String generateSimpleToken(User user) {
        // Token đơn giản: "TOKEN_userId_role_timestamp"
        // Trong production, dùng JWT với secret key
        return "TOKEN_" + user.getId() + "_" + user.getRole() + "_" + System.currentTimeMillis();
    }
}
