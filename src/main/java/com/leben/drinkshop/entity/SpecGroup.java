package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "spec_groups")
public class SpecGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "is_multiple")
    private Boolean isMultiple;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // 反向关联 Drink
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drink_id")
    private Drink drink;

    // 【关键关联】一对多关联选项
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<SpecOption> options;
}