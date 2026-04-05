package com.diiexe.pcsalessystem.repository;

import com.diiexe.pcsalessystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByCategoryId(Long categoryId);

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    java.util.List<Product> findByCategoryId(Long categoryId);

    java.util.List<Product> findByCategorySlugAndIsActiveTrue(String slug);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE p.category.slug = :slug AND (p.socketType = :socket OR p.socketType IS NULL) AND p.isActive = true")
    java.util.List<Product> findByCompatibleSocket(String slug, String socket);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE p.category.slug = :slug AND (p.ramType = :ram OR p.ramType IS NULL) AND p.isActive = true")
    java.util.List<Product> findByCompatibleRam(String slug, String ram);
}
