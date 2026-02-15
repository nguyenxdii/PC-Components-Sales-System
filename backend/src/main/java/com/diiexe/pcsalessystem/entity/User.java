package com.diiexe.pcsalessystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Users")
@Data
public class User extends BaseEntity {

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "NVARCHAR(20)")
    private String phoneNumber;
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String address;

    @Lob
    @Column(columnDefinition = "VARBINARY(MAX)")
    private byte[] avatar;

    private boolean locked = false;
    private String role; // "ADMIN", "USER", "STAFF"

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    // Bidirectional: 1 User có nhiều đơn hàng
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore // Tránh circular reference khi serialize JSON
    private List<Order> orders = new ArrayList<>();

    // Bidirectional: 1 User có nhiều comment/đánh giá
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();
}
