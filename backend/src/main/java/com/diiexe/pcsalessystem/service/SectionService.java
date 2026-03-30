package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.SectionRequest;
import com.diiexe.pcsalessystem.dto.SectionResponse;
import com.diiexe.pcsalessystem.entity.Product;
import com.diiexe.pcsalessystem.entity.Section;
import com.diiexe.pcsalessystem.entity.SectionProduct;
import com.diiexe.pcsalessystem.repository.ProductRepository;
import com.diiexe.pcsalessystem.repository.SectionProductRepository;
import com.diiexe.pcsalessystem.repository.SectionRepository;
import com.diiexe.pcsalessystem.util.SlugUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SectionProductRepository sectionProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<SectionResponse> getActiveSections() {
        LocalDateTime now = LocalDateTime.now();
        return sectionRepository.findByIsActiveOrderByDisplayOrderAsc(true)
                .stream()
                // Lọc bỏ các section chưa đến thời điểm bắt đầu
                .filter(s -> s.getStartAt() == null || !s.getStartAt().isAfter(now))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SectionResponse> getAllSections() {
        return sectionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SectionResponse create(SectionRequest request) {
        Section section = new Section();
        updateSectionFields(section, request);
        Section saved = sectionRepository.save(section);
        
        if (request.getProductIds() != null) {
            addProductsToSection(saved, request.getProductIds());
        }
        
        return mapToResponse(saved);
    }

    @Transactional
    public SectionResponse update(Long id, SectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy section"));
        updateSectionFields(section, request);
        return mapToResponse(sectionRepository.save(section));
    }

    @Transactional
    public void reorder(List<Long> sectionIds) {
        for (int i = 0; i < sectionIds.size(); i++) {
            Long id = sectionIds.get(i);
            Section section = sectionRepository.findById(id).orElse(null);
            if (section != null) {
                section.setDisplayOrder(i);
                sectionRepository.save(section);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        sectionRepository.deleteById(id);
    }

    private void updateSectionFields(Section section, SectionRequest request) {
        section.setName(request.getName());
        section.setType(request.getType());
        section.setSlug(SlugUtils.toSlug(request.getName()));
        section.setDisplayOrder(request.getDisplayOrder());
        section.setIsActive(request.getIsActive());
        section.setStartAt(request.getStartAt());
        section.setEndAt(request.getEndAt());
        
        // Thêm mapping cho Discount
        section.setHasDiscount(request.getHasDiscount() != null ? request.getHasDiscount() : false);
        section.setDiscountType(request.getDiscountType());
        section.setDiscountValue(request.getDiscountValue());
    }

    private void addProductsToSection(Section section, List<Long> productIds) {
        for (int i = 0; i < productIds.size(); i++) {
            Product product = productRepository.findById(productIds.get(i)).orElse(null);
            if (product != null) {
                SectionProduct sp = new SectionProduct();
                sp.setSection(section);
                sp.setProduct(product);
                sp.setDisplayOrder(i);
                
                applySectionDiscountToProduct(section, sp, product);
                sectionProductRepository.save(sp);
            }
        }
        sectionProductRepository.flush();
        entityManager.refresh(section);
    }

    @Transactional
    public SectionResponse addProductToSection(Long sectionId, Long productId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy section"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!sectionProductRepository.existsBySectionIdAndProductId(sectionId, productId)) {
            SectionProduct sp = new SectionProduct();
            sp.setSection(section);
            sp.setProduct(product);
            sp.setDisplayOrder(section.getSectionProducts().size());

            applySectionDiscountToProduct(section, sp, product);
            sectionProductRepository.save(sp);
            
            // Đồng bộ thủ công vào list để mapToResponse lấy được ngay
            section.getSectionProducts().add(sp);
            sectionProductRepository.flush(); 
        }

        return mapToResponse(section);
    }

    @Transactional
    public SectionResponse removeProductFromSection(Long sectionId, Long productId) {
        SectionProduct sp = sectionProductRepository.findBySectionIdAndProductId(sectionId, productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong section này"));
        
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy section"));
        
        // Loại bỏ khỏi list trong bộ nhớ
        section.getSectionProducts().removeIf(item -> item.getProduct().getId().equals(productId));
        
        sectionProductRepository.delete(sp);
        sectionProductRepository.flush(); 
        
        return mapToResponse(section);
    }

    private void applySectionDiscountToProduct(Section section, SectionProduct sp, Product product) {
        // Ưu tiên FLASH_SALE cũ (nếu có) hoặc Discount mới của Section
        if ("FLASH_SALE".equals(section.getType())) {
            sp.setDiscountPercent(10);
            sp.setSalePrice(product.getPrice() * 0.9);
        } else if (Boolean.TRUE.equals(section.getHasDiscount()) && section.getDiscountValue() != null) {
            if ("PERCENT".equals(section.getDiscountType())) {
                sp.setDiscountPercent(section.getDiscountValue().intValue());
                sp.setSalePrice(product.getPrice() * (1 - section.getDiscountValue() / 100));
            } else if ("AMOUNT".equals(section.getDiscountType())) {
                double salePrice = product.getPrice() - section.getDiscountValue();
                sp.setSalePrice(Math.max(0, salePrice));
                sp.setDiscountPercent((int) ((product.getPrice() - salePrice) / product.getPrice() * 100));
            }
        }
    }

    private SectionResponse mapToResponse(Section section) {
        SectionResponse response = new SectionResponse();
        response.setId(section.getId());
        response.setName(section.getName());
        response.setType(section.getType());
        response.setSlug(section.getSlug());
        response.setDisplayOrder(section.getDisplayOrder());
        response.setIsActive(section.getIsActive());
        response.setStartAt(section.getStartAt());
        response.setEndAt(section.getEndAt());
        
        // Map discount sang response
        response.setHasDiscount(section.getHasDiscount());
        response.setDiscountType(section.getDiscountType());
        response.setDiscountValue(section.getDiscountValue());
        
        if (section.getSectionProducts() != null) {
            response.setProducts(section.getSectionProducts().stream()
                .map(sp -> {
                    SectionResponse.SectionProductResponse spr = new SectionResponse.SectionProductResponse();
                    spr.setProductId(sp.getProduct().getId());
                    spr.setName(sp.getProduct().getName());
                    spr.setSlug(sp.getProduct().getSlug());
                    spr.setMainImageUrl(sp.getProduct().getImageUrl());
                    spr.setOriginalPrice(sp.getProduct().getPrice());
                    spr.setSalePrice(sp.getSalePrice());
                    spr.setDiscountPercent(sp.getDiscountPercent());
                    spr.setDisplayOrder(sp.getDisplayOrder());
                    return spr;
                }).collect(Collectors.toList()));
        }
        
        return response;
    }
}
