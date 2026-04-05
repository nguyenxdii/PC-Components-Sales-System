package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.ProductRequest;
import com.diiexe.pcsalessystem.dto.ProductResponse;
import com.diiexe.pcsalessystem.entity.Brand;
import com.diiexe.pcsalessystem.entity.Category;
import com.diiexe.pcsalessystem.entity.Product;
import com.diiexe.pcsalessystem.repository.BrandRepository;
import com.diiexe.pcsalessystem.repository.CategoryRepository;
import com.diiexe.pcsalessystem.repository.CategoryRepository;
import com.diiexe.pcsalessystem.repository.ProductRepository;
import com.diiexe.pcsalessystem.entity.ProductImage;
import com.diiexe.pcsalessystem.util.SlugUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public java.util.Map<String, Object> getAll(Long categoryId, String categorySlug, Long brandId, Long sectionId, Double minPrice, Double maxPrice, String socketType, String ramType, String sort, int page, int size, boolean activeOnly) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (categorySlug != null && !categorySlug.isBlank()) {
                predicates.add(cb.equal(root.get("category").get("slug"), categorySlug));
            }

            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }

            if (sectionId != null) {
                // Join với SectionProduct để lọc theo sectionId (đã map trong Product entity)
                jakarta.persistence.criteria.Join<Product, com.diiexe.pcsalessystem.entity.SectionProduct> sectionJoin = root.join("sectionProducts");
                predicates.add(cb.equal(sectionJoin.get("section").get("id"), sectionId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (socketType != null && !socketType.isBlank()) {
                predicates.add(cb.equal(root.get("socketType"), socketType));
            }

            if (ramType != null && !ramType.isBlank()) {
                predicates.add(cb.equal(root.get("ramType"), ramType));
            }

            // Lọc sản phẩm đang hoạt động (truy cập công khai) hoặc cả sản phẩm ẩn (cho Admin)
            if (activeOnly) {
                predicates.add(cb.equal(root.get("isActive"), true));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Logic sắp xếp: Ưu tiên lọc theo giá thực tế (salePrice || price)
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null) {
            switch (sort) {
                case "price_asc": 
                    // Sắp xếp theo price để ổn định
                    sortOrder = Sort.by(Sort.Direction.ASC, "price"); 
                    break;
                case "price_desc": 
                    sortOrder = Sort.by(Sort.Direction.DESC, "price"); 
                    break;
                case "oldest": 
                    sortOrder = Sort.by(Sort.Direction.ASC, "createdAt"); 
                    break;
            }
        }

        // Áp dụng phân trang: Sử dụng tham số size linh hoạt
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortOrder);
        org.springframework.data.domain.Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        List<ProductResponse> content = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", content);
        response.put("totalPages", productPage.getTotalPages());
        response.put("totalElements", productPage.getTotalElements());
        response.put("currentPage", productPage.getNumber());

        return response;
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        return mapToResponse(product);
    }

    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với slug: " + slug));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request, MultipartFile[] files) {
        if (productRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên sản phẩm đã tồn tại");
        }

        if (request.getSku() != null && !request.getSku().isBlank() && productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Mã SKU đã tồn tại");
        }

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : SlugUtils.toSlug(request.getName());

        if (productRepository.existsBySlug(slug)) {
            throw new RuntimeException("Slug '" + slug + "' đã tồn tại");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id: " + request.getBrandId()));

        Product product = new Product();
        updateProductFields(product, request);
        product.setSlug(slug);
        product.setCategory(category);
        product.setBrand(brand);
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (files != null && files.length > 0) {
            try {
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    if (file != null && !file.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(file, "pc-media/products");
                        
                        // Set the first valid file as the main image
                        if (product.getImageUrl() == null) {
                            product.setImageUrl(imageUrl);
                        }
                        
                        // Create and add to the secondary images list
                        ProductImage productImage = new ProductImage();
                        productImage.setImageUrl(imageUrl);
                        productImage.setProduct(product);
                        productImage.setPrimary(i == 0);
                        productImage.setDisplayOrder(i);
                        product.getImages().add(productImage);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tải ảnh lên Cloudinary", e);
            }
        }

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request, MultipartFile[] files) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        if (!product.getName().equals(request.getName()) && productRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên sản phẩm đã tồn tại");
        }

        if (request.getSku() != null && !request.getSku().equals(product.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Mã SKU đã tồn tại");
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug();
            if (!newSlug.equals(product.getSlug()) && productRepository.existsBySlug(newSlug)) {
                throw new RuntimeException("Slug '" + newSlug + "' đã tồn tại");
            }
            product.setSlug(newSlug);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id: " + request.getBrandId()));

        updateProductFields(product, request);
        product.setCategory(category);
        product.setBrand(brand);
        
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        if (files != null && files.length > 0) {
            try {
                // Determine the next display order
                int nextOrder = product.getImages().size();
                
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    if (file != null && !file.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(file, "pc-media/products");
                        
                        // If product has no image, set the first new one as main
                        if (product.getImageUrl() == null) {
                            product.setImageUrl(imageUrl);
                        }
                        
                        ProductImage productImage = new ProductImage();
                        productImage.setImageUrl(imageUrl);
                        productImage.setProduct(product);
                        productImage.setPrimary(nextOrder == 0 && i == 0);
                        productImage.setDisplayOrder(nextOrder++);
                        product.getImages().add(productImage);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tải ảnh mới lên Cloudinary", e);
            }
        }

        return mapToResponse(productRepository.save(product));
    }

    public void softDelete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    public void hardDelete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm với id: " + id);
        }
        productRepository.deleteById(id);
    }

    private void updateProductFields(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setSalePrice(request.getSalePrice());
        product.setCostPrice(request.getCostPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setSocketType(request.getSocketType());
        product.setRamType(request.getRamType());
        product.setWattage(request.getWattage());
        product.setWarrantyPeriod(request.getWarrantyPeriod());
    }

    public ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setSlug(product.getSlug());
        response.setPrice(product.getPrice());
        response.setSalePrice(product.getSalePrice());
        response.setCostPrice(product.getCostPrice());
        response.setIsActive(product.getIsActive());
        response.setStock(product.getStock());
        response.setDescription(product.getDescription());
        response.setImageUrl(product.getImageUrl());
        response.setMainImageUrl(product.getImageUrl());
        response.setSocketType(product.getSocketType());
        response.setRamType(product.getRamType());
        response.setWattage(product.getWattage());
        response.setWarrantyPeriod(product.getWarrantyPeriod());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            ProductResponse.CategoryDTO catDTO = new ProductResponse.CategoryDTO();
            catDTO.setId(product.getCategory().getId());
            catDTO.setName(product.getCategory().getName());
            catDTO.setSlug(product.getCategory().getSlug());
            response.setCategory(catDTO);
        }

        if (product.getBrand() != null) {
            ProductResponse.BrandDTO brandDTO = new ProductResponse.BrandDTO();
            brandDTO.setId(product.getBrand().getId());
            brandDTO.setName(product.getBrand().getName());
            brandDTO.setSlug(product.getBrand().getSlug());
            brandDTO.setLogoUrl(product.getBrand().getLogoUrl());
            response.setBrand(brandDTO);
        }

        if (product.getImages() != null) {
            response.setSecondaryImages(product.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
