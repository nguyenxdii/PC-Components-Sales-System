package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Payments")
@Data
public class Payment extends BaseEntity {
    
    private String paymentMethod; // COD, BANK_TRANSFER
    private Double amount;
    private String transactionCode;
    
    private String status; // UNPAID, PAID, FAILED
    
    private LocalDateTime paidAt;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
