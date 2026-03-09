package com.diiexe.pcsalessystem.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BannerRequest {
    private String name;
    private String link;
    private Integer displayOrder;
    private Boolean isActive;
    // File ảnh sẽ được hứng qua @RequestParam thay vì ở trong DTO
}
