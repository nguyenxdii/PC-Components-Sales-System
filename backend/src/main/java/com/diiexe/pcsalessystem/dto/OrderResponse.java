package com.diiexe.pcsalessystem.dto;

import lombok.Data;

@Data
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Double finalPrice;
    private String status;
    private String paymentUrl; // Dành cho MOMO
}
