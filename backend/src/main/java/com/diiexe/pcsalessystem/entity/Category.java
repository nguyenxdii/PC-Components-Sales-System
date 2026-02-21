package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Categories")
@Data
public class Category extends BaseEntity {

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    /**
     * Slug dùng cho URL: /categories/cpu, /categories/ram
     * Unique để không bị trùng lặp đường dẫn.
     */
    @Column(unique = true)
    private String slug;

    @Lob
    @Column(columnDefinition = "VARBINARY(MAX)")
    private byte[] icon;

    /**
     * Ẩn/hiện danh mục mà không cần xóa khỏi DB.
     * false = ẩn, true = hiển thị (mặc định).
     */
    private Boolean isActive = true;

    private Integer displayOrder = 0;
}
