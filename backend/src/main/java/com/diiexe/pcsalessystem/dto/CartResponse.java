package com.diiexe.pcsalessystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long id;
    private List<CartItemDTO> items;
    private Double totalAmount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productSlug;
        private String productImage;
        private Double price;
        private Integer quantity;
        private Double subtotal;
    }
}
