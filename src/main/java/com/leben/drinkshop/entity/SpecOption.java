package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "spec_options")
public class SpecOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "price_adjustment")
    private BigDecimal price;

    private Integer sortOrder;

    // 反向关联 SpecGroup
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private SpecGroup group;
}