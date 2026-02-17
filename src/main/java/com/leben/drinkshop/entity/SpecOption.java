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

    // 对应数据库的 template_id
    @Column(name = "template_id")
    private Long templateId;

    private String name; // 例如：大杯、少冰

    // 通用参考加价 (实际加价看 relation 表)
    @Column(name = "price_adjust")
    private BigDecimal priceAdjust;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // 反向关联 SpecTemplate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    private SpecTemplate template;
}