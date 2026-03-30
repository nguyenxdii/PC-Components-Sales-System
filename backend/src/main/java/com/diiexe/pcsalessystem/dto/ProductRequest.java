package com.diiexe.pcsalessystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String sku;

    private String slug;

    @NotNull(message = "Giá gốc không được để trống")
    private Double price;

    private Double salePrice;

    private Double costPrice;

    private Boolean isActive;

    private Integer stock;

    private String description;

    // Các thông số kỹ thuật (Tùy chọn)
    private String socketType;
    private String ramType;
    private Integer wattage;
    private Integer warrantyPeriod;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Thương hiệu không được để trống")
    private Long brandId;
}
