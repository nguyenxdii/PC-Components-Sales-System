package com.diiexe.pcsalessystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Carts")
@Data
public class Cart extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // Bidirectional: Giỏ hàng chứa nhiều món hàng
    // orphanRemoval = true: Xóa item khỏi list → Xóa luôn trong DB
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CartItem> cartItems = new ArrayList<>();
}
