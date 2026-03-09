package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.BannerRequest;
import com.diiexe.pcsalessystem.entity.Banner;
import com.diiexe.pcsalessystem.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin("*") // Adjust for production
public class BannerController {

    @Autowired
    private BannerService bannerService;

    // Public API for front-end homepage
    @GetMapping("/active")
    public ResponseEntity<List<Banner>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

    // Admin APIs
    @GetMapping
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Banner> createBanner(
            @ModelAttribute BannerRequest request,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bannerService.create(request, file));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Banner> updateBanner(
            @PathVariable Long id,
            @ModelAttribute BannerRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(bannerService.update(id, request, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBanner(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.ok("Xóa banner thành công!");
    }
}
