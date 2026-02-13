package com.leben.drinkshop.repository;

import com.leben.drinkshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}