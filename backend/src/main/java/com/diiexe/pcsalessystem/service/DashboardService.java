package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {
 
    @Autowired
    private OrderRepository orderRepository;
 
    @Autowired
    private UserRepository userRepository;
 
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VoucherRepository voucherRepository;
 
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        
        // Tính tổng doanh thu từ các đơn hàng đã thanh toán hoặc đang xử lý
        Double totalRevenue = orderRepository.findAll().stream()
                .filter(o -> "PROCESSING".equals(o.getStatus()) || "DELIVERED".equals(o.getStatus()) || "SHIPPED".equals(o.getStatus()))
                .mapToDouble(o -> o.getFinalPrice() != null ? o.getFinalPrice() : 0.0)
                .sum();

        stats.put("totalOrders", totalOrders);
        stats.put("totalUsers", totalUsers);
        stats.put("totalProducts", totalProducts);
        stats.put("totalRevenue", totalRevenue);
        
        // --- DATA CHO BIỂU ĐỒ ---
        // 1. Doanh thu theo tháng (6 tháng gần nhất)
        java.util.List<Map<String, Object>> monthlyRevenue = new java.util.ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            java.time.LocalDateTime nextMonthStart = monthStart.plusMonths(1);
            
            Double rev = orderRepository.findAll().stream()
                    .filter(o -> (o.getCreatedAt().isAfter(monthStart) || o.getCreatedAt().isEqual(monthStart)) && o.getCreatedAt().isBefore(nextMonthStart))
                    .filter(o -> !"CANCELLED".equals(o.getStatus()))
                    .mapToDouble(o -> o.getFinalPrice() != null ? o.getFinalPrice() : 0.0)
                    .sum();
            
            Map<String, Object> data = new HashMap<>();
            data.put("name", monthStart.getMonth().toString().substring(0, 3));
            data.put("revenue", rev);
            monthlyRevenue.add(data);
        }
        stats.put("monthlyRevenue", monthlyRevenue);

        // 2. Sản phẩm bán chạy (Top 5)
        java.util.List<Map<String, Object>> topProducts = orderRepository.findAll().stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .flatMap(o -> o.getOrderDetails().stream())
                .collect(java.util.stream.Collectors.groupingBy(
                        od -> od.getProduct().getName(),
                        java.util.stream.Collectors.summingInt(com.diiexe.pcsalessystem.entity.OrderDetail::getQuantity)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", e.getKey());
                    data.put("sales", e.getValue());
                    return data;
                })
                .collect(java.util.stream.Collectors.toList());
        stats.put("topProducts", topProducts);

        // --- HỆ THỐNG THÔNG BÁO ---
        java.util.List<Map<String, Object>> notifications = new java.util.ArrayList<>();
        java.time.LocalDateTime oneDayAgo = now.minusDays(1);

        // 1. Đơn hàng mới (trong 24h qua)
        orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(oneDayAgo))
                .forEach(o -> {
                    Map<String, Object> note = new HashMap<>();
                    note.put("type", "ORDER");
                    note.put("message", "Có đơn hàng mới #" + o.getOrderCode() + " từ " + (o.getUser() != null ? o.getUser().getFullName() : "Khách ẩn danh"));
                    note.put("target", "/admin/orders?search=" + o.getOrderCode());
                    notifications.add(note);
                });
        
        // 2. Sản phẩm sắp hết hàng (< 10)
        productRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() < 10)
                .forEach(p -> {
                    Map<String, Object> note = new HashMap<>();
                    note.put("type", "STOCK");
                    note.put("message", "Sản phẩm \"" + p.getName() + "\" sắp hết hàng (Còn: " + p.getStock() + ")");
                    note.put("target", "/admin/products?search=" + p.getName());
                    notifications.add(note);
                });

        // 3. Voucher sắp hết hạn (< 24h) hoặc hết số lượng (< 5)
        java.time.LocalDateTime oneDayFromNow = now.plusDays(1);
        voucherRepository.findAll().stream()
                .forEach(v -> {
                    if (v.getQuantity() != null && v.getQuantity() < 5) {
                        Map<String, Object> note = new HashMap<>();
                        note.put("type", "VOUCHER");
                        note.put("message", "Voucher \"" + v.getCode() + "\" sắp hết số lượng (Còn: " + v.getQuantity() + ")");
                        note.put("target", "/admin/vouchers?search=" + v.getCode());
                        notifications.add(note);
                    } else if (v.getExpirationDate() != null && v.getExpirationDate().isAfter(now) && v.getExpirationDate().isBefore(oneDayFromNow)) {
                        Map<String, Object> note = new HashMap<>();
                        note.put("type", "VOUCHER");
                        note.put("message", "Voucher \"" + v.getCode() + "\" sắp hết hạn (Chưa đầy 24h nữa)");
                        note.put("target", "/admin/vouchers?search=" + v.getCode());
                        notifications.add(note);
                    }
                });
        
        stats.put("notifications", notifications);

        return stats;
    }
}
