package com.diiexe.pcsalessystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BuildPCItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildPCItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id")
    @JsonBackReference
    private BuildPC buildPC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    // Tên slot (CPU, Mainboard, v.v.)
    private String slotName;
}
