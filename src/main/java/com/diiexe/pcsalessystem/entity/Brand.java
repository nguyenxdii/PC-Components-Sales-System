package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Brands")
@Data
public class Brand extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "VARBINARY(MAX)")
    private byte[] logo;
}
