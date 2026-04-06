package com.leben.drinkshop.controller;

import com.leben.drinkshop.dto.CommonEntity;
import com.leben.drinkshop.dto.request.DrinkRequest;
import com.leben.drinkshop.dto.request.MerchantUpdateInfoRequest;
import com.leben.drinkshop.dto.response.DrinkSpecItemResponse;
import com.leben.drinkshop.dto.response.DrinksResponse;
import com.leben.drinkshop.dto.response.OrderResponse;
import com.leben.drinkshop.dto.response.ShopCategoriesResponse;
import com.leben.drinkshop.entity.Shop;
import com.leben.drinkshop.service.MerchantService;
import com.leben.drinkshop.service.OrderService;
import com.leben.drinkshop.service.ShopService;
import com.leben.drinkshop.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/merchant") // 区分于用户的 /orders
@RequiredArgsConstructor
public class MerchantController {

    private final OrderService orderService;
    private final MerchantService merchantService;
    private final ShopService shopService;

    /**
     * 获取商家的全部订单
     */
    @GetMapping("/orders/all")
    public CommonEntity<List<OrderResponse>> getAllOrders(
            @RequestHeader("Authorization") String token) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }

        // status 传 null 代表查全部
        return CommonEntity.success(orderService.getMerchantOrders(shopId, null));
    }

    /**
     * 获取待制作订单 (Status = 0)
     */
    @GetMapping("/orders/pending")
    public CommonEntity<List<OrderResponse>> getPendingOrders(
            @RequestHeader("Authorization") String token) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效");
        }

        return CommonEntity.success(orderService.getMerchantOrders(shopId, 0));
    }

    /**
     * 获取已完成订单 (Status = 1)
     * (包含已评价和未评价的，因为业务逻辑是 1 代表制作完成/订单结束)
     */
    @GetMapping("/orders/completed")
    public CommonEntity<List<OrderResponse>> getCompletedOrders(
            @RequestHeader("Authorization") String token) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效");
        }

        return CommonEntity.success(orderService.getMerchantOrders(shopId, 1));
    }

    /**
     * 获取退款/售后订单 (Status = 2)
     */
    @GetMapping("/orders/refund")
    public CommonEntity<List<OrderResponse>> getRefundOrders(
            @RequestHeader("Authorization") String token) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效");
        }

        return CommonEntity.success(orderService.getMerchantOrders(shopId, 2));
    }

    @GetMapping("/spec/all")
    public CommonEntity<List<DrinkSpecItemResponse>> getAllSpecs(
            @RequestHeader("Authorization") String token) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效");
        }
        List<DrinkSpecItemResponse> flatList = merchantService.getAllSpecItems();
        return CommonEntity.success(flatList);
    }

    /**
     * 获取商家的全部商品分类
     */
    @GetMapping("/category/all")
    public CommonEntity<List<ShopCategoriesResponse>> getCategories(
            @RequestHeader("Authorization") String token) {

        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效，请重新登录");
        }

        List<ShopCategoriesResponse> list = merchantService.getShopCategories(shopId);
        return CommonEntity.success(list);
    }

    @PostMapping("/category/add")
    public CommonEntity<String> addCategory(
            @RequestHeader("Authorization") String token,
            @RequestParam String name) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效");
        }
        merchantService.addShopCategory(shopId, name);
        return CommonEntity.success("添加成功");
    }


    @DeleteMapping("/category/delete/{id}")
    public CommonEntity<String> deleteCategory(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long categoryId) {

        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效");
        }

        merchantService.deleteShopCategory(shopId, categoryId);
        return CommonEntity.success("删除成功");

    }

    /**
     * 批量更新分类排序
     */
    @PostMapping("/category/sort/update")
    public CommonEntity<String> updateCategorySort(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Long> ids) {
        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效");
        }

        merchantService.updateSort(shopId, ids);
        return CommonEntity.success("更新成功");
    }

    @PostMapping("/drink/save")
    public CommonEntity<String> saveDrink(
            @RequestHeader("Authorization") String token,
            @RequestBody DrinkRequest request) {

        Long shopId = JwtUtils.getIdFromToken(token);
        if (shopId == null) {
            return CommonEntity.error("Token无效");
        }
        merchantService.saveOrUpdateDrink(shopId, request);
        return CommonEntity.success("保存成功");
    }

    @GetMapping("/{shopId}/drinks")
    public CommonEntity<List<DrinksResponse>> getShopDrinks(
            @PathVariable Long shopId) {
        List<DrinksResponse> list=merchantService.getShopAllDrinks(shopId);
        return CommonEntity.success(list);
    }

    @PostMapping("/update/info")
    public CommonEntity<String> updateMerchantInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody MerchantUpdateInfoRequest request
    ) {
        Long merchantId = JwtUtils.getIdFromToken(token);
        if (merchantId == null) {
            return CommonEntity.error("Token无效");
        }

        try {
            merchantService.updateMerchantInfo(merchantId, request);
            return CommonEntity.success("修改成功");
        } catch (Exception e) {
            return CommonEntity.error(e.getMessage());
        }
    }

    @PostMapping("/update/status")
    public CommonEntity<String> updateStatus(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer status) {
        Long merchantId = JwtUtils.getIdFromToken(token);
        if (merchantId == null) {
            return CommonEntity.error("Token无效");
        }
        try {
            shopService.updateStatus(merchantId, status);
            return CommonEntity.success("状态修改成功");
        } catch (Exception e) {
            return CommonEntity.error(e.getMessage());
        }
    }


}