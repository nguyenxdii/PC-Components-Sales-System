package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.entity.Voucher;
import com.diiexe.pcsalessystem.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
@CrossOrigin(origins = "*")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.saveVoucher(voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Long id, @RequestBody Voucher voucherDetails) {
        return voucherService.getVoucherById(id).map(v -> {
            v.setCode(voucherDetails.getCode());
            v.setType(voucherDetails.getType());
            v.setIsFreeShip(voucherDetails.getIsFreeShip());
            v.setValue(voucherDetails.getValue());
            v.setMaxDiscountValue(voucherDetails.getMaxDiscountValue());
            v.setMinOrderValue(voucherDetails.getMinOrderValue());
            v.setQuantity(voucherDetails.getQuantity());
            v.setStartDate(voucherDetails.getStartDate());
            v.setExpirationDate(voucherDetails.getExpirationDate());
            v.setIsActive(voucherDetails.getIsActive());
            return ResponseEntity.ok(voucherService.saveVoucher(v));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok().build();
    }
}
