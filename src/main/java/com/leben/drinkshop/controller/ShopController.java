package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.response.CommentResponse;
import com.leben.drinkshop.dto.response.DrinksResponse;
import com.leben.drinkshop.dto.response.MerchantLoginResponse;
import com.leben.drinkshop.dto.response.ShopResponse;
import com.leben.drinkshop.entity.Shop;
import com.leben.drinkshop.service.ShopService;
import com.leben.drinkshop.service.UserService;
import com.leben.drinkshop.util.JwtUtils;
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

    /**
     * 1. 商家注册 (改回简单返回 String)
     */
    @PostMapping("/register")
    public CommonEntity<String> register(@RequestBody Shop shop) {
        if (shop.getAccount() == null || shop.getPassword() == null) {
            return CommonEntity.error("账号或密码不能为空");
        }
        shopService.registerShop(shop);
        return CommonEntity.success("商家入驻成功");
    }

    /**
     * 2. 商家登录
     */
    @PostMapping("/login")
    public CommonEntity<MerchantLoginResponse> login(@RequestParam String account,
                                                     @RequestParam String password) {
        Shop shop = shopService.login(account, password);

        if (shop == null) {
            return CommonEntity.error(401, "账号或密码错误");
        }

        // 2. 生成 Token (注意：角色标识改为 "MERCHANT" 或 "SHOP"，方便前端和网关区分身份)
        String token = JwtUtils.generateToken(shop.getId(), "MERCHANT");

        // 3. 组装返回数据 DTO
        MerchantLoginResponse response = new MerchantLoginResponse();
        response.setToken(token);

        MerchantLoginResponse.ShopInfo info = new MerchantLoginResponse.ShopInfo();
        info.setId(shop.getId());
        info.setAccount(shop.getAccount());
        info.setShopName(shop.getName()); // 实体类字段是 name
        info.setImg(shop.getImg());
        info.setPhone(shop.getPhone());
        info.setDescription(shop.getDescription());
        info.setMinOrder(shop.getMinOrder());
        info.setDeliveryFee(shop.getDeliveryFee());
        info.setRating(shop.getRating());
        info.setTotalSales(shop.getTotalSales());
        info.setStatus(shop.getStatus());
        info.setLatitude(shop.getLatitude());
        info.setLongitude(shop.getLongitude());

        response.setShopInfo(info);

        return CommonEntity.success("登录成功", response);
    }

}
