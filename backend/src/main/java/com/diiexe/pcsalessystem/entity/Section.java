package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý các "Tab" / Section hiển thị trên trang chủ.
 *
 * Ví dụ:
 *   - name = "PC AMD GAMING",  type = COLLECTION  → Tab sản phẩm linh kiện AMD
 *   - name = "Flash Sale T3",  type = FLASH_SALE  → Tab giảm giá có thời hạn
 *   - name = "Hàng mới về",    type = NEW_ARRIVAL → Tab sản phẩm mới nhất
 *
 * Mỗi Section có thể được gắn nhiều SectionProduct (sản phẩm bên trong tab đó).
 * Admin tạo bao nhiêu Section thì trang chủ hiển thị bấy nhiêu Tab.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Sections")
@Data
public class Section extends BaseEntity {

    /**
     * Tên hiển thị của tab.
     * Ví dụ: "PC AMD GAMING", "Flash Sale", "Hàng mới về"
     */
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    /**
     * Loại tab – phân biệt để FE áp dụng UI khác nhau.
     *
     * COLLECTION  → Tập hợp sản phẩm thông thường (PC AMD, PC Mini, ...)
     * FLASH_SALE  → Giảm giá có thời hạn, hiển thị countdown
     * NEW_ARRIVAL → Hàng mới về
     */
    @Column(nullable = false)
    private String type; // COLLECTION | FLASH_SALE | NEW_ARRIVAL

    /**
     * Slug dùng cho URL "Xem tất cả" -> /sections/pc-amd-gaming
     */
    @Column(unique = true)
    private String slug;

    /**
     * Thứ tự hiển thị trên trang chủ (số nhỏ hơn = lên trên).
     */
    private Integer displayOrder = 0;

    /**
     * Bật/tắt hiển thị section này trên trang chủ.
     */
    private Boolean isActive = true;

    // ---- Chỉ dùng khi type = FLASH_SALE ----

    /** Thời điểm bắt đầu Flash Sale. Null nếu không phải Flash Sale. */
    private LocalDateTime startAt;

    /** Thời điểm kết thúc Flash Sale. Null nếu không phải Flash Sale. */
    private LocalDateTime endAt;

    // ---- Relationship ----

    /**
     * Danh sách sản phẩm bên trong tab này.
     * CHA MỞ: Để lấy được list sản phẩm khi load trang chủ.
     */
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SectionProduct> sectionProducts = new ArrayList<>();
}
