package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.CommentResponse;
import com.leben.drinkshop.service.CommentService;
import com.leben.drinkshop.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/status") 
    public CommonEntity<String> updateCommentStatusByOrder(
            @RequestParam Long orderId,
            @RequestParam Integer status,
            @RequestHeader("Authorization") String token) {

        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) return CommonEntity.error("认证失败");

        // 调用新的 Service 方法
        int updatedCount = commentService.updateStatusByOrderId(orderId, status);

        return updatedCount > 0 ?
                CommonEntity.success("成功更新 " + updatedCount + " 条评价状态") :
                CommonEntity.error("该订单下暂无评价记录");
    }

    @GetMapping("/shop/list")
    public CommonEntity<List<CommentResponse>> getShopCommentList(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer status) {

        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }
        return commentService.getShopComments(shopId, status);
    }

    @GetMapping("/user/list")
    public CommonEntity<List<CommentResponse>> getUserCommentList(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer status
    ) {
        Long userId = JwtUtils.getIdFromToken(token);
        if (userId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }
        return commentService.getUserComments(userId, status);
    }

    @GetMapping("/admin/list")
    public CommonEntity<List<CommentResponse>> getAllCommentList(
            @RequestParam(required = false) Integer status) {
        return commentService.getAllComments(status);
    }
}
