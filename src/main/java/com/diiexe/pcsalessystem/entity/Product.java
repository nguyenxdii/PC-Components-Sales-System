package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Products")
@Data
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String sku;

    private Double price;
    private Double costPrice; // Ẩn với khách, dùng cho admin tính lãi

    private Integer stock;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Lob
    @Column(columnDefinition = "VARBINARY(MAX)")
    private byte[] image;

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
