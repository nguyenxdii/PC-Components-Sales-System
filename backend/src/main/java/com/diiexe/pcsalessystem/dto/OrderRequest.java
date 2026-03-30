package com.diiexe.pcsalessystem.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private String shippingAddress;
    private String receiverPhone;
    private String paymentMethod; // COD, MOMO
    private String voucherCode;
}
