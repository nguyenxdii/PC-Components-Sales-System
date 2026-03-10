package com.diiexe.pcsalessystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(length = 1000)
    private String iconUrl;

    /**
     * Ẩn/hiện danh mục mà không cần xóa khỏi DB.
     * false = ẩn, true = hiển thị (mặc định).
     */
    private Boolean isActive = true;

    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore // Tránh lỗi infinite recursion khi return JSON
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();
}
