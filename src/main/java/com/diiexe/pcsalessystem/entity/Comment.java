package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Comments")
@Data
public class Comment extends BaseEntity {
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;

    private Integer rating; // 1-5 sao (null = câu hỏi)

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne
    @JoinColumn(name = "reply_to_id")
    private Comment replyTo; // Admin trả lời
}
