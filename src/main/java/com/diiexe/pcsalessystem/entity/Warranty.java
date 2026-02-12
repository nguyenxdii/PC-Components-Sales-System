package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Warranties")
@Data
public class Warranty extends BaseEntity {
    
    private String serialNumber;
    private LocalDate startDate; // Ngày mua
    private LocalDate endDate;   // Hết hạn BH
    
    @OneToOne
    @JoinColumn(name = "order_detail_id")
    private OrderDetail orderDetail;
}
