package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.CategoryRequest;
import com.diiexe.pcsalessystem.entity.Category;
import com.diiexe.pcsalessystem.repository.CategoryRepository;
import com.diiexe.pcsalessystem.repository.ProductRepository;
import com.diiexe.pcsalessystem.util.SlugUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Category> getAllActive() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public List<Category> getAll() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));
    }

    public Category create(String name, String slug, Boolean isActive, MultipartFile file) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        String generatedSlug = (slug != null && !slug.isBlank())
                ? slug
                : SlugUtils.toSlug(name);

        if (categoryRepository.existsBySlug(generatedSlug)) {
            throw new RuntimeException("Danh mục này đã tồn tại");
        }

        Category category = new Category();
        category.setName(name);
        category.setSlug(generatedSlug);
        category.setIsActive(isActive != null ? isActive : true);

        if (file != null && !file.isEmpty()) {
            try {
                category.setIcon(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi xử lý file ảnh", e);
            }
        }

        return categoryRepository.save(category);
    }

    public Category update(Long id, String name, String slug, Boolean isActive, MultipartFile file) {
        Category category = getById(id);

        if (!category.getName().equals(name)
                && categoryRepository.existsByName(name)) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }
        category.setName(name);

        if (slug != null && !slug.isBlank()) {
            String newSlug = slug;
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new RuntimeException("Danh mục này đã tồn tại");
            }
            category.setSlug(newSlug);
        }

        if (isActive != null) {
            category.setIsActive(isActive);
        }

        if (file != null && !file.isEmpty()) {
            try {
                category.setIcon(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi xử lý file ảnh", e);
            }
        }

        return categoryRepository.save(category);
    }

    public void softDelete(Long id) {
        Category category = getById(id);
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    public void hardDelete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy danh mục với id: " + id);
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new RuntimeException("Không thể xóa danh mục đang có sản phẩm. Vui lòng chuyển cấu hình sản phẩm trước khi xóa.");
        }
        categoryRepository.deleteById(id);
    }
}