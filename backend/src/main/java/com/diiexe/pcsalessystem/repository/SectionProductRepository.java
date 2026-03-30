package com.diiexe.pcsalessystem.repository;

import com.diiexe.pcsalessystem.entity.SectionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionProductRepository extends JpaRepository<SectionProduct, Long> {
    
    // Tìm các sản phẩm trong một section theo thứ tự hiển thị
    List<SectionProduct> findBySectionIdOrderByDisplayOrderAsc(Long sectionId);
    
    // Kiểm tra xem sản phẩm đã có trong section chưa
    boolean existsBySectionIdAndProductId(Long sectionId, Long productId);

    // Tìm một sản phẩm cụ thể trong section
    Optional<SectionProduct> findBySectionIdAndProductId(Long sectionId, Long productId);
    
    // Xóa tất cả sản phẩm khỏi 1 section (Dùng khi cập nhật)
    void deleteBySectionId(Long sectionId);
}
