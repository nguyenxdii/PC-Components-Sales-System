package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.entity.Section;
import com.diiexe.pcsalessystem.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Component
public class SaleExpiryTask {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private NotificationService notificationService;

    // Chạy mỗi 1 giờ để kiểm tra các đợt Flash Sale sắp hết hạn
    @Scheduled(fixedRate = 3600000)
    public void checkExpiringSales() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<Section> expiringSections = sectionRepository.findExpiringFlashSales(now, tomorrow);
        
        for (Section section : expiringSections) {
            long hoursLeft = Duration.between(now, section.getEndAt()).toHours();
            String title = "Khuyến mãi sắp kết thúc";
            String message = String.format("Chương trình '%s' chỉ còn %d giờ là kết thúc. Vui lòng kiểm tra!", 
                                           section.getName(), hoursLeft);
            
            notificationService.createNotification(title, message, "SALE_EXPIRING", section.getId());
        }
    }
}
