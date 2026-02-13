package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.CommentResponse;
import com.leben.drinkshop.dto.response.DrinksResponse;
import com.leben.drinkshop.dto.response.ShopResponse;
import com.leben.drinkshop.service.ShopService;
import com.leben.drinkshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final UserService userService;

    /**
     * 获取用户页面店铺详情
     */
    @GetMapping("/{shopId}/menu")
    public CommonEntity<List<DrinksResponse>> getShopMenu(
            @PathVariable Long shopId,
            @RequestParam Double userLat,
            @RequestParam Double userLon) {
        return shopService.getShopMenu(shopId,userLat,userLon);
    }

    @GetMapping("/feed")
    public CommonEntity<Page<ShopResponse>> getShopsFeed(
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(required = false) Long seed,
                                                           @RequestParam Double userLat,
                                                           @RequestParam Double userLon) {
        return shopService.getShopFeedNearBy(page, size, seed, userLat, userLon);
    }

    /**
     * 获取单个店铺详情 (基础信息+距离)
     */
    @GetMapping("/{shopId}")
    public CommonEntity<ShopResponse> getShopDetail(
            @PathVariable Long shopId,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon) {
        return shopService.getShopDetail(shopId, userLat, userLon);
    }

    /**
     * 获取【商家评价】列表
     */
    @GetMapping("/comment/list")
    public CommonEntity<List<CommentResponse>> getShopCommentList(
            @RequestParam("shopId") Long shopId
    ) {
        if (shopId == null) {
            return CommonEntity.error("店铺ID不能为空");
        }
        return userService.getShopComments(shopId);
    }

}
