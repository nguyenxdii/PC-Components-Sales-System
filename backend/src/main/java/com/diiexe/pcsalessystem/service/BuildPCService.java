package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.BuildPCRequest;
import com.diiexe.pcsalessystem.dto.ProductResponse;
import com.diiexe.pcsalessystem.entity.*;
import com.diiexe.pcsalessystem.repository.BuildPCRepository;
import com.diiexe.pcsalessystem.repository.ProductRepository;
import com.diiexe.pcsalessystem.repository.UserRepository;
import com.diiexe.pcsalessystem.service.GeminiService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildPCService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final GeminiService geminiService;
    private final BuildPCRepository buildPCRepository;
    private final UserRepository userRepository;
    private final Gson gson = new Gson();

    // Slugs from DataInitializer
    private static final String CAT_CPU = "cpu-bo-vi-xu-ly";
    private static final String CAT_VGA = "vga-card-do-hoa";
    private static final String CAT_MAIN = "mainboard-bo-mach-chu";
    private static final String CAT_RAM = "ram-bo-nho-trong";
    private static final String CAT_SSD = "o-cung";
    private static final String CAT_PSU = "psu-nguon-may-tinh";
    private static final String CAT_CASE = "vo-case-premium";
    private static final String CAT_COOLER = "tan-nhiet-khi";
    private static final String CAT_MONITOR = "man-hinh-gaming";
    private static final String CAT_KEYBOARD = "ban-phim-co";
    private static final String CAT_MOUSE = "chuot-gaming";
    private static final String CAT_HEADSET = "tai-nghe-gaming";

    public List<ProductResponse> suggestBuild(BuildPCRequest request) {
        try {
            return suggestBuildWithAI(request);
        } catch (Exception e) {
            log.error("AI Build Suggestion failed, falling back to heuristic: {}", e.getMessage());
            return suggestBuildHeuristic(request);
        }
    }

    public List<ProductResponse> suggestBuildAIOnly(BuildPCRequest request) {
        try {
            return suggestBuildWithAI(request);
        } catch (Exception e) {
            throw new RuntimeException("AI Build Suggestion failed: " + e.getMessage());
        }
    }

    private List<ProductResponse> suggestBuildWithAI(BuildPCRequest request) throws Exception {
        double budget = request.getBudget();
        String usage = (request.getUsage() != null) ? request.getUsage() : "Chơi game";
        boolean includeGear = request.getIncludeGear() != null && request.getIncludeGear();
        
        // 1. Prepare inventory data (Menu)
        String inventoryData = getInventoryDataForAI(budget);
        
        String correctionMsg = "";
        List<Product> finalBuild = new ArrayList<>();

        // THỬ LẠI TỐI ĐA 3 LẦN NẾU AI LÀM SAI NGÂN SÁCH
        for (int attempt = 1; attempt <= 3; attempt++) {
            // 2. Prepare Prompt
            String prompt = String.format(
                "Bạn là một chuyên gia tư vấn build PC chuyên nghiệp. Dưới đây là danh sách linh kiện đang có trong kho:\n%s\n\n" +
                "Yêu cầu khách hàng: Ngân sách mục tiêu %.0f VNĐ, nhu cầu chính là '%s', có bao gồm Gear không: %s.\n" +
                "%s\n" + // Tin nhắn sửa lỗi nếu có
                "HÃY CHỌN RA BỘ PC TỐI ƯU THEO CÁC QUY TẮC SAU:\n" +
                "1. BẮT BUỘC có đủ linh kiện cốt lõi: CPU, Mainboard, RAM, VGA (nếu budget > 12tr), SSD, PSU, Vỏ Case.\n" +
                "2. QUY TẮC NGÂN SÁCH SẮT ĐÁ (CỰC KỲ QUAN TRỌNG):\n" +
                "   - Tổng giá trị bộ PC CHỈ được phép trong khoảng: [%.0f VNĐ đến %.0f VNĐ].\n" +
                "   - Đây là giới hạn cứng. Nếu vượt quá %.0f VNĐ hoặc thấp hơn %.0f VNĐ, kết quả là SAI.\n" +
                "   - Bạn phải tự nhẩm tổng giá trước khi trả lời. Nếu đang vượt, hãy hạ cấp ngay SSD, RAM hoặc Vỏ case.\n" +
                "3. Nếu 'Bao gồm Gear' là true: BẮT BUỘC chọn thêm Màn hình, Bàn phím, Chuột.\n" +
                "4. Nếu 'Bao gồm Gear' là false: TUYỆT ĐỐI KHÔNG chọn Màn hình, Bàn phím, Chuột.\n" +
                "5. Các linh kiện phải tương thích (Socket CPU khớp Mainboard, RAM đúng loại DDR).\n" +
                "6. Trả về kết quả dạng JSON array chứa SKU.\n" +
                "Ví dụ: [\"SKU-1\", \"SKU-2\", ...]\n" +
                "Chỉ trả về JSON, không giải thích.",
                inventoryData, budget, usage, includeGear ? "CÓ" : "KHÔNG", 
                correctionMsg, budget - 3000000, budget + 1000000, budget + 1000000, budget - 3000000
            );

            // 3. Call Gemini
            String fullResponse = geminiService.generateContent(prompt);
            String skuJsonText = extractTextFromGeminiResponse(fullResponse);
            List<String> skus = parseSkusFromAiResponse(skuJsonText);
            
            // 4. Fetch and Validate Price
            List<Product> currentBuild = new ArrayList<>();
            double currentTotal = 0;
            for (String sku : skus) {
                Optional<Product> pOpt = productRepository.findBySku(sku);
                if (pOpt.isPresent()) {
                    Product p = pOpt.get();
                    currentBuild.add(p);
                    currentTotal += (p.getSalePrice() != null ? p.getSalePrice() : p.getPrice());
                }
            }

            log.info("AI Attempt {}: Total Price = {}", attempt, currentTotal);

            // KIỂM TRA NGÂN SÁCH (Trần +1tr, Sàn -3tr)
            if (currentTotal <= (budget + 1000000) && currentTotal >= (budget - 3000000)) {
                finalBuild = currentBuild;
                break; // Thành công
            } else {
                // Thất bại, chuẩn bị tin nhắn mắng AI cho lần thử sau
                correctionMsg = String.format(
                    "\n--- LỖI TỪ LẦN TRƯỚC: Bạn đã chọn bộ PC có tổng giá %.0f VNĐ. " +
                    "Con số này SAI vì nó không nằm trong khoảng [%.0f - %.0f]. " +
                    "HÃY LÀM LẠI VÀ GIẢM/TĂNG GIÁ CÁC LINH KIỆN NGAY! ---\n", 
                    currentTotal, budget - 3000000, budget + 1000000
                );
                if (attempt == 3) finalBuild = currentBuild; // Cố gắng cuối cùng
            }
        }

        if (finalBuild.isEmpty()) throw new RuntimeException("AI không thể tạo cấu hình phù hợp ngân sách.");

        return finalBuild.stream()
                .map(productService::mapToResponse)
                .collect(Collectors.toList());
    }

    private String extractTextFromGeminiResponse(String rawJson) {
        try {
            JsonElement root = JsonParser.parseString(rawJson);
            if (root.isJsonObject()) {
                JsonArray candidates = root.getAsJsonObject().getAsJsonArray("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    JsonArray parts = candidates.get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return parts.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting text from Gemini response: {}", e.getMessage());
        }
        return rawJson; // Fallback
    }

    private List<String> parseSkusFromAiResponse(String text) throws Exception {
        String json = text.trim();
        // Handle markdown code blocks
        if (json.contains("```json")) {
            json = json.substring(json.indexOf("```json") + 7, json.lastIndexOf("```"));
        } else if (json.contains("```")) {
            json = json.substring(json.indexOf("```") + 3, json.lastIndexOf("```"));
        }
        
        json = json.trim();
        List<String> skus = new ArrayList<>();
        
        JsonElement element = JsonParser.parseString(json);
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement e : array) {
                skus.add(e.getAsString());
            }
        }
        return skus;
    }

    private List<ProductResponse> suggestBuildHeuristic(BuildPCRequest request) {
        double budget = request.getBudget();
        String usage = (request.getUsage() != null) ? request.getUsage().toUpperCase() : "GAMING";

        List<Product> build = new ArrayList<>();
        double remainingBudget = budget;

        // Essential categories in order of importance
        List<String> coreCategories = new ArrayList<>(List.of(
            CAT_CPU, CAT_MAIN, CAT_RAM, CAT_SSD, CAT_PSU, CAT_CASE
        ));
        
        // Add VGA if not office and budget allows
        if (!"OFFICE".equals(usage)) {
            coreCategories.add(1, CAT_VGA); // Priority 2
        }

        Map<String, Double> allocation = getAllocation(usage);
        
        String currentSocket = null;
        String currentRamType = null;

        for (String cat : coreCategories) {
            double targetPrice = budget * allocation.getOrDefault(cat, 0.1);
            // Ensure we don't spend more than remaining
            targetPrice = Math.min(targetPrice, remainingBudget * 0.8); 
            
            Product p = findBestProduct(cat, targetPrice, currentSocket, currentRamType);
            if (p != null) {
                build.add(p);
                remainingBudget -= (p.getSalePrice() != null ? p.getSalePrice() : p.getPrice());
                
                // Track compatibility
                if (CAT_CPU.equals(cat)) {
                    currentSocket = p.getSocketType();
                    currentRamType = p.getRamType();
                } else if (CAT_MAIN.equals(cat)) {
                    if (p.getSocketType() != null) currentSocket = p.getSocketType();
                    if (p.getRamType() != null) currentRamType = p.getRamType();
                }
            }
        }

        return build.stream()
                .map(productService::mapToResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Double> getAllocation(String usage) {
        Map<String, Double> map = new HashMap<>();
        if ("OFFICE".equals(usage)) {
            map.put(CAT_CPU, 0.35);
            map.put(CAT_MAIN, 0.15);
            map.put(CAT_RAM, 0.15);
            map.put(CAT_SSD, 0.15);
            map.put(CAT_PSU, 0.10);
            map.put(CAT_CASE, 0.10);
        } else { // GAMING or WORKING
            map.put(CAT_VGA, 0.40);
            map.put(CAT_CPU, 0.20);
            map.put(CAT_MAIN, 0.10);
            map.put(CAT_RAM, 0.10);
            map.put(CAT_SSD, 0.10);
            map.put(CAT_PSU, 0.05);
            map.put(CAT_CASE, 0.05);
        }
        return map;
    }

    private Product findBestProduct(String catSlug, double allocatedBudget, String socket, String ram) {
        List<Product> products;
        if (socket != null) {
            products = productRepository.findByCompatibleSocket(catSlug, socket);
        } else if (ram != null) {
            products = productRepository.findByCompatibleRam(catSlug, ram);
        } else {
            products = productRepository.findByCategorySlugAndIsActiveTrue(catSlug);
        }

        // Sort by price descending and pick the first one targetting allocatedBudget
        return products.stream()
                .filter(p -> p.getSalePrice() != null ? p.getSalePrice() <= allocatedBudget : p.getPrice() <= allocatedBudget)
                .max(Comparator.comparing(p -> p.getSalePrice() != null ? p.getSalePrice() : p.getPrice()))
                .orElse(products.stream()
                        .min(Comparator.comparing(p -> p.getSalePrice() != null ? p.getSalePrice() : p.getPrice()))
                        .orElse(null)); // Fallback to cheapest if none under budget, or null
    }

    private String getInventoryDataForAI(double budget) {
        List<String> categories = new ArrayList<>(List.of(CAT_CPU, CAT_MAIN, CAT_RAM, CAT_VGA, CAT_SSD, CAT_PSU, CAT_CASE, CAT_COOLER, 
                                     CAT_MONITOR, CAT_KEYBOARD, CAT_MOUSE, CAT_HEADSET));
        StringBuilder sb = new StringBuilder();
        
        for (String catSlug : categories) {
            sb.append("\n--- ").append(catSlug).append(" ---\n");
            List<Product> allProducts = productRepository.findByCategorySlugAndIsActiveTrue(catSlug);
            if (allProducts.isEmpty()) continue;

            // Sắp xếp theo giá tăng dần
            allProducts.sort(Comparator.comparing(p -> p.getSalePrice() != null ? p.getSalePrice() : p.getPrice()));

            List<Product> sample = new ArrayList<>();
            int size = allProducts.size();
            
            if (budget < 25000000) {
                // Nếu ngân sách thấp: CHỈ lấy tối đa 8 sản phẩm RẺ NHẤT để TUYỆT ĐỐI không cho AI thấy đồ đắt
                sample.addAll(allProducts.subList(0, Math.min(size, 8)));
            } else {
                // Nếu ngân sách cao: Lấy mẫu đa dạng (5 rẻ, 5 trung, 5 đắt)
                if (size <= 15) {
                    sample.addAll(allProducts);
                } else {
                    sample.addAll(allProducts.subList(0, 5)); // 5 rẻ nhất
                    int mid = size / 2;
                    sample.addAll(allProducts.subList(mid - 2, mid + 3)); // 5 ở giữa
                    sample.addAll(allProducts.subList(size - 5, size)); // 5 đắt nhất
                }
            }

            sample.forEach(p -> {
                sb.append(String.format("SKU: %s | %s | Giá: %.0f | Socket: %s | RAM: %s\n",
                    p.getSku(), p.getName(), (p.getSalePrice() != null ? p.getSalePrice() : p.getPrice()), 
                    p.getSocketType(), p.getRamType()));
            });
        }
        return sb.toString();
    }

    public void saveBuild(Long userId, String buildName, Map<String, String> slotProducts) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BuildPC build = BuildPC.builder()
                .name(buildName != null ? buildName : "Cấu hình ngày " + LocalDateTime.now())
                .user(user)
                .totalPrice(0.0)
                .items(new ArrayList<>())
                .build();

        double total = 0;
        for (Map.Entry<String, String> entry : slotProducts.entrySet()) {
            String slotName = entry.getKey();
            String sku = entry.getValue();
            
            Optional<Product> productOpt = productRepository.findBySku(sku);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                BuildPCItem item = BuildPCItem.builder()
                        .buildPC(build)
                        .product(product)
                        .slotName(slotName)
                        .build();
                build.getItems().add(item);
                total += (product.getSalePrice() != null ? product.getSalePrice() : product.getPrice());
            }
        }
        build.setTotalPrice(total);
        buildPCRepository.save(build);
    }

    public List<BuildPC> getUserBuilds(Long userId) {
        return buildPCRepository.findByUserId(userId);
    }

    public void deleteBuild(Long id) {
        buildPCRepository.deleteById(id);
    }
}
