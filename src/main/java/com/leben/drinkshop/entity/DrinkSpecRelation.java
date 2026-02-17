package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "drink_spec_relations")
public class DrinkSpecRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 关联 Drink
    @Column(name = "drink_id")
    private Long drinkId;

    // insertable=false, updatable=false 是为了防止和上面的 drinkId 重复映射
    // 这里用于查询时直接获取 Drink 对象
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drink_id", insertable = false, updatable = false)
    private Drink drink;

    // 2. 关联 SpecOption (通用选项)
    @Column(name = "spec_option_id")
    private Long specOptionId;

    @ManyToOne(fetch = FetchType.EAGER) // 查的时候通常想直接知道选了什么规格，所以用 EAGER
    @JoinColumn(name = "spec_option_id", insertable = false, updatable = false)
    private SpecOption specOption;

    // 3. 价格调整 (覆盖通用价格)
    @Column(name = "price_adjust")
    private BigDecimal priceAdjust;

    // 4. 是否默认选中 (1是 0否)
    @Column(name = "is_default")
    private Integer isDefault; // 或者用 Boolean，视数据库类型而定(tinyint(1)通常映射Boolean或Integer)
}