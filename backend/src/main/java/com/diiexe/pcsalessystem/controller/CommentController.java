package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.CommentRequest;
import com.diiexe.pcsalessystem.dto.CommentResponse;
import com.diiexe.pcsalessystem.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CommentResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(commentService.getCommentsByProduct(productId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(@RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addComment(request));
    }

    @GetMapping("/check-eligibility")
    public ResponseEntity<Boolean> checkEligibility(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        return ResponseEntity.ok(commentService.checkEligibility(userId, productId));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<CommentResponse>> getAllAdmin() {
        return ResponseEntity.ok(commentService.getAllCommentsAdmin());
    }
}
