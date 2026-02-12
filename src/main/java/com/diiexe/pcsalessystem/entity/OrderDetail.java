package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "OrderDetails")
@Data
public class OrderDetail extends BaseEntity {
    
    private Integer quantity;
    private Double priceAtPurchase; // ⚠️ Quan trọng!

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
