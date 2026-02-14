package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MerchantLoginResponse {

    private String token;

    // 商家/店铺详细信息
    private ShopInfo shopInfo;

    @Data
    public static class ShopInfo {
        private Long id;
        private String account;
        private String shopName;     // 对应实体类的 name
        private String img;          // 店铺头像
        private String description;  // 简介
        private String phone;
        private BigDecimal minOrder; // 起送价
        private BigDecimal deliveryFee; // 配送费
        private Double rating;       // 评分
        private Integer totalSales;  // 销量
        private Integer status;      // 状态
        // 经纬度按需返回
        private Double longitude;
        private Double latitude;
    }
}