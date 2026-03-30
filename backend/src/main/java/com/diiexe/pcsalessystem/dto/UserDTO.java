package com.diiexe.pcsalessystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Boolean locked;

    public UserDTO(Long id, String email, String fullName, String role, String phoneNumber, String address, String avatarUrl) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.avatarUrl = avatarUrl;
        this.locked = false;
    }

    // Không bao gồm password vì lý do bảo mật
}
