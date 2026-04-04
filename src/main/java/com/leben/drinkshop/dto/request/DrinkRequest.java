package com.leben.drinkshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DrinkRequest {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal packingFee;
    private Integer stock;
    private String img;
    private Long categoryId;
    private Long shopCategoryId;
    private Integer status;
    private List<SpecItemRequest> specs;

    @Data
    public static class SpecItemRequest {
        private Long specOptionId;
        private BigDecimal priceAdjust;
        private Integer isDefault;
    }
}