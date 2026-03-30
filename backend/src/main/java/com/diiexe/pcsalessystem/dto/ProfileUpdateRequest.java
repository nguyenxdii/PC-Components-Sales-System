package com.diiexe.pcsalessystem.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private String address;
    private String currentPassword;
    private String newPassword;
}
