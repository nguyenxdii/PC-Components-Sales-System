package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.CategoryRequest;
import com.diiexe.pcsalessystem.entity.Category;
import com.diiexe.pcsalessystem.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // GET /api/categories            → cho khách (active)
    // GET /api/categories?all=true   → cho admin (tất cả)
    @GetMapping
    public ResponseEntity<List<Category>> getAll(
            @RequestParam(defaultValue = "false") boolean all) {
        return ResponseEntity.ok(all ? categoryService.getAll() : categoryService.getAllActive());
    }

    // GET /api/categories/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(categoryService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // POST /api/categories
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    // PUT /api/categories/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody CategoryRequest request) {
        try {
            return ResponseEntity.ok(categoryService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /api/categories/{id}             → ẩn (soft delete)
    // DELETE /api/categories/{id}?hard=true   → xóa hẳn
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestParam(defaultValue = "false") boolean hard) {
        try {
            if (hard) {
                categoryService.hardDelete(id);
                return ResponseEntity.ok(Map.of("message", "Đã xóa danh mục"));
            }
            categoryService.softDelete(id);
            return ResponseEntity.ok(Map.of("message", "Đã ẩn danh mục"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}