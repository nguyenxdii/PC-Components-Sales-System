package com.diiexe.pcsalessystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Self-reference: 1 Comment có thể là reply của 1 comment khác
    // CON ĐÓNG: Chặn lặp vô tận ở comment cha
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // Comment cha (null nếu là comment gốc)
    
    // Bidirectional: 1 Comment có thể có nhiều replies
    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY)
    private java.util.List<Comment> replies = new java.util.ArrayList<>();
}
