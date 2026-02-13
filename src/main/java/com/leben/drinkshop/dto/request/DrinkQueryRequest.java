package com.leben.drinkshop.dto.request;

import lombok.Data;

@Data
public class DrinkQueryRequest {
    // 搜索关键词 (模糊查询)
    private String name;

    // 店铺ID (精确查询)
    private Long shopId;

    // 分类ID (精确查询)
    private Long categoryId;

    // 最低价 (范围查询)
    private java.math.BigDecimal minPrice;

    // 最高价
    private java.math.BigDecimal maxPrice;

    // 是否只看上架 (默认看上架)
    private Integer status = 1;

    private Double userLat;
    private Double userLon;
}