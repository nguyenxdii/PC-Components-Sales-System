package com.diiexe.pcsalessystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SectionRequest {
    private String name;
    private String type; // COLLECTION | FLASH_SALE | NEW_ARRIVAL
    private Integer displayOrder;
    private Boolean isActive;
    
    // Dùng cho FLASH_SALE
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Boolean hasDiscount;
    private String discountType;
    private Double discountValue;

    private List<Long> productIds; // Danh sách ID sản phẩm muốn thêm vào ngay khi tạo
}
