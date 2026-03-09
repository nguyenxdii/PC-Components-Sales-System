package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.CategoryRequest;
import com.diiexe.pcsalessystem.entity.Category;
import com.diiexe.pcsalessystem.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam("name") String name,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Vui lòng tải icon danh mục đính kèm"));
            }
            if (name == null || name.trim().isEmpty()) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Tên danh mục không được để trống"));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(name, slug, isActive, file));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    // PUT /api/categories/{id}
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestParam("name") String name,
                                    @RequestParam(value = "slug", required = false) String slug,
                                    @RequestParam(value = "isActive", required = false) Boolean isActive,
                                    @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            if (name == null || name.trim().isEmpty()) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Tên danh mục không được để trống"));
            }
            return ResponseEntity.ok(categoryService.update(id, name, slug, isActive, file));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

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
            if (e.getMessage() != null && e.getMessage().contains("sản phẩm")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}