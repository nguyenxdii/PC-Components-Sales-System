package com.diiexe.pcsalessystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String slug;
    private Double price;
    private Double salePrice;
    private Double costPrice;
    private Boolean isActive;
    private Integer stock;
    private String description;
    private String imageUrl;
    private String mainImageUrl; // Alias for frontend compatibility
    
    private String socketType;
    private String ramType;
    private Integer wattage;
    private Integer warrantyPeriod;

    private CategoryDTO category;
    private BrandDTO brand;
    
    // Thêm list ảnh phụ nếu cần
    private List<String> secondaryImages;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String slug;
    }

    @Data
    public static class BrandDTO {
        private Long id;
        private String name;
        private String slug;
        private String logoUrl;
    }
}
