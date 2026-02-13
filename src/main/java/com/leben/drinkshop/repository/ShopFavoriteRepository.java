package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.ShopFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShopFavoriteRepository extends JpaRepository<ShopFavorite, Long> {
    // 查询是否存在
    boolean existsByUserIdAndShopId(Long userId, Long shopId);

    // 删除收藏 (取消收藏)
    void deleteByUserIdAndShopId(Long userId, Long shopId);

    // 根据 userId 查询收藏列表，按时间倒序
    List<ShopFavorite> findByUserIdOrderByCreateTimeDesc(Long userId);
}
