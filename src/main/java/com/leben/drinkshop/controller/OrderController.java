package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.request.OrderSubmitRequest;
import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.OrderResponse;
import com.leben.drinkshop.service.OrderService;
import com.leben.drinkshop.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/submit")
    public CommonEntity<Long> submitOrder(@RequestBody OrderSubmitRequest request,
                                          @RequestHeader("Authorization") String token) {

        Long currentUserId = JwtUtils.getIdFromToken(token);

        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }

        Long orderId = orderService.submitOrder(request, currentUserId);

        return CommonEntity.success(orderId);
    }

    /**
     * 获取全部订单
     */
    @GetMapping("/list/all")
    public CommonEntity<List<OrderResponse>> getAllOrders(
            @RequestParam Double userLat,
            @RequestParam Double userLon,
            @RequestHeader("Authorization") String token) {

        Long currentUserId = JwtUtils.getIdFromToken(token);
        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }
        List<OrderResponse> list = orderService.getAllOrders(currentUserId,userLat,userLon);
        return CommonEntity.success(list);
    }

    /**
     * 获取待评价订单
     */
    @GetMapping("/list/comment")
    public CommonEntity<List<OrderResponse>> getOrdersToComment(
            @RequestParam Double userLat,
            @RequestParam Double userLon,
            @RequestHeader("Authorization") String token) {
        Long currentUserId = JwtUtils.getIdFromToken(token);

        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }
        List<OrderResponse> list = orderService.getOrdersToComment(currentUserId,userLat,userLon);
        return CommonEntity.success(list);
    }

    /**
     * 获取已取消/退款订单
     */
    @GetMapping("/list/cancel")
    public CommonEntity<List<OrderResponse>> getCancelledOrders(
            @RequestParam Double userLat,
            @RequestParam Double userLon,
            @RequestHeader("Authorization") String token) {
        Long currentUserId = JwtUtils.getIdFromToken(token);

        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }
        List<OrderResponse> list = orderService.getCancelledOrders(currentUserId,userLat,userLon);
        return CommonEntity.success(list);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public CommonEntity<String> cancelOrder(
            @RequestParam("orderId") Long orderId,
            @RequestHeader("Authorization") String token) {

        Long currentUserId = JwtUtils.getIdFromToken(token);
        if (currentUserId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }

        try {
            orderService.cancelOrder(orderId, currentUserId);
            return CommonEntity.success("订单取消成功");
        } catch (Exception e) {
            return CommonEntity.error(e.getMessage());
        }
    }

}