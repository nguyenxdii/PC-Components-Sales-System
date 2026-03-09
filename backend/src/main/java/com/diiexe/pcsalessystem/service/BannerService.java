package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.BannerRequest;
import com.diiexe.pcsalessystem.entity.Banner;
import com.diiexe.pcsalessystem.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Lấy banner cho trang chủ
    public List<Banner> getActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    // Lấy tất cả banner cho trang admin
    public List<Banner> getAllBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Banner getById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy banner với id: " + id));
    }

    public Banner create(BannerRequest request, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng tải lên hình ảnh cho banner");
        }

        Banner banner = new Banner();
        banner.setName(request.getName());
        banner.setLink(request.getLink());
        banner.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        banner.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        try {
            // Upload ảnh vào thu mục pc-media/system/banners
            String imageUrl = cloudinaryService.uploadImage(file, "pc-media/system/banners");
            banner.setImageUrl(imageUrl);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải ảnh lên Cloudinary", e);
        }

        return bannerRepository.save(banner);
    }

    public Banner update(Long id, BannerRequest request, MultipartFile file) {
        Banner banner = getById(id);

        if (request.getName() != null) banner.setName(request.getName());
        if (request.getLink() != null) banner.setLink(request.getLink());
        if (request.getDisplayOrder() != null) banner.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) banner.setIsActive(request.getIsActive());

        if (file != null && !file.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(file, "pc-media/system/banners");
                banner.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tải ảnh mới lên Cloudinary", e);
            }
        }

        return bannerRepository.save(banner);
    }

    public void delete(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy banner với id: " + id);
        }
        bannerRepository.deleteById(id);
    }
}
