package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.OrderRequest;
import com.diiexe.pcsalessystem.dto.OrderResponse;
import com.diiexe.pcsalessystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) throws Exception {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    // MoMo IPN Callback
    @PostMapping("/momo-callback")
    public ResponseEntity<Void> momoCallback(@RequestBody Map<String, Object> payload) {
        // MoMo gửi dữ liệu qua body (JSON)
        String orderId = (String) payload.get("orderId");
        String resultCode = String.valueOf(payload.get("resultCode"));
        String transId = String.valueOf(payload.get("transId"));
        
        System.out.println("MoMo Callback for Order: " + orderId + ", Result: " + resultCode + ", TransId: " + transId);
        
        orderService.updatePaymentStatus(orderId, resultCode, transId);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.diiexe.pcsalessystem.entity.Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<java.util.List<com.diiexe.pcsalessystem.entity.Order>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/check-status/{orderCode}")
    public ResponseEntity<Void> checkPaymentStatus(@PathVariable String orderCode) throws Exception {
        orderService.checkAndUpdatePaymentStatus(orderCode);
        return ResponseEntity.ok().build();
    }
    // ADMIN ENDPOINTS
    @GetMapping("/admin/all")
    public ResponseEntity<java.util.List<com.diiexe.pcsalessystem.entity.Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/admin/{id}/status")
    public ResponseEntity<com.diiexe.pcsalessystem.entity.Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
