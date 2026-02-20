package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.ShopCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShopCategoryRepository extends JpaRepository<ShopCategory,Long> {
    // 根据店铺ID查询分类，并按 sort 字段升序排列
    List<ShopCategory> findByShopIdOrderBySortAsc(Long shopId);
}
