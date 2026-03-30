package com.diiexe.pcsalessystem.config;

import com.diiexe.pcsalessystem.entity.Brand;
import com.diiexe.pcsalessystem.entity.Category;
import com.diiexe.pcsalessystem.entity.Product;
import com.diiexe.pcsalessystem.entity.Banner;
import com.diiexe.pcsalessystem.entity.Section;
import com.diiexe.pcsalessystem.entity.SectionProduct;
import com.diiexe.pcsalessystem.repository.BrandRepository;
import com.diiexe.pcsalessystem.repository.CategoryRepository;
import com.diiexe.pcsalessystem.repository.ProductRepository;
import com.diiexe.pcsalessystem.repository.BannerRepository;
import com.diiexe.pcsalessystem.repository.SectionRepository;
import com.diiexe.pcsalessystem.repository.SectionProductRepository;
import com.diiexe.pcsalessystem.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final BannerRepository bannerRepository;
    private final SectionRepository sectionRepository;
    private final SectionProductRepository sectionProductRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> STARTING DATA SEEDING (COMPATIBILITY-MODE)...");
        
        // 1. Khởi tạo Brands
        Map<String, Brand> brands = createBrands();

        // 2. Khởi tạo Categories (3 cấp)
        Map<String, Category> categories = createCategories();

        // 3. Khởi tạo Sản phẩm chi tiết
        createProducts(brands, categories);

        // 4. Khởi tạo Banners
        createBanners();

        // 5. Khởi tạo Sections
        createSections();

        System.out.println(">>> DATA SEEDING COMPLETED SUCCESSFULLY! <<<");
    }

    private void createBanners() {
        String[] bannerFiles = {"banner 1.png", "banner 2.jpg", "banner 3.png"};
        for (int i = 0; i < bannerFiles.length; i++) {
            String fileName = bannerFiles[i];
            String bannerName = "Banner " + (i + 1);
            Banner banner = bannerRepository.findByName(bannerName)
                    .orElse(new Banner());
            
            banner.setName(bannerName);
            banner.setImageUrl("/images/banners/" + fileName);
            banner.setLink("/products");
            banner.setIsActive(true);
            banner.setDisplayOrder(i);
            bannerRepository.save(banner);
        }
    }

    private void createSections() {
        // Section 1: Flash Sale
        Section flashSale = sectionRepository.findBySlug("gio-vang-gia-soc")
                .orElse(new Section());
        
        flashSale.setName("GIỜ VÀNG GIÁ SỐC");
        flashSale.setType("FLASH_SALE");
        flashSale.setSlug("gio-vang-gia-soc");
        flashSale.setDisplayOrder(1);
        flashSale.setIsActive(true);
        flashSale.setStartAt(LocalDateTime.now());
        flashSale.setEndAt(LocalDateTime.now().plusDays(2));
        sectionRepository.save(flashSale);

        // Section 2: ASUS Collection
        Section asusSection = sectionRepository.findBySlug("sieu-pham-asus")
                .orElse(new Section());
        
        asusSection.setName("SIÊU PHẨM ASUS");
        asusSection.setType("COLLECTION");
        asusSection.setSlug("sieu-pham-asus");
        asusSection.setDisplayOrder(2);
        asusSection.setIsActive(true);
        sectionRepository.save(asusSection);

        // Gắn sản phẩm vào Section
        List<Product> allProducts = productRepository.findAll();
        
        // Add to Flash Sale (5 products)
        int count = 0;
        for (Product p : allProducts) {
            if (count >= 5) break;
            if (!sectionProductRepository.existsBySectionIdAndProductId(flashSale.getId(), p.getId())) {
                SectionProduct sp = new SectionProduct();
                sp.setSection(flashSale);
                sp.setProduct(p);
                sp.setDisplayOrder(count);
                sp.setDiscountPercent(15 + count);
                sp.setSalePrice(p.getPrice() * (1 - (15.0 + count)/100.0));
                sectionProductRepository.save(sp);
                count++;
            }
        }

        // Add to ASUS (Products from ASUS brand)
        int asusCount = 0;
        for (Product p : allProducts) {
            if (asusCount >= 5) break;
            if (p.getBrand() != null && p.getBrand().getName().equalsIgnoreCase("ASUS")) {
                if (!sectionProductRepository.existsBySectionIdAndProductId(asusSection.getId(), p.getId())) {
                    SectionProduct sp = new SectionProduct();
                    sp.setSection(asusSection);
                    sp.setProduct(p);
                    sp.setDisplayOrder(asusCount);
                    sectionProductRepository.save(sp);
                    asusCount++;
                }
            }
        }
    }

    private Map<String, Brand> createBrands() {
        String[][] brandData = {
            {"Intel", "https://logo.clearbit.com/intel.com"},
            {"AMD", "https://logo.clearbit.com/amd.com"},
            {"NVIDIA", "https://logo.clearbit.com/nvidia.com"},
            {"ASUS", "https://logo.clearbit.com/asus.com"},
            {"MSI", "https://logo.clearbit.com/msi.com"},
            {"Gigabyte", "https://logo.clearbit.com/gigabyte.com"},
            {"Corsair", "https://logo.clearbit.com/corsair.com"},
            {"G.Skill", "https://logo.clearbit.com/gskill.com"},
            {"Samsung", "https://logo.clearbit.com/samsung.com"},
            {"Kingston", "https://logo.clearbit.com/kingston.com"},
            {"Western Digital", "https://logo.clearbit.com/wdc.com"},
            {"Seagate", "https://logo.clearbit.com/seagate.com"},
            {"NZXT", "https://logo.clearbit.com/nzxt.com"},
            {"Cooler Master", "https://logo.clearbit.com/coolermaster.com"},
            {"Deepcool", "https://logo.clearbit.com/deepcool.com"},
            {"Lian Li", "https://logo.clearbit.com/lian-li.com"},
            {"Thermaltake", "https://logo.clearbit.com/thermaltake.com"},
            {"Seasonic", "https://logo.clearbit.com/seasonic.com"},
            {"EVGA", "https://logo.clearbit.com/evga.com"},
            {"Zotac", "https://logo.clearbit.com/zotac.com"},
            {"Logitech", "https://logo.clearbit.com/logitech.com"},
            {"Razer", "https://logo.clearbit.com/razer.com"},
            {"SteelSeries", "https://logo.clearbit.com/steelseries.com"},
            {"Keychron", "https://logo.clearbit.com/keychron.com"},
            {"Akko", "https://logo.clearbit.com/akkogear.com.vn"},
            {"Acer", "https://logo.clearbit.com/acer.com"},
            {"Dell", "https://logo.clearbit.com/dell.com"},
            {"Apple", "https://logo.clearbit.com/apple.com"}
        };

        Map<String, Brand> brands = new HashMap<>();
        for (String[] data : brandData) {
            String name = data[0];
            String logo = data[1];
            String slug = SlugUtils.toSlug(name);
            Brand brand = brandRepository.findBySlug(slug).orElse(new Brand());
            brand.setName(name);
            brand.setSlug(slug);
            brand.setLogoUrl(logo);
            brand.setIsActive(true);
            brands.put(name.toLowerCase(), brandRepository.saveAndFlush(brand));
        }
        return brands;
    }

    private Map<String, Category> createCategories() {
        Map<String, Category> catMap = new HashMap<>();

        // Level 1
        Category l1LinhKien = createCategory("Linh kiện Máy tính", null, 1, null);
        Category l1MainPSU = createCategory("Mainboard & Nguồn", null, 2, null);
        Category l1TanCase = createCategory("Tản nhiệt & Vỏ Case", null, 3, null);
        Category l1MonitorLaptop = createCategory("Màn hình & Laptop", null, 4, null);
        Category l1Gear = createCategory("Gaming Gear", null, 5, null);

        catMap.put("L1_LINHKIEN", l1LinhKien);
        catMap.put("L1_MAINPSU", l1MainPSU);
        catMap.put("L1_TANCASE", l1TanCase);
        catMap.put("L1_MONLAP", l1MonitorLaptop);
        catMap.put("L1_GEAR", l1Gear);

        // Level 2
        catMap.put("L2_CPU", createCategory("CPU - Bộ vi xử lý", l1LinhKien, 1, "https://cdn-icons-png.flaticon.com/512/908/908428.png"));
        catMap.put("L2_VGA", createCategory("VGA - Card đồ họa", l1LinhKien, 2, "https://cdn-icons-png.flaticon.com/512/908/908424.png"));
        catMap.put("L2_RAM", createCategory("RAM - Bộ nhớ trong", l1LinhKien, 3, "https://cdn-icons-png.flaticon.com/512/908/908425.png"));
        catMap.put("L2_MAIN", createCategory("Mainboard - Bo mạch chủ", l1MainPSU, 1, "https://cdn-icons-png.flaticon.com/512/908/908423.png"));
        catMap.put("L2_PSU", createCategory("PSU - Nguồn máy tính", l1MainPSU, 2, "https://cdn-icons-png.flaticon.com/512/4204/4204560.png"));
        catMap.put("L2_SSD", createCategory("Ổ cứng SSD M.2", l1MainPSU, 3, "https://cdn-icons-png.flaticon.com/512/2888/2888651.png"));
        catMap.put("L2_AIO", createCategory("Tản nhiệt nước AIO", l1TanCase, 1, "https://cdn-icons-png.flaticon.com/512/3529/3529344.png"));
        catMap.put("L2_AIR", createCategory("Tản nhiệt khí", l1TanCase, 2, "https://cdn-icons-png.flaticon.com/512/908/908433.png"));
        catMap.put("L2_CASE", createCategory("Vỏ Case Premium", l1TanCase, 3, "https://cdn-icons-png.flaticon.com/512/908/908421.png"));
        catMap.put("L2_MONITOR", createCategory("Màn hình Gaming", l1MonitorLaptop, 1, "https://cdn-icons-png.flaticon.com/512/908/908427.png"));
        catMap.put("L2_LAPGAMING", createCategory("Laptop Gaming", l1MonitorLaptop, 2, "https://cdn-icons-png.flaticon.com/512/908/908422.png"));
        catMap.put("L2_LAPOFFICE", createCategory("Laptop Văn phòng", l1MonitorLaptop, 3, "https://cdn-icons-png.flaticon.com/512/908/908422.png"));
        catMap.put("L2_KEYBOARD", createCategory("Bàn phím cơ", l1Gear, 1, "https://cdn-icons-png.flaticon.com/512/908/908426.png"));
        catMap.put("L2_MOUSE", createCategory("Chuột Gaming", l1Gear, 2, "https://cdn-icons-png.flaticon.com/512/908/908429.png"));
        catMap.put("L2_HEADSET", createCategory("Tai nghe Gaming", l1Gear, 3, "https://cdn-icons-png.flaticon.com/512/908/908431.png"));

        return catMap;
    }

    private Category createCategory(String name, Category parent, int order, String icon) {
        String slug = SlugUtils.toSlug(name);
        Category cat = categoryRepository.findBySlug(slug).orElse(new Category());
        cat.setName(name);
        cat.setSlug(slug);
        cat.setParent(parent);
        cat.setDisplayOrder(order);
        cat.setIconUrl(icon);
        cat.setIsActive(true);
        return categoryRepository.saveAndFlush(cat);
    }

    private void createProducts(Map<String, Brand> brands, Map<String, Category> cats) {
        saveProduct("CPU Intel Core i9-14900K", "CPU-INT-14900K", 16490000.0, 15990000.0, brands.get("intel"), cats.get("L2_CPU"), "CPU Intel Core i9-14900K.png", "LGA1700", "DDR5", 125);
        saveProduct("CPU Intel Core i5-14600K", "CPU-INT-14600K", 8690000.0, 8250000.0, brands.get("intel"), cats.get("L2_CPU"), "CPU Intel Core i5-14600K.png", "LGA1700", "DDR5", 125);
        saveProduct("CPU AMD Ryzen 7 7800X3D", "CPU-AMD-7800X3D", 10890000.0, 9990000.0, brands.get("amd"), cats.get("L2_CPU"), "CPU AMD Ryzen 7 7800X3D.png", "AM5", "DDR5", 120);
        saveProduct("VGA ASUS ROG Strix RTX 4090", "VGA-AS-4090-ROG", 58900000.0, 56500000.0, brands.get("asus"), cats.get("L2_VGA"), "VGA ASUS ROG Strix RTX 4090.png", null, null, 450);
        saveProduct("RAM G.Skill Trident Z5 RGB 32GB DDR5", "RAM-GS-32G-D5", 3850000.0, 3650000.0, brands.get("g.skill"), cats.get("L2_RAM"), "RAM G.Skill Trident Z5 RGB 32GB DDR5.jpg", null, "DDR5", 0);
        saveProduct("Mainboard ASUS ROG Z790 HERO", "MB-AS-Z790-HERO", 18500000.0, 17900000.0, brands.get("asus"), cats.get("L2_MAIN"), "Mainboard ASUS ROG Z790 HERO.jpg", "LGA1700", "DDR5", 0);
        saveProduct("SSD Samsung 990 Pro 1TB NVMe", "SSD-SAM-990P", 3250000.0, 3100000.0, brands.get("samsung"), cats.get("L2_SSD"), "SSD Samsung 990 Pro 1TB NVMe.webp", null, null, 0);
        saveProduct("Vỏ Case Lian Li O11 Dynamic EVO", "CASE-LL-O11D", 4850000.0, 4650000.0, brands.get("lian li"), cats.get("L2_CASE"), "Vỏ Case Lian Li O11 Dynamic EVO.jpg", null, null, 0);
    }

    private void saveProduct(String name, String sku, Double price, Double salePrice, Brand brand, Category cat, String img, String socket, String ram, int watt) {
        Product p = productRepository.findBySku(sku).orElse(new Product());
        p.setName(name);
        p.setSku(sku);
        p.setSlug(SlugUtils.toSlug(name));
        p.setPrice(price);
        p.setSalePrice(salePrice);
        p.setCostPrice(price * 0.7);
        p.setStock(50);
        p.setDescription(name + " cao cấp chính hãng.");
        p.setImageUrl("/images/products/" + img.replace(" ", "%20"));
        p.setBrand(brand);
        p.setCategory(cat);
        p.setSocketType(socket);
        p.setRamType(ram);
        p.setWattage(watt);
        p.setWarrantyPeriod(36);
        p.setIsActive(true);
        productRepository.saveAndFlush(p);
    }
}
