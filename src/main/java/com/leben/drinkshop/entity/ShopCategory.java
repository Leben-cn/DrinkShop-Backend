package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "shop_categories")
public class ShopCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 归属的店铺ID
     */
    @Column(name="shop_id", nullable = false)
    private Long shopId;

    /**
     * 分类名称 (如: 店长推荐)
     */
    private String name;

    /**
     * 排序优先级
     */
    private int sort;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 是否显示 (对应 SQL 的 is_show tinyint)
     * 使用 Boolean 类型，JPA 会自动映射 1/0
     */
    @Column(name = "is_show")
    private Boolean isShow = true;

    /**
     * 创建时间
     * updatable = false 表示更新操作时不会修改此字段
     */
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // ==========================================
    // JPA 生命周期回调 (自动维护时间)
    // ==========================================

    /**
     * 在插入数据库之前执行
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        // 设置默认值
        if (isShow == null) {
            isShow = true;
        }
    }

    /**
     * 在更新数据库之前执行
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}