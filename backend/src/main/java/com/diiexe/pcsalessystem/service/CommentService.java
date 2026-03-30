package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.CommentRequest;
import com.diiexe.pcsalessystem.dto.CommentResponse;
import com.diiexe.pcsalessystem.entity.Comment;
import com.diiexe.pcsalessystem.entity.Product;
import com.diiexe.pcsalessystem.entity.User;
import com.diiexe.pcsalessystem.repository.CommentRepository;
import com.diiexe.pcsalessystem.repository.ProductRepository;
import com.diiexe.pcsalessystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.diiexe.pcsalessystem.repository.OrderDetailRepository orderDetailRepository;

    public boolean checkEligibility(Long userId, Long productId) {
        if (userId == null) return false;
        return orderDetailRepository.existsByOrderUserIdAndProductIdAndOrderStatus(userId, productId, "COMPLETED");
    }

    public List<CommentResponse> getAllCommentsAdmin() {
        List<Comment> allComments = commentRepository.findAllByOrderByCreatedAtDesc();
        return allComments.stream()
                .filter(c -> c.getParentComment() == null) // Bỏ qua tất cả các phản hồi (reply) của Admin/User
                .filter(c -> c.getUser() == null || !c.getUser().getRole().toUpperCase().contains("ADMIN")) // Bỏ qua nếu Admin tự viết bình luận gốc
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CommentResponse> getCommentsByProduct(Long productId) {
        List<Comment> rootComments = commentRepository.findByProductIdAndParentCommentIsNullOrderByCreatedAtDesc(productId);
        return rootComments.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public CommentResponse addComment(CommentRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setRating(request.getRating());
        comment.setProduct(product);

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            comment.setUser(user);
        }

        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Comment cha không tồn tại"));
            comment.setParentComment(parent);
        }

        Comment saved = commentRepository.save(comment);
        return convertToResponse(saved);
    }

    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setRating(comment.getRating());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUserName(comment.getUser() != null ? comment.getUser().getFullName() : "Khách ẩn danh");
        response.setUserAvatar(comment.getUser() != null ? comment.getUser().getAvatarUrl() : null);
        
        // Lấy thông tin sản phẩm trực tiếp hoặc từ bình luận cha (đối với reply)
        Product product = comment.getProduct();
        if (product == null && comment.getParentComment() != null) {
            product = comment.getParentComment().getProduct();
        }

        if (product != null) {
            response.setProductId(product.getId());
            response.setProductName(product.getName());
            
            // Fallback: Nếu slug bị null, tự động tạo từ tên
            String slug = product.getSlug();
            if (slug == null || slug.isBlank()) {
                slug = com.diiexe.pcsalessystem.util.SlugUtils.toSlug(product.getName());
            }
            response.setProductSlug(slug);

            // Kiểm tra lịch sử mua hàng của người dùng cho sản phẩm này
            if (comment.getUser() != null) {
                orderDetailRepository.findFirstByOrderUserIdAndProductIdAndOrderStatusOrderByOrderCreatedAtDesc(
                    comment.getUser().getId(), 
                    product.getId(), 
                    "COMPLETED"
                ).ifPresent(orderDetail -> {
                    response.setIsPurchased(true);
                    response.setPurchasedDate(orderDetail.getOrder().getCreatedAt());
                });
            }
        }

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            response.setReplies(comment.getReplies().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
}
