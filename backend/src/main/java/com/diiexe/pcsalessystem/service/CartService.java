package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.CartResponse;
import com.diiexe.pcsalessystem.entity.Cart;
import com.diiexe.pcsalessystem.entity.CartItem;
import com.diiexe.pcsalessystem.entity.Product;
import com.diiexe.pcsalessystem.entity.User;
import com.diiexe.pcsalessystem.repository.CartItemRepository;
import com.diiexe.pcsalessystem.repository.CartRepository;
import com.diiexe.pcsalessystem.repository.ProductRepository;
import com.diiexe.pcsalessystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public CartResponse getCartByUserId(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return convertToResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
            cart.getCartItems().add(newItem);
        }

        return convertToResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món hàng không tồn tại"));
        
        if (quantity <= 0) {
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        
        return convertToResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món hàng không tồn tại"));
        
        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
        
        return convertToResponse(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setCartItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });
    }

    private CartResponse convertToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        
        if (cart.getCartItems() == null) {
            response.setItems(new ArrayList<>());
            response.setTotalAmount(0.0);
            return response;
        }

        List<CartResponse.CartItemDTO> itemDTOs = cart.getCartItems().stream()
                .filter(item -> item.getProduct() != null)
                .map(item -> {
                    CartResponse.CartItemDTO dto = new CartResponse.CartItemDTO();
                    dto.setId(item.getId());
                    dto.setProductId(item.getProduct().getId());
                    dto.setProductName(item.getProduct().getName());
                    dto.setProductSlug(item.getProduct().getSlug());
                    dto.setProductImage(item.getProduct().getImageUrl());
                    
                    Double price = 0.0;
                    if (item.getProduct().getSalePrice() != null) {
                        price = item.getProduct().getSalePrice();
                    } else if (item.getProduct().getPrice() != null) {
                        price = item.getProduct().getPrice();
                    }
                    
                    dto.setPrice(price);
                    dto.setQuantity(item.getQuantity() != null ? item.getQuantity() : 0);
                    dto.setSubtotal(dto.getPrice() * dto.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());
        
        response.setItems(itemDTOs);
        response.setTotalAmount(itemDTOs.stream()
                .mapToDouble(dto -> dto.getSubtotal() != null ? dto.getSubtotal() : 0.0)
                .sum());
        
        return response;
    }
}
