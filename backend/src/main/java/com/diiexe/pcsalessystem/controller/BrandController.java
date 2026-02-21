package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.BrandRequest;
import com.diiexe.pcsalessystem.entity.Brand;
import com.diiexe.pcsalessystem.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin(origins = "*")
public class BrandController {

    @Autowired
    private BrandService brandService;

    // GET /api/brands            → cho khách (active)
    // GET /api/brands?all=true   → cho admin (tất cả)
    @GetMapping
    public ResponseEntity<List<Brand>> getAll(
            @RequestParam(defaultValue = "false") boolean all) {
        return ResponseEntity.ok(all ? brandService.getAll() : brandService.getAllActive());
    }

    // GET /api/brands/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(brandService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // POST /api/brands
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BrandRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    // PUT /api/brands/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody BrandRequest request) {
        try {
            return ResponseEntity.ok(brandService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /api/brands/{id}             → ẩn (soft delete)
    // DELETE /api/brands/{id}?hard=true   → xóa hẳn
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestParam(defaultValue = "false") boolean hard) {
        try {
            if (hard) {
                brandService.hardDelete(id);
                return ResponseEntity.ok(Map.of("message", "Đã xóa thương hiệu"));
            }
            brandService.softDelete(id);
            return ResponseEntity.ok(Map.of("message", "Đã ẩn thương hiệu"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}