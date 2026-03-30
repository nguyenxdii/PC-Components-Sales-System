package com.diiexe.pcsalessystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SectionResponse {
    private Long id;
    private String name;
    private String type;
    private String slug;
    private Integer displayOrder;
    private Boolean isActive;
    
    // Flash Sale
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    
    private Boolean hasDiscount;
    private String discountType;
    private Double discountValue;
    
    private List<SectionProductResponse> products;
    
    @Data
    public static class SectionProductResponse {
        private Long productId;
        private String name;
        private String slug;
        private String mainImageUrl;
        private Double originalPrice;
        private Double salePrice;
        private Integer discountPercent;
        private Integer displayOrder;
    }
}
