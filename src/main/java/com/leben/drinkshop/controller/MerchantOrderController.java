package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.OrderResponse;
import com.leben.drinkshop.service.OrderService;
import com.leben.drinkshop.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/merchant/orders") // 区分于用户的 /orders
@RequiredArgsConstructor
public class MerchantOrderController {

    private final OrderService orderService;

    /**
     * 1. 获取商家的全部订单
     */
    @GetMapping("/all")
    public CommonEntity<List<OrderResponse>> getAllOrders(
            @RequestHeader("Authorization") String token) {
        // 商家登录时，Token 里存的就是 shopId
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) return CommonEntity.error("Token无效，请重新登录");

        // status 传 null 代表查全部
        return CommonEntity.success(orderService.getMerchantOrders(shopId, null));
    }

    /**
     * 2. 获取待制作订单 (Status = 0)
     */
    @GetMapping("/pending")
    public CommonEntity<List<OrderResponse>> getPendingOrders(
            @RequestHeader("Authorization") String token) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) return CommonEntity.error("Token无效");

        return CommonEntity.success(orderService.getMerchantOrders(shopId, 0));
    }

    /**
     * 3. 获取已完成订单 (Status = 1)
     * (包含已评价和未评价的，因为业务逻辑是 1 代表制作完成/订单结束)
     */
    @GetMapping("/completed")
    public CommonEntity<List<OrderResponse>> getCompletedOrders(
            @RequestHeader("Authorization") String token) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) return CommonEntity.error("Token无效");

        return CommonEntity.success(orderService.getMerchantOrders(shopId, 1));
    }

    /**
     * 4. 获取退款/售后订单 (Status = 2)
     */
    @GetMapping("/refund")
    public CommonEntity<List<OrderResponse>> getRefundOrders(
            @RequestHeader("Authorization") String token) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) return CommonEntity.error("Token无效");

        return CommonEntity.success(orderService.getMerchantOrders(shopId, 2));
    }
}