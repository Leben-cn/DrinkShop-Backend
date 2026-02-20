package com.leben.drinkshop.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopCategoriesResponse {
    private Long id;
    private Long shopId;
    private String name;
    private Integer sort;
    private String icon;
    private Boolean isShow;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer drinkNum;
}