package com.diiexe.pcsalessystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ProductImages")
@Data
public class ProductImage extends BaseEntity {
    
    @Lob
    @Column(columnDefinition = "VARBINARY(MAX)")
    private byte[] image;
    
    private String altText; // Text mô tả ảnh cho SEO
    
    private Integer displayOrder; // Thứ tự hiển thị (0, 1, 2...)
    
    private boolean isPrimary = false; // Ảnh chính
    
    // CON ĐÓNG: Tránh lặp vô tận khi lấy ảnh không lôi ngược lại cả sản phẩm
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
