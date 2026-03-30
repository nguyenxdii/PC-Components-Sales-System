package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.CartRequest;
import com.diiexe.pcsalessystem.dto.CartResponse;
import com.diiexe.pcsalessystem.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request.getUserId(), request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long itemId, 
            @RequestParam Long userId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(userId, itemId, quantity));
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long itemId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(cartService.removeFromCart(userId, itemId));
    }
}
