package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Categories")
@Data
public class Category extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "VARBINARY(MAX)")
    private byte[] icon;
}
