package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Order;
import jakarta.transaction.Transactional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. 查询某用户的所有订单 (按时间倒序)
    List<Order> findAllByUserIdOrderByCreateTimeDesc(Long userId);

    // 2. 查询某用户特定状态的订单 (用于查询"已取消 status=2")
    List<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, Integer status);

    // 3. 查询待评价订单 (用户ID + 状态为已完成 + 未评价)
    List<Order> findByUserIdAndStatusAndIsCommentedFalseOrderByCreateTimeDesc(Long userId, Integer status);

    // 1. 【我的评价】根据 userId 查询已评价的订单
    List<Order> findByUserIdAndIsCommentedTrueOrderByCreateTimeDesc(Long userId);

    // 2. 【商家评价】根据 shopId 查询已评价的订单
    List<Order> findByShopIdAndIsCommentedTrueOrderByCreateTimeDesc(Long shopId);

    /**
     * 查询用户账单（即：排除状态为 2-已取消 的所有订单）
     */
    List<Order> findByUserIdAndStatusNotOrderByCreateTimeDesc(Long userId, Integer status);


    // 1. 查询该店铺的所有订单（按时间倒序）
    List<Order> findAllByShopIdOrderByCreateTimeDesc(Long shopId);

    // 2. 根据状态查询店铺订单 (0:待制作, 1:已完成, 2:退款/售后)
    List<Order> findByShopIdAndStatusOrderByCreateTimeDesc(Long shopId, Integer status);

    // 检查是否存在特定店铺的特定状态订单
    boolean existsByShopIdAndStatus(Long shopId, Integer status);

    /**
     * 统计今日订单量 (状态为1:已完成)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.shopId = :shopId AND o.status = 1 AND o.createTime >= :startTime AND o.createTime <= :endTime")
    Long countTodayOrders(@Param("shopId") Long shopId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 统计今日营业额 (状态为1:已完成)
     */
    @Query("SELECT SUM(o.payAmount) FROM Order o WHERE o.shopId = :shopId AND o.status = 1 AND o.createTime >= :startTime AND o.createTime <= :endTime")
    BigDecimal sumTodayRevenue(@Param("shopId") Long shopId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    // 根据店铺ID和时间范围查询订单
    List<Order> findByShopIdAndStatusAndCreateTimeBetweenOrderByCreateTimeDesc(
            Long shopId,
            Integer status,
            Date start,
            Date end
    );

    // 使用 JPQL 查询该商家所有状态为 1 的订单总金额
    @Query("SELECT SUM(o.payAmount) FROM Order o WHERE o.shopId = :shopId AND o.status = 1")
    BigDecimal sumTotalRevenueByShopId(@Param("shopId") Long shopId);

    /**
     * 一键更新所有饮品的月销量 (过去30天内状态为3的订单)
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE drinks d SET d.sales_volume = (" +
            "SELECT COALESCE(SUM(oi.quantity), 0) FROM order_item oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE oi.product_id = d.id AND o.status = 3 " +
            "AND o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY))", nativeQuery = true)
    void updateAllDrinksMonthlySales();

    /**
     * 一键更新所有店铺的月销量 (过去30天内状态为3的订单)
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE shops s SET s.total_sales = (" +
            "SELECT COALESCE(SUM(oi.quantity), 0) FROM order_item oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.shop_id = s.id AND o.status = 3 " +
            "AND o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY))", nativeQuery = true)
    void updateAllShopsMonthlySales();
}