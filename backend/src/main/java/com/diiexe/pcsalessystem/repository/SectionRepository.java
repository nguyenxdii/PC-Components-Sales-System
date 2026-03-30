package com.diiexe.pcsalessystem.repository;

import com.diiexe.pcsalessystem.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    
    // Lấy các Section đang hoạt động, sắp xếp theo displayOrder
    List<Section> findByIsActiveOrderByDisplayOrderAsc(Boolean isActive);
    
    Optional<Section> findBySlug(String slug);
    
    // Truy vấn các Section Flash Sale sắp hết hạn (trong vòng 24h tới) chưa được thông báo
    @Query("SELECT s FROM Section s WHERE s.type = 'FLASH_SALE' AND s.isActive = true " +
           "AND s.endAt > :now AND s.endAt < :tomorrow")
    List<Section> findExpiringFlashSales(LocalDateTime now, LocalDateTime tomorrow);
}
