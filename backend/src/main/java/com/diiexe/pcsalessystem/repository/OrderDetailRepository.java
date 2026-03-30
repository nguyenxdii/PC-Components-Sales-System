package com.diiexe.pcsalessystem.repository;

import com.diiexe.pcsalessystem.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    boolean existsByOrderUserIdAndProductIdAndOrderStatus(Long userId, Long productId, String status);
    java.util.Optional<OrderDetail> findFirstByOrderUserIdAndProductIdAndOrderStatusOrderByOrderCreatedAtDesc(Long userId, Long productId, String status);
}
