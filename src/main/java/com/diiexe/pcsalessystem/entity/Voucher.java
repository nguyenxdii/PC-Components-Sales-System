package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Vouchers")
@Data
public class Voucher extends BaseEntity {
    
    @Column(unique = true, nullable = false)
    private String code; // SALE50

    private String type; // PERCENT, FIXED
    private Double value; // 10% hoặc 50000 VNĐ

    private Double maxDiscountValue; // Giảm tối đa
    private Double minOrderValue;    // Đơn tối thiểu

    private Integer quantity;
    private LocalDateTime expirationDate;
}
