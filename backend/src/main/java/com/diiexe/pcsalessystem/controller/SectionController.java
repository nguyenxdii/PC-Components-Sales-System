package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.SectionRequest;
import com.diiexe.pcsalessystem.dto.SectionResponse;
import com.diiexe.pcsalessystem.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@CrossOrigin("*")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    // Public API dành cho trang chủ
    @GetMapping("/active")
    public ResponseEntity<List<SectionResponse>> getActiveSections() {
        return ResponseEntity.ok(sectionService.getActiveSections());
    }

    // Admin APIs
    @GetMapping
    public ResponseEntity<List<SectionResponse>> getAllSections() {
        return ResponseEntity.ok(sectionService.getAllSections());
    }

    @PostMapping
    public ResponseEntity<SectionResponse> create(@RequestBody SectionRequest request) {
        return ResponseEntity.ok(sectionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionResponse> update(@PathVariable Long id, @RequestBody SectionRequest request) {
        return ResponseEntity.ok(sectionService.update(id, request));
    }

    @PutMapping("/reorder")
    public ResponseEntity<String> reorder(@RequestBody List<Long> sectionIds) {
        sectionService.reorder(sectionIds);
        return ResponseEntity.ok("Sắp xếp lại thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        sectionService.delete(id);
        return ResponseEntity.ok("Xóa section thành công");
    }

    @PostMapping("/{id}/products/{productId}")
    public ResponseEntity<SectionResponse> addProduct(
            @PathVariable Long id, 
            @PathVariable Long productId) {
        return ResponseEntity.ok(sectionService.addProductToSection(id, productId));
    }

    @DeleteMapping("/{id}/products/{productId}")
    public ResponseEntity<SectionResponse> removeProduct(
            @PathVariable Long id, 
            @PathVariable Long productId) {
        return ResponseEntity.ok(sectionService.removeProductFromSection(id, productId));
    }
}
