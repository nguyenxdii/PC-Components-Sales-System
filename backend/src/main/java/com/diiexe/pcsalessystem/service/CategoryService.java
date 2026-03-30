package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.CategoryRequest;
import com.diiexe.pcsalessystem.entity.Category;
import com.diiexe.pcsalessystem.repository.BrandRepository;
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

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

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

    public Category create(CategoryRequest request, MultipartFile file) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        String generatedSlug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : SlugUtils.toSlug(request.getName());

        if (categoryRepository.existsBySlug(generatedSlug)) {
            throw new RuntimeException("Danh mục này đã tồn tại");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(generatedSlug);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha với id: " + request.getParentId()));
            category.setParent(parent);
        }

        if (request.getBrandIds() != null && !request.getBrandIds().isEmpty()) {
            category.getBrands().addAll(brandRepository.findAllById(request.getBrandIds()));
        }

        if (file != null && !file.isEmpty()) {
            try {
                String iconUrl = cloudinaryService.uploadImage(file, "pc-media/system/categories");
                category.setIconUrl(iconUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi xử lý file ảnh", e);
            }
        }

        return categoryRepository.save(category);
    }

    public Category update(Long id, CategoryRequest request, MultipartFile file) {
        Category category = getById(id);

        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }
        category.setName(request.getName());

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug();
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new RuntimeException("Danh mục này đã tồn tại");
            }
            category.setSlug(newSlug);
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(category.getId())) {
                throw new RuntimeException("Danh mục không thể làm cha của chính nó");
            }
            if (request.getParentId() == 0) {
                category.setParent(null);
            } else {
                Category parent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha với id: " + request.getParentId()));
                
                if (parent.getParent() != null && parent.getParent().getId().equals(category.getId())) {
                     throw new RuntimeException("Lặp danh mục (Cyclic dependency): Không thể đặt làm con của danh mục mà nó đang làm cha");
                }
                category.setParent(parent);
            }
        }

        if (request.getBrandIds() != null) {
            category.getBrands().clear();
            if (!request.getBrandIds().isEmpty()) {
                category.getBrands().addAll(brandRepository.findAllById(request.getBrandIds()));
            }
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        if (file != null && !file.isEmpty()) {
            try {
                String iconUrl = cloudinaryService.uploadImage(file, "pc-media/system/categories");
                category.setIconUrl(iconUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi xử lý file ảnh", e);
            }
        }

        return categoryRepository.save(category);
    }

    public void softDelete(Long id) {
        Category category = getById(id);
        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
    }

    public void hardDelete(Long id) {
        Category category = getById(id);
        
        if (productRepository.existsByCategoryId(id)) {
            throw new RuntimeException("Không thể xóa danh mục đang có sản phẩm. Vui lòng chuyển cấu hình sản phẩm trước khi xóa.");
        }
        
        if (!category.getChildren().isEmpty()) {
             throw new RuntimeException("Không thể xóa danh mục đang có danh mục con. Vui lòng xóa hoặc di chuyển các danh mục con trước.");
        }
        categoryRepository.deleteById(id);
    }
    
    // API lấy danh sách tree cho frontend Category Menu
    public List<Category> getCategoryTree() {
        // Chỉ lấy những danh mục cấp 1 (parent == null), nó sẽ tự đệ quy lấy children nhờ cấu trúc Entity
        return categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    }
}