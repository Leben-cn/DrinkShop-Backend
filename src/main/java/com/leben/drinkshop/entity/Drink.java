package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "drinks")
public class Drink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 基础 ID 字段 (用于插入/更新)
    @Column(name="shop_id")
    private Long shopId;

    // 2. 【新增】关联 Shop 对象 (用于查询)
    // insertable=false 表示这个字段只读，保存时以 shopId 为准
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", insertable = false, updatable = false)
    private Shop shop;

    private String name;

    private BigDecimal price;

    private String description;

    private String img;

    private Double mark;

    @Column(name="sales_volume")
    private Integer salesVolume;

    private Integer stock;

    @Column(name="create_time", updatable = false)
    private LocalDateTime createTime;

    // 3. 基础 Category ID
    @Column(name="category_id")
    private Long categoryId;

    // 4. 【新增】关联 Category 对象
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    // 5. 基础 ShopCategory ID
    @Column(name = "shop_category_id")
    private Long shopCategoryId;

    // 6. 【新增】关联 ShopCategory 对象
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_category_id", insertable = false, updatable = false)
    private ShopCategory shopCategory;

    private Integer status;

    @Column(name = "packing_fee")
    private BigDecimal packingFee;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    // 规格关联 (保持不变)
    @OneToMany(mappedBy = "drink", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<SpecGroup> specGroups;
}