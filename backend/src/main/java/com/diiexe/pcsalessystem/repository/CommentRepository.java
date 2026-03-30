package com.diiexe.pcsalessystem.repository;

import com.diiexe.pcsalessystem.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProductIdAndParentCommentIsNullOrderByCreatedAtDesc(Long productId);
    List<Comment> findAllByOrderByCreatedAtDesc();
}
