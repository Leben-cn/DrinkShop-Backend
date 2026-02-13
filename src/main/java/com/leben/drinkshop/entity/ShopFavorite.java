package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shop_favorite")
public class ShopFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) createTime = LocalDateTime.now();
    }

}