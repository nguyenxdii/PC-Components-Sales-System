package com.diiexe.pcsalessystem.repository;

import com.diiexe.pcsalessystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy thông báo theo trạng thái isRead
    List<Notification> findByIsReadOrderByCreatedAtDesc(Boolean isRead);
    
    // Lấy tất cả thông báo sắp xếp theo thời gian mới nhất
    List<Notification> findAllByOrderByCreatedAtDesc();
    
    // Kiểm tra xem đã có thông báo SALE_EXPIRING cho Section này chưa
    boolean existsByTargetIdAndType(Long targetId, String type);
}
