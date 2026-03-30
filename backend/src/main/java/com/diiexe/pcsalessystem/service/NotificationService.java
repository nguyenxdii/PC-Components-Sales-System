package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.entity.Notification;
import com.diiexe.pcsalessystem.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByIsReadOrderByCreatedAtDesc(false);
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void createNotification(String title, String message, String type, Long targetId) {
        // Chỉ tạo nếu chưa có thông báo SALE_EXPIRING cho cùng 1 targetId để tránh spam
        if ("SALE_EXPIRING".equals(type) && notificationRepository.existsByTargetIdAndType(targetId, type)) {
            return;
        }

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .targetId(targetId)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }
}
