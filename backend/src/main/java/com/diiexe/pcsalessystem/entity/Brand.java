package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Brands")
@Data
public class Brand extends BaseEntity {

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    /**
     * Slug dùng cho URL: /brands/intel, /brands/amd
     */
    @Column(unique = true)
    private String slug;

    @Column(length = 1000)
    private String logoUrl;

    /**
     * Ẩn/hiện thương hiệu mà không cần xóa khỏi DB.
     */
    private Boolean isActive = true;
}
