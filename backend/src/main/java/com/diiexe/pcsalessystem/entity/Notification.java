package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(nullable = false, columnDefinition = "NVARCHAR(1000)")
    private String message;

    @Column(nullable = false)
    private String type; // INFO | WARNING | SALE_EXPIRING | ERROR

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Liên kết với Section nếu là cảnh báo khuyến mãi
    @Column(nullable = true)
    private Long targetId;
}
