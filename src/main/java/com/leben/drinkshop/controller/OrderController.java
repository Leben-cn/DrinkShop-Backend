package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.OrderResponse;
import com.leben.drinkshop.service.OrderService;
import com.leben.drinkshop.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/order") // 区分于用户的 /orders
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 统一订单查询接口 (支持用户、商家、管理员)
     * 模糊查询订单内的饮品名称
     */
    @GetMapping("/search")
    public CommonEntity<List<OrderResponse>> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestHeader("Authorization") String token) {

        Long currentId = JwtUtils.getIdFromToken(token);
        String role = JwtUtils.getRoleFromToken(token);

        if (currentId == null || role == null) {
            return CommonEntity.error("凭证无效，请重新登录");
        }

        // 调用业务层，根据角色过滤数据
        List<OrderResponse> list = orderService.searchOrdersByRole(currentId, role, keyword);
        return CommonEntity.success(list);
    }
}
