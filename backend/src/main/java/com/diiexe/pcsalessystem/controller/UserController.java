package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.ProfileUpdateRequest;
import com.diiexe.pcsalessystem.dto.UserDTO;
import com.diiexe.pcsalessystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserDTO> updateProfile(@PathVariable Long id, @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    @PostMapping("/{id}/request-password-otp")
    public ResponseEntity<Map<String, String>> requestPasswordOtp(@PathVariable Long id, @RequestBody Map<String, String> request) {
        userService.requestPasswordOtp(id, request.get("currentPassword"));
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mã OTP đã được gửi đến email của bạn");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm-password-change")
    public ResponseEntity<UserDTO> confirmPasswordChange(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(userService.confirmPasswordChange(id, request.get("otp"), request.get("newPassword")));
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<UserDTO> updateAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(userService.updateAvatar(id, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserInfo(@PathVariable Long id) {
        return ResponseEntity.ok(convertToDTO(userService.findById(id)));
    }

    // ADMIN ENDPOINTS
    @GetMapping("/admin/all")
    public ResponseEntity<java.util.List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/admin/{id}/lock")
    public ResponseEntity<UserDTO> lockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.lockUser(id));
    }

    @PutMapping("/admin/{id}/unlock")
    public ResponseEntity<UserDTO> unlockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unlockUser(id));
    }

    private UserDTO convertToDTO(com.diiexe.pcsalessystem.entity.User user) {
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
