package com.diiexe.pcsalessystem.config;

import com.diiexe.pcsalessystem.entity.*;
import com.diiexe.pcsalessystem.repository.*;
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
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> STARTING DATABASE INITIALIZATION...");

        // 1. Luôn đảm bảo có tài khoản Admin
        createUsers();
        
        // 2. Chỉ thực hiện Seeding dữ liệu mẫu nếu chưa có sản phẩm hoặc dữ liệu cũ không đạt chuẩn
        long currentProductCount = productRepository.count();
        boolean hasNewData = productRepository.findByCompatibleSocket("cpu-bo-vi-xu-ly", "AM5").size() > 0;

        if (currentProductCount == 0 || !hasNewData) {
            if (!hasNewData && currentProductCount > 0) {
                System.out.println(">>> DETECTED OBSOLETE DATA. Deactivating existing products and re-seeding for consistency...");
                // Không xóa (Delete) vì sẽ lỗi khóa ngoại với OrderDetails, ta chỉ Ẩn (Deactivate)
                List<Product> products = productRepository.findAll();
                products.forEach(p -> p.setIsActive(false));
                productRepository.saveAll(products);
            }
            System.out.println(">>> Starting comprehensive data seeding (200+ products)...");
            
            // Init Brands
            Map<String, Brand> brands = createBrands();

            // Init Categories (L1 & L2)
            Map<String, Category> categories = createCategories();

            // Init Products (Detailed for Build PC)
            createProducts(brands, categories);

            // Init Banners
            createBanners();

            // Init Sections
            createSections();
            
            System.out.println(">>> COMPREHENSIVE DATA SEEDING COMPLETED SUCCESSFULLY! <<<");
        } else {
            System.out.println(">>> Database already has data. Skipping comprehensive seeding.");
        }
    }

    private void createUsers() {
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setFullName("Quản trị viên (EXEShop)");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("123qwe123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            admin.setLocked(false);
            userRepository.save(admin);
            System.out.println(">>> Admin account created: admin@gmail.com / 123qwe123");
        }
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

        List<Product> allProducts = productRepository.findAll();
        
        // Flash Sale - Top 8 Discounted
        allProducts.stream().limit(8).forEach(p -> {
            if (!sectionProductRepository.existsBySectionIdAndProductId(flashSale.getId(), p.getId())) {
                SectionProduct sp = new SectionProduct();
                sp.setSection(flashSale);
                sp.setProduct(p);
                sp.setDisplayOrder(0);
                sp.setDiscountPercent(20);
                sp.setSalePrice(p.getPrice() * 0.8);
                sectionProductRepository.save(sp);
            }
        });
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
            {"Logitech", "https://logo.clearbit.com/logitech.com"},
            {"Razer", "https://logo.clearbit.com/razer.com"},
            {"SteelSeries", "https://logo.clearbit.com/steelseries.com"},
            {"Acer", "https://logo.clearbit.com/acer.com"},
            {"Dell", "https://logo.clearbit.com/dell.com"},
            {"Apple", "https://logo.clearbit.com/apple.com"}
        };

        Map<String, Brand> brands = new HashMap<>();
        for (String[] data : brandData) {
            String name = data[0];
            String slug = SlugUtils.toSlug(name);
            Brand brand = brandRepository.findBySlug(slug).orElse(new Brand());
            brand.setName(name);
            brand.setSlug(slug);
            brand.setLogoUrl(data[1]);
            brand.setIsActive(true);
            brands.put(name.toLowerCase(), brandRepository.saveAndFlush(brand));
        }
        return brands;
    }

    private Map<String, Category> createCategories() {
        Map<String, Category> categories = new HashMap<>();

        // Level 1: Linh kiện máy tính
        Category l1_comp = saveCategory("Linh kiện máy tính", "linh-kien-may-tinh", null, 1, "/icons/cpu.png");

        categories.put("L2_CPU", saveCategory("CPU - Bộ vi xử lý", "cpu-bo-vi-xu-ly", l1_comp, 1, "/icons/cpu.png"));
        categories.put("L2_VGA", saveCategory("VGA - Card đồ họa", "vga-card-do-hoa", l1_comp, 2, "/icons/vga.png"));
        categories.put("L2_MAIN", saveCategory("Mainboard - Bộ mạch chủ", "mainboard-bo-mach-chu", l1_comp, 3, "/icons/main.png"));
        categories.put("L2_RAM", saveCategory("RAM - Bộ nhớ trong", "ram-bo-nho-trong", l1_comp, 4, "/icons/ram.png"));
        categories.put("L2_SSD", saveCategory("Ổ cứng SSD", "o-cung", l1_comp, 5, "/icons/ssd.png"));
        categories.put("L2_PSU", saveCategory("PSU - Nguồn máy tính", "psu-nguon-may-tinh", l1_comp, 6, "/icons/psu.png"));
        categories.put("L2_CASE", saveCategory("Vỏ Case Premium", "vo-case-premium", l1_comp, 7, "/icons/case.png"));
        
        // Level 1: Tản nhiệt
        Category l1_cool = saveCategory("Tản nhiệt", "tan-nhiet", null, 2, "/icons/cooler.png");
        categories.put("L2_AIR", saveCategory("Tản nhiệt khí", "tan-nhiet-khi", l1_cool, 1, "/icons/air.png"));
        categories.put("L2_AIO", saveCategory("Tản nhiệt nước AIO", "tan-nhiet-nuoc-aio", l1_cool, 2, "/icons/aio.png"));

        // Level 1: Gaming Gear & Laptop
        Category l1_gear = saveCategory("Gaming Gear", "gaming-gear", null, 3, "/icons/mouse.png");
        categories.put("L2_KEYBOARD", saveCategory("Bàn phím cơ", "ban-phim-co", l1_gear, 1, "/icons/kb.png"));
        categories.put("L2_MOUSE", saveCategory("Chuột Gaming", "chuot-gaming", l1_gear, 2, "/icons/mouse.png"));
        categories.put("L2_HEADSET", saveCategory("Tai nghe Gaming", "tai-nghe-gaming", l1_gear, 3, "/icons/hs.png"));

        categories.put("L2_LAPGAMING", saveCategory("Laptop Gaming", "laptop-gaming", null, 4, "/icons/laptop.png"));
        categories.put("L2_MONITOR", saveCategory("Màn hình Gaming", "man-hinh-gaming", null, 5, "/icons/monitor.png"));

        return categories;
    }

    private Category saveCategory(String name, String slug, Category parent, int order, String icon) {
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
        String imgCpuAmd = "CPU AMD Ryzen 7 7800X3D.png";
        String imgCpuI5 = "CPU Intel Core i5-14600K.png";
        String imgCpuI9 = "CPU Intel Core i9-14900K.png";
        
        String imgMbZ790 = "Mainboard ASUS ROG Z790 HERO.jpg";
        String imgMbB650 = "Mainboard GIGABYTE B650 AORUS Elite.png";
        String imgMbB760 = "Mainboard MSI MAG B760M Mortar.png";

        String[] vgaImgs = {"VGA ASUS ROG Strix RTX 4090.png", "VGA GIGABYTE Radeon RX 7900 XTX.jpg", "VGA MSI Gaming X Slim RTX 4070 Ti Super.webp"};
        String[] ramImgs = {"RAM Corsair Dominator Platinum 32GB DDR5.webp", "RAM G.Skill Trident Z5 RGB 32GB DDR5.jpg", "RAM Kingston FURY Beast 16GB DDR4.jpg"};
        String[] ssdImgs = {"SSD Crucial T705 1TB Gen 5.jpg", "SSD Samsung 990 Pro 1TB NVMe.webp", "SSD WD Black SN850X 2TB.png"};
        String[] psuImgs = {"Nguồn ASUS ROG Thor 1200W Platinum II.png", "Nguồn Corsair RM1000e 1000W 80 PLUS Gold.webp", "Nguồn MSI MPG A1000G PCIE5 1000W.png"};
        String[] coolImgs = {"Tản nhiệt Cooler Master MA812.png", "Tản nhiệt Corsair iCUE H150i Link.png", "Tản nhiệt Deepcool AK620 Digital.webp", "Tản nhiệt MSI MAG CoreLiquid M360.webp", "Tản nhiệt NZXT Kraken Elite 360 RGB.jpg", "Tản nhiệt Noctua NH-D15 chromax.black.jpg"};
        String[] caseImgs = {"Vỏ Case Fractal Design North.jpg", "Vỏ Case Lian Li O11 Dynamic EVO.jpg", "Vỏ Case NZXT H9 Elite White.jpg"};
        String[] lapImgs = {"Laptop ASUS ROG Strix G16 (2024).jpg", "Laptop ASUS ZenBook 14 OLED.jpg", "Laptop Acer Predator Helios Neo 16.jpg", "Laptop Dell XPS 13 9315.jpg", "Laptop MSI Raider GE78 HX.png", "Laptop MacBook Air M3.webp"};
        String[] monImgs = {"Màn hình ASUS ROG Swift OLED PG27AQDM.jpg", "Màn hình LG UltraGear 27GP850.png", "Màn hình Samsung Odyssey G7.webp"};
        String[] gearImgs = {"Bàn phím Akko 3098B Dragon Ball.jpg", "Bàn phím Corsair K70 RGB PRO.png", "Bàn phím Keychron Q3 Pro.webp", "Chuột Logitech G Pro X Superlight 2.jpg", "Chuột Razer DeathAdder V3 Pro.webp", "Chuột SteelSeries Aerox 3.jpg", "Tai nghe Corsair HS80 RGB Wireless.jpg", "Tai nghe Razer BlackShark V2 Pro.jpg", "Tai nghe SteelSeries Arctis Nova Pro.jpg"};

        // --- 1. INTEL LGA1700 (DDR5 & DDR4) ---
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuI9}, "CPU Intel Core i9-14900K", "CPU-INTEL-149", 10, 15000000.0, 18000000.0, "LGA1700", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuI5}, "CPU Intel Core i7-14700K", "CPU-INTEL-147", 10, 10000000.0, 12000000.0, "LGA1700", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuI5}, "CPU Intel Core i5-13600K", "CPU-INTEL-136", 10, 7000000.0, 8500000.0, "LGA1700", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuI5}, "CPU Intel Core i5-12400F", "CPU-INTEL-124", 15, 2500000.0, 4000000.0, "LGA1700", "DDR4", 0);
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuI5}, "CPU Intel Core i3-12100F", "CPU-INTEL-121", 10, 1500000.0, 2200000.0, "LGA1700", "DDR4", 0);

        generateCategoryProducts(cats.get("L2_MAIN"), brands, new String[]{imgMbZ790}, "Mainboard Z790 High-End", "MB-Z790", 10, 8000000.0, 15000000.0, "LGA1700", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_MAIN"), brands, new String[]{imgMbB760}, "Mainboard B760 Modern", "MB-B760", 15, 3500000.0, 6000000.0, "LGA1700", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_MAIN"), brands, new String[]{imgMbB760}, "Mainboard H610 Budget", "MB-H610", 15, 1800000.0, 2800000.0, "LGA1700", "DDR4", 0);

        // --- 2. AMD AM5 & AM4 ---
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuAmd}, "CPU AMD Ryzen 9 7950X", "CPU-AM5-795", 5, 14000000.0, 16000000.0, "AM5", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuAmd}, "CPU AMD Ryzen 7 7800X3D", "CPU-AM5-78X", 10, 9500000.0, 11500000.0, "AM5", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuAmd}, "CPU AMD Ryzen 5 7600X", "CPU-AM5-76X", 10, 5500000.0, 7000000.0, "AM5", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuAmd}, "CPU AMD Ryzen 7 5700X", "CPU-AM4-57X", 15, 4500000.0, 5500000.0, "AM4", "DDR4", 0);
        generateCategoryProducts(cats.get("L2_CPU"), brands, new String[]{imgCpuAmd}, "CPU AMD Ryzen 5 5600", "CPU-AM4-56X", 15, 2800000.0, 3800000.0, "AM4", "DDR4", 0);

        generateCategoryProducts(cats.get("L2_MAIN"), brands, new String[]{imgMbB650}, "Mainboard X670 Extreme", "MB-X670", 8, 8000000.0, 14000000.0, "AM5", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_MAIN"), brands, new String[]{imgMbB650}, "Mainboard B650 Gaming", "MB-B650", 15, 4000000.0, 7500000.0, "AM5", "DDR5", 0);
        generateCategoryProducts(cats.get("L2_MAIN"), brands, new String[]{imgMbB650}, "Mainboard B550 Classic", "MB-B550", 15, 2500000.0, 4500000.0, "AM4", "DDR4", 0);

        // --- 3. RAM, VGA, SSD ---
        generateCategoryProducts(cats.get("L2_RAM"), brands, new String[]{ramImgs[0], ramImgs[1]}, "RAM DDR5", "RAM-D5", 20, 2500000.0, 9000000.0, null, "DDR5", 0);
        generateCategoryProducts(cats.get("L2_RAM"), brands, new String[]{ramImgs[2]}, "RAM DDR4", "RAM-D4", 25, 700000.0, 2800000.0, null, "DDR4", 0);

        generateCategoryProducts(cats.get("L2_VGA"), brands, vgaImgs, "VGA RTX 40 Series", "VGA-RTX40", 25, 8000000.0, 65000000.0, null, "GDDR6", 250);
        generateCategoryProducts(cats.get("L2_VGA"), brands, vgaImgs, "VGA RTX 30 Series", "VGA-RTX30", 20, 4500000.0, 15000000.0, null, "GDDR6", 180);
        generateCategoryProducts(cats.get("L2_VGA"), brands, vgaImgs, "VGA Radeon RX", "VGA-RX", 15, 5000000.0, 25000000.0, null, "GDDR6", 200);

        generateCategoryProducts(cats.get("L2_SSD"), brands, ssdImgs, "SSD NVMe Gen 4/5", "SSD-H", 20, 1500000.0, 12000000.0, "NVMe", null, 0);
        generateCategoryProducts(cats.get("L2_SSD"), brands, ssdImgs, "SSD NVMe Gen 3", "SSD-M", 20, 800000.0, 2500000.0, "NVMe", null, 0);

        // --- 4. PSU, Case, Cooler ---
        generateCategoryProducts(cats.get("L2_PSU"), brands, psuImgs, "Nguồn 750W-1200W", "PSU-H", 20, 2500000.0, 8000000.0, null, null, 1000);
        generateCategoryProducts(cats.get("L2_PSU"), brands, psuImgs, "Nguồn 500W-650W", "PSU-L", 20, 800000.0, 2000000.0, null, null, 600);

        generateCategoryProducts(cats.get("L2_CASE"), brands, caseImgs, "Vỏ Case Gaming", "CASE-G", 25, 1200000.0, 9000000.0, "ATX", null, 0);
        generateCategoryProducts(cats.get("L2_CASE"), brands, caseImgs, "Vỏ Case Office", "CASE-O", 15, 400000.0, 900000.0, "mATX", null, 0);

        generateCategoryProducts(cats.get("L2_AIR"), brands, coolImgs, "Tản nhiệt Khí", "AIR", 20, 300000.0, 3500000.0, null, null, 0);
        generateCategoryProducts(cats.get("L2_AIO"), brands, coolImgs, "Tản nhiệt Nước", "AIO", 20, 1200000.0, 11000000.0, null, null, 0);
        
        // --- 5. Gear & Others ---
        generateCategoryProducts(cats.get("L2_KEYBOARD"), brands, gearImgs, "Bàn phím cơ", "KBD", 30, 800000.0, 6000000.0, null, null, 0);
        generateCategoryProducts(cats.get("L2_MOUSE"), brands, gearImgs, "Chuột Gaming", "MSE", 30, 400000.0, 4500000.0, null, null, 0);
        generateCategoryProducts(cats.get("L2_HEADSET"), brands, gearImgs, "Tai nghe Gaming", "HDS", 25, 1000000.0, 9000000.0, null, null, 0);
        generateCategoryProducts(cats.get("L2_LAPGAMING"), brands, lapImgs, "Laptop Gaming", "LAP", 25, 15000000.0, 95000000.0, null, null, 0);
        generateCategoryProducts(cats.get("L2_MONITOR"), brands, monImgs, "Màn hình", "MON", 30, 2500000.0, 55000000.0, null, null, 0);
    }

    private void generateCategoryProducts(Category cat, Map<String, Brand> brands, String[] imgs, String namePrefix, String skuPrefix, int count, double minPrice, double maxPrice, String socket, String ram, int watt) {
        List<Brand> brandList = new ArrayList<>(brands.values());
        for (int i = 0; i < count; i++) {
            String img = imgs[i % imgs.length];
            Brand brand = brandList.get(new Random().nextInt(brandList.size()));
            String name = namePrefix + " " + brand.getName() + " Elite Series " + (i + 1);
            String sku = skuPrefix.toUpperCase() + "-" + brand.getName().substring(0, 2).toUpperCase() + "-" + (1000 + i);
            
            double price = minPrice + (maxPrice - minPrice) * (i / (double) count);
            double salePrice = price * 0.95; // 5% discount for most
            
            saveProduct(name, sku, price, salePrice, brand, cat, img, socket, ram, watt);
        }
    }

    private void saveProduct(String name, String sku, Double price, Double salePrice, Brand brand, Category cat, String img, String socket, String ram, int watt) {
        String slug = SlugUtils.toSlug(name);
        Product p = productRepository.findBySku(sku)
                .orElseGet(() -> productRepository.findBySlug(slug).orElse(new Product()));

        p.setName(name);
        p.setSku(sku);
        p.setSlug(slug);
        p.setIsActive(true);
        p.setPrice(Math.round(price / 10000.0) * 10000.0); // Round down to 10k
        p.setSalePrice(Math.round(salePrice / 10000.0) * 10000.0);
        p.setCostPrice(price * 0.7);
        p.setStock(new Random().nextInt(50) + 10);
        p.setDescription(generateDetailedDescription(name, cat != null ? cat.getName() : "Linh kiện"));
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

    private String generateDetailedDescription(String name, String category) {
        return String.format(
            "Sản phẩm %s thuộc phân khúc %s cao cấp. Mang lại hiệu năng vượt trội và độ bền bỉ đáng kinh ngạc. " +
            "Thiết kế hiện đại, tản nhiệt tối ưu và tích hợp nhiều công nghệ mới nhất giúp bạn dẫn đầu trong mọi tác vụ.",
            name, category
        );
    }
}
