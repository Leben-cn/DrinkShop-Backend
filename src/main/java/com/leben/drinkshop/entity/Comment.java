package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "comment")
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 用户信息快照 (冗余字段) ---
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 64)
    private String userName;

    @Column(name = "user_avatar")
    private String userAvatar;

    // --- 订单关联 ---
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 关联订单详情ID (核心字段)
     * 用于区分同一订单中买了多杯一样的饮品时，具体评的是哪一杯
     */
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    // --- 商品信息快照 (冗余字段) ---
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", length = 128)
    private String productName;

    // --- 评价详情 ---
    /**
     * 评分 (1-5星)
     */
    @Column(name = "score", nullable = false)
    private Integer score = 5;

    @Column(name = "content", length = 500)
    private String content;

    /**
     * 评价图片 (多张用逗号分隔)
     */
    @Column(name = "picture", length = 1000)
    private String picture;

    // --- 时间字段 ---
    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 插入前自动填充默认值
     */
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (score == null) {
            score = 5; // 防止空指针，默认5星
        }
    }
}