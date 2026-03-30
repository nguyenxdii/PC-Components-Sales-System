package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.entity.Voucher;
import com.diiexe.pcsalessystem.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*")
public class PublicVoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping("/check/{code}")
    public ResponseEntity<?> checkVoucher(@PathVariable String code, @RequestParam Double totalAmount) {
        return voucherService.findByCode(code).map(voucher -> {
            // Validation logic (similar to OrderService)
            LocalDateTime now = LocalDateTime.now();
            if (Boolean.FALSE.equals(voucher.getIsActive())) {
                return ResponseEntity.badRequest().body("Mã giảm giá đã bị vô hiệu hóa");
            }
            if (voucher.getQuantity() != null && voucher.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("Mã giảm giá đã hết lượt sử dụng");
            }
            if (voucher.getExpirationDate() != null && voucher.getExpirationDate().isBefore(now)) {
                return ResponseEntity.badRequest().body("Mã giảm giá đã hết hạn");
            }
            if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
                return ResponseEntity.badRequest().body("Mã giảm giá chưa đến ngày hiệu lực");
            }
            if (voucher.getMinOrderValue() != null && totalAmount < voucher.getMinOrderValue()) {
                return ResponseEntity.badRequest().body("Đơn hàng tối thiểu " + voucher.getMinOrderValue().longValue() + "đ để áp dụng mã này");
            }

            // Calculate discount
            double discount = 0;
            boolean isFreeShip = Boolean.TRUE.equals(voucher.getIsFreeShip());
            
            if (!isFreeShip) {
                if ("PERCENT".equals(voucher.getType())) {
                    discount = (totalAmount * voucher.getValue()) / 100;
                    if (voucher.getMaxDiscountValue() != null && discount > voucher.getMaxDiscountValue()) {
                        discount = voucher.getMaxDiscountValue();
                    }
                } else if ("FIXED".equals(voucher.getType())) {
                    discount = voucher.getValue();
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("code", voucher.getCode());
            result.put("discountAmount", discount);
            result.put("isFreeShip", isFreeShip);
            result.put("type", voucher.getType());
            result.put("value", voucher.getValue());
            
            return ResponseEntity.ok(result);
        }).orElse(ResponseEntity.badRequest().body("Mã giảm giá không tồn tại"));
    }
}
