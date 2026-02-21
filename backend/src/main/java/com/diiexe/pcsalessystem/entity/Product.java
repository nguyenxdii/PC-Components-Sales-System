package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Products")
@Data
public class Product extends BaseEntity {

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(unique = true)
    private String sku;

    /**
     * Slug dùng cho URL SEO: /products/cpu-intel-core-i9-13900k
     */
    @Column(unique = true)
    private String slug;

    private Double price;        // Giá gốc (thường hiển thị gạch ngang)
    private Double salePrice;    // Giá khuyến mãi thường ngày (null = không giảm giá)
    private Double costPrice;    // Ẩn với khách, dùng cho admin tính lãi

    /**
     * Ẩn/hiện sản phẩm mà không cần xóa khỏi DB.
     * false = ẩn khỏi trang khách hàng, true = hiển thị (mặc định).
     */
    private Boolean isActive = true;

    private Integer stock;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // Bidirectional: 1 Product có nhiều ảnh
    // CHA MỞ: Để lấy được list ảnh phụ cho gallery
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

    // Thông số kỹ thuật (Build PC)
    private String socketType; // LGA1700
    private String ramType;    // DDR4, DDR5
    private Integer wattage;   // 65W
    private Integer warrantyPeriod; // 36 tháng

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;
}
