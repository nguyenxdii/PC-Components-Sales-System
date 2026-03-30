package com.diiexe.pcsalessystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentRequest {
    private String content;
    private Integer rating;
    private Long productId;
    private Long userId;
    private Long parentCommentId;
}
