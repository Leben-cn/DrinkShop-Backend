package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DrinksResponse {

    private Long id;

    private String name;

    private BigDecimal price;

    private String description;

    private String img;

    private Double mark;

    private Integer salesVolume;

    private LocalDateTime createTime;

    private Integer status;
    //平台大分类
    private CategoriesResponse categories;
    //店铺自定义分类
    private ShopCategoriesResponse shopCategories;

    private String distance;
    //规格列表
    private List<DrinkSpecItemResponse> specs;
    //打包费
    private BigDecimal packingFee;

    private Integer stock;

}