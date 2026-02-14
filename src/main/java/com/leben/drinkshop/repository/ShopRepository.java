package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Shop;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShopRepository extends JpaRepository<Shop,Long> {
    // 随机查询店铺列表
    @Query(value = "SELECT * FROM shops s " +
            "WHERE s.status = 1 " +
            "AND (6371 * acos(cos(radians(:userLat)) * cos(radians(s.latitude)) * cos(radians(s.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(s.latitude)))) < 10 " +
            "ORDER BY RAND(:seed)", // 随机排序

            countQuery = "SELECT count(*) FROM shops s " +
                    "WHERE s.status = 1 " +
                    "AND (6371 * acos(cos(radians(:userLat)) * cos(radians(s.latitude)) * cos(radians(s.longitude) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(s.latitude)))) < 10",

            nativeQuery = true)
    Page<Shop> getShopFeedNearBy(@Param("userLat") Double userLat,
                                 @Param("userLon") Double userLon,
                                 @Param("seed") Long seed,
                                 Pageable pageable);


    // 根据账号查询店铺，用于注册时查重
    Shop findByAccount(String account);
}
