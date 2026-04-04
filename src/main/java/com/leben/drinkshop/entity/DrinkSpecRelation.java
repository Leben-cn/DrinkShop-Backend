package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@Entity
@Table(name = "drink_spec_relations")
public class DrinkSpecRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 控制权交给此对象，JPA 会自动提取 drink 的 ID 填充到数据库的 drink_id 列
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drink_id", nullable = false)
    @JsonIgnore // 防止 JSON 序列化时出现死循环
    private Drink drink;

    @Column(name = "spec_option_id")
    private Long specOptionId;

    // 仅用于查询展示详情
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "spec_option_id", insertable = false, updatable = false)
    private SpecOption specOption;

    @Column(name = "price_adjust")
    private BigDecimal priceAdjust;

    @Column(name = "is_default")
    private Integer isDefault;
}