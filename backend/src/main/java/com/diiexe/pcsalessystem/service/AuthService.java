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
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public LoginResponse login(LoginRequest request) {
        // Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng!"));

        if (user.getActive() == null || !user.getActive()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email!");
        }

        // 1. Kiểm tra password với BCrypt
        // Hỗ trợ cả plain text (old users) và hashed password (new users)
        boolean passwordMatch;
        if (user.getPassword().startsWith("$2a$")) {
            // Password đã hash - dùng BCrypt verify
            passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } else {
            // Password cũ chưa hash - so sánh trực tiếp
            passwordMatch = request.getPassword().equals(user.getPassword());
            
            // Nếu khớp, tự động hash lại và lưu vào DB (optional but recommended)
            if (passwordMatch) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            }
        }

        if (!passwordMatch) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng!");
        }

        // 2. Chỉ khi mật khẩu ĐÚNG mới kiểm tra tài khoản có bị khóa hay không
        if (Boolean.TRUE.equals(user.getLocked())) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!");
        }

        // Tạo token đơn giản (Trong production nên dùng JWT)
        String token = generateSimpleToken(user);

        // Tạo UserDTO
        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getAvatarUrl()
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
        user.setActive(false); // ⚠️ Chờ xác thực OTP

        // Tạo mã OTP (6 số)
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(otp);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));

        // Lưu vào database
        User savedUser = userRepository.save(user);

        // Gửi Email OTP
        System.out.println(">>> REGISTERING USER: " + savedUser.getEmail() + " | OTP: " + otp);
        emailService.sendOtpEmail(savedUser.getEmail(), otp);

        // Trả về DTO (Token lúc này có thể là null hoặc chuỗi thông báo)
        UserDTO userDTO = new UserDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getPhoneNumber(),
                savedUser.getAddress(),
                savedUser.getAvatarUrl()
        );

        return new LoginResponse("PENDING_VERIFICATION", userDTO);
    }

    public LoginResponse verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("Tài khoản đã được kích hoạt trước đó");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(otp)) {
            throw new RuntimeException("Mã xác thực không chính xác");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác thực đã hết hạn");
        }

        // Kích hoạt tài khoản
        user.setActive(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        User savedUser = userRepository.save(user);

        // Đăng nhập tự động sau khi xác thực thành công
        String token = generateSimpleToken(savedUser);
        UserDTO userDTO = new UserDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getPhoneNumber(),
                savedUser.getAddress(),
                savedUser.getAvatarUrl()
        );

        return new LoginResponse(token, userDTO);
    }

    private String generateSimpleToken(User user) {
        // Token đơn giản: "TOKEN_userId_role_timestamp"
        // Trong production, dùng JWT với secret key
        return "TOKEN_" + user.getId() + "_" + user.getRole() + "_" + System.currentTimeMillis();
    }
}
