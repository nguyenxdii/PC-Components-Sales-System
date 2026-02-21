package com.diiexe.pcsalessystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * Bảng nối giữa Section và Product.
 * Mỗi row = "sản phẩm X xuất hiện trong tab Y".
 *
 * - Nếu Section.type = COLLECTION hoặc NEW_ARRIVAL:
 *     salePrice và discountPercent = null (hiển thị giá gốc Product.price)
 *
 * - Nếu Section.type = FLASH_SALE:
 *     salePrice và discountPercent được điền để hiển thị badge giảm giá
 *     và giá sau khi sale.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "SectionProducts",
    // Đảm bảo 1 sản phẩm không bị add trùng vào cùng 1 section
    uniqueConstraints = @UniqueConstraint(columnNames = {"section_id", "product_id"})
)
@Data
public class SectionProduct extends BaseEntity {

    /**
     * Tab chứa sản phẩm này.
     * CON ĐÓNG: Tránh vòng lặp vô tận khi serialize Section → SectionProduct → Section.
     */
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    /**
     * Sản phẩm được gắn vào tab.
     */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Thứ tự hiển thị sản phẩm trong tab (0, 1, 2...).
     * Để admin sắp xếp: sản phẩm nào nổi bật thì displayOrder nhỏ hơn.
     */
    private Integer displayOrder = 0;

    // ---- Chỉ dùng khi Section.type = FLASH_SALE ----

    /**
     * Giá Flash Sale của sản phẩm này trong đợt sale.
     * Null nếu tab không phải Flash Sale.
     */
    private Double salePrice;

    /**
     * % giảm giá để hiển thị badge "-10%".
     * Có thể tính tự động: (product.price - salePrice) / product.price * 100
     * Hoặc admin nhập tay.
     * Null nếu tab không phải Flash Sale.
     */
    private Integer discountPercent;

    /**
     * Số lượng tồn kho riêng cho đợt Flash Sale (tùy chọn).
     * Null = không giới hạn, dùng stock của Product như bình thường.
     */
    private Integer flashSaleStock;
}
