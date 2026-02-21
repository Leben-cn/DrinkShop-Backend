package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.ShopCategory;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ShopCategoryRepository extends JpaRepository<ShopCategory,Long> {
    // 根据店铺ID查询分类，并按 sort 字段升序排列
    List<ShopCategory> findByShopIdOrderBySortAsc(Long shopId);

    // 查询指定店铺当前最大的 sort 值
    @Query("SELECT MAX(c.sort) FROM ShopCategory c WHERE c.shopId = :shopId")
    Integer findMaxSortByShopId(@Param("shopId") Long shopId);
}
