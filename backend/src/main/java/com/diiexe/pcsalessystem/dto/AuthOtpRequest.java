package com.diiexe.pcsalessystem.dto;

import lombok.Data;

@Data
public class AuthOtpRequest {
    private String email;
    private String otp;
}
