    package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.ProfileUpdateRequest;
import com.diiexe.pcsalessystem.dto.UserDTO;
import com.diiexe.pcsalessystem.entity.User;
import com.diiexe.pcsalessystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EmailService emailService;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    public UserDTO updateProfile(Long id, ProfileUpdateRequest request) {
        User user = findById(id);

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public void requestPasswordOtp(Long id, String currentPassword) {
        User user = findById(id);

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new RuntimeException("Vui lòng nhập mật khẩu hiện tại");
        }

        boolean passwordMatch;
        if (user.getPassword() != null && user.getPassword().startsWith("$2a$")) {
            passwordMatch = passwordEncoder.matches(currentPassword, user.getPassword());
        } else {
            passwordMatch = currentPassword.equals(user.getPassword());
        }

        if (!passwordMatch) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(otp);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendPasswordChangeOtpEmail(user.getEmail(), otp);
    }

    public UserDTO confirmPasswordChange(Long id, String otp, String newPassword) {
        User user = findById(id);

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(otp)) {
            throw new RuntimeException("Mã xác thực không chính xác");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác thực đã hết hạn");
        }

        if (newPassword == null || newPassword.isEmpty() || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới không hợp lệ");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserDTO updateAvatar(Long id, MultipartFile file) throws IOException {
        User user = findById(id);
        String avatarUrl = cloudinaryService.uploadImage(file, "avatars");
        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public java.util.List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public UserDTO lockUser(Long id) {
        User user = findById(id);
        user.setLocked(true);
        return convertToDTO(userRepository.save(user));
    }

    public UserDTO unlockUser(Long id) {
        User user = findById(id);
        user.setLocked(false);
        return convertToDTO(userRepository.save(user));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getAvatarUrl()
        );
        dto.setLocked(user.getLocked());
        return dto;
    }
}
