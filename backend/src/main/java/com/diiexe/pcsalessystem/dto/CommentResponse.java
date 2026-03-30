package com.diiexe.pcsalessystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private Integer rating;
    private String userName;
    private Long productId;
    private String productName;
    private String productSlug;
    private LocalDateTime createdAt;
    private Boolean isPurchased;
    private LocalDateTime purchasedDate;
    private String userAvatar;
    private List<CommentResponse> replies;
}
