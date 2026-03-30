package com.diiexe.pcsalessystem.dto;

import lombok.Data;

@Data
public class MoMoPaymentResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private Long responseTime;
    private String message;
    private Integer resultCode;
    private String payUrl;
    private String qrCodeUrl;
    private String deeplink;
}
