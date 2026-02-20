package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Drink;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DrinkRepository extends JpaRepository<Drink, Long>, JpaSpecificationExecutor<Drink> {

    // 统计某商家的总销量
    @Query("SELECT SUM(d.salesVolume) FROM Drink d WHERE d.shopId = :shopId")
    Integer sumSalesByShopId(@Param("shopId") Long shopId);

    // 统计某商家的平均评分
    @Query("SELECT AVG(d.mark) FROM Drink d WHERE d.shopId = :shopId")
    Double avgMarkByShopId(@Param("shopId") Long shopId);

    List<Drink> findByShopIdAndStatus(Long shopId, Integer status);

    @Query(value = "SELECT d FROM Drink d LEFT JOIN FETCH Shop s ON d.shopId = s.id WHERE d.status = 1",
            countQuery = "SELECT count(d) FROM Drink d WHERE d.status = 1")
    Page<Drink> getDrinksFeed(Pageable pageable);

    /**
     * 【不分页查询】
     * 直接查出所有 status=1 的商品
     * 依然保留 JOIN FETCH Shop，为了后续计算距离
     */
    @Query("SELECT d FROM Drink d LEFT JOIN FETCH Shop s ON d.shopId = s.id WHERE d.status = 1")
    List<Drink> findAllOnSaleDrinks();

    /**
     * 首页商店商品简信息
     */
    List<Drink> findTop6ByShopIdAndStatusOrderBySalesVolumeDesc(Long shopId, Integer status);

    /**
     * 根据商家分类 ID 统计商品数量
     */
    Integer countByShopCategoryId(Long shopCategoryId);

}