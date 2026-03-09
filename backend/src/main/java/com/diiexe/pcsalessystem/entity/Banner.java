package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Banners")
@Data
public class Banner extends BaseEntity {

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String link;

    @Column(columnDefinition = "int default 0")
    private Integer displayOrder = 0;

    @Column(columnDefinition = "bit default 1")
    private Boolean isActive = true;
}
