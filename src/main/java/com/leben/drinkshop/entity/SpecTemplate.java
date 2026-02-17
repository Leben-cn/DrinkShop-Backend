package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "spec_templates") // 对应数据库表名
public class SpecTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 例如：规格、温度、糖度

    @Column(name = "sort_order")
    private Integer sortOrder;

    // 【重要】删除了 drink 字段！因为它现在是通用的，不属于某一个饮品。

    // 一对多：一个模板包含多个通用选项 (如：温度 -> 少冰, 去冰)
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<SpecOption> options;
}