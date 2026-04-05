package com.diiexe.pcsalessystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "BuildPC")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildPC extends BaseEntity {

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    private Double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "buildPC", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BuildPCItem> items = new ArrayList<>();
}
