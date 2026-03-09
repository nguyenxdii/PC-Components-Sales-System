package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.BrandRequest;
import com.diiexe.pcsalessystem.entity.Brand;
import com.diiexe.pcsalessystem.repository.BrandRepository;
import com.diiexe.pcsalessystem.util.SlugUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public List<Brand> getAllActive() {
        return brandRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public List<Brand> getAll() {
        return brandRepository.findAllByOrderByNameAsc();
    }

    public Brand getById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id: " + id));
    }

    public Brand create(BrandRequest request, MultipartFile file) {
        if (brandRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên thương hiệu đã tồn tại");
        }

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : SlugUtils.toSlug(request.getName());

        if (brandRepository.existsBySlug(slug)) {
            throw new RuntimeException("Slug '" + slug + "' đã tồn tại");
        }

        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setSlug(slug);
        brand.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (file != null && !file.isEmpty()) {
            try {
                String logoUrl = cloudinaryService.uploadImage(file, "pc-media/system/brands");
                brand.setLogoUrl(logoUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tải ảnh lên Cloudinary", e);
            }
        }

        return brandRepository.save(brand);
    }

    public Brand update(Long id, BrandRequest request, MultipartFile file) {
        Brand brand = getById(id);

        if (!brand.getName().equals(request.getName())
                && brandRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên thương hiệu đã tồn tại");
        }
        brand.setName(request.getName());

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug();
            if (!newSlug.equals(brand.getSlug()) && brandRepository.existsBySlug(newSlug)) {
                throw new RuntimeException("Slug '" + newSlug + "' đã tồn tại");
            }
            brand.setSlug(newSlug);
        }

        if (request.getIsActive() != null) {
            brand.setIsActive(request.getIsActive());
        }

        if (file != null && !file.isEmpty()) {
            try {
                String logoUrl = cloudinaryService.uploadImage(file, "pc-media/system/brands");
                brand.setLogoUrl(logoUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tải ảnh mới lên Cloudinary", e);
            }
        }

        return brandRepository.save(brand);
    }

    public void softDelete(Long id) {
        Brand brand = getById(id);
        brand.setIsActive(false);
        brandRepository.save(brand);
    }

    public void hardDelete(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy thương hiệu với id: " + id);
        }
        brandRepository.deleteById(id);
    }
}