package com.diiexe.pcsalessystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Orders")
@Data
public class Order extends BaseEntity {
    
    private String orderCode; // #ORD-001

    private Double totalPrice; // Tổng tiền hàng
    private Double finalPrice; // Sau khi giảm giá

    private String status; // PENDING, SHIPPING, COMPLETED, CANCELLED

    private String shippingAddress;
    private String receiverPhone;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Bidirectional: 1 Đơn hàng có nhiều chi tiết sản phẩm
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // Bidirectional: 1 Đơn hàng có 1 thông tin thanh toán
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Payment payment;
}
