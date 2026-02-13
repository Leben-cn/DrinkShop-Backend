package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 关联外键 ---
    // 这里配置多对一，方便从 Item 找到 Order，但在 JSON 返回时通常要忽略，防止死循环
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    // --- 商品快照 ---
    private Long productId;
    private String productName;
    private String productImg;

    // --- 规格信息 ---
    private String specDesc; // "少冰, 半糖"
    private String specIds;  // "101,205"

    // --- 价格与数量 ---
    private BigDecimal price; // 下单时的单价
    private Integer quantity;
}