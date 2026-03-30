package com.diiexe.pcsalessystem.dto;

import lombok.Data;

@Data
public class CartRequest {
    private Long productId;
    private Integer quantity;
    private Long userId; // For now manually pass, later use SecurityContext
}
