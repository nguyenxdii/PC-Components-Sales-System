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

    public Category create(String name, String slug, Boolean isActive, Long parentId, MultipartFile file) {
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

        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha với id: " + parentId));
            category.setParent(parent);
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

    public Category update(Long id, String name, String slug, Boolean isActive, Long parentId, MultipartFile file) {
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

        if (parentId != null) {
            if (parentId.equals(category.getId())) {
                throw new RuntimeException("Danh mục không thể làm cha của chính nó");
            }
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha với id: " + parentId));
            
            // Check to prevent cyclic dependency : parent's parent cannot be this category (Basic check)
            if (parent.getParent() != null && parent.getParent().getId().equals(category.getId())) {
                 throw new RuntimeException("Lặp danh mục (Cyclic dependency): Không thể đặt làm con của danh mục mà nó đang làm cha");
            }
            category.setParent(parent);
        } else {
             // If parentId is explicitly null in a request that supports clearing parent (you may want a separate flag for this, but for now assuming null means no change or root)
             // Alternatively, you can add a param like `clearParent`. We will assume if it's passed as null, we don't change it, unless specified.
             // If you want to be able to move a subcategory to root, we need to handle that.
             // Let's assume if it is explicitly passed as 0 or something, we clear it. Let's use parentId == 0 to move to root.
        }

        if (parentId != null && parentId == 0) {
            category.setParent(null);
        }

        if (isActive != null) {
            category.setIsActive(isActive);
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